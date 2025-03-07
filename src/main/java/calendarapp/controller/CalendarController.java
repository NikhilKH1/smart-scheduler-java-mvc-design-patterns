package calendarapp.controller;

import calendarapp.model.*;
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
    } catch (IllegalArgumentException e) {
      view.displayError("Parsing Error: " + e.getMessage());
      return false;
    }

    if (cmd instanceof CreateEventCommand) {
      CreateEventCommand createCmd = (CreateEventCommand) cmd;
      boolean success;

      if (createCmd.isRecurring()) {
        RecurringEvent recurringEvent = new RecurringEvent(
                createCmd.getEventName(),
                createCmd.getStartDateTime(),
                createCmd.getEndDateTime(),
                createCmd.getWeekdays(),
                createCmd.getRepeatCount(),
                createCmd.getRepeatUntil()
        );
        success = model.addRecurringEvent(recurringEvent, createCmd.isAutoDecline());
      } else {
        CalendarEvent event = new SingleEvent(
                createCmd.getEventName(),
                createCmd.getStartDateTime(),
                createCmd.getEndDateTime(),
                createCmd.getDescription(),
                createCmd.getLocation(),
                createCmd.isPublic(),
                createCmd.isAllDay()
        );
        success = model.addEvent(event, createCmd.isAutoDecline());
      }

      if (success) {
        view.displayMessage("Event created successfully");
      } else {
        view.displayError("Event creation failed due to conflict");
      }
      return success;
    }
    view.displayError("Unknown or unimplemented command");
    return false;
  }
}