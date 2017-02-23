# Corpus Query Language module for eXist-db
[![Build Status](https://travis-ci.org/BCDH/cql-module.png?branch=master)](https://travis-ci.org/BCDH/cql-module) [![Java 8+](https://img.shields.io/badge/java-8+-blue.svg)](http://java.oracle.com) [![License](https://img.shields.io/badge/license-GPL%202-blue.svg)](https://www.gnu.org/licenses/gpl-2.0.html) [![Download](https://img.shields.io/badge/download-version%201.1-ff69b4.svg)](http://search.maven.org/remotecontent?filepath=org/humanistika/exist/module/cql-module/1.1/cql-module-1.1-exist.jar)

This is an XQuery Function Extension Module for eXist-db. The module provides a CQL (Corpus Query Language) to XML parser based on [exquery/corpusql-parser](https://github.com/exquery/corpusql-parser).


## Compiling
Requirements: Java 8, Maven 3.

1. `git clone https://github.com/bcdh/cql-module.git`

2. `cd cql-module`

3. `mvn package`


## Installing into eXist-db
1. If you have compiled yourself (see above), copy `cql-module/target/cql-module-1.1-SNAPSHOT-exist.jar` to `$EXIST_HOME/lib/user`, or download `cql-module-1.1-exist.jar` from Maven Central to `$EXIST_HOME/lib/user`

2. Edit `$EXIST_HOME/conf.xml` and add the following to the `<builtin-modules>`:

    ```xml
    <module uri="http://humanistika.org/ns/exist/module/cql" class="org.humanistika.exist.module.cqlmodule.CQLModule"/>
    ```

3. Restart eXist-db

## Usage
The module exports a single function for use in your XQuery(s), for example:

```xquery
xquery version "1.0";

import module namespace cql = "http://humanistika.org/ns/exist/module/cql";

cql:parse("[lemma='bob' &amp; ana='x']")
```

would produce the output:

```xml
<cql:query xmlns:cql="http://humanistika.org/ns/exist/module/cql">
    <cql:position>
        <cql:and>
            <cql:attribute name="lemma">bob</cql:attribute>
            <cql:attribute name="ana">x</cql:attribute>
        </cql:and>
    </cql:position>
</cql:query>
```

For further examples of the XML that will be produced see [CorpusQLXMLVisitorTest](https://github.com/BCDH/cql-module/blob/master/src/test/java/org/humanistika/exist/module/cqlmodule/CorpusQLXMLVisitorTest.java#L42)
