package calendarapp.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.*;
import java.util.*;
import java.util.List;

import calendarapp.factory.DefaultCommandFactory;
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
  private Map<String, Map<LocalDate, List<Event>>> calendarEvents;
  private YearMonth currentMonth;
  private String selectedCalendar;
  private JLabel calendarNameLabel;
  private JButton importButton;

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
    calendarEvents = new HashMap<>();

    selectedCalendar = "Default";
    addInitialCalendar("Default", ZoneId.systemDefault());

    // ✅ Other sample calendars
    addInitialCalendar("Personal", ZoneId.systemDefault());
    addInitialCalendar("Holidays", ZoneId.systemDefault());

    // ✅ If calendar manager exists, set "Work" as active
    if (calendarManager != null) {
      calendarManager.useCalendar("Work");
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
    calendarEvents.put(name, new HashMap<>());

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

    Map<LocalDate, List<Event>> currentEvents = calendarEvents.getOrDefault(selectedCalendar, new HashMap<>());

    for (int i = 1; i <= currentMonth.lengthOfMonth(); i++) {
      LocalDate date = currentMonth.atDay(i);
      JButton dayButton = new JButton("<html><b>" + i + "</b>");

      List<Event> events = currentEvents.getOrDefault(date, new ArrayList<>());
      if (!events.isEmpty()) {
        StringBuilder sb = new StringBuilder("<br><font size='2'>");
        for (int j = 0; j < Math.min(events.size(), 2); j++) {
          sb.append("• ").append(events.get(j).name).append("<br>");
        }
        if (events.size() > 2) sb.append("...");
        sb.append("</font></html>");
        dayButton.setText("<html><b>" + i + "</b>" + sb);
      } else {
        dayButton.setText("<html><b>" + i + "</b></html>");
      }

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
    final JDialog dialog = new JDialog(frame, "Events on " + date + " (" +
            calendarTimezones.getOrDefault(selectedCalendar, ZoneId.systemDefault().toString()) + ")", true);
    Map<LocalDate, List<Event>> calendarEventMap = calendarEvents.get(selectedCalendar);
    List<Event> dayEvents = calendarEventMap.getOrDefault(date, new ArrayList<>());

    DefaultListModel<String> listModel = new DefaultListModel<>();
    for (Event event : dayEvents) {
      listModel.addElement(event.name + " (" + event.startTime + "-" + event.endTime + ")");
    }

    JList<String> eventList = new JList<>(listModel);

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(new JScrollPane(eventList), BorderLayout.CENTER);

    JPanel controls = new JPanel();
    JButton add = new JButton("Add Event");
    JButton edit = new JButton("Edit Selected");
    JButton editAll = new JButton("Edit All Matching"); // ✨ New button
    controls.add(add);
    controls.add(edit);
    controls.add(editAll);
    panel.add(controls, BorderLayout.SOUTH);

    add.addActionListener(e -> {
      JTextField nameField = new JTextField();
      JTextField startField = new JTextField("09:00");
      JTextField endField = new JTextField("10:00");
      JTextField descriptionField = new JTextField();
      JTextField locationField = new JTextField();

      JRadioButton singleBtn = new JRadioButton("Single Event");
      JRadioButton recurringBtn = new JRadioButton("Recurring Event");
      ButtonGroup typeGroup = new ButtonGroup();
      typeGroup.add(singleBtn);
      typeGroup.add(recurringBtn);
      singleBtn.setSelected(true);

      JCheckBox[] weekdayBoxes = new JCheckBox[]{
              new JCheckBox("Mon"), new JCheckBox("Tue"), new JCheckBox("Wed"),
              new JCheckBox("Thu"), new JCheckBox("Fri"), new JCheckBox("Sat"), new JCheckBox("Sun")
      };

      JTextField occurrencesField = new JTextField();
      JTextField endDateField = new JTextField();

      JLabel repeatLabel = new JLabel("Repeat on (Weekdays):");
      JPanel weekdayPanel = new JPanel(new GridLayout(1, 7));
      for (JCheckBox box : weekdayBoxes) weekdayPanel.add(box);

      JLabel countLabel = new JLabel("Repeat Count (or leave blank):");
      JLabel untilLabel = new JLabel("Repeat Until (YYYY-MM-DD or leave blank):");

      JPanel form = new JPanel(new GridLayout(0, 1));
      form.add(new JLabel("Event Name:"));      form.add(nameField);
      form.add(new JLabel("Start Time (HH:mm):")); form.add(startField);
      form.add(new JLabel("End Time (HH:mm):"));   form.add(endField);
      form.add(new JLabel("Description (Optional):")); form.add(descriptionField);
      form.add(new JLabel("Location (Optional):"));    form.add(locationField);
      form.add(singleBtn); form.add(recurringBtn);
      form.add(repeatLabel); form.add(weekdayPanel);
      form.add(countLabel);  form.add(occurrencesField);
      form.add(untilLabel);  form.add(endDateField);

      repeatLabel.setVisible(false); weekdayPanel.setVisible(false);
      countLabel.setVisible(false); occurrencesField.setVisible(false);
      untilLabel.setVisible(false); endDateField.setVisible(false);

      ItemListener toggleListener = ae -> {
        boolean show = recurringBtn.isSelected();
        repeatLabel.setVisible(show); weekdayPanel.setVisible(show);
        countLabel.setVisible(show); occurrencesField.setVisible(show);
        untilLabel.setVisible(show); endDateField.setVisible(show);
        form.revalidate(); form.repaint();
      };
      singleBtn.addItemListener(toggleListener);
      recurringBtn.addItemListener(toggleListener);

      int result = JOptionPane.showConfirmDialog(frame, form, "Add Event", JOptionPane.OK_CANCEL_OPTION);
      if (result == JOptionPane.OK_OPTION) {
        try {
          String name = nameField.getText().trim();
          String description = descriptionField.getText().trim();
          String location = locationField.getText().trim();
          LocalTime start = LocalTime.parse(startField.getText().trim());
          LocalTime end = LocalTime.parse(endField.getText().trim());

          if (end.isBefore(start) || end.equals(start)) {
            JOptionPane.showMessageDialog(frame, "End time must be after start time.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
          }

          ZonedDateTime startDateTime = date.atTime(start).atZone(ZoneId.of(calendarTimezones.get(selectedCalendar)));
          ZonedDateTime endDateTime = date.atTime(end).atZone(ZoneId.of(calendarTimezones.get(selectedCalendar)));

          EventInput input = new EventInput();
          input.setSubject(name);
          input.setStart(startDateTime);
          input.setEnd(endDateTime);
          input.setDescription(description);
          input.setLocation(location);

          if (recurringBtn.isSelected()) {
            Set<DayOfWeek> selectedDays = new HashSet<>();
            DayOfWeek[] allDays = DayOfWeek.values();
            for (int i = 0; i < weekdayBoxes.length; i++) {
              if (weekdayBoxes[i].isSelected()) {
                selectedDays.add(allDays[(i + 1) % 7]);
              }
            }

            if (selectedDays.isEmpty()) {
              JOptionPane.showMessageDialog(frame, "Select at least one weekday.", "Error", JOptionPane.ERROR_MESSAGE);
              return;
            }

            input.setRepeatingDays(selectedDays);

            if (!occurrencesField.getText().trim().isEmpty()) {
              input.setRepeatTimes(Integer.parseInt(occurrencesField.getText().trim()));
            } else if (!endDateField.getText().trim().isEmpty()) {
              LocalDate endDate = LocalDate.parse(endDateField.getText().trim());
              input.setRepeatUntil(endDate.atTime(start).atZone(startDateTime.getZone()));
            } else {
              JOptionPane.showMessageDialog(frame, "Provide repeat count or end date.", "Error", JOptionPane.ERROR_MESSAGE);
              return;
            }
          }

          // ✅ Use factory to create command
          String command = commandFactory.createEventCommand(input);
          System.out.println("COMMAND GENERATED: " + command);

          boolean success = controller.processCommand(command);
          if (success) {
            controller.processCommand("print events from " + date + " to " + date.plusMonths(1));
            JOptionPane.showMessageDialog(frame, "Event added successfully.");
            dialog.dispose();
          } else {
            JOptionPane.showMessageDialog(frame, "Failed to add event via command.", "Error", JOptionPane.ERROR_MESSAGE);
          }

        } catch (Exception ex) {
          JOptionPane.showMessageDialog(frame, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
      }

    });

    edit.addActionListener(e -> {
      int selectedIndex = eventList.getSelectedIndex();
      if (selectedIndex == -1) {
        JOptionPane.showMessageDialog(frame, "Please select an event to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
        return;
      }

      List<Event> selectedDayEvents = calendarEvents
              .getOrDefault(selectedCalendar, new HashMap<>())
              .getOrDefault(date, new ArrayList<>());

      if (selectedIndex >= selectedDayEvents.size()) {
        JOptionPane.showMessageDialog(frame, "Invalid selection. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      Event selectedEvent = selectedDayEvents.get(selectedIndex);

      // Form with pre-filled values
      JTextField nameField = new JTextField(selectedEvent.name);
      JTextField startField = new JTextField(selectedEvent.startTime.toString());
      JTextField endField = new JTextField(selectedEvent.endTime.toString());
      JTextField descField = new JTextField();
      JTextField locField = new JTextField();

      JPanel form = new JPanel(new GridLayout(0, 1));
      form.add(new JLabel("Event Name:")); form.add(nameField);
      form.add(new JLabel("Start Time (HH:mm):")); form.add(startField);
      form.add(new JLabel("End Time (HH:mm):")); form.add(endField);
      form.add(new JLabel("Description (Optional):")); form.add(descField);
      form.add(new JLabel("Location (Optional):")); form.add(locField);

      int result = JOptionPane.showConfirmDialog(frame, form, "Edit Event", JOptionPane.OK_CANCEL_OPTION);
      if (result == JOptionPane.OK_OPTION) {
        try {
          String newName = nameField.getText().trim();
          LocalTime newStartTime = LocalTime.parse(startField.getText().trim());
          LocalTime newEndTime = LocalTime.parse(endField.getText().trim());
          String newDesc = descField.getText().trim();
          String newLoc = locField.getText().trim();

          if (newEndTime.isBefore(newStartTime) || newEndTime.equals(newStartTime)) {
            JOptionPane.showMessageDialog(frame, "End time must be after start time.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
          }

          ZonedDateTime startDT = date.atTime(selectedEvent.startTime).atZone(ZoneId.of(calendarTimezones.get(selectedCalendar)));
          ZonedDateTime endDT = date.atTime(selectedEvent.endTime).atZone(ZoneId.of(calendarTimezones.get(selectedCalendar)));

          boolean updated = false;

          if (!newName.equals(selectedEvent.name)) {
            String cmd = commandFactory.editEventCommand("name", selectedEvent.name, startDT, endDT, newName);
            updated |= controller.processCommand(cmd);
          }

          if (!newStartTime.equals(selectedEvent.startTime)) {
            String cmd = commandFactory.editEventCommand(
                    "startdatetime", selectedEvent.name, startDT, endDT,
                    date.atTime(newStartTime).toString()  // no zone
            );
            updated |= controller.processCommand(cmd);
          }

          if (!newEndTime.equals(selectedEvent.endTime)) {
            String cmd = commandFactory.editEventCommand(
                    "enddatetime", selectedEvent.name, startDT, endDT,
                    date.atTime(newEndTime).toString()
            );
            updated |= controller.processCommand(cmd);
          }

          if (!newDesc.isEmpty()) {
            String cmd = commandFactory.editEventCommand("description", selectedEvent.name, startDT, endDT, newDesc);
            updated |= controller.processCommand(cmd);
          }

          if (!newLoc.isEmpty()) {
            String cmd = commandFactory.editEventCommand("location", selectedEvent.name, startDT, endDT, newLoc);
            updated |= controller.processCommand(cmd);
          }

          if (updated) {
            controller.processCommand("print events from " + date + " to " + date.plusMonths(1));
            JOptionPane.showMessageDialog(frame, "Event updated successfully.");
            dialog.dispose();
            showEventDialog(date);
          } else {
            JOptionPane.showMessageDialog(frame, "No changes were made.", "Info", JOptionPane.INFORMATION_MESSAGE);
          }

        } catch (Exception ex) {
          JOptionPane.showMessageDialog(frame, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    });

    editAll.addActionListener(e -> {
      int selectedIndex = eventList.getSelectedIndex();
      if (selectedIndex == -1) {
        JOptionPane.showMessageDialog(frame, "Please select an event to bulk edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
        return;
      }

      List<Event> selectedDayEvents = calendarEvents
              .getOrDefault(selectedCalendar, new HashMap<>())
              .getOrDefault(date, new ArrayList<>());

      if (selectedIndex >= selectedDayEvents.size()) {
        JOptionPane.showMessageDialog(frame, "Invalid selection. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }

      Event selectedEvent = selectedDayEvents.get(selectedIndex);
      String eventNameToMatch = selectedEvent.name;

      JTextField nameField = new JTextField(selectedEvent.name);
      JTextField startField = new JTextField(selectedEvent.startTime.toString());
      JTextField endField = new JTextField(selectedEvent.endTime.toString());

      JPanel editForm = new JPanel(new GridLayout(0, 1));
      editForm.add(new JLabel("New Event Name:"));
      editForm.add(nameField);
      editForm.add(new JLabel("New Start Time (HH:mm):"));
      editForm.add(startField);
      editForm.add(new JLabel("New End Time (HH:mm):"));
      editForm.add(endField);

      int formResult = JOptionPane.showConfirmDialog(frame, editForm,
              "Edit All '" + eventNameToMatch + "' From " + date, JOptionPane.OK_CANCEL_OPTION);

      if (formResult == JOptionPane.OK_OPTION) {
        try {
          String updatedName = nameField.getText().trim();
          LocalTime updatedStart = LocalTime.parse(startField.getText().trim());
          LocalTime updatedEnd = LocalTime.parse(endField.getText().trim());

          if (updatedEnd.isBefore(updatedStart) || updatedEnd.equals(updatedStart)) {
            JOptionPane.showMessageDialog(frame, "End time must be after start time.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
          }

          // Loop over all future dates and update matching events
          for (Map.Entry<LocalDate, List<Event>> entry : calendarEvents.getOrDefault(selectedCalendar, new HashMap<>()).entrySet()) {
            LocalDate eventDate = entry.getKey();
            if (!eventDate.isBefore(date)) {
              List<Event> eventListForDate = entry.getValue();
              for (Event ev : eventListForDate) {
                if (ev.name.equals(eventNameToMatch)) {
                  if (checkConflict(eventListForDate, updatedStart, updatedEnd, eventListForDate.indexOf(ev))) {
                    continue; // 💥 Skip conflicts
                  }
                  ev.name = updatedName;
                  ev.startTime = updatedStart;
                  ev.endTime = updatedEnd;
                }
              }
            }
          }

          updateCalendarView();
          JOptionPane.showMessageDialog(frame, "Recurring events updated from " + date + ".");
          dialog.dispose();
          showEventDialog(date); // Refresh dialog to show updated events
        } catch (Exception ex) {
          JOptionPane.showMessageDialog(frame, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    });

    String timezone = calendarTimezones.getOrDefault(selectedCalendar, ZoneId.systemDefault().toString());
    dialog.getContentPane().add(panel);
    dialog.setSize(520, 400);
    dialog.setLocationRelativeTo(frame);
    dialog.setVisible(true);
  }


  private boolean checkConflict(List<Event> events, LocalTime newStart, LocalTime newEnd, int excludeIndex) {
    for (int i = 0; i < events.size(); i++) {
      if (i == excludeIndex) continue;
      Event ev = events.get(i);
      if (!(newEnd.isBefore(ev.startTime) || newStart.isAfter(ev.endTime))) {
        return true;
      }
    }
    return false;
  }


  private boolean addEventToDate(LocalDate date, Event event) {
    Map<LocalDate, List<Event>> calendarEventMap = calendarEvents.get(selectedCalendar);
    List<Event> eventsForDate = calendarEventMap.getOrDefault(date, new ArrayList<>());

    // 💥 Conflict check
    for (Event ev : eventsForDate) {
      if (!(event.endTime.isBefore(ev.startTime) || event.startTime.isAfter(ev.endTime))) {
        return false; // Conflict found
      }
    }

    // ✅ No conflict
    eventsForDate.add(event);
    calendarEventMap.put(date, eventsForDate);
    return true;
  }

  private void exportCalendarToCSV() {
    JTextField filenameField = new JTextField();
    JPanel panel = new JPanel(new GridLayout(0, 1));
    panel.add(new JLabel("Enter filename (without extension):"));
    panel.add(filenameField);

    int result = JOptionPane.showConfirmDialog(
            frame,
            panel,
            "Export Calendar",
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

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileToSave))) {
          writer.println("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private");

          Map<LocalDate, List<Event>> selectedEvents = calendarEvents.get(selectedCalendar);
          for (Map.Entry<LocalDate, List<Event>> entry : selectedEvents.entrySet()) {
            LocalDate date = entry.getKey();
            for (Event event : entry.getValue()) {
              String line = String.format(
                      "\"%s\",%s,%s,%s,%s,FALSE,,,TRUE",
                      event.name,
                      date.toString(),
                      event.startTime.toString(),
                      date.toString(),
                      event.endTime.toString()
              );
              writer.println(line);
            }
          }

          JOptionPane.showMessageDialog(frame, "Calendar exported to: " + fileToSave.getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {
          JOptionPane.showMessageDialog(frame, "Error exporting calendar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
    calendarEvents.clear();

    for (ICalendarEvent e : events) {
      ZonedDateTime start = ZonedDateTime.from(e.getStartDateTime());
      LocalDate date = start.toLocalDate();
      LocalTime startTime = start.toLocalTime();
      LocalTime endTime = ZonedDateTime.from(e.getEndDateTime()).toLocalTime();
      String calName = selectedCalendar;

      Event guiEvent = new Event(e.getSubject(), startTime, endTime);
      calendarEvents
              .computeIfAbsent(calName, k -> new HashMap<>())
              .computeIfAbsent(date, d -> new ArrayList<>())
              .add(guiEvent);
    }

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
