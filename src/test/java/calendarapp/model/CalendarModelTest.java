//package calendarapp.model;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import java.time.LocalDateTime;
//
//import static org.junit.Assert.assertFalse;
//import static org.junit.Assert.assertTrue;
//
//public class CalendarModelTest {
//  private CalendarModel model;
//
//  @Before
//  public void setUp() {
//    model = new CalendarModel();
//  }
//
//  @Test
//  public void testAddSingleEventNoConflict() {
//    LocalDateTime start = LocalDateTime.of(2025, 3, 10, 9, 0);
//    LocalDateTime end = LocalDateTime.of(2025, 3, 10, 10, 0);
//    CalendarEvent event = new SingleEvent("Meeting", start, end);
//    boolean result = model.addEvent(event, true);
//    assertTrue("Event should be added successfully when there is no conflict", result);
//  }
//
//  public void testAddSingleEventConflictWithAutoDecline() {
//    LocalDateTime start1 = LocalDateTime.of(2025, 3, 10, 9, 0);
//    LocalDateTime end1 = LocalDateTime.of(2025, 3, 10, 10, 0);
//    CalendarEvent event1 = new SingleEvent("Meeting", start1, end1);
//    boolean result1 = model.addEvent(event1, true);
//    assertTrue("Event should be added successfully", result1);
//
//    LocalDateTime start2 = LocalDateTime.of(2025, 3, 10, 9, 0);
//    LocalDateTime end2 = LocalDateTime.of(2025, 3, 10, 10, 0);
//    CalendarEvent event2 = new SingleEvent("Meeting", start2, end2);
//    boolean result2 = model.addEvent(event2, true);
//    assertFalse("Event should not be added successfully as there is conflict and autoDecline is true", result2);
//  }
//
//  @Test
//  public void testAddSingleEventConflictNoAutoDecline() {
//    LocalDateTime start1 = LocalDateTime.of(2025, 3, 10, 9, 0);
//    LocalDateTime end1 = LocalDateTime.of(2025, 3, 10, 10, 0);
//    CalendarEvent event1 = new SingleEvent("Meeting", start1, end1);
//    boolean result1 = model.addEvent(event1, true);
//    assertTrue("Event should be added successfully", result1);
//
//    LocalDateTime start2 = LocalDateTime.of(2025, 3, 10, 9, 0);
//    LocalDateTime end2 = LocalDateTime.of(2025, 3, 10, 10, 0);
//    CalendarEvent event2 = new SingleEvent("Meeting", start2, end2);
//    boolean result2 = model.addEvent(event2, true);
//    assertFalse("Event should not be added successfully", result2);
//  }
//
//  @Test
//  public void testEndDateBeforeStartDate() {
//    LocalDateTime start = LocalDateTime.of(2025, 3, 10, 9, 0);
//    LocalDateTime end = LocalDateTime.of(2025, 3, 10, 8, 0);
//    CalendarEvent event = new SingleEvent("Meeting", start, end);
//    boolean result = model.addEvent(event, true);
//    assertTrue("Event end date should be after start date", result);
//  }
//
//
//}
