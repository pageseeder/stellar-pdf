package org.pageseeder.stellar.core;

public class TitlePageItem {

  private String name;
  private String xpath;
  private String format;

  public void setName(String name) {
    this.name = name;
  }

  public void setXpath(String xpath) {
    this.xpath = xpath;
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public boolean hasNameAndXpath() {
    return this.name != null && !this.name.isEmpty()
        && this.xpath != null && !this.xpath.isEmpty();
  }

  public String getName() {
    return name;
  }

  public String getXpath() {
    return xpath;
  }

  public String getFormat() {
    return format;
  }

}
