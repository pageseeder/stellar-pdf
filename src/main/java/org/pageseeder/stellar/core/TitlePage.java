package org.pageseeder.stellar.core;

import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.xpath.XPathExpressionException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;

/**
 * Utility class providing methods to inject title page elements into a PSML document.
 *
 * @author Christophe lauret
 *
 * @since 0.6.2
 * @version 0.7.0
 */
public class TitlePage {

  private static final Logger LOGGER = LoggerFactory.getLogger(TitlePage.class);

  // Parser that handles ISO dates (2023-10-10) and ISO date-times (2023-10-10T10:00:00Z)
  private static final DateTimeFormatter ISO_PARSER = new DateTimeFormatterBuilder()
      .appendOptional(DateTimeFormatter.ISO_DATE_TIME)
      .appendOptional(DateTimeFormatter.ISO_DATE)
      .toFormatter();

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
    if (firstSection != null && config != null) {
      firstSection.setAttribute("class", "title-page");
      if (!config.isEmpty()) {
        createTitlePageFragment(doc, config, firstSection);
      }
    }
  }

  /**
   * Creates a title page fragment from the given configuration.
   *
   * @param doc the XML document in which to inject the fragment;
   * @param config the title page configuration (may be null, in which case default block is injected)
   * @param firstSection the first section in the document
   */
  private static void createTitlePageFragment(Document doc, TitlePageConfig config, Element firstSection) {
    Element titleInfoFragment = doc.createElement("fragment");
    titleInfoFragment.setAttribute("id", "title-page-"+System.currentTimeMillis());
    titleInfoFragment.setAttribute("type", "title-page");

    for (TitlePageItem item : config.getItems()) {
      if (item.hasNameAndXpath()) {
        Element block = doc.createElement("block");
        block.setAttribute("label", item.getName());
        try {
          String xpath = item.getXpath().trim();
          String value = getValue(doc, xpath, item.getFormat());
          block.setTextContent(value);

        } catch (Exception ex) {
          LOGGER.warn("Unable to get value for xpath `{}`: {}", item.getXpath(), ex.getMessage());
        }
        titleInfoFragment.appendChild(block);
      } else {
        LOGGER.warn("Invalid title page item: {}", item);
      }
    }
    firstSection.appendChild(titleInfoFragment);
  }

  /**
   * Formats a given date/time value into a specified format using patterns.
   * This method attempts to parse an ISO8601 string into a temporal object
   * and reformat it according to the provided format pattern.
   *
   * @param value the ISO8601 date/time value as a string to format
   * @param format the desired output format specified as a date/time pattern
   * @return the formatted date/time string, or null if the parsing or formatting fails
   */
  private static String applyFormat(String value, String format) {
    try {
      // Parse ISO8601 string into the best matching temporal object
      TemporalAccessor temporal = ISO_PARSER.parseBest(value,
          ZonedDateTime::from,
          OffsetDateTime::from,
          LocalDateTime::from,
          LocalDate::from);

      // Reformat to the requested pattern
      return DateTimeFormatter.ofPattern(format).format(temporal);
    } catch (Exception ex) {
      LOGGER.warn("Failed to format date value '{}' with pattern '{}': {}", value, format, ex.getMessage());
      return value;
    }
  }

  private static String getValue(Document doc, String xpath, @Nullable String format) throws XPathExpressionException {
    switch (xpath) {
      case "current-date()":
        LocalDate date = LocalDate.now();
        return format == null ? date.toString() : date.format(DateTimeFormatter.ofPattern(format));
      case "current-time()":
        LocalTime time = LocalTime.now();
        return format == null ? time.toString() : time.format(DateTimeFormatter.ofPattern(format));
      case "current-dateTime()":
        LocalDateTime dateTime = LocalDateTime.now();
        return format == null ? dateTime.toString() : dateTime.format(DateTimeFormatter.ofPattern(format));
      default:
        String value = Utils.getElementValue(doc, xpath);
        if (format != null) {
          value = applyFormat(value, format);
        }
        return value;
    }
  }
}
