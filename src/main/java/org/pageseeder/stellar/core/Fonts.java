package org.pageseeder.stellar.core;

import com.lowagie.text.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.IOException;

/**
 * Utility class for loading TrueType font files into an {@link ITextRenderer}.
 *
 * @author Christophe lauret
 *
 * @since 0.5.0
 * @version 0.5.2
 */
public final class Fonts {

  private static final Logger LOGGER = LoggerFactory.getLogger(Fonts.class);

  private Fonts() {}

  /**
   * Loads TrueType font files from the specified directory into the given ITextRenderer.
   * This method scans the provided directory for `.ttf` font files and adds them to the
   * renderer's font resolver. If no font files are found, no action is taken.
   *
   * @param renderer the {@link ITextRenderer} instance where the fonts should be loaded
   * @param fontsDir the directory containing the TrueType font files to be loaded
   */
  public static void loadFonts(ITextRenderer renderer, File fontsDir) {
    try {
      ITextFontResolver fontResolver = renderer.getFontResolver();
      File[] fontFiles = fontsDir.listFiles((dir, name) -> name.endsWith(".ttf"));
      if (fontFiles != null) {
        for (File font : fontFiles) {
          fontResolver.addFont(font.getAbsolutePath(), true);
          LOGGER.debug("Added font {}", font.getName());
        }
      }
    } catch (DocumentException | IOException ex) {
      LOGGER.error("Unable to load fonts", ex);
    }
  }

}
