# Corpus Query Language module for Elemental or eXist-db
[![Build Status](https://github.com/BCDH/cql-module/actions/workflows/ci.yml/badge.svg)](https://github.com/BCDH/cql-module/actions/workflows/ci.yml)
[![Java 8+](https://img.shields.io/badge/java-8+-blue.svg)](http://java.oracle.com)
[![License](https://img.shields.io/badge/license-GPL%202-blue.svg)](https://www.gnu.org/licenses/gpl-2.0.html)
[![Maven Central](https://img.shields.io/maven-central/v/org.humanistika.exist.module/cql-module?logo=apachemaven&label=maven+central&color=green)](https://search.maven.org/search?q=g:org.humanistika.exist.module)

This is an XQuery Function Extension Module for Elemental or eXist-db. The module provides a CQL (Corpus Query Language) to XML parser based on [exquery/corpusql-parser](https://github.com/exquery/corpusql-parser).


## Installation into Elemental or eXist-db
You can install the module into Elemental or eXist-db in either one of two ways:
1. As an EXPath Package (.xar file)
2. Directly as a XQuery Java Extension Module (.jar file)

### Easy Option - EXPath Package Installation into eXist-db (.xar)
1. Download the latest XAR file from the releases here: https://github.com/BCDH/cql-module/releases/
   * or if you have [compiled](#compiling) the code yourself you can find the `cql-module-1.5.0-SNAPSHOT.xar` file in the `target` subfolder.

2. You can take the .xar file and upload it via Elemental or eXist-db's EXPath Package Manager app from its Dashboard

3. Restart Elemental (or eXist-db)

### Advanced Option - Direct Installation into eXist-db (.jar)
1. Download the latest Jar file from the releases here: https://github.com/BCDH/cql-module/releases/
   * or if you have [compiled](#compiling) the code yourself you can find the `cql-module-1.5.0-SNAPSHOT-exist.jar` file in the `target` subfolder.

2. Copy the .jar file to either `$ELEMENTAL_HOME/lib` (or substitute `$EXIST_HOME` instead of `$ELEMENTAL_HOME` for eXist-db).

3. Edit `$ELEMENTAL_HOME/etc/conf.xml` and add the following to the `<builtin-modules>`:

    ```xml
    <module uri="http://humanistika.org/ns/exist/module/cql" class="org.humanistika.exist.module.cqlmodule.CQLModule"/>
    ```
4. Edit `$ELEMENTAL_HOME/etc/startup.xml` and add the following to the `<dependencies>`:

   ```xml
         <dependency>
           <groupId>org.humanistika.exist.module</groupId>
           <artifactId>cql-module</artifactId>
           <version>1.5.0-SNAPSHOT</version> <!-- NOTE: Modify this to the version you are using -->
           <relativePath>cql-module-1.5.0-SNAPSHOT-exist.jar</relativePath> <!-- NOTE: this should reflect the exact filename in lib folder that you copied earlier -->
         </dependency>
   ```

5. Restart Elemental (or eXist-db)


## Usage
The module exports a single function for use in your XQuery(s), for example:

```xquery
xquery version "3.1";

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

## Compiling
Requirements: Java 8, Maven 3.

1. `git clone https://github.com/bcdh/cql-module.git`

2. `cd cql-module`

3. `mvn package`
