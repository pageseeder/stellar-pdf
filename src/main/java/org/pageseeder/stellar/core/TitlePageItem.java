package org.pageseeder.stellar.core;

import org.eclipse.jdt.annotation.Nullable;

import java.util.Objects;

/**
 * Represents a configurable item for the title page of a PDF document.
 * Each item consists of a name, an XPath expression to extract the content,
 * and an optional format specification.
 *
 * <p>This class is used in conjunction with {@link TitlePageConfig} to define
 * the structure and content of PDF title pages.</p>
 *
 * @since 0.6.2
 * @version 0.6.2
 */
public class TitlePageItem {

  /**
   * The name/label of the title page item
   */
  private String name = "";

  /**
   * The XPath expression used to extract content from the source document
   */
  private String xpath = "";

  /**
   * Optional format specification for the extracted content
   */
  private @Nullable String format;

  /**
   * Sets the name of this title page item.
   *
   * @param name the name to set (must not be null)
   * @throws NullPointerException if the specified name is null
   */
  public void setName(String name) {
    Objects.requireNonNull(name);
    if (!name.matches("[a-zA-Z0-9_\\-]+")) {
      throw new IllegalArgumentException("Name must be a valid block label name: " + name);
    }
    this.name = name;
  }

  /**
   * Sets the XPath expression for this title page item.
   *
   * @param xpath the XPath expression to set (must not be null)
   * @throws NullPointerException if the specified xpath is null
   */
  public void setXpath(String xpath) {
    this.xpath = Objects.requireNonNull(xpath);
  }

  /**
   * Sets the format specification for this title page item.
   *
   * @param format the format specification (may be null)
   */
  public void setFormat(String format) {
    this.format = format;
  }

  /**
   * Checks if this item has both name and XPath values specified.
   *
   * @return true if both name and xpath are non-empty strings
   */
  public boolean hasNameAndXpath() {
    return !this.name.isEmpty() && !this.xpath.isEmpty();
  }

  /**
   * Returns the name of this title page item.
   *
   * @return the name (never null)
   */
  public String getName() {
    return this.name;
  }

  /**
   * Returns the XPath expression of this title page item.
   *
   * @return the XPath expression (never null)
   */
  public String getXpath() {
    return this.xpath;
  }

  /**
   * Returns the format specification of this title page item.
   *
   * @return the format specification (may be null)
   */
  public @Nullable String getFormat() {
    return this.format;
  }

}
