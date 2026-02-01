package calendarapp.factory;

import java.time.ZonedDateTime;

/**
 * Represents the input for creating or editing an event in the calendar system.
 * This class encapsulates the details of an event, such as the event's subject,
 * start and end times, repeating days, repeat count, description, location, and
 * whether the event is recurring.
 */
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

  /**
   * Constructs an empty EventInput object with recurring set to false by default.
   */
  public EventInput() {
    this.recurring = false;
  }

  /**
   * Sets the subject (title) of the event.
   *
   * @param subject the subject/title of the event
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }

  /**
   * Sets the start date and time of the event.
   *
   * @param start the start date and time of the event
   */
  public void setStart(ZonedDateTime start) {
    this.start = start;
  }

  /**
   * Sets the end date and time of the event.
   *
   * @param end the end date and time of the event
   */
  public void setEnd(ZonedDateTime end) {
    this.end = end;
  }

  /**
   * Sets the days of the week for a recurring event (e.g., "Mon,Wed,Fri").
   *
   * @param daysString a string representing the repeating days of the week
   */
  public void setRepeatingDays(String daysString) {
    this.repeatingDays = daysString;
  }

  /**
   * Sets the number of times the event should repeat.
   *
   * @param repeatTimes the number of times to repeat the event
   */
  public void setRepeatTimes(Integer repeatTimes) {
    this.repeatTimes = repeatTimes;
  }

  /**
   * Sets the end date for a recurring event.
   *
   * @param repeatUntil the date when the event repeats until
   */
  public void setRepeatUntil(ZonedDateTime repeatUntil) {
    this.repeatUntil = repeatUntil;
  }

  /**
   * Sets the description of the event.
   *
   * @param description a detailed description of the event
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Sets the location of the event.
   *
   * @param location the location where the event will take place
   */
  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * Sets whether the event is recurring.
   *
   * @param recurring true if the event is recurring, false otherwise
   */
  public void setRecurring(boolean recurring) {
    this.recurring = recurring;
  }

  /**
   * Gets the subject (title) of the event.
   *
   * @return the subject/title of the event
   */
  public String getSubject() {
    return subject;
  }

  /**
   * Gets the start date and time of the event.
   *
   * @return the start date and time of the event
   */
  public ZonedDateTime getStart() {
    return start;
  }

  /**
   * Gets the end date and time of the event.
   *
   * @return the end date and time of the event
   */
  public ZonedDateTime getEnd() {
    return end;
  }

  /**
   * Gets the repeating days of the week for recurring events.
   *
   * @return a string representing the repeating days (e.g., "Mon,Wed,Fri")
   */
  public String getRepeatingDays() {
    return repeatingDays;
  }

  /**
   * Gets the number of times the event will repeat.
   *
   * @return the number of times the event should repeat
   */
  public Integer getRepeatTimes() {
    return repeatTimes;
  }

  /**
   * Gets the end date for recurring events.
   *
   * @return the date when the event repeats until
   */
  public ZonedDateTime getRepeatUntil() {
    return repeatUntil;
  }

  /**
   * Gets the description of the event.
   *
   * @return the description of the event
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets the location of the event.
   *
   * @return the location where the event will take place
   */
  public String getLocation() {
    return location;
  }

  /**
   * Checks if the event is recurring.
   *
   * @return true if the event is recurring, false otherwise
   */
  public boolean isRecurring() {
    return recurring;
  }
}
