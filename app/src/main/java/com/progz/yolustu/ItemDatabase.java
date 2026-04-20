package com.progz.yolustu;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ShoppingItem.class}, version = 1, exportSchema = false)
public abstract class ItemDatabase extends RoomDatabase {

    // Link the Waiter (DAO) to the Kitchen (Database)
    public abstract ShoppingItemDao shoppingItemDao();

    private static volatile ItemDatabase INSTANCE;

    public static ItemDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (ItemDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    ItemDatabase.class, "yolustu_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}