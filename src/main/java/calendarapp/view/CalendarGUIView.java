package calendarapp.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import calendarapp.model.event.ReadOnlyCalendarEvent;
import calendarapp.factory.EditInput;
import calendarapp.factory.ICommandFactory;
import calendarapp.factory.EventInput;
import calendarapp.controller.CalendarController;
import calendarapp.model.ICalendarManager;
import calendarapp.model.event.RecurringEvent;
import calendarapp.model.event.SingleEvent;

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

  public CalendarGUIView(ICalendarManager manager, CalendarController controller) {
    this.calendarManager = manager;
    this.controller = controller;

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
    // Adjust if week starts on Sunday
    if (startDayOfWeek == 7) {
      startDayOfWeek = 0;
    }
    for (int i = 0; i < startDayOfWeek; i++) {
      calendarPanel.add(new JLabel(""));
    }

    // Add placeholder buttons for all days
    for (int i = 1; i <= currentMonth.lengthOfMonth(); i++) {
      JButton dayButton = new JButton(String.valueOf(i));
      dayButton.setToolTipText("Click to view events on " + currentMonth.atDay(i));
      final int dayNum = i;
      dayButton.addActionListener(e -> showEventDialog(currentMonth.atDay(dayNum)));
      calendarPanel.add(dayButton);
    }

    // Request events for the current month
    ZoneId zone = ZoneId.of(calendarTimezones.getOrDefault(selectedCalendar, ZoneId.systemDefault().toString()));
    ZonedDateTime startZDT = currentMonth.atDay(1).atStartOfDay(zone);
    ZonedDateTime endZDT = currentMonth.atEndOfMonth().atTime(23, 59).atZone(zone);

    String command = commandFactory.printEventsBetweenCommand(startZDT, endZDT);
    controller.processCommand(command);

    // Force revalidate and repaint
    calendarPanel.revalidate();
    calendarPanel.repaint();
    frame.revalidate();
    frame.repaint();
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
    List<ReadOnlyCalendarEvent> flattenedEvents = new ArrayList<>();

    // 🔁 Expand recurring events into single events
    for (ReadOnlyCalendarEvent e : events) {

      if (e.isRecurring() && e instanceof RecurringEvent) {
        List<SingleEvent> occurrences = ((RecurringEvent) e).generateOccurrences(null);
        flattenedEvents.addAll(occurrences);
      } else {
        flattenedEvents.add(e);
      }
    }

    // 📅 Build the day event map
    for (ReadOnlyCalendarEvent event : flattenedEvents) {
      LocalDate date = event.getStartDateTime().toLocalDate();
      if (date.getMonth() == currentMonth.getMonth() && date.getYear() == currentMonth.getYear()) {
        dayEventMap.computeIfAbsent(date, k -> new ArrayList<>()).add(event);
      }
    }


    // 🖼️ Update buttons with events
    Component[] components = calendarPanel.getComponents();
    for (int i = 7; i < components.length; i++) { // Skip day headers
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
              ReadOnlyCalendarEvent event = dayEvents.get(j);
              sb.append("• ").append(event.getSubject()).append("<br>");
            }
            if (dayEvents.size() > 2) sb.append("...");
            sb.append("</font></html>");

            dayButton.setText(sb.toString());
            dayButton.setBackground(new Color(230, 240, 255)); // Light blue for any events
          }
        } catch (NumberFormatException ignored) {
          // Skip non-numeric cells
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
          if (e.isRecurring() && e instanceof RecurringEvent) {
            List<SingleEvent> occurrences = ((RecurringEvent) e).generateOccurrences(null);
            for (SingleEvent occurrence : occurrences) {
              if (occurrence.getStartDateTime().toLocalDate().equals(date)) {
                dayEvents.add(occurrence);
                String start = occurrence.getStartDateTime().toLocalTime().toString();
                String end = occurrence.getEndDateTime().toLocalTime().toString();
                listModel.addElement(occurrence.getSubject() + " (" + start + " - " + end + ")");
              }
            }
          } else {
            if (e.getStartDateTime().toLocalDate().equals(date)) {
              dayEvents.add(e);
              String start = e.getStartDateTime().toLocalTime().toString();
              String end = e.getEndDateTime().toLocalTime().toString();
              listModel.addElement(e.getSubject() + " (" + start + " - " + end + ")");
            }
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
          // Check if the event name is empty
          if (name.isEmpty()) {
            JOptionPane.showMessageDialog(addDialog,
                    "Event name cannot be empty.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            return; // Exit without creating the event
          }

          String startTimeText = startField.getText().trim();
          String endTimeText = endField.getText().trim();

          // Ensure that a start time was provided
          if (startTimeText.isEmpty()) {
            JOptionPane.showMessageDialog(addDialog,
                    "Start time must be provided.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            return;
          }

          // Parse the start time
          LocalTime startTime = LocalTime.parse(startTimeText);

          // If end time is missing, treat event as all-day.
          boolean isAllDay = false;
          LocalTime endTime = null;
          if (endTimeText.isEmpty()) {
            isAllDay = true;
            // Set end time to end of day (23:59:59)
            endTime = LocalTime.of(23, 59, 59);
          } else {
            endTime = LocalTime.parse(endTimeText);
            // Optional: disallow same start and end times for non-all-day events
            if (startTime.equals(endTime)) {
              JOptionPane.showMessageDialog(addDialog,
                      "Start and End times cannot be the same for a timed event.",
                      "Invalid Time Entry",
                      JOptionPane.ERROR_MESSAGE);
              return;
            }
          }
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
            Set<DayOfWeek> repeatingDaysSet = new HashSet<>();
            for (JCheckBox box : dayChecks) {
              if (box.isSelected()) {
                repeatingDaysSet.add(DayOfWeek.valueOf(box.getText().toUpperCase()));
              }
            }

            if (repeatingDaysSet.isEmpty()) {
              JOptionPane.showMessageDialog(addDialog,
                      "Please select at least one day of the week for the recurring event.",
                      "Missing Information",
                      JOptionPane.WARNING_MESSAGE);
              return;
            }

            // Convert the Set to a string (e.g., "MWF")
            String repeatingDaysString = daysToString(repeatingDaysSet);

            // Now store the string in the EventInput
            input.setRepeatingDays(repeatingDaysString);
            input.setRecurring(true);

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
            ZoneId zoneMain = ZoneId.of(calendarTimezones.getOrDefault(selectedCalendar, ZoneId.systemDefault().toString()));
            ZonedDateTime startZDTMain = currentMonth.atDay(1).atStartOfDay(zoneMain);
            ZonedDateTime endZDTMain = currentMonth.atEndOfMonth().atTime(23, 59).atZone(zoneMain);
            String refreshCmd = commandFactory.printEventsBetweenCommand(startZDTMain, endZDTMain);
            controller.processCommand(refreshCmd);
            updateCalendarView();
            refreshDayEvents.run();
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
      ZonedDateTime fromStart = selectedEvent.getStartDateTime();
      ZonedDateTime fromEnd = selectedEvent.getEndDateTime();

      JPanel editPanel = new JPanel(new BorderLayout());

      // Property selector
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

      int result = JOptionPane.showConfirmDialog(dialog, editPanel, "Edit Event Property",
              JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

      if (result == JOptionPane.OK_OPTION) {
        String newValue = inputField.getText().trim();
        String selectedProp = (String) propertyDropdown.getSelectedItem();

        if (newValue.isEmpty()) {
          JOptionPane.showMessageDialog(dialog, "New value cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
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
            boolean success = controller.processCommand(commandFactory.createEditCommand(input));

            if (success) {
              refreshDayEvents.run(); // 💥 refresh open list in-place
            } else {
              JOptionPane.showMessageDialog(dialog, "Failed to edit event", "Error", JOptionPane.ERROR_MESSAGE);
            }
          }

        } catch (Exception ex) {
          JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
      ZonedDateTime fromStart = selectedEvent.getStartDateTime();
      ZonedDateTime fromEnd = selectedEvent.getEndDateTime();

      JPanel editPanel = new JPanel(new BorderLayout());

      String[] recurringProps = {"Title", "Start Time", "End Time", "Description", "Location", "Repeat Until", "Repeat Times", "Weekdays"};
      JComboBox<String> propertyDropdown = new JComboBox<>(recurringProps);

      JPanel inputPanel = new JPanel(new GridLayout(0, 1));
      JLabel inputLabel = new JLabel("New Value:");
      JTextField inputField = new JTextField();

      inputPanel.add(inputLabel);
      inputPanel.add(inputField);

      editPanel.add(new JLabel("Select property to edit:"), BorderLayout.NORTH);
      editPanel.add(propertyDropdown, BorderLayout.CENTER);
      editPanel.add(inputPanel, BorderLayout.SOUTH);

      // Autofill value logic
      Runnable updateInputField = () -> {
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
          case "Repeat Until":
            inputLabel.setText("New Repeat Until (YYYY-MM-DD):");
            inputField.setText(""); // No current value shown
            break;
          case "Repeat Times":
            inputLabel.setText("New Repeat Times (integer):");
            inputField.setText("");
            break;
          case "Weekdays":
            inputLabel.setText("New Weekdays (e.g. MO,TU):");
            inputField.setText("");
            break;
        }
      };
      updateInputField.run();
      propertyDropdown.addActionListener(ev -> updateInputField.run());

      int result = JOptionPane.showConfirmDialog(dialog, editPanel, "Edit Recurring Property",
              JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

      if (result == JOptionPane.OK_OPTION) {
        String newValue = inputField.getText().trim();
        String selectedProp = (String) propertyDropdown.getSelectedItem();

        if (newValue.isEmpty()) {
          JOptionPane.showMessageDialog(dialog, "New value cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }

        String property = null;
        String finalValue = newValue;

        try {
          switch (selectedProp) {
            case "Title":
              property = "name";
              break;
            case "Start Time":
              LocalTime newStart = LocalTime.parse(newValue);
              property = "startdatetime";
              finalValue = fromStart.toLocalDate().atTime(newStart).toString();
              break;
            case "End Time":
              LocalTime newEnd = LocalTime.parse(newValue);
              property = "enddatetime";
              finalValue = fromEnd.toLocalDate().atTime(newEnd).toString();
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
              Integer.parseInt(newValue); // Validate it's numeric
              property = "repeattimes";
              break;
            case "Weekdays":
              property = "repeatingdays";
              break;
          }

          if (property != null) {
            EditInput input = new EditInput(property, selectedEvent.getSubject(), fromStart, fromEnd, finalValue, true);
            boolean success = controller.processCommand(commandFactory.createEditRecurringEventCommand(input));
            if (success) {
              controller.processCommand("print events on " + date);
              refreshDayEvents.run();// refresh
              dialog.dispose();
            } else {
              JOptionPane.showMessageDialog(dialog, "Failed to edit recurring event", "Error", JOptionPane.ERROR_MESSAGE);
            }
          }

        } catch (Exception ex) {
          JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    });



    dialog.getContentPane().add(panel);
    dialog.setSize(520, 400);
    dialog.setLocationRelativeTo(frame);
    dialog.setVisible(true);
  }

  private String daysToString(Set<DayOfWeek> days) {
    if (days.isEmpty()) {
      return "MTWRFSU"; // If no days selected, assume every day
    }
    StringBuilder sb = new StringBuilder();
    // Ensure we process days in a consistent order
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
      sb.append("<p><b>Repeats on:</b> ").append(event.getWeekdays() == null ? "N/A" : event.getWeekdays()).append("</p>");
      if (event.RepeatUntil() != null) {
        sb.append("<p><b>Repeat Until:</b> ").append(event.RepeatUntil()).append("</p>");
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

          ZoneId zone = ZoneId.of(calendarTimezones.getOrDefault(selectedCalendar, ZoneId.systemDefault().toString()));
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
              ZonedDateTime until = LocalDate.parse(repeatUntilStr, dateFormatter).atStartOfDay(zone);
              input.setRepeatUntil(until);
            }

            if (!repeatCountStr.isEmpty()) {
              int count = Integer.parseInt(repeatCountStr);
              input.setRepeatTimes(count);
            }

            if (repeatUntilStr.isEmpty() && repeatCountStr.isEmpty()) {
              input.setRepeatTimes(4); // default
            }
          }

          String cmd = commandFactory.createEventCommand(input);
          if (controller.processCommand(cmd)) {
            addedCount++;
          }

        } catch (Exception perLineError) {
          System.err.println("Skipping line " + lineNumber + ": " + perLineError.getMessage());
        }
        finally {
          suppressCreationMessages = false;
        }
      }

      controller.processCommand("print events from " + currentMonth.atDay(1) + " to " + currentMonth.atEndOfMonth());
      JOptionPane.showMessageDialog(frame, "Imported " + addedCount + " events into '" + selectedCalendar + "'.", "Import Success", JOptionPane.INFORMATION_MESSAGE);

    } catch (Exception ex) {
      JOptionPane.showMessageDialog(frame, "Error importing calendar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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

    //initializeData();
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