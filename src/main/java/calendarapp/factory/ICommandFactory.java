package calendarapp.factory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;

public interface ICommandFactory {
  String buildCreateEventCommand(String name,
                                 LocalDate date,
                                 LocalTime start,
                                 LocalTime end,
                                 boolean isRecurring,
                                 Set<DayOfWeek> repeatingDays,
                                 Integer repeatCount,
                                 LocalDate repeatUntil,
                                 String description,
                                 String location,
                                 ZoneId zoneId);

  String createEventCommand(EventInput input);

  String createCalendarCommand(String name, ZoneId timezone);

  String useCalendarCommand(String calendarName);

  String createEditCommand(EditInput input);

  String exportCalendarCommand(String filePath);



}