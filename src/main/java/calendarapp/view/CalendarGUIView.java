package calendarapp.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.*;
import java.util.*;
import java.util.List;
import calendarapp.model.event.ReadOnlyCalendarEvent;
import calendarapp.factory.EditInput;
import calendarapp.factory.ICommandFactory;
import calendarapp.factory.EventInput;
import calendarapp.controller.CalendarController;
import calendarapp.model.ICalendarManager;

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
  private List<ReadOnlyCalendarEvent> events;
  private List<ReadOnlyCalendarEvent> lastRenderedEvents = new ArrayList<>();

  public CalendarGUIView(ICalendarManager manager, CalendarController controller) {
    this.calendarManager = manager;
    this.controller = controller;

    initializeData();
    initializeComponents();
    initializeLayout();       // Then set up layout using those components
    registerListeners();
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
    // Clear and set up basic grid (headers, blank cells)
    calendarPanel.removeAll();
    calendarPanel.setLayout(new GridLayout(0, 7));

    Color calendarColor = calendarColors.getOrDefault(selectedCalendar, Color.LIGHT_GRAY);
    calendarNameLabel.setText(selectedCalendar);
    monthLabel.setText(currentMonth.getMonth() + " " + currentMonth.getYear());
    monthLabel.setBackground(calendarColor);
    monthLabel.setForeground(Color.WHITE);

    String[] days = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
    for (String day : days) {
      JLabel label = new JLabel(day, SwingConstants.CENTER);
      label.setFont(new Font("Arial", Font.BOLD, 14));
      calendarPanel.add(label);
    }

    LocalDate firstDay = currentMonth.atDay(1);
    int startDayOfWeek = firstDay.getDayOfWeek().getValue();
    // Adjust if week starts on Sunday
    if (startDayOfWeek == 7) {
      startDayOfWeek = 0;
    }
    for (int i = 0; i < startDayOfWeek; i++) {
      calendarPanel.add(new JLabel(""));
    }

    // Get events for the whole month directly from your model
    ZoneId zone = ZoneId.of(calendarTimezones.getOrDefault(selectedCalendar, ZoneId.systemDefault().toString()));
    ZonedDateTime startZDT = currentMonth.atDay(1).atStartOfDay(zone);
    ZonedDateTime endZDT = currentMonth.atEndOfMonth().atTime(23, 59).atZone(zone);

    // Directly fetch events from the model (or calendarManager)
    String command = commandFactory.printEventsBetweenCommand(startZDT, endZDT);
    controller.processCommand(command);

    // Rebuild the entire grid with events pre-populated using displayEvents()
//    displayEvents(events);

    if (lastRenderedEvents.isEmpty()) {
      displayEvents(Collections.emptyList());  // This renders just the blank calendar with days
    }

  }




  /**
   * @param events the list of calendar events to display
   */
  // Step 2: calendarEvents will be populated inside displayEvents().
  // We'll capture those and render them in that method now.
  @Override
  public void displayEvents(List<ReadOnlyCalendarEvent> events) {
    this.lastRenderedEvents = events;

    Map<LocalDate, List<ReadOnlyCalendarEvent>> dayEventMap = new HashMap<>();

    for (ReadOnlyCalendarEvent e : events) {
      if (e.isRecurring()) {
        LocalDate startDate = e.getStartDateTime().toLocalDate();
        Set<DayOfWeek> repeatDays = parseWeekdays(e.getWeekdays());
        if (repeatDays == null || repeatDays.isEmpty()) continue; // safety check

        LocalDate endDate;

        if (e.RepeatUntil() != null) {
          endDate = e.RepeatUntil().toLocalDate();
        } else if (e.getRepeatCount() != null) {
          // Estimate: repeat count × 7 days (worst case)
          endDate = startDate.plusDays(e.getRepeatCount() * 7L);
        } else {
          endDate = currentMonth.atEndOfMonth(); // fallback
        }

        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
          if (repeatDays.contains(d.getDayOfWeek())) {
            dayEventMap.computeIfAbsent(d, key -> new ArrayList<>()).add(e);
          }
        }

      } else {
        LocalDate date = e.getStartDateTime().toLocalDate();
        dayEventMap.computeIfAbsent(date, key -> new ArrayList<>()).add(e);
      }
    }

    // Now update the calendar panel visually (unchanged from before)
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
    int startDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue();
    if (startDayOfWeek == 7) startDayOfWeek = 0;

    for (int i = 0; i < startDayOfWeek; i++) {
      calendarPanel.add(new JLabel(""));
    }

    for (int i = 1; i <= currentMonth.lengthOfMonth(); i++) {
      LocalDate date = currentMonth.atDay(i);
      List<ReadOnlyCalendarEvent> dayEvents = dayEventMap.getOrDefault(date, new ArrayList<>());

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
    final JDialog dialog = new JDialog(frame,
            "Events on " + date + " (" +
                    calendarTimezones.getOrDefault(selectedCalendar, ZoneId.systemDefault().toString()) + ")", true);

    DefaultListModel<String> listModel = new DefaultListModel<>();
    JList<String> eventList = new JList<>(listModel);
    List<ReadOnlyCalendarEvent> dayEvents = new ArrayList<>();

    Runnable refreshDayEvents = () -> {
      dayEvents.clear();
      listModel.clear();

      ZoneId zone = ZoneId.of(calendarTimezones.getOrDefault(selectedCalendar, ZoneId.systemDefault().toString()));
      ZonedDateTime refreshStart = currentMonth.atDay(1).atStartOfDay(zone);
      ZonedDateTime refreshEnd = currentMonth.atEndOfMonth().atTime(23, 59).atZone(zone);

      String refreshCmd = commandFactory.printEventsBetweenCommand(refreshStart, refreshEnd);
      controller.processCommand(refreshCmd);
      updateCalendarView();

      if (lastRenderedEvents != null) {
        for (ReadOnlyCalendarEvent e : lastRenderedEvents) {
          if (e.getStartDateTime().toLocalDate().equals(date)) {
            dayEvents.add(e);
            String start = e.getStartDateTime().toLocalTime().toString();
            String end = e.getEndDateTime().toLocalTime().toString();
            listModel.addElement(e.getSubject() + " (" + start + " - " + end + ")");
          }
        }
      }
    };

    refreshDayEvents.run();

    eventList.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          int index = eventList.getSelectedIndex();
          if (index >= 0 && index < dayEvents.size()) {
            ReadOnlyCalendarEvent selected = dayEvents.get(index);
            showEventDetailsPopup(selected);
          }
        }
      }
    });

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

    add.addActionListener(e -> {
      JDialog addDialog = new JDialog(frame, "Create Event on " + date, true);
      JPanel inputPanel = new JPanel(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(5, 5, 5, 5);
      gbc.fill = GridBagConstraints.HORIZONTAL;

      JTextField nameField = new JTextField(15);
      JTextField startField = new JTextField("09:00", 15);
      JTextField endField = new JTextField("10:00", 15);
      JTextField descField = new JTextField(15);
      JTextField locField = new JTextField(15);

      JRadioButton singleButton = new JRadioButton("Single", true);
      JRadioButton recurringButton = new JRadioButton("Recurring");
      ButtonGroup group = new ButtonGroup();
      group.add(singleButton);
      group.add(recurringButton);

      JPanel recurringPanel = new JPanel(new GridLayout(0, 3));
      JCheckBox[] dayChecks = {
              new JCheckBox("Monday"), new JCheckBox("Tuesday"), new JCheckBox("Wednesday"),
              new JCheckBox("Thursday"), new JCheckBox("Friday"), new JCheckBox("Saturday"), new JCheckBox("Sunday")
      };
      JTextField repeatUntilField = new JTextField();
      JTextField repeatCountField = new JTextField();
      for (JCheckBox cb : dayChecks) recurringPanel.add(cb);
      recurringPanel.add(new JLabel("Repeat Until (yyyy-MM-dd):"));
      recurringPanel.add(repeatUntilField);
      recurringPanel.add(new JLabel("Repeat Times:"));
      recurringPanel.add(repeatCountField);
      recurringPanel.setVisible(false);

      singleButton.addActionListener(a -> {
        recurringPanel.setVisible(false);
        addDialog.pack();
      });
      recurringButton.addActionListener(a -> {
        recurringPanel.setVisible(true);
        addDialog.pack();
      });

      int row = 0;
      gbc.gridx = 0; gbc.gridy = row; inputPanel.add(new JLabel("Title:"), gbc);
      gbc.gridx = 1; gbc.gridy = row++; inputPanel.add(nameField, gbc);

      gbc.gridx = 0; gbc.gridy = row; inputPanel.add(new JLabel("Start Time (HH:mm):"), gbc);
      gbc.gridx = 1; gbc.gridy = row++; inputPanel.add(startField, gbc);

      gbc.gridx = 0; gbc.gridy = row; inputPanel.add(new JLabel("End Time (HH:mm):"), gbc);
      gbc.gridx = 1; gbc.gridy = row++; inputPanel.add(endField, gbc);

      gbc.gridx = 0; gbc.gridy = row; inputPanel.add(new JLabel("Description:"), gbc);
      gbc.gridx = 1; gbc.gridy = row++; inputPanel.add(descField, gbc);

      gbc.gridx = 0; gbc.gridy = row; inputPanel.add(new JLabel("Location:"), gbc);
      gbc.gridx = 1; gbc.gridy = row++; inputPanel.add(locField, gbc);

      gbc.gridx = 0; gbc.gridy = row; inputPanel.add(singleButton, gbc);
      gbc.gridx = 1; gbc.gridy = row++; inputPanel.add(recurringButton, gbc);

      gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
      inputPanel.add(recurringPanel, gbc);

      JPanel buttonsPanel = new JPanel();
      JButton okButton = new JButton("OK");
      JButton cancelButton = new JButton("Cancel");
      buttonsPanel.add(okButton);
      buttonsPanel.add(cancelButton);

      okButton.addActionListener(ev -> {
        try {
          String name = nameField.getText().trim();
          LocalTime startTime = LocalTime.parse(startField.getText().trim());
          LocalTime endTime = LocalTime.parse(endField.getText().trim());
          String desc = descField.getText().trim();
          String loc = locField.getText().trim();

          ZoneId zone = ZoneId.of(calendarTimezones.getOrDefault(selectedCalendar, ZoneId.systemDefault().toString()));
          ZonedDateTime startZDT = date.atTime(startTime).atZone(zone);
          ZonedDateTime endZDT = date.atTime(endTime).atZone(zone);

          EventInput input = new EventInput();
          input.setSubject(name);
          input.setStart(startZDT);
          input.setEnd(endZDT);
          input.setDescription(desc);
          input.setLocation(loc);

          if (recurringButton.isSelected()) {
            Set<DayOfWeek> repeatingDays = new HashSet<>();
            for (JCheckBox box : dayChecks) {
              if (box.isSelected()) {
                repeatingDays.add(DayOfWeek.valueOf(box.getText().toUpperCase()));
              }
            }
            input.setRepeatingDays(repeatingDays);

            if (!repeatUntilField.getText().trim().isEmpty()) {
              input.setRepeatUntil(LocalDate.parse(repeatUntilField.getText().trim()).atStartOfDay(zone));
            }
            if (!repeatCountField.getText().trim().isEmpty()) {
              input.setRepeatTimes(Integer.parseInt(repeatCountField.getText().trim()));
            }
          }

          String cmd = commandFactory.createEventCommand(input);
          if (controller.processCommand(cmd)) {
            addDialog.dispose();
            refreshDayEvents.run(); // 🔥 now updates list in same open dialog
          } else {
            JOptionPane.showMessageDialog(addDialog, "Failed to create event", "Error", JOptionPane.ERROR_MESSAGE);
          }

        } catch (Exception ex) {
          JOptionPane.showMessageDialog(addDialog, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
      });

      cancelButton.addActionListener(ev -> addDialog.dispose());

      addDialog.getContentPane().setLayout(new BorderLayout());
      addDialog.getContentPane().add(inputPanel, BorderLayout.CENTER);
      addDialog.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
      addDialog.pack();
      addDialog.setResizable(false);
      addDialog.setLocationRelativeTo(frame);
      addDialog.setVisible(true);
    });

    edit.addActionListener(e -> {
      int selectedIndex = eventList.getSelectedIndex();
      if (selectedIndex == -1) {
        JOptionPane.showMessageDialog(dialog, "Please select an event to edit.");
        return;
      }

      ReadOnlyCalendarEvent selectedEvent = dayEvents.get(selectedIndex);

      JTextField nameField = new JTextField(selectedEvent.getSubject());
      JTextField startField = new JTextField(selectedEvent.getStartDateTime().toLocalTime().toString());
      JTextField endField = new JTextField(selectedEvent.getEndDateTime().toLocalTime().toString());
      JTextField descField = new JTextField(selectedEvent.getDescription());
      JTextField locField = new JTextField(selectedEvent.getLocation());

      JPanel editPanel = new JPanel(new GridLayout(0, 1));
      editPanel.add(new JLabel("Name:"));
      editPanel.add(nameField);
      editPanel.add(new JLabel("Start Time (HH:mm):"));
      editPanel.add(startField);
      editPanel.add(new JLabel("End Time (HH:mm):"));
      editPanel.add(endField);
      editPanel.add(new JLabel("Description:"));
      editPanel.add(descField);
      editPanel.add(new JLabel("Location:"));
      editPanel.add(locField);

      int result = JOptionPane.showConfirmDialog(dialog, editPanel, "Edit Event",
              JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

      if (result == JOptionPane.OK_OPTION) {
        String newName = nameField.getText().trim();
        LocalTime newStart = LocalTime.parse(startField.getText().trim());
        LocalTime newEnd = LocalTime.parse(endField.getText().trim());
        String newDesc = descField.getText().trim();
        String newLoc = locField.getText().trim();

        ZonedDateTime fromStart = selectedEvent.getStartDateTime();
        ZonedDateTime fromEnd = selectedEvent.getEndDateTime();

        boolean anyFailed = false;

        if (!newName.equals(selectedEvent.getSubject())) {
          String cmd = commandFactory.createEditCommand(new EditInput("name", selectedEvent.getSubject(), fromStart, fromEnd, newName, false));
          if (!controller.processCommand(cmd)) anyFailed = true;
        }

        if (!newStart.equals(fromStart.toLocalTime())) {
          String newStartStr = fromStart.toLocalDate().atTime(newStart).toString();
          String cmd = commandFactory.createEditCommand(new EditInput("startdatetime", selectedEvent.getSubject(), fromStart, fromEnd, newStartStr, false));
          if (!controller.processCommand(cmd)) anyFailed = true;
        }

        if (!newEnd.equals(fromEnd.toLocalTime())) {
          String newEndStr = fromEnd.toLocalDate().atTime(newEnd).toString();
          String cmd = commandFactory.createEditCommand(new EditInput("enddatetime", selectedEvent.getSubject(), fromStart, fromEnd, newEndStr, false));
          if (!controller.processCommand(cmd)) anyFailed = true;
        }

        if (!newDesc.equals(selectedEvent.getDescription())) {
          String cmd = commandFactory.createEditCommand(new EditInput("description", selectedEvent.getSubject(), fromStart, fromEnd, newDesc, false));
          if (!controller.processCommand(cmd)) anyFailed = true;
        }

        if (!newLoc.equals(selectedEvent.getLocation())) {
          String cmd = commandFactory.createEditCommand(new EditInput("location", selectedEvent.getSubject(), fromStart, fromEnd, newLoc, false));
          if (!controller.processCommand(cmd)) anyFailed = true;
        }

        controller.processCommand("print events on " + date);

        if (anyFailed) {
          JOptionPane.showMessageDialog(dialog, "Failed to edit event", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
          dialog.dispose(); // ✅ Close dialog on success
        }
      }
    });


    editAll.addActionListener(e -> {
      int selectedIndex = eventList.getSelectedIndex();
      if (selectedIndex == -1) {
        JOptionPane.showMessageDialog(dialog, "Please select an event to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
        return;
      }

      ReadOnlyCalendarEvent selectedEvent = dayEvents.get(selectedIndex);
      JPanel inputPanel = new JPanel(new GridLayout(0, 2));

      JTextField nameField = new JTextField(selectedEvent.getSubject());
      JTextField startField = new JTextField(selectedEvent.getStartDateTime().toLocalTime().toString());
      JTextField endField = new JTextField(selectedEvent.getEndDateTime().toLocalTime().toString());
      JTextField descField = new JTextField(selectedEvent.getDescription());
      JTextField locField = new JTextField(selectedEvent.getLocation());

      inputPanel.add(new JLabel("New Title:"));
      inputPanel.add(nameField);
      inputPanel.add(new JLabel("New Start Time (HH:mm):"));
      inputPanel.add(startField);
      inputPanel.add(new JLabel("New End Time (HH:mm):"));
      inputPanel.add(endField);
      inputPanel.add(new JLabel("New Description:"));
      inputPanel.add(descField);
      inputPanel.add(new JLabel("New Location:"));
      inputPanel.add(locField);

      int result = JOptionPane.showConfirmDialog(dialog, inputPanel, "Edit All Recurring Events", JOptionPane.OK_CANCEL_OPTION);

      if (result == JOptionPane.OK_OPTION) {
        try {
          String newName = nameField.getText().trim();
          LocalTime newStartTime = LocalTime.parse(startField.getText().trim());
          LocalTime newEndTime = LocalTime.parse(endField.getText().trim());
          String newDesc = descField.getText().trim();
          String newLoc = locField.getText().trim();

          ZonedDateTime oldStart = selectedEvent.getStartDateTime();
          ZonedDateTime oldEnd = selectedEvent.getEndDateTime();

          // Prepare EditInput
          EditInput input = new EditInput();
          input.setRecurring(true);
          input.setEventName(selectedEvent.getSubject());
          input.setFromStart(oldStart);
          input.setFromEnd(oldEnd);

          boolean anyChanged = false;

          // Generate and execute individual property change commands
          if (!newName.equals(selectedEvent.getSubject())) {
            input.setProperty("name");
            input.setNewValue(newName);
            controller.processCommand(commandFactory.createEditRecurringEventCommand(input));
            anyChanged = true;
          }

          if (!newStartTime.equals(oldStart.toLocalTime())) {
            input.setProperty("startdatetime");
            input.setNewValue(oldStart.toLocalDate().atTime(newStartTime).toString());
            controller.processCommand(commandFactory.createEditRecurringEventCommand(input));
            anyChanged = true;
          }

          if (!newEndTime.equals(oldEnd.toLocalTime())) {
            input.setProperty("enddatetime");
            input.setNewValue(oldEnd.toLocalDate().atTime(newEndTime).toString());
            controller.processCommand(commandFactory.createEditRecurringEventCommand(input));
            anyChanged = true;
          }

          if (!newDesc.equals(selectedEvent.getDescription())) {
            input.setProperty("description");
            input.setNewValue(newDesc);
            controller.processCommand(commandFactory.createEditRecurringEventCommand(input));
            anyChanged = true;
          }

          if (!newLoc.equals(selectedEvent.getLocation())) {
            input.setProperty("location");
            input.setNewValue(newLoc);
            controller.processCommand(commandFactory.createEditRecurringEventCommand(input));
            anyChanged = true;
          }

          controller.processCommand("print events on " + date);

          if (anyChanged) {
            dialog.dispose();
          } else {
            JOptionPane.showMessageDialog(dialog, "No changes made.", "Info", JOptionPane.INFORMATION_MESSAGE);
          }

        } catch (Exception ex) {
          JOptionPane.showMessageDialog(dialog, "Invalid input format: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    });


    dialog.getContentPane().add(panel);
    dialog.setSize(520, 400);
    dialog.setLocationRelativeTo(frame);
    dialog.setVisible(true);
  }

  private Set<DayOfWeek> parseWeekdays(Object value) {
    Set<DayOfWeek> days = new HashSet<>();
    if (value == null) return days;

    String str = value.toString().toUpperCase().replaceAll("\\s", "");
    String[] parts = str.split("[,|;]");

    for (String part : parts) {
      try {
        // Try full names: MONDAY, TUESDAY, ...
        days.add(DayOfWeek.valueOf(part));
      } catch (IllegalArgumentException e) {
        // Try short codes: M, T, W, Th, F, Sa, Su
        switch (part) {
          case "M": days.add(DayOfWeek.MONDAY); break;
          case "T": days.add(DayOfWeek.TUESDAY); break;
          case "W": days.add(DayOfWeek.WEDNESDAY); break;
          case "TH": days.add(DayOfWeek.THURSDAY); break;
          case "F": days.add(DayOfWeek.FRIDAY); break;
          case "SA": days.add(DayOfWeek.SATURDAY); break;
          case "SU": days.add(DayOfWeek.SUNDAY); break;
          default: /* skip invalid */ break;
        }
      }
    }

    return days;
  }


  private void showEventDetailsPopup(ReadOnlyCalendarEvent event) {
    StringBuilder sb = new StringBuilder("<html>");
    sb.append("<h2>").append(event.getSubject()).append("</h2>");
    sb.append("<p><b>Start:</b> ").append(event.getStartDateTime()).append("</p>");
    sb.append("<p><b>End:</b> ").append(event.getEndDateTime()).append("</p>");
    sb.append("<p><b>Description:</b> ").append(event.getDescription() == null || event.getDescription().isEmpty() ? "N/A" : event.getDescription()).append("</p>");
    sb.append("<p><b>Location:</b> ").append(event.getLocation() == null || event.getLocation().isEmpty() ? "N/A" : event.getLocation()).append("</p>");

    if (event.isRecurring()) {
      sb.append("<p><b>Recurring:</b> Yes</p>");
      sb.append("<p><b>Repeats on:</b> ").append(event.getWeekdays() == null ? "N/A" : event.getWeekdays()).append("</p>");
      if (event.RepeatUntil() != null) {
        sb.append("<p><b>Repeat Until:</b> ").append(event.RepeatUntil()).append("</p>");
      } else if (event.getRepeatCount() != null) {
        sb.append("<p><b>Repeat Count:</b> ").append(event.getRepeatCount()).append("</p>");
      }
    } else {
      sb.append("<p><b>Recurring:</b> No</p>");
    }

    sb.append("</html>");

    JLabel label = new JLabel(sb.toString());
    label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    JOptionPane.showMessageDialog(frame, label, "Event Details", JOptionPane.INFORMATION_MESSAGE);
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
      String header = reader.readLine(); // Skip header
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
        String desc = parts[5].trim();
        String loc = parts[6].trim();

        // Skip multi-day events for now
        if (!startDate.equals(endDate)) continue;

        ZoneId zone = ZoneId.of(calendarTimezones.getOrDefault(selectedCalendar, ZoneId.systemDefault().toString()));
        ZonedDateTime start = startDate.atTime(startTime).atZone(zone);
        ZonedDateTime end = endDate.atTime(endTime).atZone(zone);

        EventInput input = new EventInput();
        input.setSubject(name);
        input.setStart(start);
        input.setEnd(end);
        input.setDescription(desc);
        input.setLocation(loc);

        String cmd = commandFactory.createEventCommand(input);
        if (controller.processCommand(cmd)) {
          addedCount++;
        }
      }

      controller.processCommand("print events from " + currentMonth.atDay(1) + " to " + currentMonth.atEndOfMonth());
      JOptionPane.showMessageDialog(frame, "Imported " + addedCount + " events into '" + selectedCalendar + "'.", "Import Success", JOptionPane.INFORMATION_MESSAGE);

    } catch (Exception ex) {
      JOptionPane.showMessageDialog(frame, "Error importing calendar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  public void initialize() {
    updateCalendarView();
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


  public void setController(CalendarController controller) {
    this.controller = controller;
  }

  public void setCommandFactory(ICommandFactory factory) {
    this.commandFactory = factory;
  }




}

