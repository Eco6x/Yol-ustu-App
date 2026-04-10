# 📍 Yol Üstü:  Location-Based Grocery Reminder

🎞**Video Demo:** Coming soon! :)

## ✍🏻 Project Description

"Yol Üstü" is a location-aware Android application designed to bridge the gap between digital shopping lists and the physical world. 
Forgetting essential items like bread or milk is a common frustration especially for students living abroad, often happening because of responsiblities students have during their daily lives. 
Yol Üstü solves this by utilizing geographic proximity. Instead of reminding a user to buy groceries at 5:00 PM when they might still be at work, the app triggers a push notification precisely when they walk or drive past a targeted supermarket chain, such as BİM, Şok, or A101.

The core functionality revolves around a clean, distraction-free user interface where users can quickly input items and associate them with specific store brands. Behind the scenes, the application leverages background location tracking to monitor the user's movement relative to pre-defined coordinates. When the device's GPS detects that the user has crossed the boundary of a "geofence" around a selected store, a local broadcast is triggered, alerting the user to check their list. 

This project was developed as part of our Software Architecture coursework at Altınbaş University. It required us to integrate several complex Android concepts, including background services, dynamic permission requests, local data persistence, and efficient API utilization. The end result is a practical, lightweight utility tool that demonstrates a strong understanding of mobile software architecture and user-centric design.

## 📚 File Structure & Descriptions

Below is a breakdown of the core files written for this project and their specific responsibilities:

* `MainActivity.java`: The main entry point of the application. It initializes the UI, checks for necessary location permissions, and hosts the `RecyclerView` that displays the active shopping list.

* `GeofenceReceiver.java`: A `BroadcastReceiver` that runs in the background. It listens for transition events from the Google Play Services API (specifically, entering a geofenced area) and triggers the local push notification.


* `ItemDatabase.java`: Contains the Room Database configuration. It defines the SQLite database instance and provides the Data Access Object (DAO) connections.

* `ShoppingItem.java`: The entity class representing a single grocery item. It defines the table structure, including columns for the item name, associated store, and a boolean for its completed status.

* `ListAdapter.java`: Manages the data binding for the user interface. It takes the list of items from the database and inflates the individual XML row layouts for the main screen.

* `activity_main.xml`: The primary frontend layout file, designed using Material Design guidelines to provide a clean and intuitive user experience.

##  🛠️ Design Choices & Technical Debates

During development, our team faced several architectural decisions. 

**Balancing Battery Life and Precision**
* Our primary technical hurdle involved optimizing location tracking. While we initially weighed the merits of continuous GPS polling for high-resolution coordinates, it became clear that the resulting power consumption would lead to a poor user experience and high churn. To solve this, we integrated the Google Play Services Geofencing API. By offloading the monitoring to the Android system and only triggering the app when specific boundaries are breached, we achieved a sustainable equilibrium between notification accuracy and battery conservation.

**Evaluating Local Storage Solutions**
* We also carefully considered whether to utilize SharedPreferences, standard SQLite, or the Room Persistence Library for managing shopping data. SharedPreferences proved insufficient for the complex relational requirements of linking items to geographic data, and while raw SQLite was a viable engine, the manual overhead was excessive. We ultimately selected Room; its ability to provide compile-time query validation and its seamless fit with modern Android design patterns allowed us to minimize structural bugs and significantly shorten our development cycle.


📔For a detailed breakdown of our system design, please read the [Architecture.md File](ARCHITECTURE.md).📔

## 👨‍💻 Team Members
| Studend ID | Name | GitHub Username |
|------------|------|-----------------|
| 210513474 | Ammar Hajar | Eco6x |
| 210513413 | Mohammad Yaseen | muhammedeluveyfi-prog|
| 230513563 | Abdulkadir Janabi | FX-VXI |
| 230513382 | Taha Hatahet | Tahaaaaaaaaaaaaaaaaaaa |
