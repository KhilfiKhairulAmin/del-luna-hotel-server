package utils;

public class ErrorHandler {
  public static String handleError(Exception e) {
    Logger.getInstance().error(e.getMessage());
    return "{\"server\": \"Invalid data format\"}";
  }
}
