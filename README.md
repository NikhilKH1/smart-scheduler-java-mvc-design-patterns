# Calendar Application

This project is an enhanced version of a calendar application that allows users to manage multiple calendars, handle timezones, create and edit events, and copy events between calendars. The application follows the Model-View-Controller (MVC) design pattern and utilizes abstraction, dynamic dispatch, and the command pattern.

## Design Changes and Justifications

#### Multiple Calendar Support

1. **Added ICalendarManager and CalendarManager**
* **Justification**:  Enables the management of multiple calendars within the application. This allows users to create, use, and edit named calendars efficiently.

2. **Separated calendar-level and event-level command interfaces**
* **Justification**: Introduced ICalendarManagerCommand and ICalendarModelCommand, both extending a base ICommand interface which remains unchanged from Assignment 4. This clearly distinguishes commands that operate on calendars (e.g., create, use, edit calendar) from those that operate on events (e.g., create, edit, query events), improving scalability and modularity.

3. **Added new command classes (CreateCalendarCommand, UseCalendarCommand, EditCalendarCommand)**
* **Justification**: The project continues to follow the Command Design Pattern from Assignment 4. In this assignment, we built upon that approach to support additional features, such as multi-calendar functionality. By extending the existing command structure, we were able to introduce new commands for calendar-level operations without modifying the core logic of the application.

#### Timezone Support

4. **Replaced LocalDateTime with the abstract Temporal type throughout the model and event classes**
* **Justification**: Enables compatibility with both ZonedDateTime and other Temporal types. This abstraction allows for timezone-aware event creation, editing, and querying without breaking backward compatibility.

#### Modular IO and Input Handling
5. **Created an io package with ICommandSource, ConsoleCommandSource, and FileCommandSource**
* **Justification**: Decouples input sources from core logic. This modular structure allows input to come from various sources (e.g., console, files), making the app extensible for GUI or web interfaces in the future.

#### Exporter Extensibility
6. **Introduced the IExporter interface**
* **Justification**: Enabling future extensions with minimal modifications. In Assignment 4, we had a CSV exporter within the utils package. In this assignment, we introduced an Exporter interface to support different formats, making it easier to add new export types like PDF or TXT when needed.

## How to Run the Program

### Running from the Terminal (Using JAR File)

1. Ensure that you have Java installed (Java 11).
2. Open a terminal and navigate to the directory containing the `calendarapp.jar` file.
3. Run the application in interactive mode:


    java -jar calendarapp.jar --mode interactive

- This mode allows users to input commands interactively.

4. Run the application in headless mode:
 
- To run Valid Commands file:


    java -jar calendarapp.jar --mode headless ValidCommands.txt
- To run Invalid Commands file:


    java -jar calendarapp.jar --mode headless InvalidCommands.txt

## Working Features
### New Features Added:

### Multiple Calendars

Users can create, use, and edit multiple named calendars.

### Timezone Support

Each calendar has its own timezone.

Events are automatically adjusted when changing timezones.

### Event Copying

Users can copy events within the same calendar or across different calendars.

### Existing Features
#### Event Creation:
Create single events and recurring events.
Specify details such as start and end date/time, description, location, public/private status, and all-day events.
#### Event Querying:
Query events on a specific date.
Query events within a given date and time range.
Check if busy at a specific time.
#### Event Editing:
Edit the properties like event name, description, location, startdatetime and enddatetime for single event.
Edit the additional properties like repeatuntil, repeatingdays, repeattimes for recurring event.
#### Exporting:
Export calendar events to a CSV file compatible with Google Calendar.
The folder for testing the export command is placed in test/res folder. Please use them before running the CSVExporterTest.


## Additional commands supported:

##### To create a new calendar:
- create calendar --name \<calName> --timezone area/location

##### To edit a calendar:
- edit calendar --name \<name-of-calendar> --property \<property-name> \<new-property-value>

##### To use a calendar:
- use calendar --name \<name-of-calendar>

##### To copy event/events from one calendar to another/same calendar:
- copy event \<eventName> on \<date> --target \<calendarName> to \<date>


- copy events on \<date> --target \<calendarName> to \<date>


- copy events between \<startDate> and \<endDate> --target \<calendarName> to \<startDate>

## Team Contributions
### Nikhil:

- Implemented event copying within and across calendars.
- Refined the command set.
- Adjusted the autoDecline behavior.

### Nisha:

- Designed and implemented multiple calendars.
- Timezone support across multiple calendars.
- Commands for creating, using, and editing calendars.

### Both:

- Jointly worked on writing the test cases.

## Concepts covered in the project:

#### Abstraction and Abstract Data Types:
The project uses interfaces and abstract classes (such as ICalendarEvent and AbstractCalendarEvent) to encapsulate event details and provide clear contracts.
#### Command Design Pattern:
In this project, we used commands like CreateEventCommand, EditEventCommand, and CopyEventsBetweenDatesCommand to separate the logic of executing operations from the part of the system that invokes them. This approach was also used in the previous assignment. It allowed us to easily add new features, like supporting multiple calendars, by simply creating new command classes without affecting the existing code.
#### Model-View-Controller (MVC):
The project is structured using the Model-View-Controller (MVC) design pattern. The Model handles the calendar data and logic, the View displays the information, and the Controller processes user inputs and updates the Model and View accordingly.
#### Dynamic Dispatch:
Methods such as processCommand use dynamic dispatch to handle various types of commands, like manage, edit and use calendars and also creating events, querying events, etc.
#### Factory Pattern:
In the project, command objects and calendar models are created based on the input received. This approach helps manage the creation of different objects and ensures that the right type of object is used for the given task, following the idea of a factory. This makes it easier to add or modify the types of objects without changing the core logic.
#### Other Concepts:
Emphasis on modular design to facilitate future enhancements.


