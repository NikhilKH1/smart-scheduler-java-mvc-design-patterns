package calendarapp.view;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JDialog;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JRadioButton;
import javax.swing.JFileChooser;
import javax.swing.ButtonGroup;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import calendarapp.controller.ICalendarController;
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
  private boolean suppressCreationMessages = false;
  private List<ReadOnlyCalendarEvent> events;
  private List<ReadOnlyCalendarEvent> lastRenderedEvents = new ArrayList<>();
  private JTextField repeatUntilField = new JTextField();
  private JTextField repeatCountField = new JTextField();

  public CalendarGUIView(ICalendarManager manager, ICalendarController controller) {
    this.calendarManager = manager;
    this.controller = (CalendarController) controller;

    currentMonth = YearMonth.now();
    calendarColors = new HashMap<>();
    calendarTimezones = new HashMap<>();

    selectedCalendar = "Default";
    ZoneId defaultZone = ZoneId.systemDefault();

    if (calendarManager.getCalendar("Default") == null) {
      calendarManager.addCalendar("Default", defaultZone);
    }

    calendarColors.put("Default", new Color(70, 130, 180));
    calendarTimezones.put("Default", defaultZone.toString());

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
      String newSelection = (String) calendarDropdown.getSelectedItem();
      if (newSelection == null || newSelection.trim().isEmpty()) return;
      String command = commandFactory.useCalendarCommand(newSelection);
      boolean success = controller.processCommand(command);
      if (!success) {
        JOptionPane.showMessageDialog(frame,
                "Failed to switch to calendar '" + newSelection + "'.",
                "Error", JOptionPane.ERROR_MESSAGE);

        calendarDropdown.setSelectedItem(selectedCalendar);
        return;
      }
      selectedCalendar = newSelection;
      calendarNameLabel.setText(newSelection);
      calendarNameLabel.setBackground(calendarColors.getOrDefault(newSelection, Color.LIGHT_GRAY));
      updateCalendarView();
    });
    addCalendarButton.addActionListener(e -> addNewCalendar());

    prevMonthButton.addActionListener(e -> {
      currentMonth = currentMonth.minusMonths(1);
      updateCalendarView();
    });

    nextMonthButton.addActionListener(e -> {
      currentMonth = currentMonth.plusMonths(1);
      updateCalendarView();
    });

    exportButton.addActionListener(e -> exportCalendarToCSV());

    importButton.addActionListener(e -> importCalendarFromCSV());
  }


  private void updateCalendarView() {
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
    if (startDayOfWeek == 7) {
      startDayOfWeek = 0;
    }
    for (int i = 0; i < startDayOfWeek; i++) {
      calendarPanel.add(new JLabel(""));
    }

    for (int i = 1; i <= currentMonth.lengthOfMonth(); i++) {
      JButton dayButton = new JButton(String.valueOf(i));
      dayButton.setToolTipText("Click to view events on " + currentMonth.atDay(i));
      final int dayNum = i;
      dayButton.addActionListener(e -> showEventDialog(currentMonth.atDay(dayNum)));
      calendarPanel.add(dayButton);
    }

    ZoneId zone = ZoneId.of(calendarTimezones.getOrDefault(selectedCalendar,
            ZoneId.systemDefault().toString()));
    ZonedDateTime startZDT = currentMonth.atDay(1).atStartOfDay(zone);
    ZonedDateTime endZDT = currentMonth.atEndOfMonth().atTime(23, 59).atZone(zone);

    String command = commandFactory.printEventsBetweenCommand(startZDT, endZDT);
    controller.processCommand(command);

    calendarPanel.revalidate();
    calendarPanel.repaint();
    frame.revalidate();
    frame.repaint();
  }

  /**
   * @param events the list of calendar events to display
   */
  @Override
  public void displayEvents(List<ReadOnlyCalendarEvent> events) {
    this.lastRenderedEvents = new ArrayList<>(events);

    Map<LocalDate, List<ReadOnlyCalendarEvent>> dayEventMap = new HashMap<>();

    for (ReadOnlyCalendarEvent event : events) {
      LocalDate date = event.getStartDateTime().toLocalDate();
      if (date.getMonth() == currentMonth.getMonth() && date.getYear() == currentMonth.getYear()) {
        dayEventMap.computeIfAbsent(date, k -> new ArrayList<>()).add(event);
      }
    }

    Component[] components = calendarPanel.getComponents();
    for (int i = 7; i < components.length; i++) {
      Component comp = components[i];
      if (comp instanceof JButton) {
        JButton dayButton = (JButton) comp;
        try {
          int dayNum = Integer.parseInt(dayButton.getText().split("<")[0].trim());
          LocalDate date = currentMonth.atDay(dayNum);
          List<ReadOnlyCalendarEvent> dayEvents = dayEventMap.getOrDefault(date, new ArrayList<>());

          if (!dayEvents.isEmpty()) {
            StringBuilder sb = new StringBuilder("<html><b>").append(dayNum).append("</b>");
            sb.append("<br><font size='2'>");
            for (int j = 0; j < Math.min(dayEvents.size(), 2); j++) {
              ReadOnlyCalendarEvent e = dayEvents.get(j);
              sb.append("• ");
              if (e.isRecurring()) {
                sb.append("↻ ");
              }
              sb.append(e.getSubject()).append("<br>");
            }
            if (dayEvents.size() > 2) sb.append("...");
            sb.append("</font></html>");

            dayButton.setText(sb.toString());
            dayButton.setBackground(new Color(230, 240, 255));
          }
        } catch (NumberFormatException ignored) {
        }
      }
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

    int result = JOptionPane.showConfirmDialog(frame, inputPanel, "Create New Calendar",
            JOptionPane.OK_CANCEL_OPTION);

    if (result == JOptionPane.OK_OPTION) {
      String name = calendarNameField.getText().trim();
      String timezone = (String) timezoneDropdown.getSelectedItem();

      if (name.isEmpty()) {
        JOptionPane.showMessageDialog(frame, "Calendar name cannot be empty.",
                "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      if (calendarColors.containsKey(name)) {
        JOptionPane.showMessageDialog(frame, "Calendar already exists.",
                "Error", JOptionPane.ERROR_MESSAGE);
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
        JOptionPane.showMessageDialog(frame, "Failed to create calendar via command.",
                "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }


      calendarDropdown.setSelectedItem(name);
      JOptionPane.showMessageDialog(frame, "Calendar '" + name
              + "' created successfully in timezone " + timezone + ".");
    }
  }

  private void showEventDialog(LocalDate date) {
    String timezone = calendarTimezones.getOrDefault(selectedCalendar, ZoneId.systemDefault().toString());
    final JDialog dialog = new JDialog(frame,
            "Events on " + date + " (" + timezone + ")", true);

    DefaultListModel<String> listModel = new DefaultListModel<>();
    JList<String> eventList = new JList<>(listModel);
    List<ReadOnlyCalendarEvent> dayEvents = new ArrayList<>();

    Runnable refreshDayEvents = () -> refreshDayEvents(listModel, dayEvents, date);

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
      showAddEventDialog(date, refreshDayEvents);
    });

    edit.addActionListener(e -> {
      int selectedIndex = eventList.getSelectedIndex();
      if (selectedIndex == -1) {
        JOptionPane.showMessageDialog(dialog, "Please select an event to edit.");
        return;
      }
      ReadOnlyCalendarEvent selectedEvent = dayEvents.get(selectedIndex);
      showEditEventDialog(selectedEvent, refreshDayEvents, dialog);
    });

    editAll.addActionListener(e -> {
      int selectedIndex = eventList.getSelectedIndex();
      if (selectedIndex == -1) {
        JOptionPane.showMessageDialog(dialog, "Please select an event to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
        return;
      }
      ReadOnlyCalendarEvent selectedEvent = dayEvents.get(selectedIndex);
      showEditRecurringEventDialog(selectedEvent, refreshDayEvents, dialog);
    });

    dialog.getContentPane().add(panel);
    dialog.setSize(520, 400);
    dialog.setLocationRelativeTo(frame);
    dialog.setVisible(true);
  }

  private void refreshDayEvents(DefaultListModel<String> listModel, List<ReadOnlyCalendarEvent> dayEvents, LocalDate date) {
    dayEvents.clear();
    listModel.clear();

    ZoneId zone = ZoneId.of(calendarTimezones.getOrDefault(selectedCalendar, ZoneId.systemDefault().toString()));
    ZonedDateTime refreshStart = currentMonth.atDay(1).atStartOfDay(zone);
    ZonedDateTime refreshEnd = currentMonth.atEndOfMonth().atTime(23, 59).atZone(zone);

    String refreshCmd = commandFactory.printEventsBetweenCommand(refreshStart, refreshEnd);
    controller.processCommand(refreshCmd);

    for (ReadOnlyCalendarEvent e : lastRenderedEvents) {
      if (e.getStartDateTime().toLocalDate().equals(date)) {
        dayEvents.add(e);
        String start = e.getStartDateTime().toLocalTime().toString();
        String end = e.getEndDateTime().toLocalTime().toString();
        listModel.addElement(e.getSubject() + " (" + start + " - " + end + ")");
      }
    }
  }

  private void showAddEventDialog(LocalDate date, Runnable refreshDayEvents) {
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

    JPanel recurringPanel = createRecurringPanel();
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
    gbc.gridx = 0; gbc.gridy = row;
    inputPanel.add(new JLabel("Title:"), gbc);
    gbc.gridx = 1; inputPanel.add(nameField, gbc);
    row++;

    gbc.gridx = 0; gbc.gridy = row;
    inputPanel.add(new JLabel("Start Time (HH:mm):"), gbc);
    gbc.gridx = 1; inputPanel.add(startField, gbc);
    row++;

    gbc.gridx = 0; gbc.gridy = row;
    inputPanel.add(new JLabel("End Time (HH:mm):"), gbc);
    gbc.gridx = 1; inputPanel.add(endField, gbc);
    row++;

    gbc.gridx = 0; gbc.gridy = row;
    inputPanel.add(new JLabel("Description:"), gbc);
    gbc.gridx = 1; inputPanel.add(descField, gbc);
    row++;

    gbc.gridx = 0; gbc.gridy = row;
    inputPanel.add(new JLabel("Location:"), gbc);
    gbc.gridx = 1; inputPanel.add(locField, gbc);
    row++;

    gbc.gridx = 0; gbc.gridy = row;
    inputPanel.add(singleButton, gbc);
    gbc.gridx = 1; inputPanel.add(recurringButton, gbc);
    row++;

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
        if (name.isEmpty()) {
          JOptionPane.showMessageDialog(addDialog, "Event name cannot be empty.",
                  "Invalid Input", JOptionPane.ERROR_MESSAGE);
          return;
        }
        String startTimeText = startField.getText().trim();
        String endTimeText = endField.getText().trim();

        if (startTimeText.isEmpty()) {
          JOptionPane.showMessageDialog(addDialog, "Start time must be provided.",
                  "Invalid Input", JOptionPane.ERROR_MESSAGE);
          return;
        }

        LocalTime startTime = LocalTime.parse(startTimeText);
        LocalTime endTime;
        if (endTimeText.isEmpty()) {
          endTime = LocalTime.of(23, 59, 59);
        } else {
          endTime = LocalTime.parse(endTimeText);
          if (startTime.equals(endTime)) {
            JOptionPane.showMessageDialog(addDialog,
                    "Start and End times cannot be the same for a timed event.",
                    "Invalid Time Entry", JOptionPane.ERROR_MESSAGE);
            return;
          }
        }

        String desc = descField.getText().trim();
        String loc = locField.getText().trim();

        ZoneId zone = ZoneId.of(calendarTimezones.getOrDefault(selectedCalendar,
                ZoneId.systemDefault().toString()));
        ZonedDateTime startZDT = date.atTime(startTime).atZone(zone);
        ZonedDateTime endZDT = date.atTime(endTime).atZone(zone);

        EventInput input = new EventInput();
        input.setSubject(name);
        input.setStart(startZDT);
        input.setEnd(endZDT);
        input.setDescription(desc);
        input.setLocation(loc);

        if (recurringButton.isSelected()) {
          Set<DayOfWeek> repeatingDaysSet = new HashSet<>();
          for (Component comp : recurringPanel.getComponents()) {
            if (comp instanceof JCheckBox) {
              JCheckBox cb = (JCheckBox) comp;
              if (cb.isSelected() && isWeekday(cb.getText())) {
                repeatingDaysSet.add(DayOfWeek.valueOf(cb.getText().toUpperCase()));
              }
            }
          }
          if (repeatingDaysSet.isEmpty()) {
            JOptionPane.showMessageDialog(addDialog,
                    "Please select at least one day of the week for the recurring event.",
                    "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
          }
          String repeatingDaysString = daysToString(repeatingDaysSet);
          input.setRepeatingDays(repeatingDaysString);
          input.setRecurring(true);

          JTextField repeatUntilField = getRepeatUntilField(recurringPanel);
          JTextField repeatCountField = getRepeatCountField(recurringPanel);

          boolean hasRepeatUntil = !repeatUntilField.getText().trim().isEmpty();
          boolean hasRepeatCount = !repeatCountField.getText().trim().isEmpty();

          if (!hasRepeatUntil && !hasRepeatCount) {
            input.setRepeatTimes(4);
          } else {
            if (hasRepeatUntil) {
              ZonedDateTime repeatUntil = LocalDate.parse(repeatUntilField.getText().trim()).atStartOfDay(zone);
              input.setRepeatUntil(repeatUntil);
            }
            if (hasRepeatCount) {
              int repeatCount = Integer.parseInt(repeatCountField.getText().trim());
              input.setRepeatTimes(repeatCount);
            }
          }
        }

        String cmd = commandFactory.createEventCommand(input);
        if (controller.processCommand(cmd)) {
          addDialog.dispose();
          refreshMainView();
          refreshDayEvents.run();
        } else {
          JOptionPane.showMessageDialog(addDialog, "Failed to create event",
                  "Error", JOptionPane.ERROR_MESSAGE);
        }
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(addDialog, "Invalid input: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
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
  }

  private void showEditEventDialog(ReadOnlyCalendarEvent selectedEvent, Runnable refreshDayEvents, JDialog parentDialog) {
    ZonedDateTime fromStart = selectedEvent.getStartDateTime();
    ZonedDateTime fromEnd = selectedEvent.getEndDateTime();

    JPanel editPanel = new JPanel(new BorderLayout());
    String[] singleProps = {"Title", "Start Time", "End Time", "Description", "Location"};
    String[] recurringProps = {"Title", "Start Time", "End Time", "Description", "Location", "Repeat Until", "Repeat Times", "Weekdays"};
    JComboBox<String> propertyDropdown = new JComboBox<>(selectedEvent.isRecurring() ? recurringProps : singleProps);

    JPanel inputPanel = new JPanel(new GridLayout(0, 1));
    JLabel inputLabel = new JLabel("New Value:");
    JTextField inputField = new JTextField();
    inputPanel.add(inputLabel);
    inputPanel.add(inputField);

    editPanel.add(new JLabel("Select property to edit:"), BorderLayout.NORTH);
    editPanel.add(propertyDropdown, BorderLayout.CENTER);
    editPanel.add(inputPanel, BorderLayout.SOUTH);

    propertyDropdown.addActionListener(ev -> {
      String selected = (String) propertyDropdown.getSelectedItem();
      switch (selected) {
        case "Title":
          inputLabel.setText("New Title:");
          inputField.setText(selectedEvent.getSubject());
          break;
        case "Start Time":
          inputLabel.setText("New Start Time (HH:mm):");
          inputField.setText(fromStart.toLocalTime().toString());
          break;
        case "End Time":
          inputLabel.setText("New End Time (HH:mm):");
          inputField.setText(fromEnd.toLocalTime().toString());
          break;
        case "Description":
          inputLabel.setText("New Description:");
          inputField.setText(selectedEvent.getDescription());
          break;
        case "Location":
          inputLabel.setText("New Location:");
          inputField.setText(selectedEvent.getLocation());
          break;
      }
    });

    propertyDropdown.setSelectedIndex(0);

    int result = JOptionPane.showConfirmDialog(parentDialog, editPanel, "Edit Event Property",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (result == JOptionPane.OK_OPTION) {
      String newValue = inputField.getText().trim();
      String selectedProp = (String) propertyDropdown.getSelectedItem();
      if (newValue.isEmpty()) {
        JOptionPane.showMessageDialog(parentDialog, "New value cannot be empty.",
                "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      String property = null;
      String finalValue = newValue;
      try {
        switch (selectedProp) {
          case "Title":
            if (newValue.equals(selectedEvent.getSubject())) return;
            property = "name";
            break;
          case "Start Time":
            LocalTime newStart = LocalTime.parse(newValue);
            if (newStart.equals(fromStart.toLocalTime())) return;
            property = "startdatetime";
            finalValue = fromStart.toLocalDate().atTime(newStart).toString();
            break;
          case "End Time":
            LocalTime newEnd = LocalTime.parse(newValue);
            if (newEnd.equals(fromEnd.toLocalTime())) return;
            property = "enddatetime";
            finalValue = fromEnd.toLocalDate().atTime(newEnd).toString();
            break;
          case "Description":
            if (newValue.equals(selectedEvent.getDescription())) return;
            property = "description";
            break;
          case "Location":
            if (newValue.equals(selectedEvent.getLocation())) return;
            property = "location";
            break;
        }
        if (property != null) {
          EditInput input = new EditInput(property, selectedEvent.getSubject(), fromStart, fromEnd, finalValue, false);
          if (controller.processCommand(commandFactory.createEditCommand(input))) {
            refreshMainView();
            refreshDayEvents.run();
            parentDialog.dispose();
          } else {
            JOptionPane.showMessageDialog(parentDialog, "Failed to edit event", "Error", JOptionPane.ERROR_MESSAGE);
          }
        }
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(parentDialog, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void showEditRecurringEventDialog(ReadOnlyCalendarEvent selectedEvent, Runnable refreshDayEvents, JDialog parentDialog) {
    ZonedDateTime fromStart = selectedEvent.getStartDateTime();
    ZonedDateTime fromEnd = selectedEvent.getEndDateTime();

    JPanel editPanel = new JPanel(new BorderLayout(5, 5));
    editPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    editPanel.setPreferredSize(new Dimension(400, 200));

    String[] recurringProps = {"Title", "Description", "Location", "Repeat Until", "Repeat Times", "Weekdays"};
    JComboBox<String> propertyDropdown = new JComboBox<>(recurringProps);
    propertyDropdown.setPreferredSize(new Dimension(380, 25));

    JPanel inputPanel = new JPanel(new GridLayout(0, 1, 5, 5));
    JLabel inputLabel = new JLabel("New Value:");
    JTextField inputField = new JTextField();
    inputField.setPreferredSize(new Dimension(380, 25));

    editPanel.add(new JLabel("Select property to edit:"), BorderLayout.NORTH);
    editPanel.add(propertyDropdown, BorderLayout.CENTER);
    editPanel.add(inputPanel, BorderLayout.SOUTH);

    propertyDropdown.addActionListener(ev -> {
      inputPanel.removeAll();
      String selected = (String) propertyDropdown.getSelectedItem();
      if ("Weekdays".equals(selected)) {
        inputLabel.setText("New Weekdays (e.g. MTWRFSU for all days):");
        inputPanel.add(inputLabel);
        inputPanel.add(inputField);
        if (selectedEvent.getWeekdays() != null) {
          inputField.setText(selectedEvent.getWeekdays());
        }
      } else {
        switch (selected) {
          case "Title":
            inputLabel.setText("New Title:");
            inputField.setText(selectedEvent.getSubject());
            break;
          case "Description":
            inputLabel.setText("New Description:");
            inputField.setText(selectedEvent.getDescription());
            break;
          case "Location":
            inputLabel.setText("New Location:");
            inputField.setText(selectedEvent.getLocation());
            break;
          case "Repeat Until":
            inputLabel.setText("New Repeat Until (YYYY-MM-DD):");
            inputField.setText("");
            break;
          case "Repeat Times":
            inputLabel.setText("New Repeat Times (integer):");
            inputField.setText("");
            break;
        }
        inputPanel.add(inputLabel);
        inputPanel.add(inputField);
      }
      inputPanel.revalidate();
      inputPanel.repaint();
    });
    propertyDropdown.setSelectedItem("Title");

    int result = JOptionPane.showConfirmDialog(parentDialog, editPanel,
            "Edit Recurring Property", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (result == JOptionPane.OK_OPTION) {
      String newValue = inputField.getText().trim();
      String selectedProp = (String) propertyDropdown.getSelectedItem();
      if (newValue.isEmpty()) {
        JOptionPane.showMessageDialog(parentDialog, "New value cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      String property = null;
      String finalValue = newValue;
      try {
        switch (selectedProp) {
          case "Title":
            property = "name";
            break;
          case "Description":
            property = "description";
            break;
          case "Location":
            property = "location";
            break;
          case "Repeat Until":
            LocalDate repeatUntil = LocalDate.parse(newValue);
            property = "repeatuntil";
            finalValue = repeatUntil.toString();
            break;
          case "Repeat Times":
            Integer.parseInt(newValue);
            property = "repeattimes";
            break;
          case "Weekdays":
            property = "repeatingdays";
            break;
        }
        if (property != null) {
          EditInput input = new EditInput(property, selectedEvent.getSubject(), fromStart, fromEnd, finalValue, true);
          if (controller.processCommand(commandFactory.createEditRecurringEventCommand(input))) {
            refreshMainView();
            refreshDayEvents.run();
            parentDialog.dispose();
          } else {
            JOptionPane.showMessageDialog(parentDialog, "Failed to edit recurring event", "Error", JOptionPane.ERROR_MESSAGE);
          }
        }
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(parentDialog, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void refreshMainView() {
    ZoneId zone = ZoneId.of(calendarTimezones.getOrDefault(selectedCalendar, ZoneId.systemDefault().toString()));
    ZonedDateTime startZDTMain = currentMonth.atDay(1).atStartOfDay(zone);
    ZonedDateTime endZDTMain = currentMonth.atEndOfMonth().atTime(23, 59).atZone(zone);
    String refreshCmd = commandFactory.printEventsBetweenCommand(startZDTMain, endZDTMain);
    controller.processCommand(refreshCmd);
    updateCalendarView();
  }

  private JPanel createRecurringPanel() {
    JPanel recurringPanel = new JPanel(new GridLayout(0, 3));
    JCheckBox[] dayChecks = {
            new JCheckBox("Monday"), new JCheckBox("Tuesday"), new JCheckBox("Wednesday"),
            new JCheckBox("Thursday"), new JCheckBox("Friday"), new JCheckBox("Saturday"), new JCheckBox("Sunday")
    };
    for (JCheckBox cb : dayChecks) {
      recurringPanel.add(cb);
    }
    recurringPanel.add(new JLabel(""));
    recurringPanel.add(new JLabel(""));
    recurringPanel.add(new JLabel("Repeat Until (yyyy-MM-dd):"));
    recurringPanel.add(repeatUntilField);
    recurringPanel.add(new JLabel(""));
    recurringPanel.add(new JLabel("Repeat Times:"));
    recurringPanel.add(repeatCountField);
    recurringPanel.add(new JLabel(""));
    return recurringPanel;
  }

  private JTextField getRepeatUntilField(JPanel recurringPanel) {
    return repeatUntilField;
  }

  private JTextField getRepeatCountField(JPanel recurringPanel) {
    return repeatCountField;
  }

  private boolean isWeekday(String text) {
    try {
      DayOfWeek.valueOf(text.toUpperCase());
      return true;
    } catch (Exception ex) {
      return false;
    }
  }


  private String daysToString(Set<DayOfWeek> days) {
    if (days.isEmpty()) {
      return "MTWRFSU";
    }
    StringBuilder sb = new StringBuilder();
    TreeSet<DayOfWeek> orderedDays = new TreeSet<>(days);
    for (DayOfWeek day : orderedDays) {
      switch (day) {
        case MONDAY:    sb.append("M"); break;
        case TUESDAY:   sb.append("T"); break;
        case WEDNESDAY: sb.append("W"); break;
        case THURSDAY:  sb.append("R"); break;
        case FRIDAY:    sb.append("F"); break;
        case SATURDAY:  sb.append("S"); break;
        case SUNDAY:    sb.append("U"); break;
      }
    }
    return sb.toString();
  }

  private Set<DayOfWeek> parseWeekdays(Object value) {
    Set<DayOfWeek> days = new HashSet<>();
    if (value == null || value.toString().trim().isEmpty()) {
      return days;
    }

    String str = value.toString().toUpperCase().trim();

    for (char c : str.toCharArray()) {
      switch (c) {
        case 'M': days.add(DayOfWeek.MONDAY); break;
        case 'T': days.add(DayOfWeek.TUESDAY); break;
        case 'W': days.add(DayOfWeek.WEDNESDAY); break;
        case 'R': days.add(DayOfWeek.THURSDAY); break;
        case 'F': days.add(DayOfWeek.FRIDAY); break;
        case 'S': days.add(DayOfWeek.SATURDAY); break;
        case 'U': days.add(DayOfWeek.SUNDAY); break;
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
      String days = event.getWeekdays();
      if (days != null && !days.trim().isEmpty()) {
        sb.append("<p><b>Repeats on:</b> ").append(days).append("</p>");
      }
      if (event.RepeatUntil() != null) {
        sb.append("<p><b>Repeat Until:</b> ").append(event.RepeatUntil()).append("</p>");
      } else if (event.getRepeatCount() != null && event.getRepeatCount() > 0) {
      } else if (event.getRepeatCount() != null && event.getRepeatCount() > 0) {
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

        String fullPath = fileToSave.getAbsolutePath();
        if (!fullPath.toLowerCase().endsWith(".csv")) {
          fullPath += ".csv";
          fileToSave = new File(fullPath);
        }

        String command = commandFactory.exportCalendarCommand(fullPath);
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

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    suppressCreationMessages = true;
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String header = reader.readLine();
      int addedCount = 0;
      String line;
      int lineNumber = 1;

      while ((line = reader.readLine()) != null) {
        lineNumber++;
        try {
          String[] parts = line.split(",", -1);
          if (parts.length < 7) continue;

          String name = parts[0].replace("\"", "").trim();
          LocalDate startDate = tryParseDate(parts[1].trim());
          LocalTime startTime = LocalTime.parse(parts[2].trim(), timeFormatter);
          LocalDate endDate = tryParseDate(parts[3].trim());
          LocalTime endTime = LocalTime.parse(parts[4].trim(), timeFormatter);
          String desc = parts[5].trim();
          String loc = parts[6].trim();

          String weekdays = parts.length > 7 ? parts[7].trim().toUpperCase() : "";
          String repeatUntilStr = parts.length > 8 ? parts[8].trim() : "";
          String repeatCountStr = parts.length > 9 ? parts[9].trim() : "";

          if (!startDate.equals(endDate)) continue;

          ZoneId zone = ZoneId.of(calendarTimezones.getOrDefault(selectedCalendar,
                  ZoneId.systemDefault().toString()));
          ZonedDateTime start = startDate.atTime(startTime).atZone(zone);
          ZonedDateTime end = endDate.atTime(endTime).atZone(zone);

          EventInput input = new EventInput();
          input.setSubject(name);
          input.setStart(start);
          input.setEnd(end);
          input.setDescription(desc);
          input.setLocation(loc);

          boolean isRecurring = !weekdays.isEmpty();
          if (isRecurring) {
            input.setRecurring(true);
            input.setRepeatingDays(weekdays);

            if (!repeatUntilStr.isEmpty()) {
              ZonedDateTime until = LocalDate.parse(repeatUntilStr,
                      dateFormatter).atStartOfDay(zone);
              input.setRepeatUntil(until);
            }

            if (!repeatCountStr.isEmpty()) {
              int count = Integer.parseInt(repeatCountStr);
              input.setRepeatTimes(count);
            }

            if (repeatUntilStr.isEmpty() && repeatCountStr.isEmpty()) {
              input.setRepeatTimes(4);
            }
          }

          String cmd = commandFactory.createEventCommand(input);
          if (controller.processCommand(cmd)) {
            addedCount++;
          }

        } catch (Exception perLineError) {
          System.err.println("Skipping line " + lineNumber + ": " + perLineError.getMessage());
        }
      }

      suppressCreationMessages = false;

      controller.processCommand("print events from "
              + currentMonth.atDay(1) + " to " + currentMonth.atEndOfMonth());
      JOptionPane.showMessageDialog(frame, "Imported " + addedCount
                      + " events into '" + selectedCalendar + "'.", "Import Success",
              JOptionPane.INFORMATION_MESSAGE);

    } catch (Exception ex) {
      JOptionPane.showMessageDialog(frame, "Error importing calendar: "
              + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private LocalDate tryParseDate(String input) {
    DateTimeFormatter[] formats = new DateTimeFormatter[] {
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("MM/dd/yyyy")
    };
    for (DateTimeFormatter fmt : formats) {
      try {
        return LocalDate.parse(input.trim(), fmt);
      } catch (Exception ignored) {}
    }
    throw new IllegalArgumentException("Invalid date format: " + input);
  }



  public void initialize() {
    updateCalendarView();
  }

  @Override
  public void displayMessage(String message) {

    if (message.startsWith("Events on") && message.endsWith(":")) {
      return;
    }
    if (message.startsWith("Calendar created:") ||
            message.startsWith("Using calendar:") ||
            message.startsWith("No events found in calendar")||
            message.startsWith("Calendar exported successfully to:")) {
      return;
    }


    if (suppressCreationMessages && message.equals("Event created successfully")) {
      return;
    }

    JOptionPane.showMessageDialog(frame, message);
  }

  @Override
  public void displayError(String errorMessage) {
    JOptionPane.showMessageDialog(frame, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
  }

  @Override
  public void run() {

  }

  @Override
  public void setInput(Readable in) {
    ICalendarView.super.setInput(in);
  }

  @Override
  public void setOutput(Appendable out) {
    ICalendarView.super.setOutput(out);
  }


  public void setController(CalendarController controller) {
    this.controller = controller;
  }

  public void setCommandFactory(ICommandFactory factory) {
    this.commandFactory = factory;

    initializeComponents();
    initializeLayout();
    registerListeners();

    if (controller != null && commandFactory != null) {
      try {
        String createCmd = commandFactory.createCalendarCommand("Default", ZoneId.systemDefault());
        controller.processCommand(createCmd);

        String useCmd = commandFactory.useCalendarCommand("Default");
        controller.processCommand(useCmd);
      } catch (Exception ex) {
        System.err.println("Error creating/using default calendar: " + ex.getMessage());
      }
    }

    updateCalendarView();
    frame.setVisible(true);

  }
}
