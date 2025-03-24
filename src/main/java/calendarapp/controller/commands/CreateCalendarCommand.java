package calendarapp.controller.commands;

import calendarapp.model.ICalendarManager;
import calendarapp.view.ICalendarView;

import java.time.ZoneId;

public class CreateCalendarCommand implements ICalendarManagerCommand {
  private final String calendarName;
  private final ZoneId timezone;

  public CreateCalendarCommand(String calendarName, ZoneId timezone) {
    if (calendarName == null || calendarName.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be empty.");
    }
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null.");
    }
    this.calendarName = calendarName;
    this.timezone = timezone;
  }

  public String getCalendarName()
  {
    return calendarName;
  }
  public ZoneId getTimezone() {
    return timezone;
  }

  @Override
  public boolean execute(ICalendarManager calendarManager, ICalendarView view) {
    boolean success = calendarManager.addCalendar(calendarName, timezone);
    if (success) {
      view.displayMessage("Calendar created: " + calendarName + " (" + timezone + ")");
    } else {
      view.displayError("Calendar creation failed: Duplicate name " + calendarName);
    }
    return success;
  }
}
