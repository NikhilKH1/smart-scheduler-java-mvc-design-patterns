package calendarapp.controller;

import calendarapp.view.ICalendarView;

/**
 * The ICalendarController interface defines the method required to process a single command input.
 */
public interface ICalendarController {

  /**
   * Processes a command input.
   *
   * @param commandInput the command text to process
   * @return true if the command was processed successfully; false otherwise
   */
  public boolean processCommand(String commandInput);


  /**
   * Runs the application, starting it with the given arguments.
   * This method is typically used to initiate the program and decide which view and operation mode
   * to use based on the arguments passed.
   *
   * @param args the command-line arguments used to configure the application's mode of operation
   */
  public void run(String[] args);

  /**
   * Sets the view to be used by the controller.
   * This method allows the controller to interact with the specified view to display information
   * or handle user interactions.
   *
   * @param view the view used for displaying information to the user
   */
  public void setView(ICalendarView view);

  void run(Readable in, Appendable out);
}
