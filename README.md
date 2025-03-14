# Calendar Application

This project involves designing and implementing a virtual calendar application. The goal is to mimic core features commonly found in popular calendar applications like Google Calendar or Apple's iCalendar.

## Key Features

- **Event Management:**
    - Add and manage single and recurring events.
    - Edit event properties, including start/end times, descriptions, locations, and recurrence patterns.

- **Event Querying:**
    - Retrieve events based on specific dates or date/time ranges.
    - Check availability at particular dates and times.

- **Export Functionality:**
    - Export calendar data to a CSV format compatible with standard calendar applications.

## Architecture

The project follows the **Model-View-Controller (MVC)** architecture, clearly separating responsibilities:
- **Model:** Handles data representation and core calendar logic.
- **View:** Manages presentation of calendar data.
- **Controller:** Processes user input and coordinates interactions between Model and View.

## Technical Concepts

This project demonstrates essential programming principles:

- **Abstraction and Abstract Data Types:**
    - Interfaces and abstract classes (e.g., `CalendarEvent`, `AbstractCalendarEvent`) encapsulate event details and define clear interactions.

- **Command Pattern:**
    - Implementation of command objects (`CreateEventCommand`, `EditEventCommand`, `ExportCalendarCommand`) decouples operation invocation from execution.

- **Dynamic Dispatch:**
    - Leveraging dynamic dispatch techniques to handle various command types, enhancing flexibility and maintainability.

- **Functional Programming:**
    - Utilizing lambda expressions for concise and efficient command processing within the controller.

- **Modular Design:**
    - Emphasizes modularity to facilitate future enhancements and scalability.

## How to Run

### Compilation

1. Navigate to the project's root directory in a terminal.
2. Compile Java source files:
   ```bash
   javac -d target/classes src/calendarapp/**/*.java
   ```

### Running the Application

The main entry point is the `CalendarApp` class. The application supports two modes:

#### Interactive Mode

Allows users to type commands directly and view immediate feedback.

Run in interactive mode:
```bash
java -cp target/classes calendarapp.CalendarApp --mode interactive
```

Type commands at the prompt, and use `exit` to quit.

##### Supported Commands

- **Create Event:**
  ```
  create event --autoDecline <eventName> from <dateTime> to <dateTime>
  ```

- **Create Recurring Event:**
  ```
  create event --autoDecline <eventName> from <dateTime> to <dateTime> repeats <weekdays> for <N> times
  create event --autoDecline <eventName> from <dateTime> to <dateTime> repeats <weekdays> until <dateTime>
  ```

- **Edit Single Event:**
  ```
  edit event <property> <eventName> from <dateTime> to <dateTime> with <NewValue>
  ```

- **Edit Recurring Event:**
  ```
  edit events <property> <eventName> from <dateTime> with <NewValue>
  edit events <property> <eventName> <NewValue>
  ```

- **Query Events:**
  ```
  print events on <date>
  print events from <dateTime> to <dateTime>
  show status on <dateTime>
  ```

- **Export Calendar:**
  ```
  export cal <filename.csv>
  ```

#### Headless Mode

Executes commands sequentially from a provided file:
```bash
java -cp target/classes calendarapp.CalendarApp --mode headless <commands-file>
```
Replace `<commands-file>` with your command file path.

## Team Contributions

The project was collaboratively developed by two contributors:

- **Nikhil:**
    - Single and recurring events, command parsing, conflict checking, calendar model, and view implementation.

- **Nisha:**
    - Event editing, event querying, controller management, command execution, and utilities.

Both contributors jointly developed comprehensive test cases.
