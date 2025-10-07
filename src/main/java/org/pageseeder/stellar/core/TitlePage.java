package org.pageseeder.stellar.core;

import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @since 0.6.2
 * @version 0.6.2
 */
public class TitlePage {

  private static final Logger LOGGER = LoggerFactory.getLogger(TitlePage.class);

  private TitlePage() {}

  /**
   * Injects a title page fragment into the given XML document.
   *
   * @param doc the XML document in which to inject the fragment;
   */
  public static void injectTitleFragment(Document doc) {
    injectTitleFragment(doc, null);
  }

  /**
   * Injects a title page fragment into the given XML document, configurable.
   *
   * @param doc the XML document in which to inject the fragment;
   * @param config the title page configuration (may be null, in which case default block is injected)
   */
  public static void injectTitleFragment(Document doc, @Nullable TitlePageConfig config) {
    Element firstSection = (Element)doc.getDocumentElement().getElementsByTagName("section").item(0);
    if (firstSection != null) {
      Element titleInfoFragment = doc.createElement("fragment");
      titleInfoFragment.setAttribute("id", "title-info-"+System.currentTimeMillis());
      titleInfoFragment.setAttribute("type", "title-info");

      if (config != null && !config.getItems().isEmpty()) {
        for (TitlePageItem item : config.getItems()) {
          if (item.hasNameAndXpath()) {
            Element block = doc.createElement("block");
            block.setAttribute("label", item.getName() != null ? item.getName() : "");
            try {
              String value = Utils.getElementValue(doc, item.getXpath());
              // TODO handle format
              block.setTextContent(value);
            } catch (Exception ex) {
              LOGGER.warn("Unable to get value for xpath `{}`: {}", item.getXpath(), ex.getMessage());
            }
            titleInfoFragment.appendChild(block);
          } else {
            LOGGER.warn("Invalid title page item: {}", item);
          }
        }
      } else {
        // Default behavior if no config
        Element block = doc.createElement("block");
        block.setAttribute("label", "pdf-date");
        block.setTextContent(LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)));
        titleInfoFragment.appendChild(block);
      }

      firstSection.appendChild(titleInfoFragment);
    }
  }
}
