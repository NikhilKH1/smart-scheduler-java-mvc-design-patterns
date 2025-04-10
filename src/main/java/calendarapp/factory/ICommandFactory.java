package calendarapp.factory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public interface ICommandFactory {

  // ---------- CREATE EVENT COMMANDS ----------
  String buildCreateEventCommand(String name,
                                 LocalDate date,
                                 LocalTime start,
                                 LocalTime end,
                                 boolean isRecurring,
                                 String repeatingDays,
                                 Integer repeatCount,
                                 LocalDate repeatUntil,
                                 String description,
                                 String location,
                                 ZoneId zoneId);

  String createEventCommand(EventInput input);

  // ---------- CALENDAR COMMANDS ----------
  String createCalendarCommand(String name, ZoneId timezone);

  String useCalendarCommand(String calendarName);


  String exportCalendarCommand(String filePath);



  // ---------- EVENT EDITING ----------
  String createEditCommand(EditInput input);

  String createEditRecurringEventCommand(EditInput input);



  String printEventsBetweenCommand(ZonedDateTime start, ZonedDateTime end);

}
