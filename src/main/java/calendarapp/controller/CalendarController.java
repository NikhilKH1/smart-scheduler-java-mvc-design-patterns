package calendarapp.controller;

import calendarapp.model.CalendarModel;
import calendarapp.model.commands.BusyQueryCommand;
import calendarapp.model.commands.Command;
import calendarapp.model.commands.EditEventCommand;
import calendarapp.model.commands.EditRecurringEventCommand;
import calendarapp.model.commands.ExportCalendarCommand;
import calendarapp.model.commands.QueryByDateCommand;
import calendarapp.model.commands.QueryRangeDateTimeCommand;
import calendarapp.model.commands.CreateEventCommand;
import calendarapp.model.event.CalendarEvent;
import calendarapp.model.event.RecurringEvent;
import calendarapp.model.event.SingleEvent;
import calendarapp.view.ICalendarView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * This class processes user commands by parsing input strings, interacting with the calendar model,
 * and updating the calendar view with the results.
 * It supports commands for creating events, querying events by date or range, checking busy status,
 * editing events (both single and recurring), and exporting calendar data.
 */
public class CalendarController implements ICalendarController {
  private final CalendarModel model;
  private final ICalendarView view;
  private final CommandParser parser;
  private final Map<Class<? extends Command>, Function<Command, Boolean>> commandHandlers;

  /**
   * Constructs a CalendarController with the specified calendar model and view.
   * A default CommandParser is used.
   *
   * @param model the calendar model holding event data
   * @param view  the view used to display messages and events
   */
  public CalendarController(CalendarModel model, ICalendarView view) {
    this(model, view, new CommandParser(model));
  }

  /**
   * Constructs a CalendarController with the specified calendar model, view, and command parser.
   *
   * @param model  the calendar model holding event data
   * @param view   the view used to display messages and events
   * @param parser the command parser used to interpret user input commands
   */
  public CalendarController(CalendarModel model, ICalendarView view, CommandParser parser) {
    this.model = model;
    this.view = view;
    this.parser = parser;
    commandHandlers = new HashMap<>();
    commandHandlers.put(CreateEventCommand.class, command ->
            processCreateEvent((CreateEventCommand) command));
    commandHandlers.put(QueryByDateCommand.class, command ->
            processQueryByDate((QueryByDateCommand) command));
    commandHandlers.put(QueryRangeDateTimeCommand.class, command ->
            processQueryRange((QueryRangeDateTimeCommand) command));
    commandHandlers.put(BusyQueryCommand.class, command ->
            processBusyQuery((BusyQueryCommand) command));
    commandHandlers.put(EditEventCommand.class, command ->
            processEditEvent((EditEventCommand) command));
    commandHandlers.put(EditRecurringEventCommand.class, command ->
            processEditRecurringEvent((EditRecurringEventCommand) command));
    commandHandlers.put(ExportCalendarCommand.class, command ->
            processExportCommand((ExportCalendarCommand) command));
  }



  /**
   * Processes a command provided as a string input. The command is parsed using the command parser,
   * and the appropriate command handler is executed.
   *
   * @param commandInput the command text to process
   * @return true if the command was processed successfully; false otherwise
   */
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

  /**
   * Processes the create event command. Depending on whether the event is recurring, it creates
   * either a recurring event or a single event and attempts to add it to the calendar model.
   *
   * @param createCmd the command containing details for creating an event
   * @return true if the event was created successfully; false otherwise
   */
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

  /**
   * Processes a query-by-date command. It retrieves events on the specified date and displays
   * them in the view.
   *
   * @param queryCmd the command containing the date to query
   * @return true after processing the command
   */
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

  /**
   * Processes a query range command. It retrieves events between the specified start and
   * end date-time and displays them in the view.
   *
   * @param queryCmd the command containing the start and end date-times
   * @return true after processing the command
   */
  private boolean processQueryRange(QueryRangeDateTimeCommand queryCmd) {
    List<CalendarEvent> events =
            model.getEventsBetween(queryCmd.getStartDateTime(), queryCmd.getEndDateTime());
    if (events.isEmpty()) {
      view.displayMessage("No events found from " + queryCmd.getStartDateTime()
              + " to " + queryCmd.getEndDateTime());
    } else {
      view.displayMessage("Events from " + queryCmd.getStartDateTime()
              + " to " + queryCmd.getEndDateTime() + ":");
      view.displayEvents(events);
    }
    return true;
  }

  /**
   * Processes a busy query command. It checks whether the calendar is busy at the given date-time
   * and displays the appropriate message.
   *
   * @param busyCmd the command containing the date-time to check
   * @return true after processing the command
   */
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

  /**
   * Processes an edit event command. Depending on the mode specified (SINGLE, FROM, or ALL),
   * it attempts to edit a single event, events from a specific date-time, or all events matching
   * the event name.
   *
   * @param editCmd the command containing the details for editing an event
   * @return true if the event(s) were edited successfully; false otherwise
   */
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
        success = model.editEventsAll(editCmd.getProperty(),
                editCmd.getEventName(), editCmd.getNewValue());
        break;
      default:
        break;
    }
    if (success) {
      view.displayMessage("Event(s) edited successfully");
    } else {
      view.displayError("Failed to edit event(s)");
    }
    return success;
  }

  /**
   * Processes an edit recurring event command. It updates the recurring event properties.
   *
   * @param recCmd the command containing the details for editing a recurring event
   * @return true if the recurring event was edited successfully; false otherwise
   */
  private boolean processEditRecurringEvent(EditRecurringEventCommand recCmd) {
    boolean success = model.editRecurringEvent(recCmd.getEventName(),
            recCmd.getProperty(), recCmd.getNewValue());
    if (success) {
      view.displayMessage("Recurring event modified successfully.");
    } else {
      view.displayError("Failed to modify recurring event.");
    }
    return success;
  }

  /**
   * Processes an export command by executing the export operation.
   *
   * @param exportCmd the command responsible for exporting the calendar data
   * @return true after executing the export command
   */
  private boolean processExportCommand(ExportCalendarCommand exportCmd) {
    exportCmd.execute();
    return true;
  }
}
