package org.pageseeder.stellar.core;

import java.util.ArrayList;
import java.util.List;

public class TitlePageConfig {

  private final List<TitlePageItem> items = new ArrayList<>();

  public void addItem(String name, String xpath) {
    TitlePageItem item = new TitlePageItem();
    item.setName(name);
    item.setXpath(xpath);
    this.items.add(item);
  }

  public void addItem(TitlePageItem item) {
    this.items.add(item);
  }

  public List<TitlePageItem> getItems() {
    return this.items;
  }
}
