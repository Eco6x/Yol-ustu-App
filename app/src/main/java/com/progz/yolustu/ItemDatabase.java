package com.progz.yolustu;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {ShoppingItem.class}, version = 1)
public abstract class ItemDatabase extends RoomDatabase {
    // We will add the database access functions here later
}