# stellar-pdf

Generate beautiful, print-ready PDFs from PSML documents with the Flying Saucer library

## License

- This project is licensed under the [Apache License, Version 2.0](LICENCE.md).
- It depends on external libraries, including [Flying Saucer](https://github.com/flyingsaucerproject/flyingsaucer), which is licensed under the [GNU Lesser General Public License (LGPL)](https://www.gnu.org/licenses/lgpl-2.1.html).
- This project includes fonts for testing purposes licenced under the SIL open font licence.

## Notices and Requirements for LGPL Libraries

- When using, distributing, or modifying this project, **please be aware that you must also comply with the terms of the LGPL** as it applies to any LGPL-licensed libraries.
- For details, refer to the [LGPL license text](https://www.gnu.org/licenses/lgpl-2.1.html).
- The source code for these third-party libraries can be found at their respective repositories. For Flying Saucer, see: [https://github.com/flyingsaucerproject/flyingsaucer](https://github.com/flyingsaucerproject/flyingsaucer).

## Requirements

This project requires the following:
- **Java 11** or later.
- **Ant 1.8** or later.

## Architecture

This project uses CSS to generate PDF from PSML documents using the Flying Saucer library and OpenPDF.

Flying Saucer is primarily focused on HTML, but can also generate PDFs from XML documents. This
project provides adaptors for PSML as well built-in a CSS so that the PSML syntax can be used to
generate PDFs.

It can work on any PSML documents, but if you want to include bookmarks or a table of contents, you
should process the document.

To generate a PDF, this project uses two sets of files:
1. **Source files**: The PSML files and images to convert to PSML.
2. **Format files**: The CSS files, images and fonts used for formatting and styling.

## Limitations

This project is still in development and not yet ready for production.

Here are some known limitations:
- It does not support the `set-string` CSS property
- It only generates the TOC if processed by PageSeeder
- It does to compute the numbering headings and paragraphs.

## Stellar tasks

### `stellar:export-pdf`

This task generates a PDF from a PSML document. It supports the following attributes:

- `src`: the PSML document to process
- `dest`: the PDF file to generate
- `fontsdir`: the directory containing the fonts to use
- `stylesheet`: the CSS stylesheet to use
- `maxBookmarkLevel`: the max level of bookmarks to generate
- `maxTocLevel`: the max level generate for the Table of Contents

### Title page

It also supports the following nested elements:

 `<title-page>` with `<item>` elements to include the title page.

Each `<item>` element can have the following attributes:`

- `name`: the name of the item to include in the title page
- `xpath`: the XPath expression to extract the value from the document
- `format`: the date format using Java date formatting conventions

For example:

```xml
  <title-page>
    <item name="subtitle" xpath="//property[@name='subtitle']/@value"/>
    <item name="pubdate" xpath="//property[@name='pubdate']/@value" format="d MMMM yyyy" />
  </title-page>
```

The title page instruction injects contents into the **first section** of the document as
a fragment with type `title-page`.

```xml
  <fragment id="title-page-1763706659718" type="title-page">
     <block label="subtitle">An example of a subtitle</block>
     <block label="subtitle">21 November 2025</block>
  </fragment>
```

### Basic examples

To process a PSML document and generate a PDF with the default CSS:

```xml
  <stellar:export-pdf src="example.psml" dest="example.pdf" />
```

To process a PSML document and generate a PDF with a custom CSS:

```xml
    <stellar:export-pdf src="example.psml"
                        dest="example.pdf"
                        stylesheet="example.css"
                        fontsDir="fonts" />
```

To process a PSML document and generate a PDF with a custom title page:

```xml
    <stellar:export-pdf src="example.psml"
                        dest="example.pdf"
                        stylesheet="example.css"
                        fontsDir="fonts">
      <title-page>
        <item name="revised_date" xpath="(//property[@name='revised_date'])[1]/@value" format="d MMMM YYYY" />
      </title-page>
    </stellar:export-pdf>
```

You can find more examples in the [test resource folder](src/test/resources).

## PageSeeder usage

To use this project, you need to update your `build.xml` to load the stellar ant task,
then use the `stellar:export-pdf` task to generate the PDF.

### Load the stellar ant task

Use the `copyToLib` ant task to jar required files to the `build/output/lib` folder.
Then upload the jars to your PageSeeder project template, for example in 
the `template/[project]/stellar/lib` folder. NB You do not need to include SLF4J in 
PageSeeder as it is already provided by the environment.

You can use the following snippet to load the stellar ant task:

```xml
    <!-- Update the `stellar-dir` property to match your project -->
    <path id="stellar.classpath"><fileset dir="${stellar-dir}/lib" includes="*.jar" /></path>
    <taskdef uri="antlib:org.pageseeder.stellar.ant" classpathref="stellar.classpath"/>
```

You can then define the `stellar` namespace as `xmlns:stellar="antlib:org.pageseeder.stellar.ant"`
to use the Stellar PDF tasks in your project.

### Generate the PDF

Use the `stellar:export-pdf` task to generate the PDF, for example:
```xml
    <stellar:export-pdf src="example.psml" dest="example.pdf"
                 stylesheet="${stellar-dir}/css/example.css" />
```

### Example

The following is an example of a `build.xml` file that uses the stellar ant task to generate a PDF
for a PSML document.

```xml
<project name="pageseeder-document" 
         xmlns:ps="antlib:com.pageseeder.publishapi.ant"
         xmlns:stellar="antlib:org.pageseeder.stellar.ant">

  <target name="create-pdf" description="Create PDF document">

    <!-- Update the `stellar-dir` property to match your project -->
    <property name="publication-config" value="../../../../../../template/default/publication/default/publication-config.xml"/>
    <property name="stellar-dir" value="${basedir}/../../../stellar"/>

    <!-- 1. Load the stellar ant task -->
    <ps:progress percent="1" message="Loading stellar tasks"/>
    <path id="stellar.classpath"><fileset dir="${stellar-dir}/lib" includes="*.jar" /></path>
    <taskdef uri="antlib:org.pageseeder.stellar.ant" classpathref="stellar.classpath"/>

    <!-- 2. Load the PageSeeder configuration -->
    <ps:progress percent="5" message="Loading PageSeeder configuration"/>
    <ps:config />

    <property name="download" value="${ps.config.default.working}/download" />
    <property name="process" value="${ps.config.default.working}/process" />
    <mkdir dir="${download}"/>
    <mkdir dir="${process}"/>

    <!-- 3. Download the document from PageSeeder -->
    <ps:progress percent="10" message="Exporting PSML from ${ps.config.default.uri.path}"/>
    <ps:export src="${ps.config.default.uri.path}"
               dest="${download}"
               xrefdepth="1"
               version="current"
               publicationid="${ps.param.publicationid}">
      <xrefs types="embed,transclude,math"/>
    </ps:export>

    <!-- 4. Process the document to generate the TOC, compute the numbering, embed xrefs -->
    <ps:progress percent="30" message="Processing PSML"/>
    <ps:process src="${download}" dest="${process}" stripmetadata="false" preservesrc="false">
      <xrefs types="embed,transclude,math" >
        <include name="${ps.config.default.uri.filename}" />
      </xrefs>
      <publication config="${publication-config}"
                   rootfile="${ps.config.default.uri.filename}"
                   generatetoc="true"
                   headingleveladjust="numbering" />
    </ps:process>

    <!-- 5. Generate the PDF from the processed document -->
    <ps:progress percent="60" message="Generating PDF"/>
    <stellar:export-pdf src="${process}/${ps.config.default.uri.filename}"
                       dest="${working}/${ps.config.default.uri.filename.no.ext}.pdf"
                   fontsdir="${stellar-dir}/fonts"
                 stylesheet="${stellar-dir}/css/default.css" />

    <ps:progress percent="90"
                 result="window"
                 resultfile="${working}/${ps.config.default.uri.filename.no.ext}.pdf" />
  </target>

</project>
```
