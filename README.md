# Calendar Application

This project involves designing and implementing a virtual calendar application. The goal is to mimic features commonly found in widely used calendar apps such as Google Calendar or Apple's iCalendar.

The key features implemented in this version of the application include adding and managing single and recurring events, querying events based on dates or time ranges, checking availability, and exporting calendar data to a CSV format.

This project follows the Model-View-Controller (MVC) architecture, and demonstrates key programming concepts including abstraction, dynamic dispatch, and the use of abstract data types.

## How to Run

### Compilation

1. Open a terminal in the project’s root directory.
2. Compile all Java source files. For example:
   ```bash
    javac -d target/classes $(find . -name "*.java")

#### Running the Application

The main entry point of the application is the CalendarApp class.

### Interactive Mode

Run the application in interactive mode using:
```bash
   java -cp target/classes calendarapp.CalendarApp --mode interactive
   ```
After launching, you can type commands at the prompt. Type exit to quit.

#### Supported Commands

##### To create a new event: 

create event --autoDecline \<eventName> from \<dateStringTtimeString> to \<dateStringTtimeString>

##### To create recurring events: 

create event --autoDecline \<eventName> from \<dateStringTtimeString> to \<dateStringTtimeString> repeats \<weekdays> for \<N> times

create event --autoDecline \<eventName> from \<dateStringTtimeString> to \<dateStringTtimeString> repeats \<weekdays> until \<dateStringTtimeString>

##### To edit event:
edit event \<property> \<eventName> from \<dateStringTtimeString> to \<dateStringTtimeString> with \<NewPropertyValue>

#### To query the calendar:
print events on \<dateString>

export cal fileName.csv

show status on \<dateStringTtimeString>

### Headless Mode

Run the application in headless mode (reading commands from a file) using:
```bash
   java -cp target/classes calendarapp.CalendarApp --mode headless <commands-file>
```
Replace <commands-file> with the path to your commands file.

## Working Features
#### Event Creation:
Create single events and recurring events.
Specify details such as start and end date/time, description, location, public/private status, and all-day events.
#### Event Querying:
Query events on a specific date.
Query events within a given date and time range.
Check if the calendar is busy at a specific time.
#### Event Editing:
Edit a single event occurrence.
Edit events from a specific date/time onward.
Edit all occurrences of an event.
Edit recurring events.
#### Exporting:
Export calendar events to a CSV file compatible with Google Calendar.

[//]: # (### Areas for Improvement)

[//]: # (#### Conflict Resolution:)

[//]: # (The auto-decline mechanism for conflicting events is basic and could be improved.)

[//]: # (#### Command Parsing:)

[//]: # (The command parser works for most cases, but further refinement could improve usability.)

## Team Contributions

#### Nisha:
Contributed to the core model, controller, and event processing logic.
Implemented the MVC architecture and command dispatch system.
Focused on abstraction, error handling, and overall application design.
#### Nikhil:
Worked on the view layer and command parsing logic.
Developed utility classes (e.g., CSV exporting and helper functions).
Contributed to the design and implementation of recurring event handling.
Key Concepts and Design Patterns

## Concepts covered in the project:
#### Abstraction and Abstract Data Types:
The project uses interfaces and abstract classes (such as CalendarEvent and AbstractCalendarEvent) to encapsulate event details and provide clear contracts.
#### Command Pattern:
Commands such as CreateEventCommand, EditEventCommand, and ExportCalendarCommand are used to decouple the execution of operations from the invoker.
#### Model-View-Controller (MVC):
The project is structured using the Model-View-Controller (MVC) design pattern. The Model handles the calendar data and logic, the View displays the information, and the Controller processes user inputs and updates the Model and View accordingly.
#### Dynamic Dispatch:
Methods such as processCommand use dynamic dispatch to handle various types of commands, like creating events, querying events, etc.
#### Other Concepts:
The use of lambda expressions and functional programming techniques in the controller.
Emphasis on modular design to facilitate future enhancements.


