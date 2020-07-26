package org.amfoss.paneeer.gallery.data.base;

public enum SortingMode {
  NAME(0),
  DATE(1),
  SIZE(2),
  NUMERIC(3);

  int value;

  SortingMode(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  public static SortingMode fromValue(int value) {
    switch (value) {
      case 0:
        return NAME;
      case 1:
      default:
        return DATE;
      case 2:
        return SIZE;
      case 3:
        return NUMERIC;
    }
  }
}
