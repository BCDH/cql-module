/**
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
import org.exist.dom.memtree.DocumentImpl;
import org.exist.dom.memtree.MemTreeBuilder;
import org.junit.Test;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class CorpusQLXMLVisitorTest {

    @Test
    public void positional_attribute() throws TransformerException, IOException {
        DocumentImpl doc = parse("[lemma=\"teapot\"]");
        assertEquals("<cql:query xmlns:cql=\"http://humanistika.org/ns/exist/module/cql\"><cql:position><cql:attribute name=\"lemma\">teapot</cql:attribute></cql:position></cql:query>", toXmlString(doc));

        doc = parse("[lemma='confus.*']");
        assertEquals("<cql:query xmlns:cql=\"http://humanistika.org/ns/exist/module/cql\"><cql:position><cql:attribute name=\"lemma\">confus.*</cql:attribute></cql:position></cql:query>", toXmlString(doc));

        doc = parse("[lemma != 'teapot']");
        assertEquals("<cql:query xmlns:cql=\"http://humanistika.org/ns/exist/module/cql\"><cql:position><cql:attribute name=\"lemma\"><cql:not>teapot</cql:not></cql:attribute></cql:position></cql:query>", toXmlString(doc));

        doc = parse("[lemma='']");
        assertEquals("<cql:query xmlns:cql=\"http://humanistika.org/ns/exist/module/cql\"><cql:position><cql:attribute name=\"lemma\"/></cql:position></cql:query>", toXmlString(doc));

        doc = parse("['teapot']");
        assertEquals("<cql:query xmlns:cql=\"http://humanistika.org/ns/exist/module/cql\"><cql:position><cql:attribute>teapot</cql:attribute></cql:position></cql:query>", toXmlString(doc));
    }

    @Test
    public void positional_attribute_binary() throws TransformerException, IOException {
        DocumentImpl doc = parse("[lemma = 'teapot' & ana = 'N']");
        assertEquals("<cql:query xmlns:cql=\"http://humanistika.org/ns/exist/module/cql\"><cql:position><cql:and><cql:attribute name=\"lemma\">teapot</cql:attribute><cql:attribute name=\"ana\">N</cql:attribute></cql:and></cql:position></cql:query>", toXmlString(doc));

        doc = parse("[lemma = 'teapot' | ana = 'N' & x = 'other']");
        assertEquals("<cql:query xmlns:cql=\"http://humanistika.org/ns/exist/module/cql\"><cql:position><cql:or><cql:attribute name=\"lemma\">teapot</cql:attribute><cql:and><cql:attribute name=\"ana\">N</cql:attribute><cql:attribute name=\"x\">other</cql:attribute></cql:and></cql:or></cql:position></cql:query>", toXmlString(doc));
    }

    @Test
    public void positional_word() throws TransformerException, IOException {
        DocumentImpl doc = parse("'teapot'");
        assertEquals("<cql:query xmlns:cql=\"http://humanistika.org/ns/exist/module/cql\"><cql:position>teapot</cql:position></cql:query>", toXmlString(doc));

        doc = parse("'confus.*'");
        assertEquals("<cql:query xmlns:cql=\"http://humanistika.org/ns/exist/module/cql\"><cql:position>confus.*</cql:position></cql:query>", toXmlString(doc));

        doc = parse("'(wo)?man'");
        assertEquals("<cql:query xmlns:cql=\"http://humanistika.org/ns/exist/module/cql\"><cql:position>(wo)?man</cql:position></cql:query>", toXmlString(doc));
    }

    @Test
    public void sequence() throws TransformerException, IOException {
        DocumentImpl doc = parse("'the' 'tall' 'man'");
        assertEquals("<cql:query xmlns:cql=\"http://humanistika.org/ns/exist/module/cql\"><cql:sequence><cql:position>the</cql:position><cql:position>tall</cql:position><cql:position>man</cql:position></cql:sequence></cql:query>", toXmlString(doc));
    }

    @Test
    public void between() throws TransformerException, IOException {
        DocumentImpl doc = parse("'confus.*' []{2} 'by'");
        assertEquals("<cql:query xmlns:cql=\"http://humanistika.org/ns/exist/module/cql\"><cql:sequence><cql:position>confus.*</cql:position><cql:repeat min=\"2\" max=\"2\"><cql:any/></cql:repeat><cql:position>by</cql:position></cql:sequence></cql:query>", toXmlString(doc));

        doc = parse("'confus.*' []{1,3} 'by'");
        assertEquals("<cql:query xmlns:cql=\"http://humanistika.org/ns/exist/module/cql\"><cql:sequence><cql:position>confus.*</cql:position><cql:repeat min=\"1\" max=\"3\"><cql:any/></cql:repeat><cql:position>by</cql:position></cql:sequence></cql:query>", toXmlString(doc));

        doc = parse("'confus.*' []{2,} 'by'");
        assertEquals("<cql:query xmlns:cql=\"http://humanistika.org/ns/exist/module/cql\"><cql:sequence><cql:position>confus.*</cql:position><cql:repeat min=\"2\"><cql:any/></cql:repeat><cql:position>by</cql:position></cql:sequence></cql:query>", toXmlString(doc));

        doc = parse("'confus.*' []* 'by'");
        assertEquals("<cql:query xmlns:cql=\"http://humanistika.org/ns/exist/module/cql\"><cql:sequence><cql:position>confus.*</cql:position><cql:repeat min=\"0\"><cql:any/></cql:repeat><cql:position>by</cql:position></cql:sequence></cql:query>", toXmlString(doc));

        doc = parse("'confus.*' []+ 'by'");
        assertEquals("<cql:query xmlns:cql=\"http://humanistika.org/ns/exist/module/cql\"><cql:sequence><cql:position>confus.*</cql:position><cql:repeat min=\"1\"><cql:any/></cql:repeat><cql:position>by</cql:position></cql:sequence></cql:query>", toXmlString(doc));

        doc = parse("'confus.*' []? 'by'");
        assertEquals("<cql:query xmlns:cql=\"http://humanistika.org/ns/exist/module/cql\"><cql:sequence><cql:position>confus.*</cql:position><cql:repeat min=\"0\" max=\"1\"><cql:any/></cql:repeat><cql:position>by</cql:position></cql:sequence></cql:query>", toXmlString(doc));
    }

    /**
     * If you want to know all single adjectives used with man (not just "tall"), use this:
     * <code>"an?|the" [pos="ADJ"] "man"</code>
     */
    @Test
    public void singleAdjectives() throws TransformerException, IOException {
        DocumentImpl doc = parse("'an?|the' [pos='ADJ'] 'man'");
        assertEquals("<cql:query xmlns:cql=\"http://humanistika.org/ns/exist/module/cql\"><cql:sequence><cql:position>an?|the</cql:position><cql:position><cql:attribute name=\"pos\">ADJ</cql:attribute></cql:position><cql:position>man</cql:position></cql:sequence></cql:query>", toXmlString(doc));
    }

    /**
     * If we want to see not just single adjectives applied to "man", but multiple as well:
     * <code>"an?|the" [pos="ADJ"]+ "man"</code>
     */
    @Test
    public void multipleAdjectives() throws TransformerException, IOException {
        DocumentImpl doc = parse("'an?|the' [pos='ADJ']+ 'man'");
        assertEquals("<cql:query xmlns:cql=\"http://humanistika.org/ns/exist/module/cql\"><cql:sequence><cql:position>an?|the</cql:position><cql:repeat min=\"1\"><cql:position><cql:attribute name=\"pos\">ADJ</cql:attribute></cql:position></cql:repeat><cql:position>man</cql:position></cql:sequence></cql:query>", toXmlString(doc));
    }

    /**
     * To search for a sequence of nouns, each optionally preceded by an article:
     * <code>("an?|the"? [pos="NOU"])+</code>
     */
    @Test
    public void nouns() throws TransformerException, IOException {
        DocumentImpl doc = parse("('an?|the' [pos='NOU'] 'man')+");
        assertEquals("<cql:query xmlns:cql=\"http://humanistika.org/ns/exist/module/cql\"><cql:repeat min=\"1\"><cql:sequence><cql:position>an?|the</cql:position><cql:position><cql:attribute name=\"pos\">NOU</cql:attribute></cql:position><cql:position>man</cql:position></cql:sequence></cql:repeat></cql:query>", toXmlString(doc));
    }

    private static DocumentImpl parse(final String cql) {
        return parse(cql, false);
    }

    private static DocumentImpl parse(final String cql, final boolean showAstTree) {
        final ANTLRInputStream is = new ANTLRInputStream(cql);

        final CorpusQLLexer lexer = new CorpusQLLexer(is);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final CorpusQLParser parser = new CorpusQLParser(tokens);

        final ParseTree tree = parser.query();

        // useful for debugging
        if(showAstTree) {
            System.out.println(tree.toStringTree(parser));
        }

        final MemTreeBuilder builder = new MemTreeBuilder();
        builder.startDocument();

        final CorpusQLXMLVisitor xmlVisitor = new CorpusQLXMLVisitor(builder);
        xmlVisitor.visit(tree);

        builder.endDocument();

        return builder.getDocument();
    }

    private static String toXmlString(final DocumentImpl doc) throws TransformerException, IOException {
        try(final StringWriter writer = new StringWriter()) {
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.METHOD, "XML");
            final StreamResult result = new StreamResult(writer);
            final DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);
            return result.getWriter().toString();
        }
    }
}
