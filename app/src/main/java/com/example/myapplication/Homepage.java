package com.example.myapplication;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;


public class Homepage {

    private Spinner friendsSpinner;
    private DatabaseHelper dbHelper;
    private EditText inputMessage,inputPassword;
    private ImageView qrCodeImage,imageView;
    private TextView scannedMessage,toolbarGreeting;
    private Bitmap qrCodeBitmap;
    Drawable eyesOpen, eyesClosed;
    private MediaPlayer mediaPlayer;
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        toolbarGreeting = findViewById(R.id.toolbarGreeting);

        updateGreetingMessage();
        handler = new Handler();
        startTimeCheck();


        inputMessage = findViewById(R.id.inputMessage);
        qrCodeImage = findViewById(R.id.qrCodeImage);
        scannedMessage = findViewById(R.id.scannedMessage);
        Button generateQRButton = findViewById(R.id.generateQRButton);
        Button scanQRButton = findViewById(R.id.scanQRButton);
        imageView = findViewById(R.id.imageView);
        eyesOpen = getResources().getDrawable(R.drawable.eyes_open);
        eyesClosed = getResources().getDrawable(R.drawable.eyes_close);
        // Generate QR Code
        ImageView infoButton = findViewById(R.id.infoButton);  // Info button on homepage
        infoButton.setOnClickListener(v -> showOptionsDialog());

        inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int after) {
                // Change emoji eyes based on whether the input is empty
                if (charSequence.toString().isEmpty()) {
                    imageView.setImageDrawable(eyesOpen); // Eyes open
                } else {
                    imageView.setImageDrawable(eyesClosed); // Eyes closed
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        generateQRButton.setOnClickListener(v -> {
            String message = inputMessage.getText().toString().trim();
//            String password = ((EditText) findViewById(R.id.inputPassword)).getText().toString().trim();
            String selectedFriend = ((Spinner) findViewById(R.id.friendsSpinner)).getSelectedItem().toString();
            SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
            String myUserId = sharedPreferences.getString("username", null); // Replace with dynamic user ID retrieval logic


            if (message.isEmpty()) {
                Toast.makeText(this, "Please enter a message.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                // Generate QR Code
                String encodedMessage = generateQRMetadata(myUserId, selectedFriend, message);
                Bitmap qrCodeBitmap = generateQRCode(encodedMessage);

                if (qrCodeBitmap != null) {
                    qrCodeImage.setImageBitmap(qrCodeBitmap);
                    Toast.makeText(this, "QR Code Generated!", Toast.LENGTH_SHORT).show();
                    imageView.setImageDrawable(eyesOpen);
                } else {
                    Toast.makeText(this, "Failed to generate QR code.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

// Utility to generate QR metadata
        // Scan QR Code
        // Modify the scan button's onClickListener to include customized options
        scanQRButton.setOnClickListener(v -> scanQRCode());

        friendsSpinner = findViewById(R.id.friendsSpinner);
        dbHelper = new DatabaseHelper(this);

        // Fetch and display the list of usernames from the database
        ArrayList<String> usernames = dbHelper.getAllUsernames();

        if (usernames.isEmpty()) {
            // If no usernames found, show a message or handle it
            Toast.makeText(this, "No friends found", Toast.LENGTH_SHORT).show();
        } else {
            // Set the list of usernames in the Spinner using an ArrayAdapter
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, usernames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            friendsSpinner.setAdapter(adapter);
        }
        friendsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedUsername = parentView.getItemAtPosition(position).toString();

                if (selectedUsername.equals("All")) {
                    // Handle the "All" selection
                    Toast.makeText(Homepage.this, "All friends selected", Toast.LENGTH_SHORT).show();
                } else {
                    // Handle specific username selection
                    Toast.makeText(Homepage.this, "Selected: " + selectedUsername, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Handle the case when no item is selected (optional)
            }
        });
    }

    private String generateQRMetadata(String myUserId, String selectedFriend, String message) {
        String metadata = "krishna|";

        if ("All".equals(selectedFriend)) {
            metadata += "All|" + message;
        } else {
            metadata += myUserId + "|" + selectedFriend + "|" + message;
        }

        return metadata;
    }


    private Bitmap generateQRCode(String encodedMessage) throws WriterException {
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        return barcodeEncoder.encodeBitmap(encodedMessage, BarcodeFormat.QR_CODE, 300, 300);
    }

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    handleScannedData(result.getContents());
                } else {
                    Toast.makeText(this, "No QR code found.", Toast.LENGTH_SHORT).show();
                }
            }
    );
    private void handleScannedData(String scannedData) {
        if (scannedData == null || !scannedData.startsWith("krishna|")) {
            Toast.makeText(this, "Invalid QR Code.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] parts = scannedData.split("\\|");
        if (parts.length < 3) {
            Toast.makeText(this, "Invalid QR Code format.", Toast.LENGTH_SHORT).show();
            return;
        }

        String identifier = parts[0];
        String accessType = parts[1];

        if (!"krishna".equals(identifier)) {
            Toast.makeText(this, "Unauthorized QR Code.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Access logic based on type
        if ("All".equals(accessType)) {
            // Access granted to all
            String message = parts[2];
            showCustomDialog(message);
        } else if (parts.length >= 4) {
            SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
            String myUserId = sharedPreferences.getString("username",null);
            String friendId = parts[2];
            String message = parts[3];

            if (myUserId.equals(friendId)) {
                showCustomDialog(message);
                Toast.makeText(this, "Access granted. Message: " + message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Unauthorized access for this QR Code.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Invalid QR Code data.", Toast.LENGTH_SHORT).show();
        }
    }
    // Method to launch the QR scanner
    private void scanQRCode() {
        ScanOptions options = new ScanOptions();
        options.setOrientationLocked(true); // Keep orientation locked for square codes
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE); // Restrict to QR codes
        options.setPrompt("Align the QR code within the frame");
        options.setBeepEnabled(true); // Enable beep sound
        barcodeLauncher.launch(options);
    }

    private void showCustomDialog(String message) {
        // Inflate the custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.custom_dialog, null);

        // Create the dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false) // Prevent dialog from closing on outside touch
                .create();

        // Set the background to transparent to show the rounded card properly
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Access the dialog elements
        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        TextView dialogMessage = dialogView.findViewById(R.id.dialogMessage);
        Button closeDialogButton = dialogView.findViewById(R.id.closeDialogButton);

        // Set the message and title
        dialogTitle.setText("QR Message");
        dialogMessage.setText(message);
        // Close button action
        closeDialogButton.setOnClickListener(v -> dialog.dismiss());

        // Show the dialog
        dialog.show();
    }

    private void updateGreetingMessage1() {
        // Array of different greeting messages
        String[] greetings = {
                "Welcome to QR Messaging!",
                "Hello! How are you doing today?",
                "Good Morning! Ready to scan some QR codes?",
                "Heyy! Let's make today amazing!",
                "Welcome! Time to share some messages!"
        };

        // Generate a random index
        Random random = new Random();
        int randomIndex = random.nextInt(greetings.length);

        // Set the greeting message in the TextView
        toolbarGreeting.setText(greetings[randomIndex]);
    }

    private void updateGreetingMessage() {
        // Get the current hour of the day
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        // Update greeting message based on time of day
        if (hourOfDay >= 0 && hourOfDay < 12) {
            toolbarGreeting.setText("Good Morning!");
        } else if (hourOfDay >= 12 && hourOfDay < 17) {
            toolbarGreeting.setText("Good Afternoon!");
        } else {
            toolbarGreeting.setText("Good Evening!");
        }
    }

    private void startTimeCheck() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check the current time
                Calendar calendar = Calendar.getInstance();
                int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);

                // If it's 12:00 PM, play the bell sound and stop checking
                if (hourOfDay == 12 && minute == 00) {
                    playBellSound();
                    handler.removeCallbacksAndMessages(null);  // Stop the handler after the sound is played
                }
                // Check again in 1000 milliseconds (1 second) only if the bell sound hasn't played yet
                else {
                    handler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    private void playBellSound() {
        // Play a bell sound when it's 12:00 PM
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.bell_sound);  // Ensure bell_sound.mp3 is in the raw folder
        }
        mediaPlayer.start();  // Play the sound
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();  // Release the media player to free up resources
        }
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);  // Stop the handler when the activity is destroyed
        }
    }

    private void showOptionsDialog() {
        // Inflate the dialog layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_options, null);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        // Set buttons actions
        Button btnUser = dialogView.findViewById(R.id.btnUser);
        Button btnAddFriend = dialogView.findViewById(R.id.btnAddFriend);
        Button btnFeedback = dialogView.findViewById(R.id.btnFeedback);
        Button btnListFriends = dialogView.findViewById(R.id.btnListFriends);

        btnUser.setOnClickListener(v -> {
            // Handle the "User Profile" action
            openUserProfile();
        });

        btnAddFriend.setOnClickListener(v -> {
            // Handle the "Add Friend" action
            openAddFriendPage();
        });

        btnListFriends.setOnClickListener(v -> {
            openFriendsList();
        });

        btnFeedback.setOnClickListener(v -> {
            // Handle the "Feedback" action
            openFeedbackPage();
        });

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void openUserProfile() {
        // Open the User Profile page or dialog
        Intent intent = new Intent(Homepage.this, ProfilePageActivity.class);
        startActivity(intent);
    }

    private void openAddFriendPage() {
        // Open the Add Friend page or dialog
        Intent intent = new Intent(Homepage.this, AddFriendActivity.class);
        startActivity(intent);
    }

    private void openFriendsList() {

        Intent intent = new Intent(Homepage.this, FriendsListActivity.class);
        startActivity(intent);
    }
    private void openFeedbackPage() {
        // Open the Feedback page or dialog
        Intent intent = new Intent(Homepage.this, FriendsListActivity.class);
        startActivity(intent);
    }
}
