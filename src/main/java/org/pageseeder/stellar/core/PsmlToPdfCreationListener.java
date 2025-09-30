package org.pageseeder.stellar.core;

import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.DefaultPDFCreationListener;
import org.xhtmlrenderer.pdf.ITextRenderer;

/**
 * Used to inject the metadata from the PSML document into the PDF document.
 *
 * @author Christophe Lauret
 *
 * @since 0.5.0
 * @version 0.5.2
 */
public class PsmlToPdfCreationListener extends DefaultPDFCreationListener {

  private final Bookmarks bookmarks;

  private final Info info;

  public PsmlToPdfCreationListener(Document doc, int maxTocLevel, int maxBookmarkLevel) {
    TOC.injectLinks(doc, maxTocLevel);
    TitlePage.injectTitleFragment(doc);
    this.info = Info.load(doc);
    this.bookmarks = Bookmarks.load(doc, maxBookmarkLevel);
  }

  @Override
  public void onClose(ITextRenderer renderer) {
    this.bookmarks.writeOutline(renderer);
    this.info.writeValues(renderer);
  }

}
