package calendarapp.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import calendarapp.factory.DefaultCommandFactory;
import calendarapp.factory.EditInput;
import calendarapp.factory.ICommandFactory;
import calendarapp.factory.EventInput;
import calendarapp.controller.CalendarController;
import calendarapp.controller.CommandParser;
import calendarapp.model.CalendarManager;
import calendarapp.model.ICalendarManager;
import calendarapp.model.event.ICalendarEvent;

public class CalendarGUIView implements ICalendarView {

  private final ICalendarManager calendarManager;
  private CalendarController controller;
  private ICommandFactory commandFactory;

  private JFrame frame;
  private JPanel calendarPanel;
  private JLabel monthLabel;
  private JComboBox<String> calendarDropdown, viewDropdown;
  private JButton addCalendarButton, prevMonthButton, nextMonthButton, exportButton;
  private Map<String, Color> calendarColors;
  private Map<String, String> calendarTimezones;
  private YearMonth currentMonth;
  private String selectedCalendar;
  private JLabel calendarNameLabel;
  private JButton importButton;
  private List<ICalendarEvent> events;
  private List<ICalendarEvent> lastRenderedEvents = new ArrayList<>();

  public CalendarGUIView(ICalendarManager manager, CalendarController controller) {
    this.calendarManager = manager;
    this.controller = controller;

    initializeData();
    initializeComponents();
    initializeLayout();       // Then set up layout using those components
    registerListeners();
    updateCalendarView();
    frame.setVisible(true);

  }


  private void initializeData() {
    currentMonth = YearMonth.now();
    calendarColors = new HashMap<>();
    calendarTimezones = new HashMap<>();

    selectedCalendar = "Default";
    addInitialCalendar("Default", ZoneId.systemDefault());

    if (calendarManager != null) {
      calendarManager.useCalendar("Default");
    }

    // ✅ Ensure dropdown is updated to show "Work" selected
    if (calendarDropdown != null) {
      calendarDropdown.setSelectedItem("Work");
    }
  }

  private void addInitialCalendar(String name, ZoneId zoneId) {
    Color color = name.equals("Default") ? new Color(70, 130, 180) : new Color((int)(Math.random() * 0x1000000));

    calendarColors.put(name, color);
    calendarTimezones.put(name, zoneId.toString());

    if (calendarManager != null) {
      calendarManager.addCalendar(name, zoneId);
    }
  }


  private void initializeComponents() {
    frame = new JFrame("Calendar Application");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(1000, 700);
    frame.setLayout(new BorderLayout());

    monthLabel = new JLabel("", SwingConstants.CENTER);
    monthLabel.setOpaque(true);
    monthLabel.setFont(new Font("Arial", Font.BOLD, 22));
    monthLabel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

    calendarNameLabel = new JLabel(selectedCalendar, SwingConstants.CENTER);
    calendarNameLabel.setFont(new Font("Arial", Font.BOLD, 18));
    calendarNameLabel.setOpaque(true);
    calendarNameLabel.setBackground(calendarColors.getOrDefault(selectedCalendar, Color.LIGHT_GRAY));
    calendarNameLabel.setForeground(Color.WHITE);
    calendarNameLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

    calendarDropdown = new JComboBox<>(calendarColors.keySet().toArray(new String[0]));
    calendarDropdown.setSelectedItem("Default");
    viewDropdown = new JComboBox<>(new String[]{"Monthly"});

    calendarPanel = new JPanel();

    addCalendarButton = new JButton("+");
    prevMonthButton = new JButton("<");
    nextMonthButton = new JButton(">");

    exportButton = new JButton("Export");
    importButton = new JButton("Import");

  }


  private void initializeLayout() {
    JPanel topPanel = new JPanel(new BorderLayout());

    JPanel leftPanel = new JPanel();
    leftPanel.add(addCalendarButton);
    leftPanel.add(calendarDropdown);

    JPanel centerPanel = new JPanel(new BorderLayout());
    JPanel navPanel = new JPanel();
    navPanel.add(prevMonthButton);
    navPanel.add(monthLabel);
    navPanel.add(nextMonthButton);

    centerPanel.add(navPanel, BorderLayout.CENTER);

    JPanel rightPanel = new JPanel();
    rightPanel.add(viewDropdown);
    rightPanel.add(exportButton);
    rightPanel.add(importButton);

    topPanel.add(calendarNameLabel, BorderLayout.NORTH);
    topPanel.add(leftPanel, BorderLayout.WEST);
    topPanel.add(centerPanel, BorderLayout.CENTER);
    topPanel.add(rightPanel, BorderLayout.EAST);

    frame.add(topPanel, BorderLayout.NORTH);
    frame.add(calendarPanel, BorderLayout.CENTER);
  }

  private void registerListeners() {
    calendarDropdown.addActionListener(e -> {
      selectedCalendar = (String) calendarDropdown.getSelectedItem();

      if (controller != null) {
        String command = commandFactory.useCalendarCommand(selectedCalendar);
        boolean success = controller.processCommand(command);
        if (!success) {
          JOptionPane.showMessageDialog(frame,
                  "Failed to switch to calendar '" + selectedCalendar + "'.",
                  "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }
      }

      calendarNameLabel.setText(selectedCalendar);
      calendarNameLabel.setBackground(calendarColors.getOrDefault(selectedCalendar, Color.LIGHT_GRAY));
      updateCalendarView();
    });

    exportButton.addActionListener(e -> exportCalendarToCSV());
    importButton.addActionListener(e -> importCalendarFromCSV());
    addCalendarButton.addActionListener(e -> addNewCalendar());

    prevMonthButton.addActionListener(e -> {
      currentMonth = currentMonth.minusMonths(1);
      updateCalendarView();
    });

    nextMonthButton.addActionListener(e -> {
      currentMonth = currentMonth.plusMonths(1);
      updateCalendarView();
    });
  }


  private void updateCalendarView() {
    calendarPanel.removeAll();
    calendarPanel.setLayout(new GridLayout(0, 7));

    Color calendarColor = calendarColors.getOrDefault(selectedCalendar, Color.LIGHT_GRAY);
    calendarNameLabel.setText(selectedCalendar); // Update calendar label
    monthLabel.setText(currentMonth.getMonth() + " " + currentMonth.getYear());
    monthLabel.setBackground(calendarColor);
    monthLabel.setForeground(Color.WHITE);

    // 🌞 Day-of-week labels
    String[] days = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
    for (String day : days) {
      JLabel label = new JLabel(day, SwingConstants.CENTER);
      label.setFont(new Font("Arial", Font.BOLD, 14));
      calendarPanel.add(label);
    }

    LocalDate firstDay = currentMonth.atDay(1);
    int startDayOfWeek = firstDay.getDayOfWeek().getValue() % 7;
    for (int i = 0; i < startDayOfWeek; i++) {
      calendarPanel.add(new JLabel(""));
    }

    // Step 1: Ask backend for events this month
    LocalDate monthStart = currentMonth.atDay(1);
    LocalDate monthEnd = currentMonth.atEndOfMonth();
    String command = "print events from " + monthStart + " to " + monthEnd;
    controller.processCommand(command); // triggers displayEvents(...)
  }

  /**
   * @param events the list of calendar events to display
   */
    // Step 2: calendarEvents will be populated inside displayEvents().
    // We'll capture those and render them in that method now.
    @Override
    public void displayEvents(List<ICalendarEvent> events) {
      this.lastRenderedEvents = events;
      // Step 1: Group events by date (render-only)
      Map<LocalDate, List<ICalendarEvent>> dayEventMap = new HashMap<>();
      for (ICalendarEvent e : events) {
        LocalDate date = e.getStartDateTime().toLocalDate();
        dayEventMap.computeIfAbsent(date, d -> new ArrayList<>()).add(e);
      }

      // Step 2: Refresh calendar view with those events
      calendarPanel.removeAll();
      calendarPanel.setLayout(new GridLayout(0, 7));

      Color calendarColorMain = calendarColors.getOrDefault(selectedCalendar, Color.LIGHT_GRAY);
      calendarNameLabel.setText(selectedCalendar);
      monthLabel.setText(currentMonth.getMonth() + " " + currentMonth.getYear());
      monthLabel.setBackground(calendarColorMain);
      monthLabel.setForeground(Color.WHITE);

      String[] daysOfWeek = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
      for (String day : daysOfWeek) {
        JLabel label = new JLabel(day, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        calendarPanel.add(label);
      }

      LocalDate firstDayOfMonth = currentMonth.atDay(1);
      int startDayOfTheWeek = firstDayOfMonth.getDayOfWeek().getValue() % 7;
      for (int i = 0; i < startDayOfTheWeek; i++) {
        calendarPanel.add(new JLabel(""));
      }

      for (int i = 1; i <= currentMonth.lengthOfMonth(); i++) {
        LocalDate date = currentMonth.atDay(i);
        List<ICalendarEvent> dayEvents = dayEventMap.getOrDefault(date, new ArrayList<>());

        StringBuilder sb = new StringBuilder("<html><b>").append(i).append("</b>");
        if (!dayEvents.isEmpty()) {
          sb.append("<br><font size='2'>");
          for (int j = 0; j < Math.min(dayEvents.size(), 2); j++) {
            sb.append("• ").append(dayEvents.get(j).getSubject()).append("<br>");
          }
          if (dayEvents.size() > 2) sb.append("...");
          sb.append("</font>");
        }
        sb.append("</html>");

        JButton dayButton = new JButton(sb.toString());
        dayButton.setToolTipText("Click to view events on " + date);
        dayButton.addActionListener(e -> showEventDialog(date));
        calendarPanel.add(dayButton);
      }

      calendarPanel.revalidate();
      calendarPanel.repaint();
    }



  private void addNewCalendar() {
    JPanel inputPanel = new JPanel(new GridLayout(0, 1));
    JTextField calendarNameField = new JTextField();
    String[] timezones = ZoneId.getAvailableZoneIds().stream().sorted().toArray(String[]::new);
    JComboBox<String> timezoneDropdown = new JComboBox<>(timezones);

    inputPanel.add(new JLabel("Calendar Name:"));
    inputPanel.add(calendarNameField);
    inputPanel.add(new JLabel("Select Timezone:"));
    inputPanel.add(timezoneDropdown);

    int result = JOptionPane.showConfirmDialog(frame, inputPanel, "Create New Calendar", JOptionPane.OK_CANCEL_OPTION);

    if (result == JOptionPane.OK_OPTION) {
      String name = calendarNameField.getText().trim();
      String timezone = (String) timezoneDropdown.getSelectedItem();

      if (name.isEmpty()) {
        JOptionPane.showMessageDialog(frame, "Calendar name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      if (calendarColors.containsKey(name)) {
        JOptionPane.showMessageDialog(frame, "Calendar already exists.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      calendarColors.put(name, new Color((int)(Math.random() * 0x1000000)));
      calendarTimezones.put(name, timezone);
      calendarEvents.put(name, new HashMap<>());
      calendarDropdown.addItem(name);

      String command = commandFactory.createCalendarCommand(name, ZoneId.of(timezone));
      boolean success = controller.processCommand(command);
      if (calendarManager != null) {
        calendarManager.useCalendar(name);
      }

      if (!success) {
        JOptionPane.showMessageDialog(frame, "Failed to create calendar via command.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }


      calendarDropdown.setSelectedItem(name);
      JOptionPane.showMessageDialog(frame, "Calendar '" + name + "' created successfully in timezone " + timezone + ".");
    }
  }

  private void showEventDialog(LocalDate date) {
    // Step 1: Ask backend for events on this date
    String command = "print events on " + date;
    controller.processCommand(command);

    // We'll collect the result through displayEvents — capture a temp list for rendering
    List<ICalendarEvent> dayEvents = new ArrayList<>();
    if (lastRenderedEvents != null) {
      for (ICalendarEvent e : lastRenderedEvents) {
        if (e.getStartDateTime().toLocalDate().equals(date)) {
          dayEvents.add(e);
        }
      }
    }

    final JDialog dialog = new JDialog(frame,
            "Events on " + date + " (" +
                    calendarTimezones.getOrDefault(selectedCalendar, ZoneId.systemDefault().toString()) + ")", true);

    DefaultListModel<String> listModel = new DefaultListModel<>();
    for (ICalendarEvent event : dayEvents) {
      String start = event.getStartDateTime().toLocalTime().toString();
      String end = event.getEndDateTime().toLocalTime().toString();
      listModel.addElement(event.getSubject() + " (" + start + " - " + end + ")");
    }

    JList<String> eventList = new JList<>(listModel);
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(new JScrollPane(eventList), BorderLayout.CENTER);

    JPanel controls = new JPanel();
    JButton add = new JButton("Add Event");
    JButton edit = new JButton("Edit Selected");
    JButton editAll = new JButton("Edit All Matching");
    controls.add(add);
    controls.add(edit);
    controls.add(editAll);
    panel.add(controls, BorderLayout.SOUTH);

    // TODO: Hook up add/edit/editAll logic as in your original version,
    // replacing calendarEvents with `dayEvents` and command-based execution.

    dialog.getContentPane().add(panel);
    dialog.setSize(520, 400);
    dialog.setLocationRelativeTo(frame);
    dialog.setVisible(true);
  }


  private void exportCalendarToCSV() {
    JTextField filenameField = new JTextField();
    JPanel panel = new JPanel(new GridLayout(0, 1));
    panel.add(new JLabel("Enter filename (without extension):"));
    panel.add(filenameField);

    int result = JOptionPane.showConfirmDialog(
            frame,
            panel,
            "Export Cal",
            JOptionPane.OK_CANCEL_OPTION
    );

    if (result == JOptionPane.OK_OPTION) {
      String baseName = filenameField.getText().trim();
      if (baseName.isEmpty()) {
        JOptionPane.showMessageDialog(frame, "Filename cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setSelectedFile(new File(baseName + ".csv"));

      int userSelection = fileChooser.showSaveDialog(frame);
      if (userSelection == JFileChooser.APPROVE_OPTION) {
        File fileToSave = fileChooser.getSelectedFile();

        // Ensure .csv extension
        String filename = fileToSave.getAbsolutePath();
        if (!filename.endsWith(".csv")) {
          filename += ".csv";
          fileToSave = new File(filename);
        }

        // ✅ Use command factory
        String command = commandFactory.exportCalendarCommand(fileToSave.getName());
        System.out.println("EXPORT COMMAND: " + command); // Optional: debug

        boolean success = controller.processCommand(command);
        if (success) {
          JOptionPane.showMessageDialog(frame, "Calendar exported to: " + fileToSave.getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
          JOptionPane.showMessageDialog(frame, "Failed to export calendar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    }
  }


  private void importCalendarFromCSV() {
    JFileChooser fileChooser = new JFileChooser();
    int result = fileChooser.showOpenDialog(frame);
    if (result != JFileChooser.APPROVE_OPTION) return;

    File file = fileChooser.getSelectedFile();
    if (file == null || !file.getName().endsWith(".csv")) {
      JOptionPane.showMessageDialog(frame, "Please select a valid .csv file.", "Invalid File", JOptionPane.ERROR_MESSAGE);
      return;
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String header = reader.readLine(); // skip header
      int addedCount = 0;
      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split(",", -1);
        if (parts.length < 10) continue;

        String name = parts[0].replace("\"", "").trim();
        LocalDate startDate = LocalDate.parse(parts[1].trim());
        LocalTime startTime = LocalTime.parse(parts[2].trim());
        LocalDate endDate = LocalDate.parse(parts[3].trim());
        LocalTime endTime = LocalTime.parse(parts[4].trim());

        // Only support same-day events for now
        if (!startDate.equals(endDate)) continue;

        Map<LocalDate, List<Event>> selectedMap = calendarEvents.get(selectedCalendar);
        List<Event> eventsOnDate = selectedMap.getOrDefault(startDate, new ArrayList<>());

        boolean conflict = false;
        for (Event e : eventsOnDate) {
          if (!(endTime.isBefore(e.startTime) || startTime.isAfter(e.endTime))) {
            conflict = true;
            break;
          }
        }

        if (!conflict) {
          Event newEvent = new Event(name, startTime, endTime);
          eventsOnDate.add(newEvent);
          selectedMap.put(startDate, eventsOnDate);
          addedCount++;
        }
      }

      updateCalendarView();
      JOptionPane.showMessageDialog(frame, "Imported " + addedCount + " events into '" + selectedCalendar + "'.", "Import Success", JOptionPane.INFORMATION_MESSAGE);

    } catch (Exception ex) {
      JOptionPane.showMessageDialog(frame, "Error importing calendar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  @Override
  public void displayEvents(List<ICalendarEvent> events) {
    this.events = events;
    Map<LocalDate, List<ICalendarEvent>> dayEventMap = new HashMap<>();
    for (ICalendarEvent e : events) {
      LocalDate date = e.getStartDateTime().toLocalDate();
      dayEventMap.computeIfAbsent(date, d -> new ArrayList<>()).add(e);
    }

    calendarPanel.removeAll();
    calendarPanel.setLayout(new GridLayout(0, 7));

    Color calendarColor = calendarColors.getOrDefault(selectedCalendar, Color.LIGHT_GRAY);
    calendarNameLabel.setText(selectedCalendar);
    monthLabel.setText(currentMonth.getMonth() + " " + currentMonth.getYear());
    monthLabel.setBackground(calendarColor);
    monthLabel.setForeground(Color.WHITE);

    String[] daysOfWeek = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
    for (String day : daysOfWeek) {
      JLabel label = new JLabel(day, SwingConstants.CENTER);
      label.setFont(new Font("Arial", Font.BOLD, 14));
      calendarPanel.add(label);
    }

    LocalDate firstDayOfMonth = currentMonth.atDay(1);
    int startDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() % 7;
    for (int i = 0; i < startDayOfWeek; i++) {
      calendarPanel.add(new JLabel(""));
    }

    for (int i = 1; i <= currentMonth.lengthOfMonth(); i++) {
      LocalDate date = currentMonth.atDay(i);
      List<ICalendarEvent> dayEvents = dayEventMap.getOrDefault(date, new ArrayList<>());

      StringBuilder sb = new StringBuilder("<html><b>").append(i).append("</b>");
      if (!dayEvents.isEmpty()) {
        sb.append("<br><font size='2'>");
        for (int j = 0; j < Math.min(dayEvents.size(), 2); j++) {
          sb.append("• ").append(dayEvents.get(j).getSubject()).append("<br>");
        }
        if (dayEvents.size() > 2) sb.append("...");
        sb.append("</font>");
      }
      sb.append("</html>");

      JButton dayButton = new JButton(sb.toString());
      dayButton.setToolTipText("Click to view events on " + date);
      dayButton.addActionListener(e -> showEventDialog(date));
      calendarPanel.add(dayButton);
    }

    calendarPanel.revalidate();
    calendarPanel.repaint();
  }


  @Override
  public void displayMessage(String message) {
    JOptionPane.showMessageDialog(frame, message);
  }

  @Override
  public void displayError(String errorMessage) {
    JOptionPane.showMessageDialog(frame, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
  }


  static class Event {
    String name;
    LocalTime startTime;
    LocalTime endTime;
    boolean isRecurring;

    Event(String name, LocalTime startTime, LocalTime endTime) {
      this(name, startTime, endTime, false);
    }

    Event(String name, LocalTime startTime, LocalTime endTime, boolean isRecurring) {
      this.name = name;
      this.startTime = startTime;
      this.endTime = endTime;
      this.isRecurring = isRecurring;
    }
  }

  public static void main(String[] args) {
    ICalendarManager Manager = new CalendarManager();
    CalendarGUIView guiView = new CalendarGUIView(Manager, null); // initially no controller
    CommandParser parser = new CommandParser(Manager);
    CalendarController controller = new CalendarController(Manager, guiView, parser);

    guiView.setController(controller);
    guiView.setCommandFactory(new DefaultCommandFactory());
  }


  public void setController(CalendarController controller) {
    this.controller = controller;
  }

  public void setCommandFactory(ICommandFactory factory) {
    this.commandFactory = factory;
  }


}
