package org.pageseeder.stellar.core;

import com.lowagie.text.pdf.PdfDestination;
import com.lowagie.text.pdf.PdfOutline;
import com.lowagie.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.pdf.DOMUtil;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.InlineLayoutBox;
import org.xhtmlrenderer.render.PageBox;
import org.xhtmlrenderer.render.RenderingContext;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class for loading bookmarks from a PSML document and generating the outline of a PDF document.
 *
 * @author Christophe Lauret
 *
 * @since 0.5.0
 * @version 0.5.2
 */
public final class Bookmarks {

  private static final Logger LOGGER = LoggerFactory.getLogger(Bookmarks.class);

  public static final int DEFAULT_MAX_LEVEL = 6;

  private final List<PsmlBookmark> list;

  private Bookmarks(List<PsmlBookmark> bookmarks) {
    this.list = bookmarks;
  }

  /**
   * Loads a list of bookmarks from the specified PSML document.
   *
   * @param doc the XML document from which bookmarks are to be extracted
   * @return a list of {@code PsmlBookmark} objects parsed from the document,
   *         or an empty list if no bookmarks are found
   */
  public static Bookmarks load(Document doc) {
    return load(doc, DEFAULT_MAX_LEVEL);
  }

  /**
   * Loads a list of bookmarks from the specified PSML document.
   *
   * @param doc the XML document from which bookmarks are to be extracted
   * @param maxLevel the maximum level of bookmarks to extract
   * @return a list of {@code PsmlBookmark} objects parsed from the document,
   *         or an empty list if no bookmarks are found
   */
  public static Bookmarks load(Document doc, int maxLevel) {
    List<PsmlBookmark> bookmarks = new ArrayList<>();
    loadBookmarks(doc, bookmarks, maxLevel);
    return new Bookmarks(bookmarks);
  }

  /**
   * Generates and writes the outline (bookmarks) of a PDF document based on the given list of bookmarks.
   * Adjusts the PDF document to display the outline in the PDF viewer.
   *
   * @param renderer  the {@code ITextRenderer} used to generate and render PDF content
   */
  public void writeOutline(ITextRenderer renderer) {
    if (this.list.isEmpty()) {
      LOGGER.info("No bookmarks to render");
    } else {
      RenderingContext context = new RenderingContext(renderer.getSharedContext());
      Box root = renderer.getRootBox();
      PdfWriter writer = renderer.getWriter();
      writer.setViewerPreferences(PdfWriter.PageModeUseOutlines);
      writeBookmarks(renderer, context, root, writer.getRootOutline(), this.list);
    }
  }

  /**
   * Loads a list of bookmarks from the specified PSML document.
   *
   * @param doc the XML document from which bookmarks are to be extracted
   * @param bookmarks the list of bookmarks to populate
   * @param maxLevel the maximum level of bookmarks to extract
   */
  public static void loadBookmarks(Document doc, List<PsmlBookmark> bookmarks, int maxLevel) {
    Element toc = DOMUtil.getChild(doc.getDocumentElement(), "toc");
    if (toc != null) {
      Element tree = DOMUtil.getChild(toc, "toc-tree");
      if (tree != null) {
        List<Element> parts = DOMUtil.getChildren(tree, "toc-part");
        for (Element p : parts) {
          loadBookmark(null, p, bookmarks, maxLevel);
        }
      }
    } else {
      LOGGER.info("No TOC found");
    }
  }

  /**
   * Loads a bookmark from the specified part.
   *
   * @param parent the parent bookmark, or {@code null} if none
   * @param part the TOC part from which the bookmark is to be extracted
   * @param bookmarks the list of bookmarks to populate
   */
  private static void loadBookmark(PsmlBookmark parent, Element part, List<PsmlBookmark> bookmarks, int maxLevel) {
    String prefix = part.getAttribute("prefix");
    String title = Utils.normalizeSpace(part.getAttribute("title"));
    String idref = part.getAttribute("idref");
    String name = prefix.isEmpty() ? title : prefix + " " + title;
    PsmlBookmark bookmark = new PsmlBookmark(name, idref);
    if (parent == null) {
      bookmarks.add(bookmark);
    } else {
      parent.addChild(bookmark);
    }
    int level = Utils.getIntAttribute(part, "level", -1);
    if (level < maxLevel) {
      List<Element> parts = DOMUtil.getChildren(part, "toc-part");
      for (Element p : parts) {
        loadBookmark(bookmark, p, bookmarks, maxLevel);
      }
    }
  }

  private static void writeBookmarks(ITextRenderer renderer, RenderingContext context, Box root, PdfOutline parent, List<PsmlBookmark> bookmarks) {
    for (PsmlBookmark bookmark : bookmarks) {
      LOGGER.debug("Writing Bookmark {} {}", bookmark.getName(), bookmark.getIdref());
      writeBookmark(renderer, context, root, parent, bookmark);
    }
  }

  private static void writeBookmark(ITextRenderer renderer, RenderingContext c, Box root, PdfOutline parent, PsmlBookmark bookmark) {
    float dotsPerPoint = renderer.getOutputDevice().getDotsPerPoint();
    int startPageNo = renderer.getOutputDevice().getStartPageNo();
    String idref = bookmark.getIdref();
    PdfDestination target = null;
    Box box = bookmark.getBox();
    if (!idref.isEmpty()) {
      box = renderer.getSharedContext().getBoxById(idref);
    }
    if (box != null) {
      PageBox page = root.getLayer().getPage(c, getPageRefY(box));
      int distanceFromTop = page.getMarginBorderPadding(c, CalculatedStyle.TOP);
      distanceFromTop += box.getAbsY() - page.getTop();
      target = new PdfDestination(PdfDestination.XYZ, 0, normalizeY(page, c, distanceFromTop / dotsPerPoint), 0);
      target.addPage(renderer.getWriter().getPageReference(startPageNo + page.getPageNo() + 1));
    }
    if (target == null) {
      // TODO new PdfDestination(PdfDestination.FITH, height); where height is the height of the page
      target = new PdfDestination(PdfDestination.FITH);
    }
    PdfOutline outline = new PdfOutline(parent, target, bookmark.getName());
    writeBookmarks(renderer, c, root, outline, bookmark.getChildren());
  }

  private static int getPageRefY(Box box) {
    if (box instanceof InlineLayoutBox) {
      InlineLayoutBox iB = (InlineLayoutBox) box;
      return iB.getAbsY() + iB.getBaseline();
    } else {
      return box.getAbsY();
    }
  }

  private static float normalizeY(PageBox page, RenderingContext c,  float y) {
    return page.getHeight(c) - y;
  }

}
