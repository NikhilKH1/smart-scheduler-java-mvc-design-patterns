package calendarapp.factory;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.Set;

public class EventInput {
  private String subject;
  private ZonedDateTime start;
  private ZonedDateTime end;
  private String repeatingDays;
  private Integer repeatTimes;
  private ZonedDateTime repeatUntil;
  private String description;
  private String location;
  private boolean recurring;

  public EventInput() {
    this.recurring = false;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public void setStart(ZonedDateTime start) {
    this.start = start;
  }

  public void setEnd(ZonedDateTime end) {
    this.end = end;
  }

  public void setRepeatingDays(String daysString) {
    this.repeatingDays = daysString;
  }

  public void setRepeatTimes(Integer repeatTimes) {
    this.repeatTimes = repeatTimes;
  }

  public void setRepeatUntil(ZonedDateTime repeatUntil) {
    this.repeatUntil = repeatUntil;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public void setRecurring(boolean recurring) {
    this.recurring = recurring;
  }

  // ✅ Getters (used by CommandFactory)
  public String getSubject() {
    return subject;
  }

  public ZonedDateTime getStart() {
    return start;
  }

  public ZonedDateTime getEnd() {
    return end;
  }

  public String getRepeatingDays() {
    return repeatingDays;
  }

  public Integer getRepeatTimes() {
    return repeatTimes;
  }

  public ZonedDateTime getRepeatUntil() {
    return repeatUntil;
  }

  public String getDescription() {
    return description;
  }

  public String getLocation() {
    return location;
  }

  public boolean isRecurring() {
    return recurring;
  }
}
