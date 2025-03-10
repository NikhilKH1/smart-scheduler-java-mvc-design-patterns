package calendarapp.controller;

import calendarapp.model.CalendarModel;
import calendarapp.model.commands.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandParser {


  private CalendarModel model;
  public CommandParser(CalendarModel model) {
    this.model = model;
  }

  public Command parse(String command) {
    List<String> tokens = tokenize(command);
    if (tokens == null || tokens.isEmpty()) {
      return null;
    }
    String mainCommand = tokens.get(0).toLowerCase();
    if ("create".equals(mainCommand)) {
      return parseCreateEvent(tokens);
    } else if ("print".equals(mainCommand)) {
      return parsePrintCommand(tokens);
    } else if ("show".equals(mainCommand)) {
      return parseShowCommand(tokens);
    } else if ("edit".equals(mainCommand)) {
      return parseEditCommand(tokens);
    } else if ("export".equals(mainCommand)) {
      return parseExportCommand(tokens);
    }
    throw new IllegalArgumentException("Unknown command: " + mainCommand);
  }

  private Command parseExportCommand(List<String> tokens) {
    if (tokens.size() != 3) {
      throw new IllegalArgumentException("Invalid export command format. Expected: export cal fileName.csv");
    }
    if (!"cal".equalsIgnoreCase(tokens.get(1))) {
      throw new IllegalArgumentException("Invalid export command. Expected 'cal' after export.");
    }
    String fileName = tokens.get(2);
    if (!fileName.toLowerCase().endsWith(".csv")) {
      throw new IllegalArgumentException("Invalid file name. Must be a CSV file ending with .csv");
    }
    return new ExportCalendarCommand(model, fileName);
  }


  private Command parsePrintCommand(List<String> tokens) {
    if (tokens.size() < 2)
      throw new IllegalArgumentException("Incomplete print command");
    String secondToken = tokens.get(1).toLowerCase();
    if (!"events".equals(secondToken))
      throw new IllegalArgumentException("Expected 'events' after print");
    if (tokens.size() < 3)
      throw new IllegalArgumentException("Incomplete print events command");
    String thirdToken = tokens.get(2).toLowerCase();
    if ("on".equals(thirdToken)) {
      if (tokens.size() < 4)
        throw new IllegalArgumentException("Expected date after 'on'");
      try {
        LocalDate date = LocalDate.parse(tokens.get(3));
        return new QueryByDateCommand(date);
      } catch (DateTimeParseException e) {
        throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD");
      }
    } else if ("from".equals(thirdToken)) {
      if (tokens.size() < 6)
        throw new IllegalArgumentException("Incomplete print events range command");
      try {
        LocalDateTime start;
        LocalDateTime end;
        try {
          start = LocalDateTime.parse(tokens.get(3));
        } catch (DateTimeParseException e) {
          start = LocalDate.parse(tokens.get(3)).atStartOfDay();
        }
        if (!tokens.get(4).equalsIgnoreCase("to"))
          throw new IllegalArgumentException("Expected 'to' after start date/time");
        try {
          end = LocalDateTime.parse(tokens.get(5));
        } catch (DateTimeParseException e) {
          end = LocalDate.parse(tokens.get(5)).atTime(23, 59, 59);
        }
        return new QueryRangeDateTimeCommand(start, end);
      } catch (DateTimeParseException e) {
        throw new IllegalArgumentException("Invalid date/time format. Use YYYY-MM-DD or YYYY-MM-DDTHH:MM");
      }
    } else {
      throw new IllegalArgumentException("Expected 'on' or 'from' after 'print events'");
    }
  }

  private Command parseShowCommand(List<String> tokens) {
    if (tokens.size() < 4 || !tokens.get(1).equalsIgnoreCase("status") || !tokens.get(2).equalsIgnoreCase("on"))
      throw new IllegalArgumentException("Show status on <dateTime>");
    try {
      LocalDateTime queryTime = LocalDateTime.parse(tokens.get(3));
      return new BusyQueryCommand(queryTime);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date/time format. Use ISO, e.g., 2025-03-08T10:00");
    }
  }

  private Command parseEditCommand(List<String> tokens) {
    if (tokens.size() < 4)
      throw new IllegalArgumentException("Incomplete edit command");
    String type = tokens.get(1).toLowerCase();
    if ("events".equals(type)) {
      String property = tokens.get(2).toLowerCase();
      String eventName = stripQuotes(tokens.get(3));
      if (tokens.size() != 5) {
        throw new IllegalArgumentException("Invalid edit events command format for recurring properties");
      }
      String newValue = stripQuotes(tokens.get(4));
      return new EditRecurringEventCommand(property, eventName, newValue);
    } else if ("event".equals(type)) {
      if (tokens.size() < 10)
        throw new IllegalArgumentException("Incomplete edit event command");
      String property = tokens.get(2).toLowerCase();
      String eventName = stripQuotes(tokens.get(3));
      if (!tokens.get(4).equalsIgnoreCase("from"))
        throw new IllegalArgumentException("Expected 'from' after event name");
      LocalDateTime start;
      try {
        start = LocalDateTime.parse(tokens.get(5));
      } catch (DateTimeParseException e) {
        throw new IllegalArgumentException("Invalid start date/time format");
      }
      if (!tokens.get(6).equalsIgnoreCase("to"))
        throw new IllegalArgumentException("Expected 'to' after start date/time");
      LocalDateTime end;
      try {
        end = LocalDateTime.parse(tokens.get(7));
      } catch (DateTimeParseException e) {
        throw new IllegalArgumentException("Invalid end date/time format");
      }
      if (!tokens.get(8).equalsIgnoreCase("with"))
        throw new IllegalArgumentException("Expected 'with' after end date/time");
      String newValue = stripQuotes(tokens.get(9));
      return new EditEventCommand(property, eventName, start, end, newValue);
    }
    throw new IllegalArgumentException("Unsupported edit command type");
  }

  private Command parseCreateEvent(List<String> tokens) {
    int index = 1;
    if (index >= tokens.size() || !tokens.get(index).equalsIgnoreCase("event"))
      throw new IllegalArgumentException("Expected 'event' after create");
    index++;
    boolean autoDecline = false;
    if (index < tokens.size() && tokens.get(index).equalsIgnoreCase("--autodecline")) {
      autoDecline = true;
      index++;
    }
    if (index >= tokens.size())
      throw new IllegalArgumentException("Missing event name");
    String eventName = stripQuotes(tokens.get(index++));
    if (index >= tokens.size())
      throw new IllegalArgumentException("Expected 'from' or 'on' after event name");
    LocalDateTime startDateTime;
    LocalDateTime endDateTime;
    boolean isAllDay;
    String dateKeyword = tokens.get(index).toLowerCase();
    if (tokens.get(index).equalsIgnoreCase("from")) {
      index++;
      try {
        startDateTime = LocalDateTime.parse(tokens.get(index++));
      } catch (DateTimeParseException e) {
        throw new IllegalArgumentException("Invalid start date/time format");
      }
      if (index < tokens.size() && tokens.get(index).equalsIgnoreCase("to")) {
        index++;
        try {
          endDateTime = LocalDateTime.parse(tokens.get(index++));
          if (endDateTime.isBefore(startDateTime))
            throw new IllegalArgumentException("End date must be after start date");
          isAllDay = false;
        } catch (DateTimeParseException e) {
          throw new IllegalArgumentException("Invalid end date/time format");
        }
      } else {
        endDateTime = startDateTime.toLocalDate().atTime(23, 59, 59);
        isAllDay = true;
      }
    } else if (dateKeyword.equalsIgnoreCase("on")) {
      index++;
      try {
        startDateTime = LocalDateTime.parse(tokens.get(index++));
      } catch (DateTimeParseException e) {
        throw new IllegalArgumentException("Invalid date/time format for all-day event");
      }
      endDateTime = startDateTime.toLocalDate().atTime(23, 59, 59);
      isAllDay = true;
    } else {
      throw new IllegalArgumentException("Expected 'from' or 'on' after event name");
    }
    boolean isRecurring = false;
    String weekdays = "";
    int repeatCount = 0;
    LocalDateTime repeatUntil = null;
    if (index < tokens.size() && tokens.get(index).equalsIgnoreCase("repeats")) {
      isRecurring = true;
      index++;
      if (index >= tokens.size())
        throw new IllegalArgumentException("Missing weekdays after 'repeats'");
      weekdays = tokens.get(index++).toUpperCase();
      if (index < tokens.size() && tokens.get(index).equalsIgnoreCase("for")) {
        index++;
        repeatCount = Integer.parseInt(tokens.get(index++));
        if (repeatCount <= 0)
          throw new IllegalArgumentException("Repeat count must be a positive number");
        if (index >= tokens.size() || !tokens.get(index++).equalsIgnoreCase("times"))
          throw new IllegalArgumentException("Expected 'times' after repeat count");
      } else if (index < tokens.size() && tokens.get(index).equalsIgnoreCase("until")) {
        index++;
        repeatUntil = LocalDateTime.parse(tokens.get(index++));
      } else {
        throw new IllegalArgumentException("Expected 'for' or 'until' after weekdays");
      }
    }
    // New check: Recurring event must span within 24 hours.
    if (isRecurring && endDateTime.isAfter(startDateTime.plusHours(24))) {
      throw new IllegalArgumentException("Recurring event must end within 24 hours of the start time.");
    }
    String description = "";
    String location = "";
    boolean isPublic = true;
    while (index < tokens.size()) {
      String token = tokens.get(index++).toLowerCase();
      switch (token) {
        case "description":
          if (index >= tokens.size())
            throw new IllegalArgumentException("Missing description");
          description = stripQuotes(tokens.get(index++));
          break;
        case "location":
          if (index >= tokens.size())
            throw new IllegalArgumentException("Missing location");
          location = stripQuotes(tokens.get(index++));
          break;
        case "private":
          isPublic = false;
          break;
        case "public":
          isPublic = true;
          break;
        default:
          throw new IllegalArgumentException("Unknown token: " + token);
      }
    }
    return new CreateEventCommand(
            eventName,
            startDateTime,
            endDateTime,
            autoDecline,
            description,
            location,
            isPublic,
            isAllDay,
            isRecurring,
            weekdays,
            repeatCount,
            repeatUntil
    );
  }

  private String stripQuotes(String token) {
    if (token.startsWith("\"") && token.endsWith("\"") && token.length() > 1) {
      return token.substring(1, token.length() - 1);
    }
    return token;
  }

  public static List<String> tokenize(String command) {
    List<String> tokens = new ArrayList<>();
    Pattern pattern = Pattern.compile("\"[^\"]+\"|\\S+");
    Matcher matcher = pattern.matcher(command);
    while (matcher.find()) {
      tokens.add(matcher.group());
    }
    return tokens;
  }
}
