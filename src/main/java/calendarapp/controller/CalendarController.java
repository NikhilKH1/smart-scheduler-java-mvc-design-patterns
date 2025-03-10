package calendarapp.controller;

import calendarapp.model.CalendarModel;
import calendarapp.model.event.CalendarEvent;
import calendarapp.model.event.RecurringEvent;
import calendarapp.model.event.SingleEvent;
import calendarapp.model.commands.BusyQueryCommand;
import calendarapp.model.commands.Command;
import calendarapp.model.commands.CreateEventCommand;
import calendarapp.model.commands.EditEventCommand;
import calendarapp.model.commands.EditRecurringEventCommand;
import calendarapp.model.commands.QueryByDateCommand;
import calendarapp.model.commands.QueryRangeDateTimeCommand;
import calendarapp.view.ICalendarView;
import calendarapp.utils.ModelHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class CalendarController implements ICalendarController {
  private final CalendarModel model;
  private final ICalendarView view;
  private final CommandParser parser;

  public CalendarController(CalendarModel model, ICalendarView view) {
    this(model, view, new CommandParser());
  }

  public CalendarController(CalendarModel model, ICalendarView view, CommandParser parser) {
    this.model = model;
    this.view = view;
    this.parser = parser;
  }

  @Override
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
                  createCmd.getRepeatUntil(),
                  createCmd.getDescription(),
                  createCmd.getLocation(),
                  createCmd.isPublic(),
                  createCmd.isAllDay()
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
                  createCmd.isAllDay(),
                  null
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
    else if (cmd instanceof EditRecurringEventCommand) {
      EditRecurringEventCommand recCmd = (EditRecurringEventCommand) cmd;
      boolean success = model.editRecurringEvent(recCmd.getEventName(), recCmd.getProperty(), recCmd.getNewValue());
      if (success) {
        view.displayMessage("Recurring event modified successfully.");
      } else {
        view.displayError("Failed to modify recurring event.");
      }
      return success;
    }
    else {
      view.displayError("Unknown or unimplemented command");
      return false;
    }
  }
}
