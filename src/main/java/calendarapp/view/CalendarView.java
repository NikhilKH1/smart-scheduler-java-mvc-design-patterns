package calendarapp.view;

import calendarapp.model.event.CalendarEvent;
import java.util.List;

public class CalendarView implements ICalendarView {
  @Override
  public void displayEvents(List<CalendarEvent> events) {
    if (events.isEmpty()) {
      System.out.println("No events found.");
      return;
    }
    for (CalendarEvent event : events) {
      System.out.println(event.toString());
    }
  }

  @Override
  public void displayMessage(String message) {
    System.out.println(message);
  }

  @Override
  public void displayError(String errorMessage) {
    System.err.println(errorMessage);
  }
}
