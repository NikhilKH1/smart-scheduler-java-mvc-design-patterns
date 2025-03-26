# Calendar Application

This project is an enhanced version of a calendar application that allows users to manage multiple calendars, handle timezones, create and edit events, and copy events between calendars. The application follows the Model-View-Controller (MVC) design pattern and utilizes abstraction, dynamic dispatch, and the command pattern.

## Design Changes and Justifications

1. **Moved the `commands` package from model to controller**
* **Justification**: Command execution is a controller-level concern, not a model-level one. This change improves the separation of concerns.

2. **Created a new package for io**
* **Justification**: Previously, input handling was tightly coupled with the terminal scanner. This change modularizes input handling, making it more adaptable.

### Multiple Calendar Support

3. **Created `CalendarManager` and `ICalendarManager`**
* **Justification**: Manages multiple calendars, allowing users to switch between them efficiently.

4. **Added new command classes to support calendar creation, usage, and editing**
* **Justification**: Extends command-driven control to handle multiple calendars.

5. **Separated calendar-level and event-level command interfaces**
* **Justification**: Improves modularity and distinguishes between commands acting on calendars and those acting on events.

### Timezone Support

6. **Created an `Exporter` interface**
* **Justification**: Enables support for multiple export formats such as PDF and TXT in the future.

7. **Changed `LocalDateTime` to `Temporal`**
* **Justification**: Allows seamless support for multiple timezones without breaking existing functionality.

## How to Run the Program

### Running from the Terminal (Using JAR File)

1. Ensure that you have Java installed (Java 11 or later recommended).
2. Open a terminal and navigate to the directory containing the `calendarapp.jar` file.
3. Run the application in interactive mode:
   ```bash
   java -jar calendarapp.jar --mode interactive
- This mode allows users to input commands interactively.

4. Run the application in headless mode (executing commands from a file):

    ``` bash
    java -jar calendarapp.jar --mode headless <commands-file>
- Replace <commands-file> with the path to a text file containing valid commands.

## Working Features
All features from the previous version, plus:

### Multiple Calendars

Users can create, use, and edit multiple named calendars.

### Timezone Support

Each calendar has its own timezone.

Events are automatically adjusted when changing timezones.

### Event Copying

Users can copy events within the same calendar or across different calendars.

## Additional commands supported:

- create calendar --name \<calName> --timezone area/location


- edit calendar --name \<name-of-calendar> --property \<property-name> \<new-property-value>


- use calendar --name \<name-of-calendar>


- copy event \<eventName> on \<date> --target \<calendarName> to \<date>


- copy events on \<date> --target \<calendarName> to \<date>


- copy events between \<startDate> and \<endDate> --target \<calendarName> to \<startDate>

## Team Contributions
### Nikhil:

- Implemented event copying within and across calendars.

- Refined the command set.

- Adjusted the autoDecline behavior.

### Nisha:

- Designed and implemented multiple calendar and timezone support.

- Developed commands for creating, using, and editing calendars.

### Both:

- Collaborated on testing to ensure functionality and correctness.




