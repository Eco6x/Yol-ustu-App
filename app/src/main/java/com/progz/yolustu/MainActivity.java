package com.progz.yolustu;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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

        // Setup RecyclerView
        adapter = new ListAdapter();
        rvShoppingList.setLayoutManager(new LinearLayoutManager(this));
        rvShoppingList.setAdapter(adapter);
        
        // Initial empty state check
        updateEmptyState();
        
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

            // Create item and add via Adapter
            ShoppingItem newItem = new ShoppingItem(itemName, marketText);
            adapter.addItem(newItem);
            
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
            
            updateEmptyState();
            
            // Show Feedback
            Snackbar.make(findViewById(R.id.main), "Item added to " + marketText, Snackbar.LENGTH_SHORT).show();
        });
        
        // Optional: observe adapter data changes if possible, or handle directly above
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updateEmptyState();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                updateEmptyState();
                rvShoppingList.smoothScrollToPosition(adapter.getItemCount() - 1);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                updateEmptyState();
            }
        });
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