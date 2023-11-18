/*
 * CQL Module
 * A Corpus Query Language module for eXist
 * Copyright (C) 2016 Belgrade Center for Digital Humanities
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.humanistika.exist.module.cqlmodule;

import com.evolvedbinary.cql.parser.CorpusQLLexer;
import com.evolvedbinary.cql.parser.CorpusQLParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.exist.dom.QName;
import org.exist.dom.memtree.MemTreeBuilder;
import org.exist.xquery.*;
import org.exist.xquery.value.*;

public class CQLParserFunction extends BasicFunction {

    public final static FunctionSignature FNS_PARSE = new FunctionSignature(
            new QName("parse", CQLModule.CQL_MODULE_NS, CQLModule.CQL_MODULE_PREFIX),
            "Parses Corpus Query Language generating an XML representation of the AST",
            new SequenceType[] {
                    new FunctionParameterSequenceType("cql", Type.STRING, Cardinality.EXACTLY_ONE, "The Corpus Query Language"),
            },
            new FunctionReturnSequenceType(Type.DOCUMENT, Cardinality.ONE, "The XML AST of the Corpus Query Language")
    );

    public CQLParserFunction(final XQueryContext context, final FunctionSignature signature) {
        super(context, signature);
    }

    @Override
    public Sequence eval(final Sequence[] args, final Sequence contextSequence) throws XPathException {
        final ANTLRInputStream is = new ANTLRInputStream(args[0].getStringValue());

        final CorpusQLLexer lexer = new CorpusQLLexer(is);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final CorpusQLParser parser = new CorpusQLParser(tokens);

        final ParseTree tree = parser.query();

        final MemTreeBuilder builder = new MemTreeBuilder();
        builder.startDocument();

        final CorpusQLXMLVisitor xmlVisitor = new CorpusQLXMLVisitor(builder);
        xmlVisitor.visit(tree);

        builder.endDocument();

        return builder.getDocument();
    }

    //TODO(AR) remove testing
    public final static void main(final String[] args) {

        final ANTLRInputStream is = new ANTLRInputStream("[lemma = \"value\" & x = \"y\"]");

        final CorpusQLLexer lexer = new CorpusQLLexer(is);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final CorpusQLParser parser = new CorpusQLParser(tokens);

        final ParseTree tree = parser.query();

        System.out.println(tree.toStringTree(parser));

        final MemTreeBuilder builder = new MemTreeBuilder();
        builder.startDocument();

        final CorpusQLXMLVisitor xmlVisitor = new CorpusQLXMLVisitor(builder);
        xmlVisitor.visit(tree);

        builder.endDocument();

        System.out.println(builder.getDocument());
    }
}
