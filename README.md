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
- It does to compute the numbering headings and paragraphs (you need to use PageSeeder's process task for that)
- It does NOT support SVG images
- It does NOT support Math elements

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
     <block label="pubdate">21 November 2025</block>
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

### PSML export and process

Stellar PDF can work on portable and processed PSML documents.

You can use the [export task](https://dev.pageseeder.com/guide/publishing/ant_api/tasks/task_export.html) to
download the document from PageSeeder and process it locally.

```xml
    <ps:export src="[path on PageSeeder]"
               dest="[path on file system]"
               xrefdepth="1">
      <xrefs types="embed,transclude,math"/>
    </ps:export>
```

The PDF bookmarks are generated automatically from the document's headings.

However, if you want the Table of Contents and auto-numbers to be generated, you should use 
the [process task](https://dev.pageseeder.com/guide/publishing/ant_api/tasks/task_process.html).
with a publication configuration file, as follows:

```xml
    <ps:process src="[source portable PSML]" 
                dest="[target processed PSML]"
                stripmetadata="false" preservesrc="false">
      <xrefs types="embed,transclude,math" >
        <include name="${ps.config.default.uri.filename}" />
      </xrefs>
      <publication config="[path to publication config file]"
                   rootfile="[source portable PSML]"
                   generatetoc="true"
                   headingleveladjust="numbering" />
    </ps:process>
```

Note: You can set the `stripmetadata` attribute to `true`, but you might not be able to use the document title
for the headers and footers from the metadata in that case.

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

    <property name="working" value="${ps.config.default.working}" />
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

## CSS configuration

The CSS styles apply directly to the PSML document to generate as 
a [paged media](https://developer.mozilla.org/en-US/docs/Web/CSS/Guides/Paged_media) document.

If you don't specify a custom CSS, the [default CSS](src/main/resources/psml.css) is used.

When you specify a custom CSS, it is applied after the default CSS as an author style sheet, so you 
can override the default CSS rules.

### Default CSS

The default CSS contains basic CSS rules for the PSML to render correctly, in the same way that
the built-in CSS rules for HTML documents do.

It specifies CSS rules which are common to most PSML documents:
 - font sizes for headings, paragraphs, and code blocks
 - margins and line height for paragraphs and code blocks
 - basic styling for tables, lists, and inline PSML elements
 - display rules for PSML elements

But it is primarily designed to be extended by custom CSS rules, it produces a rather
uninspired PDF.

### Page size and margins

You can specify the page size in the CSS file, for example:

```css
@page {
    size: A4;
    margin: 24mm 20mm;
}
```

### Headers and footers

You can specify the headers and footers in the CSS file, for example:

```css
@page {

    @top-left {
        margin: 10mm 0 5mm 0;
        content: element(title);
        font-size: 9pt;
    }

    @bottom-right {
        margin: 5mm 0 10mm 0;
        content: "Page " counter(page) " of " counter(pages);
        font-size: 9pt;
    }

}

/* Use the title from the document metadata as the header title */
documentinfo > uri > displaytitle {
    display: inline;
    position: running(title);
    text-align: center;
    color: #008995;
}
```

### Title page

If you want to style the title page in your PDF, make sure that you use the `title-page` option.

To style the actual page you can use the `@page:first` selector.

To style the content, you can use the `.title-page` selector which is applied to the first section of the document.

For example:

```css
/* Hide the headers and footers on the title page and show a background image */
@page:first {
    background-image: url(images/title-cover.png);
    background-position: 0 0;
    background-repeat: no-repeat;
    background-size: 210mm 297mm;

    @top-left { content: none; }
    @top-right { content: none; }
    @bottom-left { content: none; }
    @bottom-right {  content: none; }
}

/* Style the heading 1 */
.title-page heading[level='1'] {
    color: #3e0021;
    padding: 7cm 0 0;
    font-weight: 300;
}

/* Style other elements on the title page */
.title-page block[label='pubdate'] {
    color: #ea007e;
    font-size: 15pt;
    font-weight: 300;
}
```

### Headings and paragraphs

The default CSS rules for [heading](https://dev.pageseeder.com/psml/elements/element-heading.html) 
and [paragraphs](https://dev.pageseeder.com/psml/elements/element-para.html) define the font size and 
line height for headings and paragraphs.

Typical customization include changing the color of the headings, specify rules for page breaks, 
formatting the prefix, or adjusting the margins.

Here are a few examples:

```css
heading[level='2'] {
    color: #3e0021;
    page-break-before: always;
}

heading[level='3'] {
    color: #9a0153;
}

heading[prefix]::before,
para[prefix]::before {
    color: #ea007e;
}
```

### Lists

You can specify the style of [lists](https://dev.pageseeder.com/psml/elements/element-list.html), for example:

```css
list {
    list-style-type: square;
}
```

### Tables

You can specify the style of [tables](https://dev.pageseeder.com/psml/elements/element-table.html), for example:

```css
table,
table cell,
table hcell {
    border-color: #004D81;
}

table row[part='header'] cell,
table row[part='header'] hcell {
    background-color: #004D81;
    color: white;
}
```

### Code blocks

You can specify the style of [code blocks](https://dev.pageseeder.com/psml/elements/element-preformat.html), for example:

```css
preformat {
    background-color: #f5f5f5;
    border-radius: 1.5mm;
    border-left: 2mm solid #ffaed9;
    color: #004595;
    font-size: 9pt;
    padding: 2mm;
    page-break-inside: avoid;
}
```

Complete examples of CSS files can be found in the [css test folder](src/test/resources/css).