package org.pageseeder.stellar.ant;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import java.io.*;

import org.pageseeder.stellar.core.PdfGenerator;
import org.pageseeder.stellar.core.TitlePageConfig;

public class PdfExportTask extends Task {

  private String src;

  private String dest;

  private String fontsDir;
  
  private String stylesheet;

  private int maxBookmarkLevel = 6;

  private int maxTocLevel = 6;

  private TitlePageConfig titlePageConfig = null;

  public void setSrc(String src) {
    this.src = src;
  }

  public void setDest(String dest) {
    this.dest = dest;
  }

  public void setFontsDir(String fontsDir) {
    this.fontsDir = fontsDir;
  }

  public void setStylesheet(String stylesheet) {
    this.stylesheet = stylesheet;
  }

  public void setMaxBookmarkLevel(int maxBookmarkLevel) {
    this.maxBookmarkLevel = maxBookmarkLevel;
  }

  public void setMaxTocLevel(int maxTocLevel) {
    this.maxTocLevel = maxTocLevel;
  }

  // Support for nested <title-page> configuration
  public void addConfiguredTitlePage(TitlePageConfig config) {
    this.titlePageConfig = config;
  }

  @Override
  public void execute() throws BuildException {
    if (src == null || dest == null) {
      throw new BuildException("Both src and dest attributes are required");
    }

    try {
      File input = new File(src);
      File output = new File(dest);

      ensureOutputDirectory(output.getParentFile());

      log("Exporting PSML file: "+input.getName()+" to PDF "+output.getName());

      PdfGenerator generator = newPdfGenerator();

      // Pass title-page config if present
      if (titlePageConfig != null) {
        generator.setTitlePageConfig(titlePageConfig);
      }

      generator.generatePDF(input, output);

      log("Conversion completed successfully");

    } catch (Exception e) {
      throw new BuildException("Error converting PSML to Markdown: " + e.getMessage(), e);
    }
  }

  /**
   * Ensure the output directory exists.
   * @param outputDir the output directory.
   */
  private static void ensureOutputDirectory(File outputDir) {
    if (!outputDir.exists()) {
      boolean created = outputDir.mkdirs();
      if (!created) {
        throw new BuildException("Unable to create output directory: "+ outputDir.getName());
      }
    }
  }

  /**
   * Create a new PDF generator using the configuration of this task.
   *
   * @return a new PDF generator.
   */
  private PdfGenerator newPdfGenerator() {
    PdfGenerator generator = new PdfGenerator();

    if (this.fontsDir != null) {
      File fonts = new File(this.fontsDir);
      if (!fonts.exists()) {
        throw new BuildException("Font directory does not exist: " + fonts);
      }
      generator.setFontsDir(fonts);
    }

    if (this.stylesheet != null) {
      File authorStylesheet = new File(this.stylesheet);
      if (!authorStylesheet.exists()) {
        throw new BuildException("Stylesheet file does not exist: " + authorStylesheet);
      }
      generator.setAuthorStylesheet(authorStylesheet);
    }

    generator.setMaxBookmarkLevel(this.maxBookmarkLevel);
    generator.setMaxTocLevel(this.maxTocLevel);

    return generator;
  }
}