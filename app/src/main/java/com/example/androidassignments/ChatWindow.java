package com.example.androidassignments;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import java.util.ArrayList;

public class ChatWindow extends AppCompatActivity {
    private ChatDatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private Cursor cursor;
    private ArrayList<String> messageList;
    private EditText messageInput;
    private Button sendButton;
    private ListView messageListView;
    private ChatAdapter chatAdapter;

    private boolean isTabletLayout;

    private static final String ACTIVITY_NAME = "ChatWindow";
    private static final int DELETE_MESSAGE_REQUEST_CODE = 1; // Request code for message deletion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);

        boolean isTabletLayout = findViewById(R.id.frameLayout) != null;
        Log.d("ChatWindow", "Is tablet layout: " + isTabletLayout);

        // Initialize database and UI components
        dbHelper = new ChatDatabaseHelper(this);
        db = dbHelper.getWritableDatabase();
        messageList = new ArrayList<>();
        loadCursor();

        messageListView = findViewById(R.id.messageListView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        // Initialize ChatAdapter
        chatAdapter = new ChatAdapter();
        messageListView.setAdapter(chatAdapter);

        // Load messages from the database
        loadMessagesFromDatabase();

        // Set send button listener
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString();
            if (!message.isEmpty()) {
                addMessageToDatabase(message);
                messageInput.setText(""); // Clear input field
            }
        });

        // Set item click listener for ListView
        messageListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedMessage = messageList.get(position);
            long databaseId = chatAdapter.getItemId(position);

            if (isTabletLayout) {
                Log.d("ChatWindow", "Tablet mode detected, loading fragment");
                MessageDetails.MessageFragment messageFragment = new MessageDetails.MessageFragment();
                Bundle bundle = new Bundle();
                bundle.putString("message", selectedMessage);
                bundle.putLong("id", databaseId);
                messageFragment.setArguments(bundle);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frameLayout, messageFragment);
                transaction.commit();

                Log.d("ChatWindow", "Fragment transaction committed");
            } else {
                // Launch new activity in phone layout
                Intent intent = new Intent(ChatWindow.this, MessageDetails.class);
                intent.putExtra("message", selectedMessage);
                intent.putExtra("id", databaseId);
                startActivityForResult(intent, DELETE_MESSAGE_REQUEST_CODE);
            }
        });
    }

    private void loadCursor() {
        cursor = db.rawQuery("SELECT * FROM " + ChatDatabaseHelper.TABLE_NAME, null);
    }

    private void loadMessagesFromDatabase() {
        if (cursor == null) return;

        Log.i(ACTIVITY_NAME, "Cursor's column count = " + cursor.getColumnCount());

        for (int i = 0; i < cursor.getColumnCount(); i++) {
            Log.i(ACTIVITY_NAME, "Column name: " + cursor.getColumnName(i));
        }

        if (cursor.moveToFirst()) {
            do {
                String message = cursor.getString(getColumnIndex(cursor, ChatDatabaseHelper.KEY_MESSAGE));
                if (message != null) {
                    messageList.add(message);
                    Log.i(ACTIVITY_NAME, "SQL MESSAGE: " + message);
                }
            } while (cursor.moveToNext());
        }
        chatAdapter.notifyDataSetChanged();
    }

    private void addMessageToDatabase(String message) {
        messageList.add(message); // Add message to the list

        ContentValues values = new ContentValues();
        values.put(ChatDatabaseHelper.KEY_MESSAGE, message);
        db.insert(ChatDatabaseHelper.TABLE_NAME, null, values);
        Log.i(ACTIVITY_NAME, "Inserted message: " + message);

        reloadCursor(); // Reload cursor to reflect the new message
        chatAdapter.notifyDataSetChanged(); // Update the ListView
    }

    private int getColumnIndex(Cursor cursor, String columnName) {
        if (cursor != null && columnName != null) {
            return cursor.getColumnIndex(columnName);
        }
        return -1; // Return -1 if cursor or columnName is invalid
    }

    private void reloadCursor() {
        if (cursor != null) {
            cursor.close();
        }
        loadCursor();
    }

    public void deleteMessageById(long id) {
        db.delete(ChatDatabaseHelper.TABLE_NAME, ChatDatabaseHelper.KEY_ID + "=?", new String[]{String.valueOf(id)});
        reloadCursor();
        messageList.clear();
        loadMessagesFromDatabase();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DELETE_MESSAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            long messageId = data.getLongExtra("id", -1);
            if (messageId != -1) {
                deleteMessageById(messageId);

                if (isTabletLayout) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.remove(getSupportFragmentManager().findFragmentById(R.id.frameLayout));
                    transaction.commit();

                    Log.d("ChatWindow", "Fragment transaction committed");
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cursor != null) {
            cursor.close();
        }
        db.close();
    }

    // Inner ChatAdapter class
    private class ChatAdapter extends android.widget.ArrayAdapter<String> {
        public ChatAdapter() {
            super(ChatWindow.this, android.R.layout.simple_list_item_1, messageList);
        }

        @Override
        public long getItemId(int position) {
            if (cursor != null && cursor.moveToPosition(position)) {
                return cursor.getLong(getColumnIndex(cursor, ChatDatabaseHelper.KEY_ID));
            }
            return -1; // Default return value if something goes wrong
        }
    }
}
