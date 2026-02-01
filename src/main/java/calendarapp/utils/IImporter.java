package calendarapp.utils;

import calendarapp.model.ICalendarModel;

import java.io.IOException;

/**
 * Interface for importing calendar data from an external file into a calendar model.
 */
public interface IImporter {

  /**
   * Imports data from the specified file path into the given calendar model.
   *
   * @param model    the calendar model to import data into
   * @param filePath the path to the file containing the data to import
   * @throws IOException if an error occurs while reading from the file
   */
  void importInto(ICalendarModel model, String filePath) throws IOException;
}
