package calendarapp.exceptions;

public class ExceptionHandler {
  public static void handle(Exception e) {
    // Centralized exception handling: you can log, reformat, or take other actions here.
    System.err.println("Error: " + e.getMessage());
  }
}
