package calendarapp.view;

import calendarapp.model.event.CalendarEvent;
import java.util.List;

public interface ICalendarView {
  void displayEvents(List<CalendarEvent> events);
  void displayMessage(String message);
  void displayError(String errorMessage);
}
