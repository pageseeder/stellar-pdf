package org.pageseeder.stellar.core;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

/**
 * Utility class providing methods to inject title page elements into a PSML document.
 *
 * @author Christophe lauret
 *
 * @since 0.5.0
 * @version 0.5.2
 */
public class TitlePage {

  /**
   * Injects a title page fragment into the given XML document.
   *
   * @param doc the XML document in which to inject the fragment;
   */
  public static void injectTitleFragment(Document doc) {
    Element firstSection = (Element)doc.getDocumentElement().getElementsByTagName("section").item(0);
    if (firstSection != null) {
      Element titleInfoFragment = doc.createElement("fragment");
      titleInfoFragment.setAttribute("id", "title-info-"+System.currentTimeMillis());
      titleInfoFragment.setAttribute("type", "title-info");

      // TODO Make configurable
      Element block = doc.createElement("block");
      block.setAttribute("label", "pdf-date");
      block.setTextContent(LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)));
      titleInfoFragment.appendChild(block);

      firstSection.appendChild(titleInfoFragment);
    }
  }

}
