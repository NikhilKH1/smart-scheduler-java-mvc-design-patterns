package calendarapp.controller;

import java.time.LocalDateTime;

public class RecurringResult {
  protected boolean isRecurring = false;
  protected String weekdays = "";
  protected int repeatCount = 0;
  protected LocalDateTime repeatUntil = null;
  protected int index;


  public RecurringResult() {
    this.isRecurring = isRecurring;
    this.weekdays = weekdays;
    this.repeatCount = repeatCount;
    this.repeatUntil = repeatUntil;
    this.index = index;
  }

  public boolean isRecurring() { return isRecurring; }
  public String getWeekdays() { return weekdays; }
  public int getRepeatCount() { return repeatCount; }
  public LocalDateTime getRepeatUntil() { return repeatUntil; }
  public int getIndex() { return index; }
}
