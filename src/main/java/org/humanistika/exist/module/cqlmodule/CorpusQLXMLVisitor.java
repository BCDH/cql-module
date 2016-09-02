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

import com.evolvedbinary.cql.parser.CorpusQLBaseVisitor;
import com.evolvedbinary.cql.parser.CorpusQLParser.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.exist.dom.QName;
import org.exist.dom.memtree.MemTreeBuilder;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import java.util.Optional;

import static org.humanistika.exist.module.cqlmodule.CorpusQLXMLVisitor.RepetitionMinMax.*;

/**
 * Visitor for the Corpus Query Language AST generated
 * by Antlr4. Produces XML via a {@link MemTreeBuilder}
 */
public class CorpusQLXMLVisitor extends CorpusQLBaseVisitor<CorpusQLXMLVisitor.IntermediateExpr> {

    interface IntermediateExpr {}
    private static final IntermediateExpr WITHIN = new IntermediateExpr(){};
    private static final IntermediateExpr CONTAINING = new IntermediateExpr(){};
    private static final IntermediateExpr AND = new IntermediateExpr(){};
    private static final IntermediateExpr OR = new IntermediateExpr(){};
    private static final IntermediateExpr IMPLICATION = new IntermediateExpr(){};
    static class RepetitionMinMax implements IntermediateExpr {
        public static RepetitionMinMax ZERO_OR_MORE = new RepetitionMinMax(0);
        public static RepetitionMinMax ONE_OR_MORE = new RepetitionMinMax(1);
        public static RepetitionMinMax ZERO_OR_ONE = new RepetitionMinMax(0, 1);

        private final int min;
        private final Optional<Integer> max;

        public RepetitionMinMax(final int min) {
            this.min = min;
            this.max = Optional.empty();
        }

        public RepetitionMinMax(final int min, final int max) {
            this.min = min;
            this.max = Optional.of(max);
        }

        public static RepetitionMinMax exactly(final int exactly) {
            return new RepetitionMinMax(exactly, exactly);
        }

        public int getMin() {
            return min;
        }

        public Optional<Integer> getMax() {
            return max;
        }
    }

    private static QName QName(final String localPart) {
        return new QName(localPart, CQLModule.CQL_MODULE_NS, CQLModule.CQL_MODULE_PREFIX);
    }

    private static final QName QN_QUERY = QName("query");
    private static final QName QN_WITHIN = QName("within");
    private static final QName QN_CONTAINING = QName("containing");
    private static final QName QN_AND = QName("and");
    private static final QName QN_OR = QName("or");
    private static final QName QN_IMPLICATION = QName("implication");
    private static final QName QN_SEQUENCE = QName("sequence");
    private static final QName QN_POSITION = QName("position");
    private static final QName QN_ANY = QName("any");
    private static final QName QN_REPEAT = QName("repeat");
    private static final QName QN_MIN = QName("min");
    private static final QName QN_MAX = QName("max");
    private static final QName QN_ATTRIBUTE = QName("attribute");
    private static final QName QN_NAME = QName("name");
    private static final QName QN_VALUE = QName("value");
    private static final QName QN_NOT = QName("not");
    private static final QName QN_PARENTHESIS = QName("parenthesis");

    private final MemTreeBuilder builder;

    public CorpusQLXMLVisitor(final MemTreeBuilder builder) {
        this.builder = builder;
    }

    @Override
    public IntermediateExpr visitQuery(final QueryContext ctx) {
        builder.startElement(QN_QUERY, null);
        super.visitQuery(ctx);
        builder.endElement();
        return null;
    }

    @Override
    public IntermediateExpr visitComplexQuery(final ComplexQueryContext ctx) {
        if(ctx.queryOperator() != null) {
            final QName qnQueryOperator = toQName(ctx.queryOperator());
            builder.startElement(qnQueryOperator, null);
        }

        visit(ctx.simpleQuery());

        if(ctx.queryOperator() != null) {
            visit(ctx.complexQuery());
            builder.endElement();
        }

        return null;
    }

    @Override
    public IntermediateExpr visitWithin(final WithinContext ctx) {
        return WITHIN;
    }

    @Override
    public IntermediateExpr visitContaining(final ContainingContext ctx) {
        return CONTAINING;
    }

    @Override
    public IntermediateExpr visitSimpleQuery(final SimpleQueryContext ctx) {
        if(ctx.booleanOperator() != null) {
            final QName qnBooleanOperator = toQName(ctx.booleanOperator());
            builder.startElement(qnBooleanOperator, null);
        }

        visit(ctx.sequence());

        if(ctx.booleanOperator() != null) {
            visit(ctx.simpleQuery());
            builder.endElement();
        }

        return null;
    }

    @Override
    public IntermediateExpr visitAnd(final AndContext ctx) {
        return AND;
    }

    @Override
    public IntermediateExpr visitOr(final OrContext ctx) {
        return OR;
    }

    @Override
    public IntermediateExpr visitImplication(final ImplicationContext ctx) {
        return IMPLICATION;
    }

    @Override
    public IntermediateExpr visitSequence(final SequenceContext ctx) {
        final boolean multipleSequenceParts = ctx.sequencePart().size() > 1;
        if(multipleSequenceParts) {
            builder.startElement(QN_SEQUENCE, null);
        }

        super.visitSequence(ctx);

        if(multipleSequenceParts) {
            builder.endElement();
        }

        return null;
    }

    @Override
    public IntermediateExpr visitSequencePart(final SequencePartContext ctx) {
        if(ctx.repetitionAmount() != null) {
            final RepetitionMinMax repetitionMinMax = (RepetitionMinMax)visit(ctx.repetitionAmount());

            final AttributesBuilder attsBuilder = new AttributesBuilder()
                    .add(QN_MIN, String.valueOf(repetitionMinMax.getMin()));
            repetitionMinMax.getMax().ifPresent(max -> attsBuilder.add(QN_MAX, String.valueOf(max)));

            builder.startElement(QN_REPEAT, attsBuilder.build());
        }

        // visit all children (except repetitionAmount)
        for(final ParseTree child : ctx.children) {

            // we have already handle the repetition, so skip it
            if(!(child instanceof RepetitionAmountContext)) {
                visit(child);
            }
        }

        if(ctx.repetitionAmount() != null) {
            builder.endElement();
        }

        return null;
    }

    @Override
    public IntermediateExpr visitRepetitionZeroOrMore(final RepetitionZeroOrMoreContext ctx) {
        return ZERO_OR_MORE;
    }

    @Override
    public IntermediateExpr visitRepetitionOneOrMore(final RepetitionOneOrMoreContext ctx) {
        return ONE_OR_MORE;
    }

    @Override
    public IntermediateExpr visitRepetitionZeroOrOne(final RepetitionZeroOrOneContext ctx) {
        return ZERO_OR_ONE;
    }

    @Override
    public IntermediateExpr visitRepetitionExactly(final RepetitionExactlyContext ctx) {
        return exactly(Integer.parseInt(ctx.NUMBER().getText()));
    }

    @Override
    public IntermediateExpr visitRepetitionMinMax(final RepetitionMinMaxContext ctx) {
        final int min = Integer.parseInt(ctx.NUMBER(0).getText());
        if(ctx.NUMBER().size() == 2) {
            final int max = Integer.parseInt(ctx.NUMBER(1).getText());
            return new RepetitionMinMax(min, max);
        } else {
            return new RepetitionMinMax(min);
        }
    }

    @Override
    public IntermediateExpr visitPositionPositionWord(final PositionPositionWordContext ctx) {
        builder.startElement(QN_POSITION, null);
        super.visitPositionPositionWord(ctx);
        builder.endElement();
        return null;
    }

    @Override
    public IntermediateExpr visitPositionPositionLong(final PositionPositionLongContext ctx) {
        if(ctx.positionLong() == null) {
            builder.startElement(QN_ANY, null);
            builder.endElement();
        } else {
            builder.startElement(QN_POSITION, null);
            visit(ctx.positionLong());
            builder.endElement();
        }
        return null;
    }

    @Override
    public IntermediateExpr visitPositionWord(final PositionWordContext ctx) {
        builder.characters(getString(ctx.quotedString()));
        return null;
    }

    @Override
    public IntermediateExpr visitAttValuePairEquals(final AttValuePairEqualsContext ctx) {
        builder.startElement(QN_ATTRIBUTE, new AttributesBuilder()
                .add(QN_NAME, ctx.propName().getText())
                .build()
        );

        //TODO(AR) figure out valuePart
        visit(ctx.valuePart());

        builder.endElement();
        return null;
    }

    @Override
    public IntermediateExpr visitAttValuePairNotEquals(final AttValuePairNotEqualsContext ctx) {
        builder.startElement(QN_ATTRIBUTE, new AttributesBuilder()
                .add(QN_NAME, ctx.propName().getText())
                .build()
        );

        builder.startElement(QN_NOT, null);

        //TODO(AR) figure out valuePart
        visit(ctx.valuePart());

        builder.endElement();
        builder.endElement();
        return null;
    }

    @Override
    public IntermediateExpr visitAttValuePairDefaultEquals(final AttValuePairDefaultEqualsContext ctx) {
        builder.startElement(QN_ATTRIBUTE, null);

        //TODO(AR) figure out valuePart
        visit(ctx.valuePart());

        builder.endElement();
        return null;
    }

    @Override
    public IntermediateExpr visitPositionLong(final PositionLongContext ctx) {
        if(ctx.booleanOperator() != null) {
            final QName qnBooleanOperator = toQName(ctx.booleanOperator());
            builder.startElement(qnBooleanOperator, null);
        }

        visit(ctx.positionLongPart());

        if(ctx.booleanOperator() != null) {
            visit(ctx.positionLong());
            builder.endElement();
        }

        return null;
    }

    @Override
    public IntermediateExpr visitValuePartString(final ValuePartStringContext ctx) {
        builder.characters(getString(ctx.quotedString()));
        return null;
    }

    @Override
    public IntermediateExpr visitValuePartParenthesised(final ValuePartParenthesisedContext ctx) {
        builder.startElement(QN_PARENTHESIS, null);
        super.visitValuePartParenthesised(ctx);
        builder.endElement();
        return null;
    }

    @Override
    public IntermediateExpr visitValueWith(final ValueWithContext ctx) {
        final QName qnBooleanOperator = toQName(ctx.booleanOperator());
        builder.startElement(qnBooleanOperator, null);

        visit(ctx.valuePart());
        visit(ctx.value());

        builder.endElement();

        return null;
    }


    private String getString(final QuotedStringContext quotedStringContext) {
        final String quotedString = quotedStringContext.getText();

        //drop start and end quotes
        return quotedString.substring(1, quotedString.length() - 1);
    }

    private QName toQName(final QueryOperatorContext queryOperatorContext) {
        final IntermediateExpr queryOperator = visit(queryOperatorContext);
        final QName qnQueryOperator;
        if (queryOperator == WITHIN) {
            qnQueryOperator = QN_WITHIN;
        } else if (queryOperator == CONTAINING) {
            qnQueryOperator = QN_CONTAINING;
        } else {
            throw new IllegalStateException("Unknown queryOperator: " + queryOperator);
        }

        return qnQueryOperator;
    }

    private QName toQName(final BooleanOperatorContext booleanOperatorContext) {
        final IntermediateExpr booleanOperator = visit(booleanOperatorContext);
        final QName qnBooleanOperator;
        if (booleanOperator == AND) {
            qnBooleanOperator = QN_AND;
        } else if (booleanOperator == OR) {
            qnBooleanOperator = QN_OR;
        } else if (booleanOperator == IMPLICATION) {
            qnBooleanOperator = QN_IMPLICATION;
        } else {
            throw new IllegalStateException("Unknown booleanOperator: " + booleanOperator);
        }
        return qnBooleanOperator;
    }

    private static class AttributesBuilder {
        private AttributesImpl attrs = new AttributesImpl();

        public AttributesBuilder add(final String name, final String value) {
            attrs.addAttribute("", name, name, "CDATA", value);
            return this;
        }

        public AttributesBuilder add(final QName name, final String value) {
            attrs.addAttribute("", name.getLocalPart(), name.getLocalPart(), "CDATA", value);
            return this;
        }

        public Attributes build() {
            return attrs;
        }
    }
}
