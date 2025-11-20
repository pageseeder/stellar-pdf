package org.pageseeder.stellar.core;

import com.lowagie.text.DocumentException;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.ITextUserAgent;
import org.xhtmlrenderer.resource.XMLResource;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * The PdfGenerator class provides functionality to generate PDF documents from input files.
 * It allows customization of table of contents (TOC) levels, bookmark levels, and author stylesheets.
 * Additionally, it supports font loading from a specified directory.
 *
 * @author Christophe lauret
 *
 * @since 0.5.0
 * @version 0.5.2
 */
public class PdfGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(PdfGenerator.class);

  private int maxTocLevel = 6;

  private int maxBookmarkLevel = 6;

  private @Nullable File fontsDir;

  private @Nullable String authorStylesheetUrl;

  private @Nullable TitlePageConfig titlePageConfig;

  public void setTitlePageConfig(TitlePageConfig config) {
    this.titlePageConfig = config;
  }

  public void setMaxBookmarkLevel(int maxBookmarkLevel) {
    this.maxBookmarkLevel = maxBookmarkLevel;
  }

  public void setMaxTocLevel(int maxTocLevel) {
    this.maxTocLevel = maxTocLevel;
  }

  public void setAuthorStylesheet(File stylesheet) {
    this.authorStylesheetUrl = stylesheet.toURI().toString();
  }

  public void setFontsDir(File fontsDir) {
    this.fontsDir = fontsDir;
  }

  public void generatePDF(File input, File output) throws IOException, DocumentException {
    try (OutputStream out = Files.newOutputStream(output.toPath())) {
      ITextRenderer renderer = new ITextRenderer();

      SharedContext sharedContext = renderer.getSharedContext();
      ResourceLoaderUserAgent callback = new ResourceLoaderUserAgent(renderer.getOutputDevice(), sharedContext.getDotsPerPixel());
      sharedContext.setUserAgentCallback(callback);

      PsmlReplacedElementFactory factory = new PsmlReplacedElementFactory(sharedContext.getReplacedElementFactory(), input.getParentFile());
      sharedContext.setReplacedElementFactory(factory);

      // Include embedded fonts
      if (this.fontsDir != null) {
        Fonts.loadFonts(renderer, this.fontsDir);
      }

      // Add PSML handler and set the author stylesheet
      PsmlNamespaceHandler namespaceHandler = new PsmlNamespaceHandler();
      if (this.authorStylesheetUrl != null) {
        namespaceHandler.addAuthorStylesheet(this.authorStylesheetUrl);
      }

      // Process document
      Document doc = XMLResource.load(new InputSource(input.toURI().toString())).getDocument();

      // Augment the document
      TOC.injectLinks(doc, this.maxTocLevel);
      TitlePage.injectTitleFragment(doc, this.titlePageConfig);
      PsmlDecorator.addClasses(doc);
      PsmlDecorator.addIds(doc);

      try {
        // To help debug
        File pdfPsml = new File(input.getParentFile(), input.getName().replace(".psml", ".pdf.psml"));
        Utils.writeDocumentToXML(doc, pdfPsml);
      } catch (Exception ex) {
        LOGGER.warn("Unable to write PDF PSML file", ex);
      }

      renderer.setDocument(doc, input.toURI().toString(), namespaceHandler);
      renderer.setListener(new PsmlToPdfCreationListener(doc, this.maxBookmarkLevel));
      renderer.layout();
      renderer.createPDF(out);
    }
  }

  private static class ResourceLoaderUserAgent extends ITextUserAgent {
    private ResourceLoaderUserAgent(ITextOutputDevice outputDevice, int dotsPerPixel) {
      super(outputDevice, dotsPerPixel);
    }

    @Override
    protected InputStream resolveAndOpenStream(String uri) {
      InputStream is = super.resolveAndOpenStream(uri);
      LOGGER.debug("IN resolveAndOpenStream({})", uri);
      return is;
    }

  }
}
