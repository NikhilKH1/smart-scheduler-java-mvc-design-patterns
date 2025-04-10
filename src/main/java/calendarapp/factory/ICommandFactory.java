package calendarapp.factory;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

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

  String editCalendarCommand(String name, String property, String newValue);

  String exportCalendarCommand(String filePath);

  String editCalendarNameCommand(String oldName, String newName);

  String editCalendarTimezoneCommand(String calendarName, ZoneId newZone);


  // ---------- EVENT EDITING ----------
  String createEditCommand(EditInput input);

  String createEditRecurringEventCommand(EditInput input);


  // ---------- EVENT COPYING ----------
  String copyEventCommand(String eventName, ZonedDateTime originalTime, String targetCalendar, ZonedDateTime targetTime);

  String copyEventsOnDateCommand(LocalDate date, String targetCalendar, LocalDate targetDate);

  String copyEventsBetweenDatesCommand(LocalDate start, LocalDate end, String targetCalendar, LocalDate targetStart);


  // ---------- QUERY COMMANDS ----------
  String printEventsOnCommand(LocalDate date);

  String printEventsBetweenCommand(ZonedDateTime start, ZonedDateTime end);

  String showStatusCommand(ZonedDateTime datetime);
}
