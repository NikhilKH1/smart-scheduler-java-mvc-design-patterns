package calendarapp.model;

import java.util.ArrayList;
import java.util.List;

public class CalendarModel {
  private final List<CalendarEvent> events;

  public CalendarModel() {
    events = new ArrayList<>();
  }

  public boolean addEvent(CalendarEvent event, boolean autoDecline) {
    for (CalendarEvent existing : events) {
      if (ConflictChecker.hasConflict(existing, event)) {
        if (autoDecline) {
          return false;
        }
      }
    }
    events.add(event);
    return true;
  }

  public List<CalendarEvent> getEvents() {
    return new ArrayList<>(events);
  }
}
