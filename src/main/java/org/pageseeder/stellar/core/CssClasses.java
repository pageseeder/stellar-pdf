package org.pageseeder.stellar.core;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class CssClasses {

  private CssClasses() {}

  public static void addClasses(Document doc) {
    NodeList nodes = doc.getElementsByTagName("heading");
    for (int i = 0; i < nodes.getLength(); i++) {
      Element e = (Element) nodes.item(i);
      String level = e.getAttribute("level");
      e.setAttribute("class", "h"+level);
    }
  }

}
