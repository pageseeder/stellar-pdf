package org.pageseeder.stellar.core;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class PsmlDecorator {

  private PsmlDecorator() {}

  public static void addClasses(Document doc) {
    NodeList nodes = doc.getElementsByTagName("heading");
    for (int i = 0; i < nodes.getLength(); i++) {
      Element e = (Element) nodes.item(i);
      String level = e.getAttribute("level");
      e.setAttribute("class", "h"+level);
    }
  }

  public static void addIds(Document doc) {
    // For the bookmarks we need all headings and section titles to have an ID
    NodeList headings = doc.getElementsByTagName("heading");
    for (int i = 0; i < headings.getLength(); i++) {
      Element h = (Element) headings.item(i);
      if (!h.hasAttribute("id")) {
        h.setAttribute("id", "toc-h-"+i);
      }
    }
    NodeList titles = doc.getElementsByTagName("title");
    for (int i = 0; i < titles.getLength(); i++) {
      Element t = (Element) titles.item(i);
      if (t.getParentNode().getNodeName().equals("section") && !t.hasAttribute("id")) {
        t.setAttribute("id", "toc-t-"+i);
      }
    }
  }


}
