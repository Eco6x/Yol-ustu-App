package com.progz.yolustu;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etItemName;
    private AutoCompleteTextView spinnerMarket;
    private FloatingActionButton fabAdd;
    private RecyclerView rvShoppingList;
    private LinearLayout emptyState;
    
    private ListAdapter adapter;
    private String[] defaultMarkets = new String[]{"BİM", "A101", "Şok", "Migros"};

    private ItemDatabase db;
    private ExecutorService executorService;
    
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Views
        etItemName = findViewById(R.id.etItemName);
        spinnerMarket = findViewById(R.id.spinnerMarket);
        fabAdd = findViewById(R.id.fabAdd);
        rvShoppingList = findViewById(R.id.rvShoppingList);
        emptyState = findViewById(R.id.emptyState);

        // Setup Dropdown
        ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, defaultMarkets);
        spinnerMarket.setAdapter(dropdownAdapter);

        // Setup Database & Executor
        executorService = Executors.newSingleThreadExecutor();
        db = ItemDatabase.getInstance(this);

        // Setup RecyclerView
        adapter = new ListAdapter(new ListAdapter.OnItemInteractionListener() {
            @Override
            public void onUpdate(ShoppingItem item) {
                executorService.execute(() -> {
                    db.shoppingItemDao().update(item);
                });
            }

            @Override
            public void onDelete(ShoppingItem item) {
                executorService.execute(() -> {
                    db.shoppingItemDao().delete(item);
                    loadItems();
                });
            }
        });
        rvShoppingList.setLayoutManager(new LinearLayoutManager(this));
        rvShoppingList.setAdapter(adapter);

        // Initial empty state check and load
        loadItems();
        
        // Request Permissions
        checkLocationPermissions();
        
        // Setup Add Button
        fabAdd.setOnClickListener(v -> {
            String itemName = etItemName.getText().toString().trim();
            String marketText = spinnerMarket.getText().toString().trim();
            
            if (itemName.isEmpty()) {
                Toast.makeText(this, "Please enter an item name.", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (marketText.isEmpty()) {
                marketText = "Genel"; // Provide "General" fallback
            }

            // Create item and add via Background Thread
            ShoppingItem newItem = new ShoppingItem(itemName, marketText);
            executorService.execute(() -> {
                db.shoppingItemDao().insert(newItem);
                loadItems();
            });
            
            // UI reset
            etItemName.setText("");
            etItemName.clearFocus();
            spinnerMarket.clearFocus();
            
            // Hide keyboard
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            
            // Show Feedback
            Snackbar.make(findViewById(R.id.main), "Item added to " + marketText, Snackbar.LENGTH_SHORT).show();
        });
    }

    private void loadItems() {
        if (executorService != null) {
            executorService.execute(() -> {
                List<ShoppingItem> items = db.shoppingItemDao().getAllItems();
                runOnUiThread(() -> {
                    adapter.setItems(items);
                    updateEmptyState();
                    if (items.size() > 0) {
                        rvShoppingList.smoothScrollToPosition(items.size() - 1);
                    }
                });
            });
        }
    }
    
    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 
                LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            requestBackgroundLocationIfNecessary();
        }
    }

    private void requestBackgroundLocationIfNecessary() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE + 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestBackgroundLocationIfNecessary();
        }
    }
    
    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            emptyState.setVisibility(View.VISIBLE);
            rvShoppingList.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvShoppingList.setVisibility(View.VISIBLE);
        }
    }
}