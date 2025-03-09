package calendarapp.controller;

import calendarapp.model.*;
import calendarapp.view.CalendarView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
      try {
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
      } catch (IllegalArgumentException ex) {
        view.displayError(ex.getMessage());
        return false;
      }

      if (success) {
        view.displayMessage("Event created successfully");
      } else {
        view.displayError("Event creation failed due to conflict");
      }
      return success;
    }
    else if (cmd instanceof QueryByDateCommand) {
      QueryByDateCommand queryCmd = (QueryByDateCommand) cmd;
      LocalDate queryDate = queryCmd.getQueryDate();
      List<CalendarEvent> events = model.getEventsOnDate(queryDate);
      if (events.isEmpty()) {
        view.displayMessage("No events found on " + queryDate);
      } else {
        view.displayMessage("Events on " + queryDate + ":");
        view.displayEvents(events);
      }
      return true;
    }
    else if (cmd instanceof QueryRangeDateTimeCommand) {
      QueryRangeDateTimeCommand queryCmd = (QueryRangeDateTimeCommand) cmd;
      List<CalendarEvent> events = model.getEventsBetween(queryCmd.getStartDateTime(), queryCmd.getEndDateTime());
      if (events.isEmpty()) {
        view.displayMessage("No events found from " + queryCmd.getStartDateTime() + " to " + queryCmd.getEndDateTime());
      } else {
        view.displayMessage("Events from " + queryCmd.getStartDateTime() + " to " + queryCmd.getEndDateTime() + ":");
        view.displayEvents(events);
      }
      return true;
    }
    else if (cmd instanceof BusyQueryCommand) {
      BusyQueryCommand busyCmd = (BusyQueryCommand) cmd;
      LocalDateTime queryTime = busyCmd.getQueryTime();
      boolean busy = model.isBusyAt(queryTime);
      if (busy) {
        view.displayMessage("Busy at " + queryTime);
      } else {
        view.displayMessage("Available at " + queryTime);
      }
      return true;
    }
    else if (cmd instanceof EditEventCommand) {
      EditEventCommand editCmd = (EditEventCommand) cmd;
      boolean success = false;
      switch (editCmd.getMode()) {
        case SINGLE:
          success = model.editSingleEvent(editCmd.getProperty(), editCmd.getEventName(),
                  editCmd.getOriginalStart(), editCmd.getOriginalEnd(), editCmd.getNewValue());
          break;
        case FROM:
          success = model.editEventsFrom(editCmd.getProperty(), editCmd.getEventName(),
                  editCmd.getFilterDateTime(), editCmd.getNewValue());
          break;
        case ALL:
          success = model.editEventsAll(editCmd.getProperty(), editCmd.getEventName(), editCmd.getNewValue());
          break;
      }
      if (success) {
        view.displayMessage("Event(s) edited successfully");
      } else {
        view.displayError("Failed to edit event(s)");
      }
      return success;
    }

    view.displayError("Unknown or unimplemented command");
    return false;
  }
}
