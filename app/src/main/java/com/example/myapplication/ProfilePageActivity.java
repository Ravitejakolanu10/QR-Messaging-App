package com.example.myapplication;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class ProfilePageActivity {

    private EditText editTextUsername;
    private Button btnGenerateQR, btnSaveProfile, btnScanQR, btnAddFriend;
    private ImageView qrCodeImage;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        // Initialize views
        editTextUsername = findViewById(R.id.editTextUsername);
        btnGenerateQR = findViewById(R.id.btnGenerateQR);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        qrCodeImage = findViewById(R.id.qrCodeImage);

        // Load the saved profile
        loadProfile();

        // Generate QR Code Button Click Listener
        btnGenerateQR.setOnClickListener(v -> {
            username = editTextUsername.getText().toString();
            if (!username.isEmpty()) {
                generateQRCode(username);
                saveProfile(username);  // Save the username after QR is generated
            } else {
                Toast.makeText(ProfilePageActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();
            }
        });

        // Save Profile Button Click Listener
        btnSaveProfile.setOnClickListener(v -> {
            username = editTextUsername.getText().toString();
            if (!username.isEmpty()) {
                saveProfile(username);
                Toast.makeText(ProfilePageActivity.this, "Profile Saved", Toast.LENGTH_SHORT).show();
                generateQRCode(username); // Regenerate QR if username is saved
            } else {
                Toast.makeText(ProfilePageActivity.this, "Please enter a username", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Load saved profile (if any) from SharedPreferences
    private void loadProfile() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String savedUsername = sharedPreferences.getString("username", null);

        if (savedUsername != null) {
            // If username exists, display it and generate QR code
            editTextUsername.setText(savedUsername);
            generateQRCode(savedUsername);
        } else {
            // If no username is saved, leave the field empty
            editTextUsername.setText("");
        }
    }

    // Generate QR code based on the username
    private void generateQRCode(String username) {
        // Use a library like ZXing or any other to generate QR code
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode("USER_" + username, BarcodeFormat.QR_CODE, 500, 500);
            Bitmap bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.RGB_565);

            for (int x = 0; x < 500; x++) {
                for (int y = 0; y < 500; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            qrCodeImage.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(ProfilePageActivity.this, "Error generating QR code", Toast.LENGTH_SHORT).show();
        }
    }

    // Save the username to SharedPreferences
    private void saveProfile(String username) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.apply();
    }

    // Add Friend logic

}
