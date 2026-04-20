package com.progz.yolustu;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "shopping_items")
public class ShoppingItem {

    // Room needs this ID to keep track of items
    @PrimaryKey(autoGenerate = true)
    public int id;

    private String name;
    private String market;
    private boolean isCompleted;

    public ShoppingItem(String name, String market) {
        this.name = name;
        this.market = market;
        this.isCompleted = false;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMarket() { return market; }
    public void setMarket(String market) { this.market = market; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
}