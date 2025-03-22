package calendarapp.controller;

import calendarapp.model.CalendarManager;
import calendarapp.model.CalendarModel;
import calendarapp.controller.commands.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandParser {

  private final CalendarManager calendarManager;
  private final Map<String, Function<List<String>, Command>> parsers;

  public CommandParser(CalendarManager calendarManager) {
    this.calendarManager = calendarManager;
    parsers = new HashMap<>();
    parsers.put("create", this::parseCreateCommand);
    parsers.put("print", this::parsePrintCommand);
    parsers.put("show", this::parseShowCommand);
    parsers.put("edit", this::parseEditCommand);
    parsers.put("export", this::parseExportCommand);
    parsers.put("use", this::parseUseCommand);
  }

  public Command parse(String command) {
    if (command == null || command.trim().isEmpty()) {
      throw new IllegalArgumentException("Command cannot be null or empty");
    }
    List<String> tokens = tokenize(command);
    String mainCommand = tokens.get(0).toLowerCase();
    Function<List<String>, Command> parserFunc = parsers.get(mainCommand);
    if (parserFunc != null) {
      return parserFunc.apply(tokens);
    }
    throw new IllegalArgumentException("Unknown command: " + mainCommand);
  }
  private Command parseCreateCommand(List<String> tokens) {
    if (tokens.size() >= 2 && "calendar".equalsIgnoreCase(tokens.get(1))) {
      return parseCreateCalendarCommand(tokens);
    }
    return parseCreateEvent(tokens);
  }

  private Command parseEditCommand(List<String> tokens) {
    if (tokens.size() >= 2 && "calendar".equalsIgnoreCase(tokens.get(1))) {
      return parseEditCalendarCommand(tokens);
    }
    return parseEditEvent(tokens);
  }

  private Command parseUseCommand(List<String> tokens) {
    if (tokens.size() >= 2 && "calendar".equalsIgnoreCase(tokens.get(1))) {
      return parseUseCalendarCommand(tokens);
    }
    throw new IllegalArgumentException("Invalid use command format.");
  }

  private Command parseCreateCalendarCommand(List<String> tokens) {
    String name = null, timezoneStr = null;
    for (int i = 2; i < tokens.size() - 1; i++) {
      if ("--name".equalsIgnoreCase(tokens.get(i))) {
        name = tokens.get(++i);
      } else if ("--timezone".equalsIgnoreCase(tokens.get(i))) {
        timezoneStr = tokens.get(++i);
      }
    }
    if (name == null || timezoneStr == null) {
      throw new IllegalArgumentException("Usage: create calendar --name <name> --timezone <timezone>");
    }
    try {
      ZoneId timezone = ZoneId.of(timezoneStr);
      return new CreateCalendarCommand(name, timezone);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid timezone: " + timezoneStr);
    }
  }

  private Command parseEditCalendarCommand(List<String> tokens) {
    String name = null, property = null, newValue = null;
    for (int i = 2; i < tokens.size() - 1; i++) {
      if ("--name".equalsIgnoreCase(tokens.get(i))) {
        name = tokens.get(++i);
      } else if ("--property".equalsIgnoreCase(tokens.get(i))) {
        property = tokens.get(++i);
        if (i + 1 < tokens.size()) {
          newValue = tokens.get(++i);
        }
      }
    }
    if (name == null || property == null || newValue == null) {
      throw new IllegalArgumentException("Usage: edit calendar --name <name> --property <property> <new-value>");
    }
    return new EditCalendarCommand(name, property, newValue);
  }

  private Command parseUseCalendarCommand(List<String> tokens) {
    String name = null;
    for (int i = 2; i < tokens.size() - 1; i++) {
      if ("--name".equalsIgnoreCase(tokens.get(i))) {
        name = tokens.get(++i);
      }
    }
    if (name == null) {
      throw new IllegalArgumentException("Usage: use calendar --name <name>");
    }
    return new UseCalendarCommand(name);
  }


  private Command parseExportCommand(List<String> tokens) {
    if (tokens.size() != 3 || !"cal".equalsIgnoreCase(tokens.get(1))) {
      throw new IllegalArgumentException("Invalid export format. Usage: export cal <file.csv>");
    }
    String fileName = tokens.get(2);
    if (!fileName.toLowerCase().endsWith(".csv")) {
      throw new IllegalArgumentException("Exported file must have a .csv extension");
    }
    if (calendarManager.getActiveCalendar() == null) {
      throw new IllegalArgumentException("No active calendar selected. Use 'use calendar --name <calName>' first.");
    }
    return new ExportCalendarCommand(calendarManager.getActiveCalendar(), fileName);
  }


  private Command parsePrintCommand(List<String> tokens) {
    if (tokens.size() < 3 || !"events".equalsIgnoreCase(tokens.get(1))) {
      throw new IllegalArgumentException("Invalid print command format");
    }
    String mode = tokens.get(2).toLowerCase();
    if ("on".equals(mode)) {
      return parsePrintOnCommand(tokens);
    } else if ("from".equals(mode)) {
      return parsePrintRangeCommand(tokens);
    } else {
      throw new IllegalArgumentException("Invalid print command. Use 'on' or 'from'");
    }
  }

  private Command parsePrintOnCommand(List<String> tokens) {
    if (tokens.size() < 4) {
      throw new IllegalArgumentException("Expected date after 'on'");
    }
    try {
      LocalDate date = LocalDate.parse(tokens.get(3));
      return new QueryByDateCommand(date);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD");
    }
  }

  private Command parsePrintRangeCommand(List<String> tokens) {
    if (tokens.size() < 6 || !"to".equalsIgnoreCase(tokens.get(4))) {
      throw new IllegalArgumentException("Expected format: print events from <start> to <end>");
    }
    LocalDateTime start = parseDateTime(tokens.get(3));
    LocalDateTime end = parseDateTime(tokens.get(5));
    return new QueryRangeDateTimeCommand(start, end);
  }

  private Command parseShowCommand(List<String> tokens) {
    if (tokens.size() < 4 || !"status".equalsIgnoreCase(tokens.get(1)) || !"on".equalsIgnoreCase(tokens.get(2))) {
      throw new IllegalArgumentException("Invalid show command. Usage: show status on <datetime>");
    }
    return new BusyQueryCommand(parseDateTime(tokens.get(3)));
  }

  private Command parseEditEvent(List<String> tokens) {
    if (tokens.size() < 4) throw new IllegalArgumentException("Incomplete edit command");

    switch (tokens.get(1).toLowerCase()) {
      case "events":
        return parseEditEventsCommand(tokens);
      case "event":
        return parseEditSingleEventCommand(tokens);
      default:
        throw new IllegalArgumentException("Unsupported edit command type");
    }
  }

  private Command parseEditEventsCommand(List<String> tokens) {
    String property = tokens.get(2).toLowerCase();
    String eventName = stripQuotes(tokens.get(3));
    boolean isRecurringProperty = property.equals("repeattimes") || property.equals("repeatuntil") || property.equals("repeatingdays");

    if (tokens.size() == 5) {
      String newValue = stripQuotes(tokens.get(4));
      return isRecurringProperty
              ? new EditRecurringEventCommand(property, eventName, newValue)
              : new EditEventCommand(property, eventName, newValue);
    } else if (tokens.size() == 8) {
      if (!"from".equalsIgnoreCase(tokens.get(4)) || !"with".equalsIgnoreCase(tokens.get(6))) {
        throw new IllegalArgumentException("Invalid edit events command format");
      }
      LocalDateTime filterDateTime = parseDateTime(tokens.get(5));
      String newValue = stripQuotes(tokens.get(7));
      return isRecurringProperty
              ? new EditRecurringEventCommand(property, eventName, newValue)
              : new EditEventCommand(property, eventName, filterDateTime, newValue);
    } else {
      throw new IllegalArgumentException("Invalid edit events command format");
    }
  }

  private Command parseEditSingleEventCommand(List<String> tokens) {
    if (tokens.size() < 10 || !"from".equalsIgnoreCase(tokens.get(4)) || !"to".equalsIgnoreCase(tokens.get(6)) || !"with".equalsIgnoreCase(tokens.get(8))) {
      throw new IllegalArgumentException("Invalid edit event command format");
    }
    String property = tokens.get(2).toLowerCase();
    String eventName = stripQuotes(tokens.get(3));
    LocalDateTime start = parseDateTime(tokens.get(5));
    LocalDateTime end = parseDateTime(tokens.get(7));
    String newValue = stripQuotes(tokens.get(9));
    return new EditEventCommand(property, eventName, start, end, newValue);
  }

  private Command parseCreateEvent(List<String> tokens) {
    int index = 1;
    boolean autoDecline = true;

    if (index >= tokens.size() || !"event".equalsIgnoreCase(tokens.get(index))) {
      throw new IllegalArgumentException("Expected 'event' after create");
    }
    index++;

    if (index >= tokens.size()) {
      throw new IllegalArgumentException("Missing event name");
    }
    String eventName = stripQuotes(tokens.get(index++));

    EventTimingResult timing = parseEventTiming(tokens, index);
    index = timing.index;

    RecurringResult recurring = parseRecurringSection(tokens, index);
    index = recurring.index;
    if (recurring.isRecurring && timing.end.isAfter(timing.start.plusHours(24))) {
      throw new IllegalArgumentException("Recurring event must end within 24 hours of the start time.");
    }

    PropertiesResult props = parseProperties(tokens, index);

    return new CreateEventCommand(
            eventName,
            timing.start,
            timing.end,
            autoDecline,
            props.description,
            props.location,
            props.isPublic,
            timing.isAllDay,
            recurring.isRecurring,
            recurring.weekdays,
            recurring.repeatCount,
            recurring.repeatUntil
    );
  }

  private EventTimingResult parseEventTiming(List<String> tokens, int index) {
    EventTimingResult result = new EventTimingResult();
    String keyword = tokens.get(index).toLowerCase();
    if ("from".equals(keyword)) {
      index++;
      result.start = parseDateTime(tokens.get(index++));
      if (index < tokens.size() && "to".equalsIgnoreCase(tokens.get(index))) {
        index++;
        result.end = parseDateTime(tokens.get(index++));
        if (result.end.isBefore(result.start)) {
          throw new IllegalArgumentException("End date must be after start date");
        }
        result.isAllDay = false;
      } else {
        result.end = result.start.toLocalDate().atTime(23, 59, 59);
        result.isAllDay = true;
      }
    } else if ("on".equals(keyword)) {
      index++;
      result.start = parseDateTime(tokens.get(index++));
      result.end = result.start.toLocalDate().atTime(23, 59, 59);
      result.isAllDay = true;
    } else {
      throw new IllegalArgumentException("Expected 'from' or 'on' after event name");
    }
    result.index = index;
    return result;
  }

  private RecurringResult parseRecurringSection(List<String> tokens, int index) {
    RecurringResult result = new RecurringResult();
    if (index < tokens.size() && "repeats".equalsIgnoreCase(tokens.get(index))) {
      result.isRecurring = true;
      index++;
      if (index >= tokens.size()) {
        throw new IllegalArgumentException("Missing weekdays after 'repeats'");
      }
      result.weekdays = tokens.get(index++).toUpperCase();
      if (index < tokens.size() && "for".equalsIgnoreCase(tokens.get(index))) {
        index++;
        result.repeatCount = Integer.parseInt(tokens.get(index++));
        if (result.repeatCount <= 0) {
          throw new IllegalArgumentException("Repeat count must be positive");
        }
        if (index >= tokens.size() || !"times".equalsIgnoreCase(tokens.get(index++))) {
          throw new IllegalArgumentException("Expected 'times' after repeat count");
        }
      } else if (index < tokens.size() && "until".equalsIgnoreCase(tokens.get(index))) {
        index++;
        result.repeatUntil = parseDateTime(tokens.get(index++));
      } else {
        throw new IllegalArgumentException("Expected 'for' or 'until' after weekdays");
      }
    }
    result.index = index;
    return result;
  }

  private PropertiesResult parseProperties(List<String> tokens, int index) {
    PropertiesResult result = new PropertiesResult();
    while (index < tokens.size()) {
      String token = tokens.get(index++).toLowerCase();
      switch (token) {
        case "description":
          if (index >= tokens.size()) {
            throw new IllegalArgumentException("Missing description");
          }
          result.description = stripQuotes(tokens.get(index++));
          break;
        case "location":
          if (index >= tokens.size()) {
            throw new IllegalArgumentException("Missing location");
          }
          result.location = stripQuotes(tokens.get(index++));
          break;
        case "private":
          result.isPublic = false;
          break;
        case "public":
          result.isPublic = true;
          break;
        default:
          throw new IllegalArgumentException("Unknown token: " + token);
      }
    }
    result.index = index;
    return result;
  }

  private LocalDateTime parseDateTime(String token) {
    try {
      return LocalDateTime.parse(token);
    } catch (DateTimeParseException e) {
      return LocalDate.parse(token).atStartOfDay();
    }
  }

  private String stripQuotes(String token) {
    if (token.startsWith("\"") && token.endsWith("\"") && token.length() > 1) {
      return token.substring(1, token.length() - 1);
    }
    return token;
  }

  public static List<String> tokenize(String command) {
    List<String> tokens = new ArrayList<>();
    Matcher matcher = Pattern.compile("\"[^\"]+\"|\\S+").matcher(command);
    while (matcher.find()) {
      tokens.add(matcher.group());
    }
    return tokens;
  }
}
