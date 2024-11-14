package com.example.androidassignments;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import android.util.Log;
import android.view.View;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.content.ContentValues;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ChatWindow extends AppCompatActivity {
    private ChatDatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private ArrayList<String> messageList;
    private EditText messageInput;
    private Button sendButton;

    private ArrayAdapter<String> adapter;

    private static final String ACTIVITY_NAME = "ChatWindow";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);


        dbHelper = new ChatDatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        messageList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messageList);

        ListView messageListView = findViewById(R.id.messageListView);
        messageListView.setAdapter(adapter);

        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);


        Cursor cursor = db.rawQuery("SELECT * FROM " + ChatDatabaseHelper.TABLE_NAME, null);


        Log.i(ACTIVITY_NAME, "Cursor's column count = " + cursor.getColumnCount());


        for (int i = 0; i < cursor.getColumnCount(); i++) {
            Log.i(ACTIVITY_NAME, "Column name: " + cursor.getColumnName(i));
        }


        if (cursor.moveToFirst()) {
            int messageColumnIndex = cursor.getColumnIndex(ChatDatabaseHelper.KEY_MESSAGE);
            if (messageColumnIndex != -1) {
                do {
                    String message = cursor.getString(messageColumnIndex);
                    messageList.add(message);
                    Log.i(ACTIVITY_NAME, "SQL MESSAGE: " + message);
                } while (cursor.moveToNext());
            } else {
                Log.e(ACTIVITY_NAME, "Column KEY_MESSAGE not found in the database");
            }
        }


        cursor.close();

        adapter.notifyDataSetChanged();


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageInput.getText().toString();
                if (!message.isEmpty()) {

                    messageList.add(message);


                    ContentValues values = new ContentValues();
                    values.put(ChatDatabaseHelper.KEY_MESSAGE, message);
                    db.insert(ChatDatabaseHelper.TABLE_NAME, null, values);
                    Log.i(ACTIVITY_NAME, "Inserted message: " + message);

                    messageInput.setText("");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}
