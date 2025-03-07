import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import calendarapp.model.RecurringEvent;
import calendarapp.model.SingleEvent;

import static org.junit.Assert.*;

public class RecurringEventTest {

  private RecurringEvent recurringEvent;

  @Before
  public void setUp() {
    recurringEvent = new RecurringEvent(
            "Algorithms Class",
            LocalDateTime.of(2025, 3, 10, 10, 0),
            LocalDateTime.of(2025, 3, 10, 11, 0),
            "MWF",
            5,
            null
    );
  }

  @Before
  public void setUp2() {
    // Monday (M), Wednesday (W), Friday (F), repeating 5 times
    recurringEvent = new RecurringEvent(
            "Standup Meeting",
            LocalDateTime.of(2025, 5, 5, 9, 0),
            LocalDateTime.of(2025, 5, 5, 9, 30),
            "MWF",
            5,
            null
    );
  }

  @Test
  public void testGenerateOccurrencesUntilDate() {
    recurringEvent = new RecurringEvent(
            "Data Structures Class",
            LocalDateTime.of(2025, 4, 1, 9, 0),
            LocalDateTime.of(2025, 4, 1, 10, 0),
            "TR",
            0,
            LocalDateTime.of(2025, 4, 10, 23, 59) // Clearly corrected time
    );

    List<SingleEvent> occurrences = recurringEvent.generateOccurrences();
    assertEquals("Expected exactly 4 occurrences", 4, occurrences.size());

    assertEquals(LocalDateTime.of(2025, 4, 1, 9, 0), occurrences.get(0).getStartDateTime()); // Tuesday
    assertEquals(LocalDateTime.of(2025, 4, 3, 9, 0), occurrences.get(1).getStartDateTime()); // Thursday
    assertEquals(LocalDateTime.of(2025, 4, 8, 9, 0), occurrences.get(2).getStartDateTime()); // Next Tuesday
    assertEquals(LocalDateTime.of(2025, 4, 10, 9, 0), occurrences.get(3).getStartDateTime()); // Next Thursday
  }


  @Test
  public void testNoOccurrencesGenerated() {
    recurringEvent = new RecurringEvent(
            "Empty Class",
            LocalDateTime.of(2025, 5, 1, 9, 0),
            LocalDateTime.of(2025, 5, 1, 10, 0),
            "",
            5,
            null
    );

    List<SingleEvent> occurrences = recurringEvent.generateOccurrences();
    assertTrue("Expected no occurrences to be generated", occurrences.isEmpty());
  }

  @Test
  public void testOccurrencesRespectWeekdays() {
    recurringEvent = new RecurringEvent(
            "Weekly Review",
            LocalDateTime.of(2025, 6, 2, 14, 0),
            LocalDateTime.of(2025, 6, 2, 15, 0),
            "F",
            2,
            null
    );

    List<SingleEvent> occurrences = recurringEvent.generateOccurrences();
    assertEquals("Expected exactly 2 occurrences", 2, occurrences.size());

    assertEquals(LocalDateTime.of(2025, 6, 6, 14, 0), occurrences.get(0).getStartDateTime()); // First Friday
    assertEquals(LocalDateTime.of(2025, 6, 13, 14, 0), occurrences.get(1).getStartDateTime()); // Next Friday
  }

  @Test
  public void testGenerateOccurrencesFixedCount() {
    List<SingleEvent> occurrences = recurringEvent.generateOccurrences();
    assertEquals("Should generate 5 occurrences", 5, occurrences.size());

    // Check the first occurrence date/time
    assertEquals(LocalDateTime.of(2025, 5, 5, 9, 0), occurrences.get(0).getStartDateTime());
  }

  @Test
  public void testGenerateOccurrencesUntilDate2() {
    // Modify recurringEvent to have repeatUntil
    recurringEvent = new RecurringEvent(
            "Yoga Class",
            LocalDateTime.of(2025, 6, 1, 7, 0),
            LocalDateTime.of(2025, 6, 1, 8, 0),
            "TR", // Tuesday, Thursday
            0,
            LocalDateTime.of(2025, 6, 15, 23, 59)
    );
    List<SingleEvent> occurrences = recurringEvent.generateOccurrences();
    // Check that we have multiple occurrences up until mid-June
    assertFalse("Should generate some occurrences", occurrences.isEmpty());
    // Optionally verify last occurrence is on or before 2025-06-15
    LocalDateTime lastOccurrence = occurrences.get(occurrences.size() - 1).getStartDateTime();
    assertTrue("Last occurrence is before or on 2025-06-15", !lastOccurrence.isAfter(LocalDateTime.of(2025, 6, 15, 23, 59)));
  }

  @Test
  public void testNoWeekdaysSpecified() {
    // If weekdays is empty, no occurrences should be generated
    recurringEvent = new RecurringEvent(
            "Empty Series",
            LocalDateTime.of(2025, 7, 1, 10, 0),
            LocalDateTime.of(2025, 7, 1, 11, 0),
            "",
            5,
            null
    );
    List<SingleEvent> occurrences = recurringEvent.generateOccurrences();
    assertTrue("No weekdays -> no occurrences", occurrences.isEmpty());
  }
}
