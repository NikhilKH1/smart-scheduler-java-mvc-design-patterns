package calendarapp.controller;

import calendarapp.model.CalendarModel;
import calendarapp.model.commands.*;
import calendarapp.model.commands.CreateEventCommand;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsible for parsing command strings into Command objects.
 * It supports commands such as creating events, printing events, showing status,
 * editing events, and exporting calendar data.
 */
public class CommandParser {

  private final CalendarModel model;
  private final Map<String, Function<List<String>, Command>> Parsers;

  /**
   * Constructs a CommandParser with the specified calendar model.
   *
   * @param model the calendar model used for command context
   */
  public CommandParser(CalendarModel model) {
    this.model = model;
    Parsers = new HashMap<>();
    Parsers.put("create", this::parseCreateEvent);
    Parsers.put("print", this::parsePrintCommand);
    Parsers.put("show", this::parseShowCommand);
    Parsers.put("edit", this::parseEditCommand);
    Parsers.put("export", this::parseExportCommand);
  }

  /**
   * Parses a command string and returns the corresponding Command object.
   *
   * @param command the command string to parse
   * @return the Command object represented by the command string
   * @throws IllegalArgumentException if the command is unknown or invalid
   */
  public Command parse(String command) {
    if (command == null) {
      throw new IllegalArgumentException("Command cannot be null");
    }
    List<String> tokens = tokenize(command);
    if (tokens.isEmpty()) {
      return null;
    }
    String mainCommand = tokens.get(0).toLowerCase();
    Function<List<String>, Command> parserFunc = Parsers.get(mainCommand);
    if (parserFunc != null) {
      return parserFunc.apply(tokens);
    }
    throw new IllegalArgumentException("Unknown command: " + mainCommand);
  }

  /**
   * Parses an export command.
   *
   * @param tokens the list of tokens representing the command
   * @return an ExportCalendarCommand object
   * @throws IllegalArgumentException if the command format is invalid
   */
  private Command parseExportCommand(List<String> tokens) {
    if (tokens.size() != 3)
      throw new IllegalArgumentException("Invalid export command format. "
              + "Expected: export cal fileName.csv");
    if (!"cal".equalsIgnoreCase(tokens.get(1)))
      throw new IllegalArgumentException("Invalid export command. Expected 'cal' after export.");
    String fileName = tokens.get(2);
    if (!fileName.toLowerCase().endsWith(".csv"))
      throw new IllegalArgumentException("Invalid file name. Must be a CSV file ending with .csv");
    return new ExportCalendarCommand(model, fileName);
  }

  /**
   * Parses a print command.
   *
   * @param tokens the list of tokens representing the command
   * @return a Command object for printing events
   * @throws IllegalArgumentException if the command format is incomplete or invalid
   */
  private Command parsePrintCommand(List<String> tokens) {
    if (tokens.size() < 3)
      throw new IllegalArgumentException("Incomplete print events command."
              + " Usage: print events on/from <date/time>");

    String secondToken = tokens.get(1).toLowerCase();
    if (!"events".equals(secondToken))
      throw new IllegalArgumentException("Expected 'events' after print");

    String thirdToken = tokens.get(2).toLowerCase();
    switch (thirdToken) {
      case "on":
        if (tokens.size() < 4)
          throw new IllegalArgumentException("Expected date after 'on'");
        return parsePrintOnCommand(tokens);
      case "from":
        if (tokens.size() < 6)
          throw new IllegalArgumentException("Incomplete print events range command."
                  + " Usage: print events from <start> to <end>");
        return parsePrintRangeCommand(tokens);
      default:
        throw new IllegalArgumentException("Expected 'on' or 'from' after 'print events'");
    }
  }

  /**
   * Parses a print command for a single date.
   *
   * @param tokens the list of tokens representing the command
   * @return a QueryByDateCommand object
   * @throws IllegalArgumentException if the date format is invalid
   */
  private Command parsePrintOnCommand(List<String> tokens) {
    try {
      LocalDate date = LocalDate.parse(tokens.get(3));
      return new QueryByDateCommand(date);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD");
    }
  }

  /**
   * Parses a print command for a date range.
   *
   * @param tokens the list of tokens representing the command
   * @return a QueryRangeDateTimeCommand object
   * @throws IllegalArgumentException if the date/time format is invalid
   */
  private Command parsePrintRangeCommand(List<String> tokens) {
    try {
      LocalDateTime start = parseDateTime(tokens.get(3));
      if (!"to".equalsIgnoreCase(tokens.get(4)))
        throw new IllegalArgumentException("Expected 'to' after start date/time");
      LocalDateTime end = parseDateTime(tokens.get(5));
      return new QueryRangeDateTimeCommand(start, end);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date/time format."
              + " Use YYYY-MM-DD or YYYY-MM-DDTHH:MM");
    }
  }

  /**
   * Parses a show command.
   *
   * @param tokens the list of tokens representing the command
   * @return a BusyQueryCommand object
   * @throws IllegalArgumentException if the command format is invalid
   */
  private Command parseShowCommand(List<String> tokens) {
    if (tokens.size() < 4 || !tokens.get(1).equalsIgnoreCase("status") ||
            !tokens.get(2).equalsIgnoreCase("on"))
      throw new IllegalArgumentException("Usage: show status on <dateTime>");
    try {
      LocalDateTime queryTime = LocalDateTime.parse(tokens.get(3));
      return new BusyQueryCommand(queryTime);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date/time format. Use YYYY-MM-DDTHH:MM");
    }
  }

  /**
   * Parses an edit command.
   *
   * @param tokens the list of tokens representing the command
   * @return a Command object for editing events or a single event
   * @throws IllegalArgumentException if the command type is unsupported or incomplete
   */
  private Command parseEditCommand(List<String> tokens) {
    if (tokens.size() < 4)
      throw new IllegalArgumentException("Incomplete edit command");

    String type = tokens.get(1).toLowerCase();
    switch (type) {
      case "events":
        return parseEditEventsCommand(tokens);
      case "event":
        return parseEditSingleEventCommand(tokens);
      default:
        throw new IllegalArgumentException("Unsupported edit command type");
    }
  }

  /**
   * Parses an edit events command.
   *
   * @param tokens the list of tokens representing the command
   * @return a Command object for editing events
   * @throws IllegalArgumentException if the command format is invalid
   */
  private Command parseEditEventsCommand(List<String> tokens) {
    String property = tokens.get(2).toLowerCase();
    String eventName = stripQuotes(tokens.get(3));
    boolean isRecurringProperty = property.equals("repeattimes") ||
            property.equals("repeatuntil") ||
            property.equals("repeatingdays");

    if (tokens.size() == 5) {
      String newValue = stripQuotes(tokens.get(4));
      return isRecurringProperty ?
              new EditRecurringEventCommand(property, eventName, newValue) :
              new EditEventCommand(property, eventName, newValue);
    } else if (tokens.size() == 8) {
      if (!"from".equalsIgnoreCase(tokens.get(4)))
        throw new IllegalArgumentException("Expected 'from' after event name");
      LocalDateTime filterDateTime = parseDateTime(tokens.get(5));
      if (!"with".equalsIgnoreCase(tokens.get(6)))
        throw new IllegalArgumentException("Expected 'with' after date/time");
      String newValue = stripQuotes(tokens.get(7));
      return isRecurringProperty ?
              new EditRecurringEventCommand(property, eventName, newValue) :
              new EditEventCommand(property, eventName, filterDateTime, newValue);
    } else {
      throw new IllegalArgumentException("Invalid edit events command format");
    }
  }

  /**
   * Parses an edit single event command.
   *
   * @param tokens the list of tokens representing the command
   * @return a Command object for editing a single event
   * @throws IllegalArgumentException if the command format is incomplete
   */
  private Command parseEditSingleEventCommand(List<String> tokens) {
    if (tokens.size() < 10)
      throw new IllegalArgumentException("Incomplete edit event command. Expected format:"
              + " edit event <property> <eventName> from <start> to <end> with <NewPropertyValue>");
    String property = tokens.get(2).toLowerCase();
    String eventName = stripQuotes(tokens.get(3));
    if (!"from".equalsIgnoreCase(tokens.get(4)))
      throw new IllegalArgumentException("Expected 'from' after event name");
    LocalDateTime start = parseDateTime(tokens.get(5));
    if (!"to".equalsIgnoreCase(tokens.get(6)))
      throw new IllegalArgumentException("Expected 'to' after start date/time");
    LocalDateTime end = parseDateTime(tokens.get(7));
    if (!"with".equalsIgnoreCase(tokens.get(8)))
      throw new IllegalArgumentException("Expected 'with' after end date/time");
    String newValue = stripQuotes(tokens.get(9));
    return new EditEventCommand(property, eventName, start, end, newValue);
  }

  /**
   * Parses a create event command.
   *
   * @param tokens the list of tokens representing the command
   * @return a CreateEventCommand object
   * @throws IllegalArgumentException if the command format is invalid
   */
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

    EventTimingResult timing = parseEventTiming(tokens, index);
    index = timing.index;

    RecurringResult recurring = parseRecurringSection(tokens, index);
    index = recurring.index;
    if (recurring.isRecurring && timing.end.isAfter(timing.start.plusHours(24))) {
      throw new IllegalArgumentException("Recurring event must end within 24 hours of the"
              + " start time.");
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

  /**
   * Parses the event timing section of a create event command.
   *
   * @param tokens the list of tokens
   * @param index  the current index in the token list
   * @return an EventTimingResult containing the start time, end time, all-day flag,
   * and updated index
   * @throws IllegalArgumentException if the timing format is invalid
   */
  private EventTimingResult parseEventTiming(List<String> tokens, int index) {
    EventTimingResult result = new EventTimingResult();
    String keyword = tokens.get(index).toLowerCase();
    switch (keyword) {
      case "from":
        index++;
        result.start = parseDateTime(tokens.get(index++));
        if (index < tokens.size() && "to".equalsIgnoreCase(tokens.get(index))) {
          index++;
          result.end = parseDateTime(tokens.get(index++));
          if (result.end.isBefore(result.start))
            throw new IllegalArgumentException("End date must be after start date");
          result.isAllDay = false;
        } else {
          result.end = result.start.toLocalDate().atTime(23, 59, 59);
          result.isAllDay = true;
        }
        break;
      case "on":
        index++;
        result.start = parseDateTime(tokens.get(index++));
        result.end = result.start.toLocalDate().atTime(23, 59, 59);
        result.isAllDay = true;
        break;
      default:
        throw new IllegalArgumentException("Expected 'from' or 'on' after event name");
    }
    result.index = index;
    return result;
  }


  /**
   * Parses the recurring event section of a create event command.
   *
   * @param tokens the list of tokens
   * @param index  the current index in the token list
   * @return a RecurringResult containing recurring details and updated index
   * @throws IllegalArgumentException if the recurring format is invalid
   */
  private RecurringResult parseRecurringSection(List<String> tokens, int index) {
    RecurringResult result = new RecurringResult();
    if (index < tokens.size() && tokens.get(index).equalsIgnoreCase("repeats")) {
      result.isRecurring = true;
      index++;
      if (index >= tokens.size())
        throw new IllegalArgumentException("Missing weekdays after 'repeats'");
      result.weekdays = tokens.get(index++).toUpperCase();
      if (index < tokens.size() && tokens.get(index).equalsIgnoreCase("for")) {
        index++;
        result.repeatCount = Integer.parseInt(tokens.get(index++));
        if (result.repeatCount <= 0)
          throw new IllegalArgumentException("Repeat count must be a positive number");
        if (index >= tokens.size() || !tokens.get(index++).equalsIgnoreCase("times"))
          throw new IllegalArgumentException("Expected 'times' after repeat count");
      } else if (index < tokens.size() && tokens.get(index).equalsIgnoreCase("until")) {
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
   * Parses the event properties section of a create event command.
   *
   * @param tokens the list of tokens
   * @param index  the current index in the token list
   * @return a PropertiesResult containing description, location, public flag, and updated index
   * @throws IllegalArgumentException if an expected property value is missing
   */
  private PropertiesResult parseProperties(List<String> tokens, int index) {
    PropertiesResult result = new PropertiesResult();
    while (index < tokens.size()) {
      String token = tokens.get(index++).toLowerCase();
      switch (token) {
        case "description":
          if (index >= tokens.size())
            throw new IllegalArgumentException("Missing description");
          result.description = stripQuotes(tokens.get(index++));
          break;
        case "location":
          if (index >= tokens.size())
            throw new IllegalArgumentException("Missing location");
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
   * Parses a date/time token into a LocalDateTime object. If the token does not include
   * time information, it returns the start of the day.
   *
   * @param token the date/time token to parse
   * @return a LocalDateTime object representing the parsed date and time
   * @throws DateTimeParseException if the token cannot be parsed
   */
  private LocalDateTime parseDateTime(String token) {
    try {
      return LocalDateTime.parse(token);
    } catch (DateTimeParseException e) {
      return LocalDate.parse(token).atStartOfDay();
    }
  }

  /**
   * Removes surrounding quotes from a token if present.
   *
   * @param token the token to process
   * @return the token without surrounding quotes
   */
  private String stripQuotes(String token) {
    if (token.startsWith("\"") && token.endsWith("\"") && token.length() > 1) {
      return token.substring(1, token.length() - 1);
    }
    return token;
  }

  /**
   * Tokenizes a command string into a list of tokens.
   *
   * @param command the command string to tokenize
   * @return a list of tokens extracted from the command string
   */
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
