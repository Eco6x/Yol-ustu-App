# Yol Üstü: Location-Based Grocery Reminder

**Video Demo:** Coming soon! :)

## Project Description

"Yol Üstü" is a location-aware Android application designed to bridge the gap between digital shopping lists and the physical world. 
Forgetting essential items like bread or milk is a common frustration especially for students living abroad, often happening because of responsiblities students have during their daily lives. 
Yol Üstü solves this by utilizing geographic proximity. Instead of reminding a user to buy groceries at 5:00 PM when they might still be at work, the app triggers a push notification precisely when they walk or drive past a targeted supermarket chain, such as BİM, Şok, or A101.

The core functionality revolves around a clean, distraction-free user interface where users can quickly input items and associate them with specific store brands. Behind the scenes, the application leverages background location tracking to monitor the user's movement relative to pre-defined coordinates. When the device's GPS detects that the user has crossed the boundary of a "geofence" around a selected store, a local broadcast is triggered, alerting the user to check their list. 

This project was developed as part of our Software Architecture coursework at Altınbaş University. It required us to integrate several complex Android concepts, including background services, dynamic permission requests, local data persistence, and efficient API utilization. The end result is a practical, lightweight utility tool that demonstrates a strong understanding of mobile software architecture and user-centric design.

## File Structure & Descriptions

Below is a breakdown of the core files written for this project and their specific responsibilities:

* `MainActivity.java`: The main entry point of the application. It initializes the UI, checks for necessary location permissions, and hosts the `RecyclerView` that displays the active shopping list.

* `GeofenceReceiver.java`: A `BroadcastReceiver` that runs in the background. It listens for transition events from the Google Play Services API (specifically, entering a geofenced area) and triggers the local push notification.


* `ItemDatabase.java`: Contains the Room Database configuration. It defines the SQLite database instance and provides the Data Access Object (DAO) connections.

* `ShoppingItem.java`: The entity class representing a single grocery item. It defines the table structure, including columns for the item name, associated store, and a boolean for its completed status.

* `ListAdapter.java`: Manages the data binding for the user interface. It takes the list of items from the database and inflates the individual XML row layouts for the main screen.

* `activity_main.xml`: The primary frontend layout file, designed using Material Design guidelines to provide a clean and intuitive user experience.

## Design Choices & Technical Debates

During development, our team faced several architectural decisions. 

**Battery Optimization vs. Accuracy:** The most significant debate was how to handle location tracking. Initially, we considered using standard GPS polling to constantly check the user's coordinates. However, we realized this would severely drain the device's battery and lead to uninstalls. We opted to use the Google Play Services Geofencing API instead. This hands the heavy lifting over to the Android OS, waking our app up only when a boundary is crossed, ensuring a balance between reliability and battery life.

**Data Storage Options:** We debated whether to use SharedPreferences, raw SQLite, or the Room Persistence Library for storing the shopping list. SharedPreferences was too limited for complex data (like linking items to specific coordinates). While raw SQLite would work, it required extensive boilerplate code. We ultimately chose Room because it provides compile-time verification of SQL queries and integrates smoothly with modern Android architecture patterns, reducing bugs and speeding up our development process.
