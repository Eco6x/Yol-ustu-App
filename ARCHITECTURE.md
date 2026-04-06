# Title Page

## Change History
## Table of Contents
## List of Figures

## 1. Scope
## 2. References
## 3. Software Architecture
## 4. Architectural Goals & Constraints
## 5. Logical Architecture
Our logical architecture follows the Model-View-Controller (MVC) pattern to separate the application's internal data from the user interface. This separation of concerns ensures that our location tracking logic does not interfere with the UI thread.

The Model: Represents the application's data layer. We utilize a local SQLite database (managed via Android Room) to define the ShoppingItem entities. This layer handles the storage and retrieval of user-entered grocery items and their associated market locations.

The View: Represents the user interface. Built using Android XML layouts (activity_main.xml) and a RecyclerView with a custom ListAdapter, this layer strictly observes the data and renders the current shopping list to the user. It contains no heavy business logic.

The Controller: Acts as the bridge between the View and the Model. MainActivity.java captures user input from the UI and commands the Model to update the database. Additionally, our GeofenceReceiver.java acts as an event-driven controller, listening for location broadcasts from the Android OS to trigger background notifications.
## 6. Process Architecture

## 7. Development Architecture
The development architecture defines the software's static organization. For Yol Üstü, we utilize a standard Android Gradle build system structure.
* **Data Persistence Layer:** We implement the Android Architecture Components Room library as an abstraction layer over SQLite. This ensures robust local data storage for our `ShoppingItem` entities and provides compile-time verification of SQL queries, minimizing runtime database crashes.



## 8. Physical Architecture
The physical architecture maps the software components to the hardware of the mobile device. Yol Üstü operates entirely on the user's Android smartphone without relying on external cloud servers for core business logic.
* Device Hardware: The application interfaces directly with the device's physical GPS receiver and location sensors.
* Power Management: To mitigate the high battery drain typical of continuous GPS polling, the application utilizes the hardware's low-power geofencing capabilities. The Android OS offloads the boundary monitoring to the physical modem/sensor hub, waking the main CPU only when a geographic threshold is crossed.
* Storage: Data is persisted physically on the device's internal flash memory using the Room SQLite database.
## 9. Scenarios
To validate our architecture, we define the following core scenario (the "+1" of our view model), which illustrates how the logical, process, development, and physical views interact during a standard user journey:

Scenario 1: Adding an Item and Triggering a Geofence Notification

User Input (Logical/View): The user opens the application and types "Milk" into the activity_main.xml input field and selects "BİM" as the target market.

Data Storage (Development/Model): The controller (MainActivity.java) receives this input and writes a new ShoppingItem record into the local SQLite database.

Hardware Registration (Physical): The application registers a geofence with the physical device's GPS hardware using the predefined coordinates for the selected market. The user then closes the application.

Background Processing (Process): The application enters an idle state. Later, when the user physically walks within a 100-meter radius of the BİM coordinates, the Android OS broadcasts a location event.

Event Handling & Notification (Process/Controller): The GeofenceReceiver.java wakes up in the background, intercepts the broadcast, and pushes a high-priority notification to the user's lock screen reminding them to buy "Milk".
## 10. Size and Performance
## 11. Quality

## Appendices

### Acronyms and Abbreviations
### Definitions
### Design Principles






