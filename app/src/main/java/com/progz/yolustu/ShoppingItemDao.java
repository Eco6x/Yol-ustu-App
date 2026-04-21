package com.progz.yolustu;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ShoppingItemDao {

    // Command to save a new item
    @Insert
    void insert(ShoppingItem item);

    // Command to update an item (used when Ammar clicks the checkbox)
    @Update
    void update(ShoppingItem item);

    // Command to delete an item
    @Delete
    void delete(ShoppingItem item);

    // Command to read the whole list
    @Query("SELECT * FROM shopping_items")
    List<ShoppingItem> getAllItems();
}