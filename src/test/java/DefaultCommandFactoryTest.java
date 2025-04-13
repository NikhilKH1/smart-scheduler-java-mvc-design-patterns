import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import calendarapp.factory.DefaultCommandFactory;
import calendarapp.factory.EditInput;
import calendarapp.factory.EventInput;

/**
 * JUnit test class for DefaultCommandFactory.
 */
public class DefaultCommandFactoryTest {

  private DefaultCommandFactory factory;

  @Before
  public void setup() {
    factory = new DefaultCommandFactory();
  }

  private class DummyEventInput extends EventInput {
    private String subject;
    private ZonedDateTime start;
    private ZonedDateTime end;
    private String description;
    private String location;
    private String repeatingDays;
    private Integer repeatTimes;
    private ZonedDateTime repeatUntil;

    public DummyEventInput(String subject, ZonedDateTime start,
                           ZonedDateTime end,
                           String description, String location,
                           String repeatingDays, Integer repeatTimes, ZonedDateTime
                                   repeatUntil) {
      this.subject = subject;
      this.start = start;
      this.end = end;
      this.description = description;
      this.location = location;
      this.repeatingDays = repeatingDays;
      this.repeatTimes = repeatTimes;
      this.repeatUntil = repeatUntil;
    }

    @Override
    public String getSubject() {
      return subject;
    }

    @Override
    public ZonedDateTime getStart() {
      return start;
    }

    @Override
    public ZonedDateTime getEnd() {
      return end;
    }

    @Override
    public String getDescription() {
      return description;
    }

    @Override
    public String getLocation() {
      return location;
    }

    @Override
    public String getRepeatingDays() {
      return repeatingDays;
    }

    @Override
    public Integer getRepeatTimes() {
      return repeatTimes;
    }

    @Override
    public ZonedDateTime getRepeatUntil() {
      return repeatUntil;
    }

    @Override
    public boolean isRecurring() {
      return repeatingDays != null
              && !repeatingDays.isEmpty();
    }
  }

  private class DummyEditInput extends EditInput {
    private String property;
    private String eventName;
    private ZonedDateTime fromStart;
    private ZonedDateTime fromEnd;
    private String newValue;
    private boolean recurring;

    public DummyEditInput(String property, String eventName, ZonedDateTime fromStart,
                          ZonedDateTime fromEnd,
                          String newValue, boolean recurring) {
      super(property, eventName, fromStart, fromEnd, newValue, recurring);
      this.property = property;
      this.eventName = eventName;
      this.fromStart = fromStart;
      this.fromEnd = fromEnd;
      this.newValue = newValue;
      this.recurring = recurring;
    }

    @Override
    public String getProperty() {
      return property;
    }

    @Override
    public String getEventName() {
      return eventName;
    }

    @Override
    public ZonedDateTime getFromStart() {
      return fromStart;
    }

    @Override
    public ZonedDateTime getFromEnd() {
      return fromEnd;
    }

    @Override
    public String getNewValue() {
      return newValue;
    }

    @Override
    public boolean isRecurring() {
      return recurring;
    }
  }

  @Test
  public void testCreateCalendarCommand() {
    String name = "Work";
    ZoneId zone = ZoneId.of("Asia/Kolkata");
    String command = factory.createCalendarCommand(name, zone);
    assertEquals("create calendar --name \"Work\" --timezone Asia/Kolkata",
            command);
  }

  @Test
  public void testUseCalendarCommand() {
    String command = factory.useCalendarCommand("Personal");
    assertEquals("use calendar --name \"Personal\"", command);
  }

  @Test
  public void testExportCalendarCommand() {
    String filePath = "/tmp/calendar.txt";
    String command = factory.exportCalendarCommand(filePath);
    assertEquals("export cal \"/tmp/calendar.txt\"", command);
  }

  @Test
  public void testCreateEventCommand_Single() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9,
            0, 0, 0, ZoneId.systemDefault());
    ZonedDateTime end = start.plusHours(1);
    DummyEventInput input = new DummyEventInput("Meeting", start, end,
            "Discuss plan", "Room 101", "", null,
            null);
    String command = factory.createEventCommand(input);
    String expected = "create event \"Meeting\" from " + start.toLocalDateTime() +
            " to " + end.toLocalDateTime() +
            " description \"Discuss plan\" location \"Room 101\"";
    assertEquals(expected, command);
  }

  @Test
  public void testCreateEventCommand_Recurring_WithRepeatTimes() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9,
            0, 0, 0, ZoneId.systemDefault());
    ZonedDateTime end = start.plusHours(1);
    DummyEventInput input = new DummyEventInput("Meeting", start, end,
            "Weekly sync", "Room 202", "MTW", 5,
            null);
    String command = factory.createEventCommand(input);
    String expected = "create event \"Meeting\" from " + start.toLocalDateTime() +
            " to " + end.toLocalDateTime() +
            " repeats MTW for 5 times description \"Weekly sync\" location \"Room 202\"";
    assertEquals(expected, command);
  }

  @Test
  public void testCreateEventCommand_Recurring_WithRepeatUntil() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9,
            0, 0, 0, ZoneId.systemDefault());
    ZonedDateTime end = start.plusHours(1);
    ZonedDateTime repeatUntil = start.plusWeeks(4);
    DummyEventInput input = new DummyEventInput("Training", start, end,
            "Monthly Training", "Auditorium", "MTWRFSU",
            null, repeatUntil);
    String command = factory.createEventCommand(input);
    String expected = "create event \"Training\" from " + start.toLocalDateTime() +
            " to " + end.toLocalDateTime() +
            " repeats MTWRFSU until " + repeatUntil.toLocalDateTime() +
            " description \"Monthly Training\" location \"Auditorium\"";
    assertEquals(expected, command);
  }

  @Test
  public void testCreateEditCommand_NonRecurring() {
    ZonedDateTime fromStart = ZonedDateTime.of(2025, 6, 1, 9,
            0, 0, 0, ZoneId.systemDefault());
    ZonedDateTime fromEnd = fromStart.plusHours(1);
    EditInput input = new DummyEditInput("description", "Meeting",
            fromStart, fromEnd, "Updated description", false);
    String command = factory.createEditCommand(input);
    String expected = "edit event description \"Meeting\" from " + fromStart.toLocalDateTime() +
            " to " + fromEnd.toLocalDateTime() + " with \"Updated description\"";
    assertEquals(expected, command);
  }

  @Test
  public void testCreateEditCommand_Recurring_RepeatingDays() {
    ZonedDateTime fromStart = ZonedDateTime.of(2025, 6, 1, 9,
            0, 0, 0, ZoneId.systemDefault());
    ZonedDateTime fromEnd = fromStart.plusHours(1);
    DummyEditInput input = new DummyEditInput("repeatingdays", "Meeting",
            fromStart, fromEnd, "MTWRF", true);
    String command = factory.createEditCommand(input);
    String expected = "edit events repeatingdays \"Meeting\" MTWRF";
    assertEquals(expected, command);
  }

  @Test
  public void testCreateEditCommand_Recurring_RepeatUntil() {
    ZonedDateTime fromStart = ZonedDateTime.of(2025, 6, 1, 9,
            0, 0, 0, ZoneId.systemDefault());
    DummyEditInput input = new DummyEditInput("repeatuntil", "Meeting",
            fromStart, null, "2025-06-30T09:00:00Z", true);
    String command = factory.createEditCommand(input);
    String expected = "edit events repeatuntil \"Meeting\" \"2025-06-30T09:00:00Z\"";
    assertEquals(expected, command);
  }

  @Test
  public void testCreateEditCommand_Recurring_OtherProperty() {
    ZonedDateTime fromStart = ZonedDateTime.of(2025, 6, 1, 9,
            0, 0, 0, ZoneId.systemDefault());
    DummyEditInput input = new DummyEditInput("location", "Meeting",
            fromStart, null, "Room 404", true);
    String command = factory.createEditCommand(input);
    String expected = "edit events location \"Meeting\" from " + fromStart.toLocalDateTime() +
            " with \"Room 404\"";
    assertEquals(expected, command);
  }

  @Test
  public void testPrintEventsBetweenCommand() {
    ZonedDateTime start = ZonedDateTime.of(2025, 6, 1, 9,
            0, 0, 0, ZoneId.systemDefault());
    ZonedDateTime end = start.plusHours(2);
    String command = factory.printEventsBetweenCommand(start, end);
    String expected = "print events from " + start.toLocalDateTime()
            + " to " + end.toLocalDateTime();
    assertEquals(expected, command);
  }

  @Test
  public void testCreateEditRecurringEventCommand_RepeatingDays() {
    ZonedDateTime fromStart = ZonedDateTime.of(2025, 6, 1,
            9, 0, 0, 0, ZoneId.systemDefault());
    ZonedDateTime fromEnd = fromStart.plusHours(1);
    DummyEditInput input = new DummyEditInput("repeatingdays",
            "Meeting", fromStart, fromEnd, "MTWRF", true);
    String command = factory.createEditRecurringEventCommand(input);
    String expected = "edit events repeatingdays \"Meeting\" MTWRF";
    assertEquals(expected, command);
  }

  @Test
  public void testCreateEditRecurringEventCommand_RepeatTimes() {
    ZonedDateTime fromStart = ZonedDateTime.of(2025, 6, 1,
            9, 0, 0, 0, ZoneId.systemDefault());
    ZonedDateTime fromEnd = fromStart.plusHours(1);
    DummyEditInput input = new DummyEditInput("repeattimes", "Meeting",
            fromStart, fromEnd, "10", true);
    String command = factory.createEditRecurringEventCommand(input);
    String expected = "edit events repeattimes \"Meeting\" \"10\"";
    assertEquals(expected, command);
  }

  @Test
  public void testCreateEditRecurringEventCommand_RepeatUntil() {
    ZonedDateTime fromStart = ZonedDateTime.of(2025, 6, 1, 9,
            0, 0, 0, ZoneId.systemDefault());
    ZonedDateTime fromEnd = fromStart.plusHours(1);
    DummyEditInput input = new DummyEditInput("repeatuntil", "Meeting",
            fromStart, fromEnd, "2025-06-30", true);
    String command = factory.createEditRecurringEventCommand(input);
    String expected = "edit events repeatuntil \"Meeting\" \"2025-06-30\"";
    assertEquals(expected, command);
  }

  @Test
  public void testEditCalendarTimezoneCommand_validTimezone() {
    String result = factory.editCalendarTimezoneCommand("Work",
            ZoneId.of("America/New_York"));
    assertEquals("edit calendar --name \"Work\" --property "
            + "timezone America/New_York", result);
  }

  @Test
  public void testEditCalendarTimezoneCommand_withSpacesInName() {
    String result = factory.editCalendarTimezoneCommand("My Calendar",
            ZoneId.of("Europe/London"));
    assertEquals("edit calendar --name \"My Calendar\" --property "
            + "timezone Europe/London", result);
  }

  @Test
  public void testEditCalendarTimezoneCommand_withDefaultTimezone() {
    ZoneId defaultZone = ZoneId.systemDefault();
    String result = factory.editCalendarTimezoneCommand("Default", defaultZone);
    assertEquals("edit calendar --name \"Default\" --property timezone "
            + defaultZone, result);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarTimezoneCommand_nullName() {
    factory.editCalendarTimezoneCommand(null, ZoneId.of("UTC"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarTimezoneCommand_nullZone() {
    factory.editCalendarTimezoneCommand("TestCal", null);

  }

  @Test
  public void testImportCalendarCommand() {
    String cmd = factory.importCalendarCommand("myfile.csv");
    assertEquals("import cal \"myfile.csv\"", cmd);
  }


  @Test
  public void testCreateEditCommand_SingleEvent_AllProperties() {
    ZonedDateTime fromStart = ZonedDateTime.parse("2025-06-01T10:00:00Z[UTC]");
    ZonedDateTime fromEnd = ZonedDateTime.parse("2025-06-01T11:00:00Z[UTC]");
    EditInput input = new EditInput("description", "Team Meeting",
            fromStart, fromEnd, "Updated Desc", false);
    String expected = "edit event description \"Team Meeting\" from 2025-06-01T10:00 "
            + "to 2025-06-01T11:00 with \"Updated Desc\"";
    assertEquals(expected, factory.createEditCommand(input));
  }

  @Test
  public void testCreateEditCommand_Recurring_RepeatingDaysMain() {
    EditInput input = new EditInput("repeatingdays", "Yoga Class",
            "MWF", true);
    String expected = "edit events repeatingdays \"Yoga Class\" MWF";
    assertEquals(expected, factory.createEditCommand(input));
  }

  @Test
  public void testCreateEditCommand_Recurring_RepeatUntilMain() {
    EditInput input = new EditInput("repeatuntil", "Yoga Class",
            "2025-12-31T10:00", true);
    String expected = "edit events repeatuntil \"Yoga Class\" \"2025-12-31T10:00\"";
    assertEquals(expected, factory.createEditCommand(input));
  }

  @Test
  public void testCreateEditCommand_Recurring_RepeatTimes() {
    EditInput input = new EditInput("repeattimes", "Yoga Class",
            "10", true);
    String expected = "edit events repeattimes \"Yoga Class\" \"10\"";
    assertEquals(expected, factory.createEditCommand(input));
  }

  @Test
  public void testCreateEditCommand_Recurring_OtherPropertyMain() {
    ZonedDateTime from = ZonedDateTime.parse("2025-06-01T09:00:00Z[UTC]");
    EditInput input = new EditInput("location", "Yoga Class", from,
            null, "Studio A", true);
    String expected = "edit events location \"Yoga Class\" from 2025-06-01T09:00 "
            + "with \"Studio A\"";
    assertEquals(expected, factory.createEditCommand(input));
  }

  @Test
  public void testCreateEditRecurringEventCommand_StartDatetime() {
    ZonedDateTime from = ZonedDateTime.parse("2025-07-01T10:00:00Z[UTC]");
    EditInput input = new EditInput("startdatetime", "Yoga Class",
            from, null, "2025-07-05T10:00", true);
    String expected = "edit events startdatetime \"Yoga Class\" from 2025-07-01T10:00 "
            + "with 2025-07-05T10:00";
    assertEquals(expected, factory.createEditRecurringEventCommand(input));
  }

  @Test
  public void testCreateEditRecurringEventCommand_UnsupportedProperty() {
    EditInput input = new EditInput("color", "Yoga Class",
            "blue", true);
    Exception ex = assertThrows(IllegalArgumentException.class,
      () -> factory.createEditRecurringEventCommand(input));
    assertTrue(ex.getMessage().contains("Unsupported recurring property"));
  }


}
