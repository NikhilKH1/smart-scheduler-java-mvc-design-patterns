package calendarapp.controller;

import calendarapp.model.CalendarModel;
import calendarapp.controller.commands.Command;
import calendarapp.view.ICalendarView;

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
    try {
      Command cmd = parser.parse(commandInput);
      if (cmd == null) {
        view.displayError("Command parsing returned null");
        return false;
      }
      return cmd.execute(model, view);
    } catch (IllegalArgumentException e) {
      view.displayError("Parsing Error: " + e.getMessage());
      return false;
    } catch (Exception e) {
      view.displayError("Execution Error: " + e.getMessage());
      return false;
    }
  }
}
