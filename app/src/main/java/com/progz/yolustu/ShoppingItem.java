package com.progz.yolustu;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "shopping_items")
public class ShoppingItem {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String itemName;
    public String marketName;
}
