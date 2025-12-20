package org.codequistify.master.domain.shared.net;

public enum UrlScheme {
  HTTPS("https://"),
  WSS("wss://");

  private final String value;

  UrlScheme(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
}
