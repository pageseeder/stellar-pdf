package org.pageseeder.stellar;

import org.pageseeder.stellar.core.PdfGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public final class Main {

  public static void main(String[] args) throws Exception {
    Properties main = new Properties();
    File f  = new File("main.properties");
    checkExists(f);
    try (InputStream in = new FileInputStream(f)) {
      main.load(in);
    }

    // Get arguments
    File source = getFile(main.getProperty("source"));
    File output = getFile(main.getProperty("output"));
    File stylesheet = getFile(main.getProperty("stylesheet"));
    File fontsDir = getFile(main.getProperty("fonts"));
    int maxBookmarkLevel = getInt(main.getProperty("maxBookmarkLevel"), 6);
    int maxTocLevel = getInt(main.getProperty("maxTocLevel"), 6);

    // Check arguments
    checkExists(source);
    checkExists(output.getParentFile());
    if (stylesheet != null) checkExists(stylesheet);
    if (fontsDir != null) checkExists(fontsDir);

    // Generate the PDF
    PdfGenerator generator = new PdfGenerator();
    generator.setMaxBookmarkLevel(maxBookmarkLevel);
    generator.setMaxTocLevel(maxTocLevel);
    if (stylesheet != null) generator.setAuthorStylesheet(stylesheet);
    if (fontsDir != null) generator.setFontsDir(fontsDir);
    generator.generatePDF(source, output);
  }

  private static int getInt(String value, int defaultValue) {
    return value != null ? Integer.parseInt(value) : defaultValue;
  }

  private static File getFile(String value) {
    return value != null ? new File(value) : null;
  }

  private static void checkExists(File file) {
    if (!file.exists()) {
      throw new IllegalArgumentException(file.getName()+" file not found in "+file.getParentFile().getAbsoluteFile());
    }
  }
}
