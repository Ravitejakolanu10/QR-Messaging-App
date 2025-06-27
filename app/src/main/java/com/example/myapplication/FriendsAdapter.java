package com.example.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder> {

    private List<String> friendsList;
    private Context context;
    private DatabaseHelper databaseHelper; // Add DatabaseHelper instance

    // Constructor to initialize the list of friends and DatabaseHelper
    public FriendsAdapter(List<String> friendsList, Context context) {
        this.context = context;
        this.friendsList = friendsList;
        this.databaseHelper = new DatabaseHelper(context); // Initialize DatabaseHelper
    }

    // Create a new ViewHolder for each item in the list
    @Override
    public FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item, parent, false);
        return new FriendsViewHolder(view);
    }

    // Bind data to each ViewHolder (each friend)
    @Override
    public void onBindViewHolder(FriendsViewHolder holder, int position) {
        String friendUsername = friendsList.get(position);
        holder.textViewFriendUsername.setText(friendUsername);

        // Show the popup menu on three dots click
        holder.imageViewMoreOptions.setOnClickListener(v -> showPopupMenu(v, position));
    }

    // Return the total number of items in the list
    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    // Show the popup menu for "Remove Friend"
    private void showPopupMenu(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.inflate(R.menu.friend_menu); // Load the menu resource

        // Handle the "Remove Friend" option
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.remove_friend) {
                removeFriend(position);
                return true;
            }
            return false;
        });

        popupMenu.show(); // Show the menu
    }

    // Remove a friend from the list and the database
    private void removeFriend(int position) {
        String friendUsername = friendsList.get(position);

        // Remove from the RecyclerView list
        friendsList.remove(position);
        notifyItemRemoved(position);

        // Remove from the database
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int rowsDeleted = db.delete(DatabaseHelper.TABLE_FRIENDS,
                DatabaseHelper.COLUMN_USERNAME + " = ?", new String[]{friendUsername});

        db.close();

        if (rowsDeleted > 0) {
            Toast.makeText(context, friendUsername + " has been removed.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Failed to remove friend.", Toast.LENGTH_SHORT).show();
        }
    }

    // ViewHolder class to hold the view for each item
    public static class FriendsViewHolder extends RecyclerView.ViewHolder {
        TextView textViewFriendUsername;
        ImageView imageViewMoreOptions;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            textViewFriendUsername = itemView.findViewById(R.id.textViewFriendUsername);
            imageViewMoreOptions = itemView.findViewById(R.id.imageViewMoreOptions);
        }
    }
}
