package calendarapp.controller;

import calendarapp.model.CalendarModel;
import calendarapp.model.commands.*;
import calendarapp.model.event.CalendarEvent;
import calendarapp.model.commands.CreateEventCommand;
import calendarapp.model.event.RecurringEvent;
import calendarapp.model.event.SingleEvent;
import calendarapp.view.ICalendarView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class CalendarController implements ICalendarController {
  private final CalendarModel model;
  private final ICalendarView view;
  private final CommandParser parser;
  private final Map<Class<? extends Command>, Function<Command, Boolean>> commandHandlers;

  public CalendarController(CalendarModel model, ICalendarView view) {
    this(model, view, new CommandParser(model));
  }

  public CalendarController(CalendarModel model, ICalendarView view, CommandParser parser) {
    this.model = model;
    this.view = view;
    this.parser = parser;
    commandHandlers = new HashMap<>();
    commandHandlers.put(CreateEventCommand.class, c -> processCreateEvent((CreateEventCommand) c));
    commandHandlers.put(QueryByDateCommand.class, c -> processQueryByDate((QueryByDateCommand) c));
    commandHandlers.put(QueryRangeDateTimeCommand.class, c -> processQueryRange((QueryRangeDateTimeCommand) c));
    commandHandlers.put(BusyQueryCommand.class, c -> processBusyQuery((BusyQueryCommand) c));
    commandHandlers.put(EditEventCommand.class, c -> processEditEvent((EditEventCommand) c));
    commandHandlers.put(EditRecurringEventCommand.class, c -> processEditRecurringEvent((EditRecurringEventCommand) c));
    commandHandlers.put(ExportCalendarCommand.class, c -> processExportCommand((ExportCalendarCommand) c));
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
    if (cmd == null) {
      view.displayError("Command is null");
      return false;
    }

    Function<Command, Boolean> handler = commandHandlers.get(cmd.getClass());
    if (handler != null) {
      return handler.apply(cmd);
    } else {
      view.displayError("Unknown or unimplemented command");
      return false;
    }
  }


  private boolean processCreateEvent(CreateEventCommand createCmd) {
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

  private boolean processQueryByDate(QueryByDateCommand queryCmd) {
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

  private boolean processQueryRange(QueryRangeDateTimeCommand queryCmd) {
    List<CalendarEvent> events = model.getEventsBetween(queryCmd.getStartDateTime(), queryCmd.getEndDateTime());
    if (events.isEmpty()) {
      view.displayMessage("No events found from " + queryCmd.getStartDateTime() + " to " + queryCmd.getEndDateTime());
    } else {
      view.displayMessage("Events from " + queryCmd.getStartDateTime() + " to " + queryCmd.getEndDateTime() + ":");
      view.displayEvents(events);
    }
    return true;
  }

  private boolean processBusyQuery(BusyQueryCommand busyCmd) {
    LocalDateTime queryTime = busyCmd.getQueryTime();
    boolean busy = model.isBusyAt(queryTime);
    if (busy) {
      view.displayMessage("Busy at " + queryTime);
    } else {
      view.displayMessage("Available at " + queryTime);
    }
    return true;
  }

  private boolean processEditEvent(EditEventCommand editCmd) {
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

  private boolean processEditRecurringEvent(EditRecurringEventCommand recCmd) {
    boolean success = model.editRecurringEvent(recCmd.getEventName(), recCmd.getProperty(), recCmd.getNewValue());
    if (success) {
      view.displayMessage("Recurring event modified successfully.");
    } else {
      view.displayError("Failed to modify recurring event.");
    }
    return success;
  }

  private boolean processExportCommand(ExportCalendarCommand exportCmd) {
    exportCmd.execute();
    return true;
  }
}
