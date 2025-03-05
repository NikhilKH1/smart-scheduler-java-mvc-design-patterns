package calendarapp.view;

import java.util.List;

import calendarapp.model.CalendarEvent;

public class CalendarView {
  public void displayMessage(String message) {
    System.out.println(message);
  }
  public void displayError(String error) {
    System.err.println(error);
  }

  public void displayEvents(List<CalendarEvent> events) {
    if (events.isEmpty()) {
      System.out.println("No events found.");
    } else {
      for (CalendarEvent event : events) {
        System.out.println("- " + event.getSubject() + ": " +
                event.getStartDateTime() + " to " + event.getEndDateTime());
      }
    }
  }

}
