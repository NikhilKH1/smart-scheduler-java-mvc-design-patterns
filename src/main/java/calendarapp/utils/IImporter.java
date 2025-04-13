package calendarapp.utils;

import calendarapp.model.ICalendarModel;

import java.io.IOException;

public interface IImporter {
  void importInto(ICalendarModel model, String filePath) throws IOException;
}
