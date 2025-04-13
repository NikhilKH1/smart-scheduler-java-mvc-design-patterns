import calendarapp.model.event.ReadOnlyCalendarEvent;
import calendarapp.view.ICalendarView;

import org.junit.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * The ICalendar View Junit Test class.
 */
public class ICalendarViewTest {

  static class DummyView implements ICalendarView {
    public boolean displayEventsCalled = false;
    public boolean displayMessageCalled = false;
    public boolean displayErrorCalled = false;
    public boolean runCalled = false;
    public boolean inputSet = false;
    public boolean outputSet = false;

    @Override
    public void displayEvents(List<ReadOnlyCalendarEvent> events) {
      displayEventsCalled = true;
    }

    @Override
    public void displayMessage(String message) {
      displayMessageCalled = true;
    }

    @Override
    public void displayError(String errorMessage) {
      displayErrorCalled = true;
    }

    @Override
    public void run() {
      runCalled = true;
    }

    @Override
    public void setInput(Readable in) {
      inputSet = true;
    }

    @Override
    public void setOutput(Appendable out) {
      outputSet = true;
    }
  }

  @Test
  public void testInterfaceMethods() {
    DummyView view = new DummyView();
    view.displayEvents(List.of());
    view.displayMessage("hi");
    view.displayError("err");
    view.run();
    view.setInput(new StringReader(""));
    view.setOutput(new StringWriter());

    assertTrue(view.displayEventsCalled);
    assertTrue(view.displayMessageCalled);
    assertTrue(view.displayErrorCalled);
    assertTrue(view.runCalled);
    assertTrue(view.inputSet);
    assertTrue(view.outputSet);
  }

  @Test
  public void testSetInputAndOutputDefaultMethods() {
    ICalendarView view = new ICalendarView() {
      @Override
      public void setInput(Readable in) {
        ICalendarView.super.setInput(in);
      }

      @Override
      public void setOutput(Appendable out) {
        ICalendarView.super.setOutput(out);
      }

      @Override
      public void displayEvents(List<ReadOnlyCalendarEvent> events) {}

      @Override
      public void displayMessage(String message) {}

      @Override
      public void displayError(String errorMessage) {}

      @Override
      public void run() {}
    };

    StringReader reader = new StringReader("hello");
    StringWriter writer = new StringWriter();
    view.setInput(reader);
    view.setOutput(writer);

    assertNotNull(reader);
    assertNotNull(writer);
  }


}
