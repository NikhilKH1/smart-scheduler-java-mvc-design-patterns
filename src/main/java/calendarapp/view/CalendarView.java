package calendarapp.view;

import calendarapp.model.CalendarEvent;
import java.util.List;

public class CalendarView {

  public void displayEvents(List<CalendarEvent> events) {
    if (events.isEmpty()) {
      System.out.println("No events found.");
      return;
    }
    for (CalendarEvent event : events) {
      System.out.println(event.toString());
    }
  }

  public void displayMessage(String message) {
    System.out.println(message);
  }

  public void displayError(String errorMessage) {
    System.err.println(errorMessage);
  }
}
