package calendarapp.controller;

import calendarapp.controller.commands.BusyQueryCommand;
import calendarapp.controller.commands.CopyEventsBetweenDatesCommand;
import calendarapp.controller.commands.CopyEventsOnDateCommand;
import calendarapp.controller.commands.CopySingleEventCommand;
import calendarapp.controller.commands.CreateCalendarCommand;
import calendarapp.controller.commands.CreateEventCommand;
import calendarapp.controller.commands.EditCalendarCommand;
import calendarapp.controller.commands.EditEventCommand;
import calendarapp.controller.commands.EditRecurringEventCommand;
import calendarapp.controller.commands.ExportCalendarCommand;
import calendarapp.controller.commands.ICommand;
import calendarapp.controller.commands.QueryByDateCommand;
import calendarapp.controller.commands.QueryRangeDateTimeCommand;
import calendarapp.controller.commands.UseCalendarCommand;
import calendarapp.model.ICalendarManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses user commands and converts them into executable commands.
 * The CommandParser handles parsing various commands for creating, editing,
 * printing, exporting, and manipulating calendar events.
 */
public class CommandParser {

  private final ICalendarManager calendarManager;
  private final Map<String, Function<List<String>, ICommand>> parsers;

  /**
   * Constructs a CommandParser that can parse commands for interacting with the calendar.
   *
   * @param calendarManager the calendar manager for managing calendars and events
   */
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

  /**
   * Parses a command string and returns the corresponding ICommand object.
   *
   * @param command the command to parse
   * @return the corresponding ICommand object
   * @throws IllegalArgumentException if the command is invalid
   */
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

  /**
   * Parses the "copy" command, which copies events between calendars or dates.
   *
   * @param tokens the list of tokens representing the command
   * @return the corresponding ICommand object
   * @throws IllegalArgumentException if the command format is invalid
   */
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

  /**
   * Parses the "copy event" command, which copies a single event to another calendar.
   *
   * @param tokens the list of tokens representing the command
   * @return the corresponding ICommand object
   * @throws IllegalArgumentException if the command format is invalid
   */
  private ICommand parseCopySingleEvent(List<String> tokens) {
    if (tokens.size() < 9 || !"on".equalsIgnoreCase(tokens.get(3))
            || !"--target".equalsIgnoreCase(tokens.get(5))
            || !"to".equalsIgnoreCase(tokens.get(7))) {
      throw new IllegalArgumentException("Invalid copy event format");
    }
    String eventName = stripQuotes(tokens.get(2));
    ZonedDateTime sourceDateTime = parseDateTime(tokens.get(4));
    String targetCalendar = stripQuotes(tokens.get(6)).trim();
    ZoneId targetZone = calendarManager.getCalendar(targetCalendar).getTimezone();
    ZonedDateTime targetDateTime = parseDateTimeWithZone(tokens.get(8), targetZone);

    return new CopySingleEventCommand(eventName, sourceDateTime, targetCalendar, targetDateTime);
  }

  /**
   * Parses the date and time with a specific time zone.
   *
   * @param token the date/time string
   * @param zone  the target time zone
   * @return the ZonedDateTime object
   * @throws DateTimeParseException if the date/time format is invalid
   */
  private ZonedDateTime parseDateTimeWithZone(String token, ZoneId zone) {
    try {
      return ZonedDateTime.of(java.time.LocalDateTime.parse(token), zone);
    } catch (DateTimeParseException e) {
      return LocalDate.parse(token).atStartOfDay(zone);
    }
  }

  /**
   * Parses the "copy events on" command, which copies all events on a specific
   * date to another calendar.
   *
   * @param tokens the list of tokens representing the command
   * @return the corresponding ICommand object
   * @throws IllegalArgumentException if the command format is invalid
   */
  private ICommand parseCopyEventsOnDate(List<String> tokens) {
    if (tokens.size() < 7 || !"--target".equalsIgnoreCase(tokens.get(4))
            || !"to".equalsIgnoreCase(tokens.get(6))) {
      throw new IllegalArgumentException("Invalid copy events on format");
    }

    ZoneId zone = calendarManager.getActiveCalendar().getTimezone();

    ZonedDateTime sourceDate = LocalDate.parse(tokens.get(3)).atStartOfDay(zone);
    String targetCalendar = stripQuotes(tokens.get(5)).trim();
    ZonedDateTime targetDate = LocalDate.parse(tokens.get(7)).atStartOfDay(
            calendarManager.getCalendar(targetCalendar).getTimezone());

    return new CopyEventsOnDateCommand(sourceDate, targetCalendar, targetDate);
  }


  /**
   * Parses the "copy events between" command, which copies all events between
   * two dates to another calendar.
   *
   * @param tokens the list of tokens representing the command
   * @return the corresponding ICommand object
   * @throws IllegalArgumentException if the command format is invalid
   */
  private ICommand parseCopyEventsBetween(List<String> tokens) {
    if (tokens.size() < 10 || !"and".equalsIgnoreCase(tokens.get(4))
            || !"--target".equalsIgnoreCase(tokens.get(6))
            || !"to".equalsIgnoreCase(tokens.get(8))) {
      throw new IllegalArgumentException("Invalid copy events between format");
    }

    ZoneId activeZone = calendarManager.getActiveCalendar().getTimezone();

    ZonedDateTime startDate = LocalDate.parse(tokens.get(3)).atStartOfDay(activeZone);
    ZonedDateTime endDate = LocalDate.parse(tokens.get(5)).atStartOfDay(activeZone);

    String targetCalendar = stripQuotes(tokens.get(7)).trim();
    ZoneId targetZone = calendarManager.getCalendar(targetCalendar).getTimezone();
    ZonedDateTime targetStartDate = LocalDate.parse(tokens.get(9)).atStartOfDay(targetZone);

    return new CopyEventsBetweenDatesCommand(startDate, endDate, targetCalendar, targetStartDate);
  }

  /**
   * Parses the "create" command, which creates events or calendars.
   *
   * @param tokens the list of tokens representing the command
   * @return the corresponding ICommand object
   * @throws IllegalArgumentException if the command format is invalid
   */
  private ICommand parseCreateCommand(List<String> tokens) {
    if (tokens.size() >= 2 && "calendar".equalsIgnoreCase(tokens.get(1))) {
      return parseCreateCalendarCommand(tokens);
    }
    return parseCreateEvent(tokens);
  }

  /**
   * Parses an "edit" command.
   *
   * @param tokens the list of tokens representing the command
   * @return a Command object for editing events or a single event
   * @throws IllegalArgumentException if the command type is unsupported or incomplete
   */
  private ICommand parseEditCommand(List<String> tokens) {
    if (tokens.size() >= 2 && "calendar".equalsIgnoreCase(tokens.get(1))) {
      return parseEditCalendarCommand(tokens);
    }
    return parseEditEvent(tokens);
  }

  /**
   * Parses the "use" command to switch to a specific calendar.
   *
   * @param tokens the list of tokens representing the command
   * @return the corresponding ICommand object
   * @throws IllegalArgumentException if the command format is invalid
   */
  private ICommand parseUseCommand(List<String> tokens) {
    if (tokens.size() >= 2 && "calendar".equalsIgnoreCase(tokens.get(1))) {
      return parseUseCalendarCommand(tokens);
    }
    throw new IllegalArgumentException("Invalid use command format.");
  }

  /**
   * Parses the "create calendar" command, which creates a new calendar.
   *
   * @param tokens the list of tokens representing the command
   * @return the corresponding ICommand object for creating a calendar
   * @throws IllegalArgumentException if the command format is invalid
   */
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
      throw new IllegalArgumentException("Usage: create calendar --name <name> "
              + "--timezone <timezone>");
    }
    try {
      ZoneId timezone = ZoneId.of(timezoneStr);
      return new CreateCalendarCommand(name, timezone);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid timezone: " + timezoneStr);
    }
  }

  /**
   * Parses the "edit calendar" command to modify the properties of an existing calendar.
   *
   * @param tokens the list of tokens representing the command
   * @return the corresponding ICommand object for editing a calendar
   * @throws IllegalArgumentException if the command format is invalid
   */
  private ICommand parseEditCalendarCommand(List<String> tokens) {
    String name = null;
    String property = null;
    String newValue = null;
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
      throw new IllegalArgumentException("Usage: edit calendar --name <name> --property "
              + "<property> <new-value>");
    }
    return new EditCalendarCommand(name, property, newValue);
  }

  /**
   * Parses the "use calendar" command, which selects a specific calendar to use.
   *
   * @param tokens the list of tokens representing the command
   * @return the corresponding ICommand object for using a calendar
   * @throws IllegalArgumentException if the command format is invalid
   */
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

  /**
   * Parses the "export" command to export the calendar data to a file.
   *
   * @param tokens the list of tokens representing the command
   * @return the corresponding ICommand object for exporting the calendar
   * @throws IllegalArgumentException if the command format is invalid
   */
  private ICommand parseExportCommand(List<String> tokens) {
    if (tokens.size() < 3 || !"cal".equalsIgnoreCase(tokens.get(1))) {
      throw new IllegalArgumentException("Invalid export format. Usage: export cal <filePath.csv>");
    }

    StringBuilder pathBuilder = new StringBuilder();
    for (int i = 2; i < tokens.size(); i++) {
      pathBuilder.append(tokens.get(i));
      if (i != tokens.size() - 1) {
        pathBuilder.append(" ");
      }
    }

    String filePathRaw = pathBuilder.toString().trim();
    String filePath;

    if (filePathRaw.startsWith("\"") && filePathRaw.endsWith("\"")) {
      filePath = filePathRaw.substring(1, filePathRaw.length() - 1);
    } else {
      filePath = filePathRaw;
    }

    if (!filePath.toLowerCase().endsWith(".csv")) {
      throw new IllegalArgumentException("Exported file must have a .csv extension");
    }

    return new ExportCalendarCommand(filePath);
  }


  /**
   * Parses the "print" command to print events either on a specific date or in a range.
   *
   * @param tokens the list of tokens representing the command
   * @return the corresponding ICommand object for printing events
   * @throws IllegalArgumentException if the command format is invalid
   */
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

  /**
   * Parses the "print on" command to print events on a specific date.
   *
   * @param tokens the list of tokens representing the command
   * @return the corresponding ICommand object for printing events on a specific date
   * @throws IllegalArgumentException if the command format is invalid
   */
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


  /**
   * Parses the "print from" command to print events within a specific date range.
   *
   * @param tokens the list of tokens representing the command
   * @return the corresponding ICommand object for printing events in a range
   * @throws IllegalArgumentException if the command format is invalid
   */
  private ICommand parsePrintRangeCommand(List<String> tokens) {
    if (tokens.size() < 6 || !"to".equalsIgnoreCase(tokens.get(4))) {
      throw new IllegalArgumentException("Expected format: print events from <start> to <end>");
    }
    ZonedDateTime start = parseDateTime(tokens.get(3));
    ZonedDateTime end = parseDateTime(tokens.get(5));
    return new QueryRangeDateTimeCommand(start, end);
  }

  /**
   * Parses the "show status" command to check if the calendar is busy at a specified time.
   *
   * @param tokens the list of tokens representing the command
   * @return the corresponding ICommand object for checking if the calendar is busy
   * @throws IllegalArgumentException if the command format is invalid
   */
  private ICommand parseShowCommand(List<String> tokens) {
    if (tokens.size() < 4 || !"status".equalsIgnoreCase(tokens.get(1))
            || !"on".equalsIgnoreCase(tokens.get(2))) {
      throw new IllegalArgumentException("Invalid show command. Usage: show status on <datetime>");
    }
    return new BusyQueryCommand(parseDateTime(tokens.get(3)));
  }

  /**
   * Parses the "edit event" command to edit a specific event.
   *
   * @param tokens the list of tokens representing the command
   * @return the corresponding ICommand object for editing an event
   * @throws IllegalArgumentException if the command format is invalid
   */
  private ICommand parseEditEvent(List<String> tokens) {
    if (tokens.size() < 4) {
      throw new IllegalArgumentException("Incomplete edit command");
    }

    switch (tokens.get(1).toLowerCase()) {
      case "events":
        return parseEditEventsCommand(tokens);
      case "event":
        return parseEditSingleEventCommand(tokens);
      default:
        throw new IllegalArgumentException("Unsupported edit command type");
    }
  }

  /**
   * Parses the "edit events" command to modify properties of multiple events.
   *
   * @param tokens the list of tokens representing the command
   * @return the corresponding ICommand object for editing multiple events
   * @throws IllegalArgumentException if the command format is invalid
   */
  private ICommand parseEditEventsCommand(List<String> tokens) {
    String property = tokens.get(2).toLowerCase();
    String eventName = stripQuotes(tokens.get(3));
    boolean isRecurringProperty = property.equals("repeattimes") || property.equals("repeatuntil")
            || property.equals("repeatingdays");

    if (tokens.size() == 5) {
      String newValue = stripQuotes(tokens.get(4));

      if (isRecurringProperty) {
        if (property.equals("repeatuntil")) {
          ZonedDateTime newRepeatUntil = parseDateTime(newValue);
          return new EditRecurringEventCommand(property, eventName, newRepeatUntil);
        } else {
          return new EditRecurringEventCommand(property, eventName, newValue);
        }
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

  /**
   * Parses the "edit single event" command to modify a specific event's properties.
   *
   * @param tokens the list of tokens representing the command
   * @return the corresponding ICommand object for editing a single event
   * @throws IllegalArgumentException if the command format is invalid
   */
  private ICommand parseEditSingleEventCommand(List<String> tokens) {
    if (tokens.size() < 10 || !"from".equalsIgnoreCase(tokens.get(4))
            || !"to".equalsIgnoreCase(tokens.get(6)) || !"with".equalsIgnoreCase(tokens.get(8))) {
      throw new IllegalArgumentException("Invalid edit event command format");
    }
    String property = tokens.get(2).toLowerCase();
    String eventName = stripQuotes(tokens.get(3));
    ZonedDateTime start = parseDateTime(tokens.get(5));
    ZonedDateTime end = parseDateTime(tokens.get(7));
    String newValue = stripQuotes(tokens.get(9));
    return new EditEventCommand(property, eventName, start, end, newValue);
  }

  /**
   * Parses the "create event" command to create a new event with the given parameters.
   *
   * @param tokens the list of tokens representing the command
   * @return the corresponding ICommand object for creating an event
   * @throws IllegalArgumentException if any required parameters are missing or invalid
   */
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

    ParsedEventTiming timing = parseEventTiming(tokens, index);
    index = timing.getIndex();

    ParsedRecurringEvent recurring = parseRecurringSection(tokens, index);
    index = recurring.getIndex();
    if (recurring.isRecurring() &&
            ((ZonedDateTime) timing.getEnd())
                    .isAfter(((ZonedDateTime) timing.getStart()).plusHours(24))) {
      throw new IllegalArgumentException("Recurring event must end within "
              + "24 hours of the start time.");
    }

    ParsedEventProperties props = parseProperties(tokens, index);

    return new CreateEventCommand(eventName, timing.getStart(), timing.getEnd(), autoDecline,
            props.getDescription(), props.getLocation(), props.isPublic(), timing.isAllDay(),
            recurring.isRecurring(), recurring.getWeekdays(), recurring.getRepeatCount(),
            recurring.getRepeatUntil()
    );
  }

  /**
   * Parses the timing details (start and end date) of the event from the command tokens.
   *
   * @param tokens the list of tokens representing the command
   * @param index  the current index in the token list
   * @return a ParsedEventTiming object containing event start, end, and all-day status
   * @throws IllegalArgumentException if the start and end times are invalid or in incorrect format
   */
  private ParsedEventTiming parseEventTiming(List<String> tokens, int index) {
    ParsedEventTiming result = new ParsedEventTiming();
    String keyword = tokens.get(index).toLowerCase();

    if ("from".equals(keyword)) {
      index++;
      ZonedDateTime start = parseDateTime(tokens.get(index++));
      result.start = start;

      if (index < tokens.size() && "to".equalsIgnoreCase(tokens.get(index))) {
        index++;
        ZonedDateTime end = parseDateTime(tokens.get(index++));
        if (end.isBefore(start)) {
          throw new IllegalArgumentException("End date must be after start date");
        }
        result.end = end;
        result.isAllDay = false;
      } else {
        result.end = start.toLocalDate().atTime(23, 59, 59).atZone(start.getZone());
        result.isAllDay = true;
      }

    } else if ("on".equals(keyword)) {
      index++;
      ZonedDateTime start = parseDateTime(tokens.get(index++));
      result.start = start;
      result.end = start.toLocalDate().atTime(23, 59, 59).atZone(start.getZone());
      result.isAllDay = true;

    } else {
      throw new IllegalArgumentException("Expected 'from' or 'on' after event name");
    }

    result.index = index;
    return result;
  }


  /**
   * Parses the recurring section of the event, including repeat count and weekdays.
   *
   * @param tokens the list of tokens representing the command
   * @param index  the current index in the token list
   * @return a ParsedRecurringEvent object containing repeat-related details
   * @throws IllegalArgumentException if recurring details are missing or incorrectly formatted
   */
  private ParsedRecurringEvent parseRecurringSection(List<String> tokens, int index) {
    ParsedRecurringEvent result = new ParsedRecurringEvent();

    if (index < tokens.size() && "repeats".equalsIgnoreCase(tokens.get(index))) {
      result.isRecurring = true;
      index++;

      if (index >= tokens.size()) {
        throw new IllegalArgumentException("Missing weekdays after 'repeats'");
      }
      String weekdaysStr = stripQuotes(tokens.get(index++)).toUpperCase();

      if (weekdaysStr.isEmpty()) {
        result.weekdays = "MTWRFSU";
      } else {
        result.weekdays = weekdaysStr;
      }

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

  /**
   * Parses additional event properties like description, location, and visibility (public/private).
   *
   * @param tokens the list of tokens representing the command
   * @param index  the current index in the token list
   * @return a ParsedEventProperties object containing parsed event properties
   * @throws IllegalArgumentException if properties are missing or in incorrect format
   */
  private ParsedEventProperties parseProperties(List<String> tokens, int index) {
    ParsedEventProperties result = new ParsedEventProperties();
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

  /**
   * Parses a date-time token into a ZonedDateTime object.
   *
   * @param token the date-time token (either ZonedDateTime or LocalDate)
   * @return the corresponding ZonedDateTime object
   * @throws DateTimeParseException if the token cannot be parsed into a valid date-time
   */
  private ZonedDateTime parseDateTime(String token) {
    ZoneId zone = calendarManager.getActiveCalendar().getTimezone();
    try {
      return ZonedDateTime.of(LocalDateTime.parse(token), zone);
    } catch (DateTimeParseException e) {
      return LocalDate.parse(token).atStartOfDay(zone);
    }
  }

  /**
   * Strips quotes from a token (if any) to return the raw string.
   *
   * @param token the token to be stripped of quotes
   * @return the token without the leading and trailing quotes
   */
  private String stripQuotes(String token) {
    if (token.startsWith("\"") && token.endsWith("\"") && token.length() > 1) {
      return token.substring(1, token.length() - 1);
    }
    return token;
  }

  /**
   * Tokenizes a command string into a list of individual tokens.
   *
   * @param command the command string to tokenize
   * @return a list of tokens extracted from the command
   */
  public static List<String> tokenize(String command) {
    List<String> tokens = new ArrayList<>();
    Matcher matcher = Pattern.compile("\"[^\"]+\"|\\S+").matcher(command);
    while (matcher.find()) {
      tokens.add(matcher.group());
    }
    return tokens;
  }
}
