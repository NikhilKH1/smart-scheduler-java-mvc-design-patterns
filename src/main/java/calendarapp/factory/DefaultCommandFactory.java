package calendarapp.factory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class DefaultCommandFactory implements ICommandFactory {

  @Override
  public String buildCreateEventCommand(String name,
                                        LocalDate date,
                                        LocalTime start,
                                        LocalTime end,
                                        boolean isRecurring,
                                        Set<DayOfWeek> repeatingDays,
                                        Integer repeatCount,
                                        LocalDate repeatUntil,
                                        String description,
                                        String location,
                                        ZoneId zoneId) {
    StringBuilder command = new StringBuilder();
    command.append("create event \"").append(name).append("\" ");

    ZonedDateTime startDateTime = date.atTime(start).atZone(zoneId);
    command.append("from ").append(startDateTime.toLocalDateTime()).append(" ");

    if (isRecurring) {
      StringBuilder days = new StringBuilder();
      for (DayOfWeek day : repeatingDays) {
        switch (day) {
          case MONDAY: days.append("M"); break;
          case TUESDAY: days.append("T"); break;
          case WEDNESDAY: days.append("W"); break;
          case THURSDAY: days.append("R"); break;
          case FRIDAY: days.append("F"); break;
          case SATURDAY: days.append("S"); break;
          case SUNDAY: days.append("U"); break;
        }
      }
      command.append("repeats ").append(days).append(" ");

      if (repeatCount != null) {
        command.append("for ").append(repeatCount).append(" times ");
      } else if (repeatUntil != null) {
        ZonedDateTime until = repeatUntil.atTime(start).atZone(zoneId);
        command.append("until ").append(until.toLocalDateTime()).append(" ");
      }
    } else {
      ZonedDateTime endDateTime = date.atTime(end).atZone(zoneId);
      command.append("to ").append(endDateTime.toLocalDateTime()).append(" ");
    }

    if (description != null && !description.trim().isEmpty()) {
      command.append("description \"").append(description.trim()).append("\" ");
    }
    if (location != null && !location.trim().isEmpty()) {
      command.append("location \"").append(location.trim()).append("\" ");
    }

    return command.toString().trim();
  }

  @Override
  public String createEventCommand(EventInput input) {
    return buildCreateEventCommand(
            input.getSubject(),
            input.getStart().toLocalDate(),
            input.getStart().toLocalTime(),
            input.getEnd().toLocalTime(),
            input.getRepeatingDays() != null,
            input.getRepeatingDays(),
            input.getRepeatTimes(),
            input.getRepeatUntil() != null ? input.getRepeatUntil().toLocalDate() : null,
            input.getDescription(),
            input.getLocation(),
            input.getStart().getZone()
    );
  }

  @Override
  public String createCalendarCommand(String name, ZoneId zoneId) {
    return String.format("create calendar --name \"%s\" --timezone %s", name, zoneId.getId());
  }

  @Override
  public String useCalendarCommand(String name) {
    return String.format("use calendar --name \"%s\"", name);
  }

  @Override
  public String createEditCommand(EditInput input) {
    String newValueEscaped = "\"" + input.getNewValue().trim() + "\"";
    String commandType = input.isRecurring() ? "edit events" : "edit event";

    // Properties that do NOT require from/to range
    if (input.isRecurring()) {
      switch (input.getProperty()) {
        case "repeatuntil":
        case "repeattimes":
        case "repeatingdays":
          return String.format("%s %s \"%s\" %s",
                  commandType,
                  input.getProperty(),
                  input.getEventName(),
                  input.getProperty().equals("repeatingdays") ? input.getNewValue().trim() : newValueEscaped);
      }
    }

    // Default format: edit event[s] <prop> "<name>" from ... to ... with "..."
    String startStr = input.getFromStart().toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    String endStr = input.getFromEnd().toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

    return String.format("%s %s \"%s\" from %s to %s with %s",
            commandType,
            input.getProperty(),
            input.getEventName(),
            startStr,
            endStr,
            newValueEscaped);
  }

  @Override
  public String exportCalendarCommand(String filePath) {
    return String.format("export cal %s", filePath);
  }


}
