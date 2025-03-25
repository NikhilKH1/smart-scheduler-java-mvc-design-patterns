package calendarapp.utils;

import calendarapp.model.event.ICalendarEvent;

import java.io.IOException;
import java.util.List;

/**
 * Interface for exporting calendar events to various formats.
 */
public interface IExporter {
  /**
   * Exports a list of calendar events to a file at the given path.
   *
   * @param events   the list of calendar events
   * @param filePath the output file path
   * @return the absolute path of the exported file
   * @throws IOException if an error occurs during export
   */
  String export(List<ICalendarEvent> events, String filePath) throws IOException;
}
