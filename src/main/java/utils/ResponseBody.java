package utils;

import java.util.ArrayList;

public class ResponseBody {
  public boolean isSuccess = false;
  public ArrayList<String> properties;
  public ArrayList<String> data;

  public ResponseBody() {
    properties = new ArrayList<String>();
    data = new ArrayList<String>();
  }

  public void setProperty(String property, String datum) {
    int i = properties.indexOf(property);
    if (i != -1) {
      data.set(i, datum);
      return;
    }
    properties.add(property);
    data.add(datum);
  }

  public void clearProperties() {
    properties.clear();
    data.clear();
  }
}
