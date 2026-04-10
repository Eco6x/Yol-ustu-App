package com.progz.yolustu;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ShoppingItemDao {
    // Command to save a new item
    @Insert
    void insert(ShoppingItem item);

    // Command to read the list
    @Query("SELECT * FROM shopping_items")
    List<ShoppingItem> getAllItems();
}