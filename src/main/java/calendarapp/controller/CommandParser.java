package calendarapp.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandParser {
  public Command parse(String command){
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
    if(index>=tokens.size() || !tokens.get(index).equalsIgnoreCase("event")){
      throw new IllegalArgumentException("Expected 'event' after create") ;
    }
    index++;
    boolean autoDecline = false;
    if(index < tokens.size() && tokens.get(index).equalsIgnoreCase("--autoDecline")){
      autoDecline = true;
      index++;
    }
    if (index >= tokens.size()) {
      throw new IllegalArgumentException("Missing event name") ;
    }
    String eventName = tokens.get(index++);

    if(index>=tokens.size() || !tokens.get(index).equalsIgnoreCase("from")) {
      throw new IllegalArgumentException("Expected 'from' after event") ;
    }
    index++;

    LocalDateTime startDateTime = LocalDateTime.parse(tokens.get(index++));
    if(index >= tokens.size() || !tokens.get(index).equalsIgnoreCase("to")) {
      throw new IllegalArgumentException("Expected 'to' after event") ;
    }
    index++;

    LocalDateTime endDateTime = LocalDateTime.parse(tokens.get(index));
    if(endDateTime.isBefore(startDateTime)){
      throw new IllegalArgumentException("End date should be after start date") ;
    }

    return new CreateEventCommand(eventName, startDateTime, endDateTime, autoDecline);
  }

  public static List<String> tokenize(String command){
    List<String> tokens = new ArrayList<>();
    Pattern pattern = Pattern.compile("\"[^\"]+\"|\\S+");
    Matcher matcher = pattern.matcher(command);
    while (matcher.find()) {
      tokens.add(matcher.group());
    }
    return tokens;
  }
}

