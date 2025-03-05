package calendarapp.controller;

import calendarapp.model.CalendarEvent;
import calendarapp.model.CalendarModel;
import calendarapp.model.SingleEvent;
import calendarapp.view.CalendarView;

public class CalendarController {
  private final CalendarModel model;
  private final CalendarView view;
  private final CommandParser parser;

  public CalendarController(CalendarModel model, CalendarView view) {
    this(model, view, new CommandParser());
  }

  public CalendarController(CalendarModel model, CalendarView view, CommandParser parser) {
    this.model = model;
    this.view = view;
    this.parser = parser;
  }

  public boolean processCommand(String commandInput) {
    Command cmd;
    try {
      cmd = parser.parse(commandInput);
    }
    catch (IllegalArgumentException e) {
      view.displayError("Parsing error: " + e.getMessage());
      return false;
    }
    if(cmd instanceof CreateEventCommand) {
      CreateEventCommand createCmd = (CreateEventCommand) cmd;

      CalendarEvent event = new SingleEvent(
              createCmd.getEventName(),
              createCmd.getStartDateTime(),
              createCmd.getEndDateTime()
              );
      boolean success = false;
      try {
        success = model.addEvent(event, createCmd.isAutoDecline());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      if(success) {
        view.displayMessage("Event created successfully");
      }
      else {
        view.displayError("Event creation failed");
      }
      return success;
    }
    view.displayError("Command not implemented");
    return false;
  }
}
