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

import org.exist.xquery.AbstractInternalModule;
import org.exist.xquery.FunctionDef;

import java.util.List;
import java.util.Map;

public class CQLModule extends AbstractInternalModule {

    public static String CQL_MODULE_NS = "http://humanistika.org/ns/exist/module/cql";
    public static String CQL_MODULE_PREFIX = "cql";

    private final static FunctionDef[] functions = {
            new FunctionDef(CQLParserFunction.FNS_PARSE, CQLParserFunction.class)
    };

    public CQLModule(final Map<String, List<? extends Object>> parameters) {
        super(functions, parameters);
    }

    @Override
    public String getNamespaceURI() {
        return CQL_MODULE_NS;
    }

    @Override
    public String getDefaultPrefix() {
        return CQL_MODULE_PREFIX;
    }

    @Override
    public String getDescription() {
        return "Module for working with Corpus Query Language";
    }

    @Override
    public String getReleaseVersion() {
        return "3.0.RC1";
    }
}
