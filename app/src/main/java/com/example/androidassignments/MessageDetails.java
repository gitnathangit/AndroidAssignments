package com.example.androidassignments;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.content.Intent;





public class MessageDetails extends AppCompatActivity {
    private long messageId;
    private String messageContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_details);


        Intent intent = getIntent();
        messageId = intent.getLongExtra("id", -1);
        messageContent = intent.getStringExtra("message");


        MessageFragment fragment = new MessageFragment();
        Bundle args = new Bundle();
        args.putString("message", messageContent);
        args.putLong("id", messageId);
        fragment.setArguments(args);


        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout, fragment);
        transaction.commit();
    }

    public static class MessageFragment extends Fragment {
        private ChatWindow chatWindow; // Reference to ChatWindow for tablets
        private long id;

        public MessageFragment(ChatWindow chatWindow) {
            this.chatWindow = chatWindow; // Constructor for tablet mode
        }

        public MessageFragment() {
            this.chatWindow = null; // Constructor for phone mode
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_message, container, false);

            TextView messageTextView = view.findViewById(R.id.messageText);
            TextView idTextView = view.findViewById(R.id.messageId);
            Button deleteButton = view.findViewById(R.id.deleteMessageButton);


            Log.d("MessageFragment", "onCreateView called");

            Bundle args = getArguments();
            if (args != null) {
                String message = args.getString("message");
                id = args.getLong("id", -1);

                messageTextView.setText(message);
                idTextView.setText("ID: " + id);
            }

            // Set delete button click listener
            deleteButton.setOnClickListener(v -> {
                if (chatWindow != null) {
                    // Tablet: Delete message and remove fragment
                    chatWindow.deleteMessageById(id);

                    FragmentTransaction transaction = chatWindow.getSupportFragmentManager().beginTransaction();
                    transaction.remove(this);
                    transaction.commit();
                } else {
                    // Phone: Return result to ChatWindow
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("id", id);
                    if (getActivity() != null) {
                        getActivity().setResult(RESULT_OK, resultIntent);
                        getActivity().finish();
                    }
                }
            });

            return view;
        }
    }
}
