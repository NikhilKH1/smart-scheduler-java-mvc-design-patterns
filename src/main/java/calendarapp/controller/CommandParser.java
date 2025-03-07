package calendarapp.controller;

import calendarapp.model.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandParser {

  public Command parse(String command) {
    List<String> tokens = tokenize(command);
    if (tokens == null || tokens.isEmpty()) {
      return null;
    }

    String mainCommand = tokens.get(0).toLowerCase();
    if ("create".equals(mainCommand)) {
      return parseCreateEvent(tokens);
    }
    return null;
  }

  private Command parseCreateEvent(List<String> tokens) {
    int index = 1;
    if (index >= tokens.size() || !tokens.get(index).equalsIgnoreCase("event")) {
      throw new IllegalArgumentException("Expected 'event' after create");
    }
    index++;

    boolean autoDecline = false;
    if (index < tokens.size() && tokens.get(index).equalsIgnoreCase("--autoDecline")) {
      autoDecline = true;
      index++;
    }

    if (index >= tokens.size()) {
      throw new IllegalArgumentException("Missing event name");
    }
    String eventName = stripQuotes(tokens.get(index++));

    if (index >= tokens.size()) {
      throw new IllegalArgumentException("Expected 'from' or 'on' after event name");
    }

    LocalDateTime startDateTime;
    LocalDateTime endDateTime;
    boolean isAllDay;

    String dateKeyword = tokens.get(index).toLowerCase();

    if (index < tokens.size() && tokens.get(index).equalsIgnoreCase("from")) {
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
          if (endDateTime.isBefore(startDateTime)) {
            throw new IllegalArgumentException("End date should be after start date");
          }
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

      if (index >= tokens.size()) {
        throw new IllegalArgumentException("Missing weekdays after 'repeats'");
      }
      weekdays = tokens.get(index++).toUpperCase();

      if (index < tokens.size() && tokens.get(index).equalsIgnoreCase("for")) {
        index++;
        repeatCount = Integer.parseInt(tokens.get(index++));
        if (index >= tokens.size() || !tokens.get(index++).equalsIgnoreCase("times")) {
          throw new IllegalArgumentException("Expected 'times' after repeat count");
        }
      } else if (index < tokens.size() && tokens.get(index).equalsIgnoreCase("until")) {
        index++;
        repeatUntil = LocalDateTime.parse(tokens.get(index++));
      } else {
        throw new IllegalArgumentException("Expected 'for' or 'until' after weekdays");
      }
    }

    // Optional parameters parsing (description, location, public/private)
    String description = "";
    String location = "";
    boolean isPublic = true;

    while (index < tokens.size()) {
      String token = tokens.get(index++).toLowerCase();
      switch (token) {
        case "description":
          if (index >= tokens.size()) throw new IllegalArgumentException("Missing description");
          description = stripQuotes(tokens.get(index++));
          break;
        case "location":
          if (index >= tokens.size()) throw new IllegalArgumentException("Missing location");
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
