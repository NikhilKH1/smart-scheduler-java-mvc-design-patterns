package calendarapp.factory;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * The DefaultCommandFactory class is responsible for generating the command strings
 * for various calendar operations, such as creating, editing, and copying events.
 * It constructs commands for both single and recurring events, calendar manipulation,
 * and event status management, based on the provided inputs.
 */
public class DefaultCommandFactory implements ICommandFactory {

  /**
   * Creates a command to create a new calendar with a specified name and timezone.
   *
   * @param name   the name of the calendar
   * @param zoneId the timezone of the calendar
   * @return a command string to create the calendar
   */
  @Override
  public String createCalendarCommand(String name, ZoneId zoneId) {
    return String.format("create calendar --name \"%s\" --timezone %s", name, zoneId.getId());
  }

  /**
   * Creates a command to use an existing calendar by its name.
   *
   * @param name the name of the calendar to use
   * @return a command string to use the specified calendar
   */
  @Override
  public String useCalendarCommand(String name) {
    return String.format("use calendar --name \"%s\"", name);
  }

  /**
   * Creates a command to export a calendar to a specified file.
   *
   * @param filePath the path of the file to export the calendar to
   * @return a command string to export the calendar to the file
   */
  @Override
  public String exportCalendarCommand(String filePath) {
    return "export cal \"" + filePath + "\"";
  }

  /**
   * Creates a command to create a new event. If the event is recurring, it generates
   * a recurring event command; otherwise, it generates a single event command.
   *
   * @param input an EventInput object containing the event details
   * @return a command string to create the event
   */
  @Override
  public String createEventCommand(EventInput input) {
    if (input.getRepeatingDays() != null && !input.getRepeatingDays().isEmpty()) {
      return createRecurringEventCommand(input);
    } else {
      return createSingleEventCommand(input);
    }
  }

  /**
   * Creates a command to create a single event.
   *
   * @param input an EventInput object containing the event details
   * @return a command string to create the single event
   */
  private String createSingleEventCommand(EventInput input) {
    ZonedDateTime start = input.getStart();
    ZonedDateTime end = input.getEnd();
    StringBuilder cmd = new StringBuilder();

    cmd.append(String.format("create event \"%s\" from %s to %s", input.getSubject(),
            start.toLocalDateTime(), end.toLocalDateTime()));

    if (input.getDescription() != null && !input.getDescription().isBlank()) {
      cmd.append(" description \"").append(input.getDescription().trim()).append("\"");
    }
    if (input.getLocation() != null && !input.getLocation().isBlank()) {
      cmd.append(" location \"").append(input.getLocation().trim()).append("\"");
    }

    return cmd.toString().trim();
  }

  /**
   * Creates a command to create a recurring event.
   *
   * @param input an EventInput object containing the event details
   * @return a command string to create the recurring event
   */
  private String createRecurringEventCommand(EventInput input) {
    StringBuilder cmd = new StringBuilder();
    ZonedDateTime start = input.getStart();
    ZonedDateTime end = input.getEnd();

    cmd.append(String.format("create event \"%s\" from %s to %s repeats ",
            input.getSubject(), start.toLocalDateTime(), end.toLocalDateTime()));

    String repeatingDaysStr = input.getRepeatingDays();
    if (repeatingDaysStr != null && !repeatingDaysStr.isEmpty()) {
      cmd.append(repeatingDaysStr);
    } else {
      cmd.append("MTWRFSU");
    }

    if (input.getRepeatTimes() != null) {
      cmd.append(" for ").append(input.getRepeatTimes()).append(" times");
    } else if (input.getRepeatUntil() != null) {
      cmd.append(" until ").append(input.getRepeatUntil().toLocalDateTime());
    }

    if (input.getDescription() != null && !input.getDescription().isBlank()) {
      cmd.append(" description \"").append(input.getDescription().trim()).append("\"");
    }
    if (input.getLocation() != null && !input.getLocation().isBlank()) {
      cmd.append(" location \"").append(input.getLocation().trim()).append("\"");
    }

    return cmd.toString().trim();
  }

  /**
   * Creates a command to edit an event's properties.
   *
   * @param input an EditInput object containing the event edit details
   * @return a command string to edit the event
   */
  @Override
  public String createEditCommand(EditInput input) {
    String commandType = input.isRecurring() ? "edit events" : "edit event";
    String property = input.getProperty();
    String name = input.getEventName();
    String newValue = "\"" + input.getNewValue().trim() + "\"";

    if (input.isRecurring()) {
      if (property.equalsIgnoreCase("repeatingdays")) {
        return String.format("%s %s \"%s\" %s", commandType, property, name,
                input.getNewValue().trim());
      }
      if (property.equalsIgnoreCase("repeatuntil") ||
              property.equalsIgnoreCase("repeattimes")) {
        return String.format("%s %s \"%s\" %s", commandType, property, name, newValue);
      }
      return String.format("%s %s \"%s\" from %s with %s",
              commandType, property, name, input.getFromStart().toLocalDateTime(), newValue);
    }

    return String.format("%s %s \"%s\" from %s to %s with %s",
            commandType, property, name, input.getFromStart().toLocalDateTime(),
            input.getFromEnd().toLocalDateTime(), newValue);
  }

  /**
   * Creates a command to print all events between a specified time range.
   *
   * @param start the start time of the range
   * @param end   the end time of the range
   * @return a command string to print events between the times
   */
  @Override
  public String printEventsBetweenCommand(ZonedDateTime start, ZonedDateTime end) {
    return String.format("print events from %s to %s",
            start.toLocalDateTime(),
            end.toLocalDateTime());
  }

  /**
   * Creates a command to edit a recurring event's properties.
   *
   * @param input an EditInput object containing the event edit details
   * @return a command string to edit the recurring event
   */
  @Override
  public String createEditRecurringEventCommand(EditInput input) {
    String property = input.getProperty();
    String name = input.getEventName();
    String newValue = input.getNewValue().trim();

    switch (property.toLowerCase()) {
      case "repeatingdays":
        return String.format("edit events repeatingdays \"%s\" %s", name, newValue);

      case "repeattimes":
        return String.format("edit events repeattimes \"%s\" \"%s\"", name, newValue);

      case "repeatuntil":
        return String.format("edit events repeatuntil \"%s\" \"%s\"", name, newValue);

      case "name":
      case "description":
      case "location":
        return String.format("edit events %s \"%s\" from %s with \"%s\"",
                property, name, input.getFromStart().toLocalDateTime(), newValue);

      case "startdatetime":
      case "enddatetime":
        return String.format("edit events %s \"%s\" from %s with %s",
                property, name, input.getFromStart().toLocalDateTime(), newValue);

      default:
        throw new IllegalArgumentException("Unsupported recurring property: " + property);
    }
  }

  @Override
  public String importCalendarCommand(String filePath) {
    return "import cal \"" + filePath + "\"";
  }

  @Override
  public String editCalendarTimezoneCommand(String name, ZoneId newZone) {
    return "edit calendar --name \"" + name + "\" --property timezone " + newZone;
  }



}
