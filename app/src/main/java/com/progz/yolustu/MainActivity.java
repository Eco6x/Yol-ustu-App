package com.progz.yolustu;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
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

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // ─── Views ────────────────────────────────────────────────────────────────
    private TextInputEditText etItemName;
    private AutoCompleteTextView spinnerMarket;
    private FloatingActionButton fabAdd;
    private RecyclerView rvShoppingList;
    private LinearLayout emptyState;

    private ListAdapter adapter;
    private String[] defaultMarkets = new String[] { "BİM", "A101", "Şok", "Migros" };

    // ─── Database & Background Work ───────────────────────────────────────────
    private ItemDatabase db;
    private ExecutorService executorService;

    // ─── Geofencing ───────────────────────────────────────────────────────────
    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final float GEOFENCE_RADIUS_METERS = 150f;

    // ─── Market Locations (Ankara) ────────────────────────────────────────────
    private static final double[][] MARKET_COORDS = {
            // BİM stores
            { 39.9208, 32.8541 },   // BİM – Kızılay
            { 39.9355, 32.8597 },   // BİM – Ulus
            { 39.9073, 32.8616 },   // BİM – Bahçelievler
            // A101 stores
            { 39.9229, 32.8600 },   // A101 – Kızılay
            { 39.9420, 32.8390 },   // A101 – Etlik
            // Şok stores
            { 39.9180, 32.8510 },   // Şok – Kızılay
            { 39.9310, 32.8660 },   // Şok – Çankaya
            // Migros stores
            { 39.9253, 32.8627 },   // Migros – Kızılay
            // ── TEST coordinate ──────────────────────────────────────────────
            { 39.921966, 32.858209 }, // TEST – custom location (will match "BİM" for list lookup)
    };

    private static final String[] MARKET_NAMES = {
            "BİM", "BİM", "BİM",
            "A101", "A101",
            "Şok", "Şok",
            "Migros",
            "BİM",   // TEST entry – uses BİM so the notification shows your BİM shopping list
    };

    // ─── Lifecycle ────────────────────────────────────────────────────────────

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
        ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, defaultMarkets);
        spinnerMarket.setAdapter(dropdownAdapter);

        // Setup Database, Executor & GeofencingClient
        executorService = Executors.newSingleThreadExecutor();
        db = ItemDatabase.getInstance(this);
        geofencingClient = LocationServices.getGeofencingClient(this);

        // Setup RecyclerView
        adapter = new ListAdapter(new ListAdapter.OnItemInteractionListener() {
            @Override
            public void onUpdate(ShoppingItem item) {
                executorService.execute(() -> db.shoppingItemDao().update(item));
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

        loadItems();
        checkLocationPermissions();

        // Setup Add Button
        fabAdd.setOnClickListener(v -> {
            String itemName = etItemName.getText().toString().trim();
            String marketText = spinnerMarket.getText().toString().trim();

            if (itemName.isEmpty()) {
                Toast.makeText(this, "Lütfen bir ürün adı girin.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (marketText.isEmpty())
                marketText = "Genel";

            ShoppingItem newItem = new ShoppingItem(itemName, marketText);
            String finalMarket = marketText;
            executorService.execute(() -> {
                db.shoppingItemDao().insert(newItem);
                loadItems();
            });

            etItemName.setText("");
            etItemName.clearFocus();
            spinnerMarket.clearFocus();

            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            Snackbar.make(findViewById(R.id.main), finalMarket + " listesine eklendi ✓", Snackbar.LENGTH_SHORT).show();
        });
    }

    // ─── Location & Geofencing ────────────────────────────────────────────────

    private void checkLocationPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        // On Android 13+ we also need notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]),
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            requestBackgroundLocationIfNecessary();
        }
    }

    private void requestBackgroundLocationIfNecessary() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.ACCESS_BACKGROUND_LOCATION },
                        LOCATION_PERMISSION_REQUEST_CODE + 1);
            } else {
                registerGeofences();
            }
        } else {
            registerGeofences();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            boolean locationGranted = false;
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                        grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    locationGranted = true;
                }
            }
            // If location was already granted previously, but we requested notifications now
            if (locationGranted || ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                requestBackgroundLocationIfNecessary();
            }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE + 1
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            registerGeofences();
        }
    }

    private void registerGeofences() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Skipping geofence registration — location permission not granted.");
            return;
        }

        List<Geofence> geofenceList = new ArrayList<>();
        for (int i = 0; i < MARKET_COORDS.length; i++) {
            // Use index as suffix to make each request ID unique for multiple branches of the same chain
            String requestId = MARKET_NAMES[i] + "_" + i;
            geofenceList.add(new Geofence.Builder()
                    .setRequestId(requestId)
                    .setCircularRegion(
                            MARKET_COORDS[i][0],    // latitude
                            MARKET_COORDS[i][1],    // longitude
                            GEOFENCE_RADIUS_METERS  // 150 metre radius
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build());
        }

        GeofencingRequest request = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofenceList)
                .build();

        geofencingClient.addGeofences(request, getGeofencePendingIntent())
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Geofences registered: " + geofenceList.size() + " locations."))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Geofence registration failed: " + e.getMessage()));
    }

    private PendingIntent getGeofencePendingIntent() {
        if (geofencePendingIntent != null)
            return geofencePendingIntent;
        Intent intent = new Intent(this, GeofenceReceiver.class);
        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        return geofencePendingIntent;
    }

    // ─── Data ─────────────────────────────────────────────────────────────────

    private void loadItems() {
        if (executorService != null) {
            executorService.execute(() -> {
                List<ShoppingItem> items = db.shoppingItemDao().getAllItems();
                runOnUiThread(() -> {
                    adapter.setItems(items);
                    updateEmptyState();
                    if (!items.isEmpty()) {
                        rvShoppingList.smoothScrollToPosition(items.size() - 1);
                    }
                });
            });
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