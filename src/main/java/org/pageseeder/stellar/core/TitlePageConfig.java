package org.pageseeder.stellar.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration class for managing title page items in PDF generation.
 * Holds a collection of {@link TitlePageItem} objects that define the content
 * and structure of a document's title page through XPath expressions.
 *
 * @since 0.6.2
 */
public class TitlePageConfig {

  private final List<TitlePageItem> items = new ArrayList<>();

  /**
   * Creates a new empty title page configuration.
   */
  public TitlePageConfig() {}

  /**
   * Creates and adds a new title page item with the specified name and XPath expression.
   *
   * @param name  the name/label for the title page item
   * @param xpath the XPath expression to extract the content
   */
  public void addItem(String name, String xpath) {
    TitlePageItem item = new TitlePageItem();
    item.setName(name);
    item.setXpath(xpath);
    this.items.add(item);
  }

  /**
   * Adds an existing {@link TitlePageItem} to this configuration.
   *
   * @param item the title page item to add
   */
  public void addItem(TitlePageItem item) {
    this.items.add(item);
  }

  /**
   * Returns the list of configured title page items.
   *
   * @return the list of title page items
   */
  public List<TitlePageItem> getItems() {
    return this.items;
  }

  /**
   * Checks if this configuration has any title page items.
   *
   * @return true if no items have been added, false otherwise
   */
  public boolean isEmpty() {
    return this.items.isEmpty();
  }

}
