package com.example.myapplication;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class AddFriendActivity {

    private EditText editTextUsername;
    private Button btnAddFriendManually, btnScanQR;
    private String currentUser;
    private ImageView qrCodeImage;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        // Initialize views
        editTextUsername = findViewById(R.id.editTextUsername);
        btnAddFriendManually = findViewById(R.id.btnAddFriendManually);
        btnScanQR = findViewById(R.id.btnScanQR);
        qrCodeImage = findViewById(R.id.qrCodeImage);

        // Initialize DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Get current username from SharedPreferences
        currentUser = getSharedPreferences("UserProfile", MODE_PRIVATE).getString("username", null);
        if (currentUser == null) {
            Toast.makeText(AddFriendActivity.this, "Please create a profile first.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Manually add friend button listener
        btnAddFriendManually.setOnClickListener(v -> {
            String friendUsername = editTextUsername.getText().toString();
            if (!friendUsername.isEmpty()) {
                addFriend(friendUsername);
            } else {
                Toast.makeText(AddFriendActivity.this, "Please enter a username.", Toast.LENGTH_SHORT).show();
            }
        });

        // Scan QR button listener
        btnScanQR.setOnClickListener(v -> {
            // Start QR code scanning activity
            IntentIntegrator intentIntegrator = new IntentIntegrator(AddFriendActivity.this);
            intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            intentIntegrator.setPrompt("Scan your friend's QR code");
            intentIntegrator.setCameraId(0);  // Use front camera (optional)
            intentIntegrator.setBeepEnabled(true);  // Enable beep on scan
            intentIntegrator.setBarcodeImageEnabled(true); // Enable saving barcode image
            intentIntegrator.initiateScan();
        });
    }

    // Handle the result from the QR code scan
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            // If the scan was successful, handle the result
            if (result.getContents() != null) {
                String scannedData = result.getContents();
                if (scannedData.startsWith("USER_")) {
                    // Remove the "USER_" prefix and add friend
                    addFriend(scannedData.substring(5));  // Adds friend based on the ID
                } else {
                    Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Scan canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Add friend logic using SQLite
    private void addFriend(String friendUsername) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Check if friend already exists in the database
        Cursor cursor = db.query(DatabaseHelper.TABLE_FRIENDS, new String[]{DatabaseHelper.COLUMN_USERNAME},
                DatabaseHelper.COLUMN_USERNAME + "=?", new String[]{friendUsername}, null, null, null);

        if (cursor.getCount() > 0) {
            Toast.makeText(AddFriendActivity.this, "This friend is already added.", Toast.LENGTH_SHORT).show();
            cursor.close();
        } else {
            // Insert friend into the database
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_USERNAME, friendUsername);
            long rowId = db.insert(DatabaseHelper.TABLE_FRIENDS, null, values);

            if (rowId != -1) {
                Toast.makeText(AddFriendActivity.this, "Friend added successfully.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(AddFriendActivity.this, "Failed to add friend.", Toast.LENGTH_SHORT).show();
            }
            cursor.close();
        }
        db.close();
    }
}
