package calendarapp.controller;

import calendarapp.controller.commands.*;
import calendarapp.model.ICalendarManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandParser {

  private final ICalendarManager calendarManager;
  private final Map<String, Function<List<String>, ICommand>> parsers;

  public CommandParser(ICalendarManager calendarManager) {
    this.calendarManager = calendarManager;
    parsers = new HashMap<>();
    parsers.put("create", this::parseCreateCommand);
    parsers.put("print", this::parsePrintCommand);
    parsers.put("show", this::parseShowCommand);
    parsers.put("edit", this::parseEditCommand);
    parsers.put("export", this::parseExportCommand);
    parsers.put("use", this::parseUseCommand);
    parsers.put("copy", this::parseCopyCommand);
  }

  public ICommand parse(String command) {
    if (command == null || command.trim().isEmpty()) {
      throw new IllegalArgumentException("Command cannot be null or empty");
    }
    List<String> tokens = tokenize(command);
    String mainCommand = tokens.get(0).toLowerCase();
    Function<List<String>, ICommand> parserFunc = parsers.get(mainCommand);
    if (parserFunc != null) {
      return parserFunc.apply(tokens);
    }
    throw new IllegalArgumentException("Unknown command: " + mainCommand);
  }

  private ICommand parseCopyCommand(List<String> tokens) {
    if (tokens.size() < 2) {
      throw new IllegalArgumentException("Incomplete copy command");
    }

    String type = tokens.get(1).toLowerCase();

    switch (type) {
      case "event":
        return parseCopySingleEvent(tokens);
      case "events":
        if (tokens.get(2).equalsIgnoreCase("on")) {
          return parseCopyEventsOnDate(tokens);
        } else if (tokens.get(2).equalsIgnoreCase("between")) {
          return parseCopyEventsBetween(tokens);
        } else {
          throw new IllegalArgumentException("Invalid copy events format");
        }
      default:
        throw new IllegalArgumentException("Unsupported copy command type");
    }
  }

  private ICommand parseCopySingleEvent(List<String> tokens) {
    if (tokens.size() < 9 || !"on".equalsIgnoreCase(tokens.get(3)) || !"--target".equalsIgnoreCase(tokens.get(5)) || !"to".equalsIgnoreCase(tokens.get(7))) {
      throw new IllegalArgumentException("Invalid copy event format");
    }
    String eventName = stripQuotes(tokens.get(2));
    ZonedDateTime sourceDateTime = parseDateTime(tokens.get(4));
    String targetCalendar = stripQuotes(tokens.get(6)).trim();
    ZoneId targetZone = calendarManager.getCalendar(targetCalendar).getTimezone();
    ZonedDateTime targetDateTime = parseDateTimeWithZone(tokens.get(8), targetZone);

    return new CopySingleEventCommand(eventName, sourceDateTime, targetCalendar, targetDateTime);
  }

  private ZonedDateTime parseDateTimeWithZone(String token, ZoneId zone) {
    try {
      return ZonedDateTime.of(java.time.LocalDateTime.parse(token), zone);
    } catch (DateTimeParseException e) {
      return LocalDate.parse(token).atStartOfDay(zone);
    }
  }

  private ICommand parseCopyEventsOnDate(List<String> tokens) {
    if (tokens.size() < 7 || !"--target".equalsIgnoreCase(tokens.get(4)) || !"to".equalsIgnoreCase(tokens.get(6))) {
      throw new IllegalArgumentException("Invalid copy events on format");
    }
    LocalDate sourceDate = LocalDate.parse(tokens.get(3));
    String targetCalendar = stripQuotes(tokens.get(5)).trim();
    LocalDate targetDate = LocalDate.parse(tokens.get(7));
    return new CopyEventsOnDateCommand(sourceDate, targetCalendar, targetDate);
  }

  private ICommand parseCopyEventsBetween(List<String> tokens) {
    if (tokens.size() < 9 || !"and".equalsIgnoreCase(tokens.get(4)) || !"--target".equalsIgnoreCase(tokens.get(6)) || !"to".equalsIgnoreCase(tokens.get(8))) {
      throw new IllegalArgumentException("Invalid copy events between format");
    }
    LocalDate startDate = LocalDate.parse(tokens.get(3));
    LocalDate endDate = LocalDate.parse(tokens.get(5));
    String targetCalendar = stripQuotes(tokens.get(7)).trim();
    LocalDate targetStartDate = LocalDate.parse(tokens.get(9));
    return new CopyEventsBetweenDatesCommand(startDate, endDate, targetCalendar, targetStartDate);
  }

  private ICommand parseCreateCommand(List<String> tokens) {
    if (tokens.size() >= 2 && "calendar".equalsIgnoreCase(tokens.get(1))) {
      return parseCreateCalendarCommand(tokens);
    }
    return parseCreateEvent(tokens);
  }

  private ICommand parseEditCommand(List<String> tokens) {
    if (tokens.size() >= 2 && "calendar".equalsIgnoreCase(tokens.get(1))) {
      return parseEditCalendarCommand(tokens);
    }
    return parseEditEvent(tokens);
  }

  private ICommand parseUseCommand(List<String> tokens) {
    if (tokens.size() >= 2 && "calendar".equalsIgnoreCase(tokens.get(1))) {
      return parseUseCalendarCommand(tokens);
    }
    throw new IllegalArgumentException("Invalid use command format.");
  }

  private ICommand parseCreateCalendarCommand(List<String> tokens) {
    String name = null;
    String timezoneStr = null;
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

  private ICommand parseEditCalendarCommand(List<String> tokens) {
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

  private ICommand parseUseCalendarCommand(List<String> tokens) {
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

  private ICommand parseExportCommand(List<String> tokens) {
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

  private ICommand parsePrintCommand(List<String> tokens) {
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

  private ICommand parsePrintOnCommand(List<String> tokens) {
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

  private ICommand parsePrintRangeCommand(List<String> tokens) {
    if (tokens.size() < 6 || !"to".equalsIgnoreCase(tokens.get(4))) {
      throw new IllegalArgumentException("Expected format: print events from <start> to <end>");
    }
    ZonedDateTime start = parseDateTime(tokens.get(3));
    ZonedDateTime end = parseDateTime(tokens.get(5));
    return new QueryRangeDateTimeCommand(start, end);
  }

  private ICommand parseShowCommand(List<String> tokens) {
    if (tokens.size() < 4 || !"status".equalsIgnoreCase(tokens.get(1)) || !"on".equalsIgnoreCase(tokens.get(2))) {
      throw new IllegalArgumentException("Invalid show command. Usage: show status on <datetime>");
    }
    return new BusyQueryCommand(parseDateTime(tokens.get(3)));
  }

  private ICommand parseEditEvent(List<String> tokens) {
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

  private ICommand parseEditEventsCommand(List<String> tokens) {
    String property = tokens.get(2).toLowerCase();
    String eventName = stripQuotes(tokens.get(3));
    boolean isRecurringProperty = property.equals("repeattimes") || property.equals("repeatuntil") || property.equals("repeatingdays");

    if (tokens.size() == 5) {
      String newValue = stripQuotes(tokens.get(4));
      if (isRecurringProperty) {
        ZonedDateTime newRepeatUntil = parseDateTime(newValue);
        return new EditRecurringEventCommand(property, eventName, newRepeatUntil.toString());
      } else {
        return new EditEventCommand(property, eventName, newValue);
      }
    } else if (tokens.size() == 8) {
      if (!"from".equalsIgnoreCase(tokens.get(4)) || !"with".equalsIgnoreCase(tokens.get(6))) {
        throw new IllegalArgumentException("Invalid edit events command format");
      }
      ZonedDateTime filterDateTime = parseDateTime(tokens.get(5));
      String newValue = stripQuotes(tokens.get(7));
      return isRecurringProperty
              ? new EditRecurringEventCommand(property, eventName, newValue)
              : new EditEventCommand(property, eventName, filterDateTime, newValue);
    } else {
      throw new IllegalArgumentException("Invalid edit events command format");
    }
  }

  private ICommand parseEditSingleEventCommand(List<String> tokens) {
    if (tokens.size() < 10 || !"from".equalsIgnoreCase(tokens.get(4)) || !"to".equalsIgnoreCase(tokens.get(6)) || !"with".equalsIgnoreCase(tokens.get(8))) {
      throw new IllegalArgumentException("Invalid edit event command format");
    }
    String property = tokens.get(2).toLowerCase();
    String eventName = stripQuotes(tokens.get(3));
    ZonedDateTime start = parseDateTime(tokens.get(5));
    ZonedDateTime end = parseDateTime(tokens.get(7));
    String newValue = stripQuotes(tokens.get(9));
    return new EditEventCommand(property, eventName, start, end, newValue);
  }

  private ICommand parseCreateEvent(List<String> tokens) {
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
    index = timing.getIndex();

    RecurringResult recurring = parseRecurringSection(tokens, index);
    index = recurring.getIndex();
    if (recurring.isRecurring() && timing.getEnd().isAfter(timing.getStart().plusHours(24))) {
      throw new IllegalArgumentException("Recurring event must end within 24 hours of the start time.");
    }

    PropertiesResult props = parseProperties(tokens, index);

    return new CreateEventCommand(
            eventName,
            timing.getStart(),
            timing.getEnd(),
            autoDecline,
            props.getDescription(),
            props.getLocation(),
            props.isPublic(),
            timing.isAllDay(),
            recurring.isRecurring(),
            recurring.getWeekdays(),
            recurring.getRepeatCount(),
            recurring.getRepeatUntil()
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
        result.end = result.start.toLocalDate().atTime(23, 59, 59).atZone(result.start.getZone());
        result.isAllDay = true;
      }
    } else if ("on".equals(keyword)) {
      index++;
      result.start = parseDateTime(tokens.get(index++));
      result.end = result.start.toLocalDate().atTime(23, 59, 59).atZone(result.start.getZone());
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


  private ZonedDateTime parseDateTime(String token) {
    try {
      return ZonedDateTime.of(java.time.LocalDateTime.parse(token), calendarManager.getActiveCalendar().getTimezone());
    } catch (DateTimeParseException e) {
      return LocalDate.parse(token).atStartOfDay(calendarManager.getActiveCalendar().getTimezone());
    }
  }


  private ZonedDateTime parseDateTime(String date, String time) {
    LocalDate localDate = LocalDate.parse(date);
    LocalTime localTime = LocalTime.parse(time);
    ZoneId zone = calendarManager.getActiveCalendar().getTimezone();
    return ZonedDateTime.of(localDate, localTime, zone);
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
