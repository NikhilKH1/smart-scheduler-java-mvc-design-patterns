# Calendar Application

This project is a command-line calendar application that supports creating, editing, querying, and exporting calendar events. The application follows the Model-View-Controller (MVC) architecture and employs various design patterns and programming concepts such as abstraction, dynamic dispatch, and abstract data types.

## How to Run

### Prerequisites

- Java 8 or higher must be installed.
- Ensure that your JAVA_HOME environment variable is set correctly.

### Compilation

1. Open a terminal in the project’s root directory.
2. Compile all Java source files. For example:
   ```bash
   javac -d bin $(find . -name "*.java")
This command compiles all Java files and outputs the class files to the bin directory.

Running the Application
The main entry point of the application is the CalendarApp class.

### Interactive Mode

Run the application in interactive mode using:
```bash
   java -cp bin calendarapp.CalendarApp --mode interactive
   ```
After launching, you can type commands at the prompt. Type exit to quit.

### Headless Mode

Run the application in headless mode (reading commands from a file) using:
```bash
   java -cp bin calendarapp.CalendarApp --mode headless <commands-file>
```
Replace <commands-file> with the path to your commands file.

### Features

#### Working Features
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
Edit recurring events (e.g., update repeat count, repeat until date, or repeating days).
#### Exporting:
Export calendar events to a CSV file compatible with Google Calendar.
### Areas for Improvement
#### Conflict Resolution:
The auto-decline mechanism for conflicting events is basic and could be improved.
#### Error Handling:
Additional error handling for edge cases (such as incorrect date formats) would enhance the application.
#### Command Parsing:
The command parser works for most cases, but further refinement could improve usability.

### Team Contributions

#### Nisha:
Contributed to the core model, controller, and event processing logic.
Implemented the MVC architecture and command dispatch system.
Focused on abstraction, error handling, and overall application design.
#### Nikhil:
Worked on the view layer and command parsing logic.
Developed utility classes (e.g., CSV exporting and helper functions).
Contributed to the design and implementation of recurring event handling.
Key Concepts and Design Patterns

### Abstraction and Abstract Data Types:
The project uses interfaces and abstract classes (such as CalendarEvent and AbstractCalendarEvent) to encapsulate event details and provide clear contracts.
### Command Pattern:
Commands such as CreateEventCommand, EditEventCommand, and ExportCalendarCommand are used to decouple the execution of operations from the invoker.
### Model-View-Controller (MVC):
The project is structured around the MVC architecture, separating the model (business logic), view (user interface), and controller (command processing).
### Dynamic Dispatch:
The application leverages dynamic dispatch to handle various event types and command types at runtime.
### Other Concepts:
The use of lambda expressions and functional programming techniques in the controller.
Emphasis on modular design to facilitate future enhancements.


