package calendarapp.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import calendarapp.model.Command;
import calendarapp.model.CreateEventCommand;

public class CommandParser {
  public Command parse(String command) {
    List<String> tokens = tokenize(command);
    if (tokens == null || tokens.size() == 0) {
      return null;
    }

    String mainCommand = tokens.get(0).toLowerCase();
    if (mainCommand.equals("create")) {
      return parserCreateEvent(tokens);
    }
    return null;
  }

  CreateEventCommand parserCreateEvent(List<String> tokens) {
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

    if (index >= tokens.size() || !tokens.get(index).equalsIgnoreCase("from")) {
      throw new IllegalArgumentException("Expected 'from' after event name");
    }
    index++;

    LocalDateTime startDateTime;
    try {
      startDateTime = LocalDateTime.parse(tokens.get(index++));
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid start date/time format");
    }

    LocalDateTime endDateTime = null;
    boolean isAllDay = false;
    if (index < tokens.size() && tokens.get(index).equalsIgnoreCase("to")) {
      index++;
      try {
        endDateTime = LocalDateTime.parse(tokens.get(index++));
        if (endDateTime.isBefore(startDateTime)) {
          throw new IllegalArgumentException("End date should be after start date");
        }
      } catch (DateTimeParseException e) {
        throw new IllegalArgumentException("Invalid end date/time format");
      }
    } else {
      // No end time provided: mark as all day event.
      isAllDay = true;
      // Set end time to end of the day (23:59:59) of the start date.
      endDateTime = startDateTime.toLocalDate().atTime(23, 59, 59);
    }

    // Parse optional parameters: description, location, event type (public/private)
    String description = "";
    String location = "";
    boolean isPublic = true; // default is public

    while (index < tokens.size()) {
      String token = tokens.get(index);
      if (token.equalsIgnoreCase("description")) {
        index++;
        if (index < tokens.size()) {
          description = stripQuotes(tokens.get(index++));
        } else {
          throw new IllegalArgumentException("Expected description text after 'description'");
        }
      } else if (token.equalsIgnoreCase("location")) {
        index++;
        if (index < tokens.size()) {
          location = stripQuotes(tokens.get(index++));
        } else {
          throw new IllegalArgumentException("Expected location text after 'location'");
        }
      } else if (token.equalsIgnoreCase("public")) {
        isPublic = true;
        index++;
      } else if (token.equalsIgnoreCase("private")) {
        isPublic = false;
        index++;
      } else {
        throw new IllegalArgumentException("Unknown token: " + token);
      }
    }

    return new CreateEventCommand(eventName, startDateTime, endDateTime,
            autoDecline, description, location, isPublic, isAllDay);
  }

  // Utility method to remove surrounding quotes (if any)
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
