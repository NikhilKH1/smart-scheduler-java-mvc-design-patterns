package calendarapp.factory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.DayOfWeek;
import java.util.Set;

public class DefaultCommandFactory implements ICommandFactory {

  // ---------- CALENDAR COMMANDS ----------

  @Override
  public String createCalendarCommand(String name, ZoneId zoneId) {
    return String.format("create calendar --name \"%s\" --timezone %s", name, zoneId.getId());
  }

  @Override
  public String useCalendarCommand(String name) {
    return String.format("use calendar --name \"%s\"", name);
  }

  @Override
  public String editCalendarCommand(String name, String property, String newValue) {
    return String.format("edit calendar --name \"%s\" --property %s %s", name, property, newValue);
  }

  @Override
  public String exportCalendarCommand(String fileName) {
    return String.format("export cal %s", fileName);
  }


  // ---------- EVENT CREATION COMMANDS ----------

  @Override
  public String createEventCommand(EventInput input) {
    if (input.getRepeatingDays() != null && !input.getRepeatingDays().isEmpty()) {
      return createRecurringEventCommand(input);
    } else {
      return createSingleEventCommand(input);
    }
  }

  private String createSingleEventCommand(EventInput input) {
    ZonedDateTime start = input.getStart();
    ZonedDateTime end = input.getEnd();
    StringBuilder cmd = new StringBuilder();

    cmd.append(String.format("create event \"%s\" from %s to %s",
            input.getSubject(),
            start.toLocalDateTime(),
            end.toLocalDateTime()));

    if (input.getDescription() != null && !input.getDescription().isBlank()) {
      cmd.append(" description \"").append(input.getDescription().trim()).append("\"");
    }
    if (input.getLocation() != null && !input.getLocation().isBlank()) {
      cmd.append(" location \"").append(input.getLocation().trim()).append("\"");
    }

    return cmd.toString().trim();
  }

  private String createRecurringEventCommand(EventInput input) {
    StringBuilder cmd = new StringBuilder();
    ZonedDateTime start = input.getStart();
    ZonedDateTime end = input.getEnd();

    cmd.append(String.format("create event \"%s\" from %s to %s repeats ",
            input.getSubject(),
            start.toLocalDateTime(),
            end.toLocalDateTime()));

    String repeatingDaysStr = input.getRepeatingDays();
    if (repeatingDaysStr != null && !repeatingDaysStr.isEmpty()) {
      cmd.append(repeatingDaysStr);
    } else {
      // If no weekdays specified, assume every day
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


  private String dayToChar(DayOfWeek day) {
    switch (day) {
      case MONDAY:
        return "M";
      case TUESDAY:
        return "T";
      case WEDNESDAY:
        return "W";
      case THURSDAY:
        return "R";
      case FRIDAY:
        return "F";
      case SATURDAY:
        return "S";
      case SUNDAY:
        return "U";
      default:
        throw new IllegalArgumentException("Unknown day: " + day);
    }
  }


  // ---------- EVENT EDITING COMMANDS ----------

  @Override
  public String createEditCommand(EditInput input) {
    String commandType = input.isRecurring() ? "edit events" : "edit event";
    String property = input.getProperty();
    String name = input.getEventName();
    String newValue = "\"" + input.getNewValue().trim() + "\"";

    if (input.isRecurring()) {
      if (property.equalsIgnoreCase("repeatingdays")) {
        return String.format("%s %s \"%s\" %s", commandType, property, name, input.getNewValue().trim());
      }
      if (property.equalsIgnoreCase("repeatuntil") || property.equalsIgnoreCase("repeattimes")) {
        return String.format("%s %s \"%s\" %s", commandType, property, name, newValue);
      }
      // For time-based recurring edits
      return String.format("%s %s \"%s\" from %s with %s",
              commandType,
              property,
              name,
              input.getFromStart().toLocalDateTime(),
              newValue);
    }

    // For single event edits
    return String.format("%s %s \"%s\" from %s to %s with %s",
            commandType,
            property,
            name,
            input.getFromStart().toLocalDateTime(),
            input.getFromEnd().toLocalDateTime(),
            newValue);
  }

  // ---------- COPY EVENT COMMANDS ----------

  @Override
  public String copyEventCommand(String eventName, ZonedDateTime originalTime, String targetCalendar, ZonedDateTime targetTime) {
    return String.format("copy event \"%s\" on %s --target %s to %s",
            eventName,
            originalTime.toLocalDateTime(),
            targetCalendar,
            targetTime.toLocalDateTime());
  }

  @Override
  public String copyEventsOnDateCommand(LocalDate date, String targetCalendar, LocalDate targetDate) {
    return String.format("copy events on %s --target %s to %s",
            date,
            targetCalendar,
            targetDate);
  }

  @Override
  public String copyEventsBetweenDatesCommand(LocalDate start, LocalDate end, String targetCalendar, LocalDate targetStart) {
    return String.format("copy events between %s and %s --target %s to %s",
            start,
            end,
            targetCalendar,
            targetStart);
  }


  @Override
  public String printEventsOnCommand(LocalDate date) {
    return "print events on " + date;
  }

  @Override
  public String printEventsBetweenCommand(ZonedDateTime start, ZonedDateTime end) {
    return String.format("print events from %s to %s",
            start.toLocalDateTime(),
            end.toLocalDateTime());
  }

  @Override
  public String showStatusCommand(ZonedDateTime datetime) {
    return "show status on " + datetime.toLocalDateTime();
  }

  @Override
  public String editCalendarNameCommand(String oldName, String newName) {
    return String.format("edit calendar --name \"%s\" --property name %s", oldName, newName);
  }

  @Override
  public String editCalendarTimezoneCommand(String calendarName, ZoneId newZone) {
    return String.format("edit calendar --name \"%s\" --property timezone %s", calendarName, newZone.getId());
  }

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
                property,
                name,
                input.getFromStart().toLocalDateTime(),
                newValue);

      case "startdatetime":
      case "enddatetime":
        return String.format("edit events %s \"%s\" from %s with %s",
                property,
                name,
                input.getFromStart().toLocalDateTime(),
                newValue);

      default:
        throw new IllegalArgumentException("Unsupported recurring property: " + property);
    }
  }

  @Override
  public String buildCreateEventCommand(String name,
                                        LocalDate date,
                                        LocalTime start,
                                        LocalTime end,
                                        boolean isRecurring,
                                        String repeatingDays,
                                        Integer repeatCount,
                                        LocalDate repeatUntil,
                                        String description,
                                        String location,
                                        ZoneId zoneId) {
    StringBuilder cmd = new StringBuilder();
    ZonedDateTime startZDT = date.atTime(start).atZone(zoneId);
    ZonedDateTime endZDT = date.atTime(end).atZone(zoneId);

    if (isRecurring && repeatingDays != null && !repeatingDays.isEmpty()) {
      cmd.append(String.format("create event \"%s\" on %s repeats ", name, startZDT.toLocalDateTime()));
      // Simply append the recurring days string:
      cmd.append(repeatingDays);

      if (repeatCount != null) {
        cmd.append(" for ").append(repeatCount).append(" times");
      } else if (repeatUntil != null) {
        ZonedDateTime untilZDT = repeatUntil.atTime(start).atZone(zoneId);
        cmd.append(" until ").append(untilZDT.toLocalDateTime());
      }
    } else {
      // Single event
      cmd.append(String.format("create event \"%s\" from %s to %s",
              name,
              startZDT.toLocalDateTime(),
              endZDT.toLocalDateTime()));
    }

    if (description != null && !description.isBlank()) {
      cmd.append(" description \"").append(description.trim()).append("\"");
    }
    if (location != null && !location.isBlank()) {
      cmd.append(" location \"").append(location.trim()).append("\"");
    }

    return cmd.toString().trim();
  }
}
