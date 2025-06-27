package com.example.myapplication;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FriendsListActivity {

    private RecyclerView recyclerView;
    private FriendsAdapter friendAdapter;
    private ArrayList<String> friendList;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

        recyclerView = findViewById(R.id.recyclerView);
        dbHelper = new DatabaseHelper(this);
        friendList = new ArrayList<>();

        // Correcting the initialization of the adapter
        friendAdapter = new FriendsAdapter(friendList, this);  // Pass friendList and context in correct order

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(friendAdapter);

        loadFriends();  // Load friends from the database
    }

    private void loadFriends() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(DatabaseHelper.TABLE_FRIENDS, new String[]{DatabaseHelper.COLUMN_USERNAME},
                    null, null, null, null, null);

            // Clear the existing list before adding new data
            friendList.clear();

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String friendUsername = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USERNAME));
                    friendList.add(friendUsername);
                } while (cursor.moveToNext());
            }

            // Check if the friend list is empty
            if (friendList.isEmpty()) {
                Toast.makeText(this, "No friends added yet.", Toast.LENGTH_SHORT).show();
            }

            friendAdapter.notifyDataSetChanged();  // Notify the adapter to update the RecyclerView
        } finally {
            if (cursor != null) {
                cursor.close();  // Ensure cursor is closed to avoid memory leaks
            }
            db.close();  // Close the database
        }
    }
}
