package org.pageseeder.stellar.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Utility class providing methods to inject TOC links into a PSML document.
 *
 * @author Christophe lauret
 *
 * @since 0.5.0
 * @version 0.5.2
 */
public class TOC {

  private static final Logger LOGGER = LoggerFactory.getLogger(TOC.class);

  public static final int DEFAULT_MAX_LEVEL = 6;

  private TOC() {}

  /**
   * Injects hyperlink elements into the given XML document for each "toc-part" element.
   * For each "toc-part" element, a "link" element is created with its "href", "prefix",
   * and content set, and injected as the first child node if the "toc-part" has children;
   * otherwise, it is appended.
   *
   * @param doc the XML document in which to inject "link" elements;
   *            must not be null and should have a root element containing "toc-part" elements
   */
  public static void injectLinks(Document doc) {
    injectLinks(doc, DEFAULT_MAX_LEVEL);
  }

  /**
   * Injects hyperlink elements into the given XML document for each "toc-part" element.
   * For each "toc-part" element, a "link" element is created with its "href", "prefix",
   * and content set, and injected as the first child node if the "toc-part" has children;
   * otherwise, it is appended.
   *
   * @param doc the XML document in which to inject "link" elements;
   *            must not be null and should have a root element containing "toc-part" elements
   * @param maxLevel the maximum level of TOC parts to inject
   */
  public static void injectLinks(Document doc, int maxLevel) {
    NodeList parts = doc.getDocumentElement().getElementsByTagName("toc-part");
    for (int i = 0; i < parts.getLength(); i++) {
      Element part = (Element) parts.item(i);
      int level = Utils.getIntAttribute(part, "level", -1);
      if (level <= maxLevel) {
        Element link = doc.createElement("link");
        link.setAttribute("href", "#" + part.getAttribute("idref"));
        link.setAttribute("prefix", part.getAttribute("prefix"));
        link.setTextContent(Utils.normalizeSpace(part.getAttribute("title")));
        if (part.hasChildNodes()) {
          part.insertBefore(link, part.getFirstChild());
        } else {
          part.appendChild(link);
        }
      } else {
        LOGGER.debug("Ignoring toc-part level: {}", level);
      }
    }
  }

}
