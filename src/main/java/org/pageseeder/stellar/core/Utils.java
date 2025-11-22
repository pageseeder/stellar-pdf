package org.pageseeder.stellar.core;

import com.lowagie.text.pdf.PdfDate;
import org.eclipse.jdt.annotation.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class providing common helper functions for string normalization,
 * date formatting, and XML element manipulation.
 *
 * <p>This class is not intended to be instantiated, and all methods are static.
 *
 * @author Christophe Lauret
 *
 * @since 0.5.0
 * @version 0.7.0
 */
public final class Utils {

  private Utils() {}

  /**
   * Normalizes the input string by trimming leading and trailing whitespace
   * and replacing sequences of whitespace characters (spaces, tabs, new lines, etc.)
   * with a single space.
   *
   * @param text the input string to normalize; must not be null
   * @return a new string that is trimmed and has condensed whitespace,
   *         or an empty string if the input is an empty or whitespace-only string
   */
  public static String normalizeSpace(String text) {
    return text.trim().replaceAll("\\s+", " ");
  }

  /**
   * Converts an ISO8601 date-time string to a {@link PdfDate} instance.
   *
   * @param isoDate the ISO8601 date-time string to convert; must not be null or empty
   * @return a {@link PdfDate} object representing the input date-time in PDF date format
   * @throws IllegalArgumentException if the input string is null, empty, or not a valid ISO8601 date-time
   */
  public static PdfDate toPdfDate(String isoDate) {
    return new PdfDate(toPdfDateFormat(isoDate));
  }

  /**
   * Converts an ISO8601 date-time string to PDF date format (D:YYYYMMDDhhmmss+hh'mm').
   *
   * @param isoDate ISO8601 string, e.g., "2023-08-02T13:45:00+10:00"
   * @return PDF date string, e.g., "D:20230802134500+10'00'"
   * @throws IllegalArgumentException if input is invalid or null
   */
  public static String toPdfDateFormat(@Nullable String isoDate) {
    if (isoDate == null || isoDate.isEmpty()) {
      throw new IllegalArgumentException("Input date must not be null or empty");
    }
    OffsetDateTime odt = OffsetDateTime.parse(isoDate);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'D:'yyyyMMddHHmmssXXXXX");
    // Format as D:YYYYMMDDhhmmss+hh:mm or D:YYYYMMDDhhmmss-hh:mm or Z
    String formatted = odt.format(formatter);
    // PDF expects offset as +hh'mm' not +hh:mm
    if (formatted.endsWith("Z")) {
      // UTC time (spec allows D:YYYYMMDDhhmmssZ, but most readers expect offset form)
      return formatted.replace("Z", "+00'00'");
    }
    // Convert last colon in timezone to single quote
    int len = formatted.length();
    String tz = formatted.substring(len - 6);
    String tzPDF = tz.substring(0, 3) + "'" + tz.substring(4, 6) + "'";
    return formatted.substring(0, len - 6) + tzPDF;
  }

  /**
   * Get an integer attribute from an element.
   *
   * @param element the element to get the attribute from
   * @param name the name of the attribute to get
   * @param defaultValue the default value to return if the attribute is not present or cannot be parsed
   *
   * @return the attribute value or the default value if not present
   */
  public static int getIntAttribute(Element element, String name, int defaultValue) {
    try {
      return Integer.parseInt(element.getAttribute(name));
    } catch (NumberFormatException ex) {
      return defaultValue;
    }
  }

  /**
   * Find the first child element with the given name.
   *
   * @param parent the parent element
   * @param targetName the name of the child element to find
   *
   * @return the child element or <code>null</code> if not found
   */
  public static @Nullable Element findFirstChild(Element parent, String targetName) {
    NodeList children = parent.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node n = children.item(i);
      if (n.getNodeType() == Node.ELEMENT_NODE && n.getNodeName().equals(targetName)) {
        return (Element)n;
      }
    }
    return null;
  }

  public static String getElementValue(Document doc, String expression) throws XPathExpressionException {
    XPath xpath = XPathFactory.newInstance().newXPath();
    return xpath.evaluate(expression, doc);
  }

  public static NodeList getNodes(Document doc, String expression) throws XPathExpressionException {
    XPath xpath = XPathFactory.newInstance().newXPath();
    return (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);
  }

  public static void writeDocumentToXML(Document doc, File file)
      throws TransformerException, TransformerFactoryConfigurationError {
    TransformerFactory factory = TransformerFactory.newInstance();
    Transformer transformer = factory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    DOMSource source = new DOMSource(doc);
    StreamResult result = new StreamResult(file);
    transformer.transform(source, result);
  }
}
