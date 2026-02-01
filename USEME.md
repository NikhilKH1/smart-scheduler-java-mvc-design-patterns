# USEME: Using the Calendar Application GUI

This document provides a step-by-step guide on how to use each feature supported by the Calendar Application GUI. The application is designed to be intuitive and user-friendly, with support for multiple calendars, timezone management, event creation, editing, and import/export functionality.

---

## Launching the Application

- Run the application by executing the provided JAR file from the terminal:
  ```bash
  java -jar CalendarApp.jar

On launch, a default calendar opens automatically. It is based on your system’s current timezone.

### Running from IntelliJ IDEA:

- You can also run the application directly from IntelliJ in three different modes:

### GUI Mode:
   - Open CalendarApp.java.


   - Click the green run button next to the main method or right-click and select Run 'CalendarApp.main()'.


   - The application will launch with the GUI interface.

### Interactive Mode:
##### Open Run Configuration: 
- Go to Run > Edit Configurations

##### Create a New Configuration:
- Click on the + symbol at the top-left corner.


- Select Application from the list.


- Give a name to the configuration (e.g., CalendarApp Interactive).

##### Set Main Class and Classpath:

- Under Build and run, choose the appropriate classpath/module.


- Set the Main class to CalendarApp.
#### Set Program Arguments:
- In the Program arguments field, enter:
```
--mode interactive
```

##### Run the Program:
Click Apply and then OK.
##### Run the configuration.
- You’ll be prompted to enter commands via the terminal.

### Headless Mode:

##### Open Run Configuration:
- Go to Run > Edit Configurations

##### Create a New Configuration:
- Click on the + symbol at the top-left corner.


- Select Application from the list.


- Give a name to the configuration (e.g., CalendarApp Headless).

##### Set Main Class and Classpath:

- Under Build and run, choose the appropriate classpath/module.


- Set the Main class to CalendarApp.
#### Set Program Arguments:
- In the Program arguments field, enter:
```
--mode headless path/to/your/script.txt
```
- Replace path/to/your/script.txt with the actual path to your command script.
##### Run the Program:
Click Apply and then OK.

##### Run the configuration.
- The application will execute all commands from the file silently and terminate when done.

## Adding a New Calendar
- Click the plus (+) button on the top-left corner.


- A dialog box will appear. Enter the calendar name and select the timezone from the dropdown.


- Click OK to create the calendar.

 Each calendar is uniquely color-coded for easy identification.

## Selecting a Calendar

- Use the dropdown menu next to the plus button to switch between calendars.


- The name of the currently selected calendar is displayed prominently at the top.

## Change Timezone:
- There is a button next to the calendar dropdown labeled "Change Timezone".


- When clicked, a dialog opens where you can select a different timezone.


- After selecting your preferred timezone and clicking OK, the calendar updates to reflect the new timezone.

## Navigating Between Months
- Use the left (◀) and right (▶) arrows located beside the month name to navigate to previous or next months.

## Adding an Event
- Click on any date box in the calendar view.


- A dialog with three options appears: choose "Add Event."


- Enter the event details in the form. 


- Choose Single option if you are creating a single event or Recurring option if you are creating a recurring event.


- For recurring event, additional recurring details should be filled.


- Click Ok to add the event.

## Editing an Event

- Click on the date containing the event.


- Select the event from the list.


- Click "Edit Selected."


- Choose the property to be edited from the Dropdown.


- Click OK to edit the event.

## Editing All Matching Events

- Click on the date containing the event.


- Select the event from the list.


- Click "Edit All Matching"


- Changes will be applied to all future occurrences of the selected recurring event.


- Choose the property to be edited from the Dropdown.


- Click OK to edit the event.

## Viewing Events

- All events are displayed directly within the calendar grid.


- To know more details of an event, click on the date and double click on th event. All the information related to that event will be displayed.

## Exporting Events:

- Click the Export button.


- Enter the filename, choose the destination folder, and click Export.


- A CSV file compatible with Google Calendar will be generated.

## Importing Events:

- Click the Import button.


- Navigate to the CSV file you wish to import.


- The events will be added to the currently selected calendar.