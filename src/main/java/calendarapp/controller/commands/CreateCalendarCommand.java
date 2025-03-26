package calendarapp.controller.commands;

import calendarapp.model.ICalendarManager;
import calendarapp.view.ICalendarView;

import java.time.ZoneId;

/**
 * Command to create a new calendar with a specified name and timezone.
 */
public class CreateCalendarCommand implements ICalendarManagerCommand {
  private final String calendarName;
  private final ZoneId timezone;

  /**
   * Constructs a CreateCalendarCommand.
   *
   * @param calendarName the name of the new calendar
   * @param timezone the timezone of the new calendar
   * @throws IllegalArgumentException if the calendar name is empty or the timezone is null
   */
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

  /**
   * Returns the name of the calendar.
   *
   * @return the calendar name
   */
  public String getCalendarName() {
    return calendarName;
  }

  /**
   * Returns the timezone of the calendar.
   *
   * @return the calendar timezone
   */
  public ZoneId getTimezone() {
    return timezone;
  }

  /**
   * Creates a new calendar with the given name and timezone.
   * If the calendar is created successfully, a success message is displayed.
   * If a calendar with the same name already exists, an error message is displayed.
   *
   * @param calendarManager the calendar manager used to create the calendar
   * @param view the view used to display messages
   * @return true if the calendar was created successfully, false otherwise
   */
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
