package com.example.sawaalkaro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.questionpaperprovider.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private RecyclerView chatsRv;
    private EditText userMsgEdt;
    private FloatingActionButton sendMsgFAB;
    private ArrayList<ChatsModal>chatsModalArrayList;
    private ChatAdapter chatAdapter;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;
    private DatabaseReference chatRef;

    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(62, TimeUnit.SECONDS)
            .build();


    private FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;

    ChatsModal chatsModal;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatsRv = findViewById(R.id.idRVChats);
        userMsgEdt = findViewById(R.id.idEDtMessage);
        sendMsgFAB = findViewById(R.id.idFABSend);

        chatsModalArrayList = new ArrayList<>();

        chatAdapter = new ChatAdapter(chatsModalArrayList);
        chatsRv.setAdapter(chatAdapter);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        chatsRv.setLayoutManager(manager);

        firebaseDatabase = FirebaseDatabase.getInstance();
        chatRef = firebaseDatabase.getReference("messages");

        firebaseDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatsModalArrayList.clear();

                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                    ChatsModal chatMessage = snapshot1.getValue(ChatsModal.class);
                    if (chatMessage != null){
                        chatsModalArrayList.add(chatMessage);
                    }
                }
                chatAdapter.notifyDataSetChanged();
                chatsRv.smoothScrollToPosition(chatAdapter.getItemCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "Database Error:"+ error.getMessage());

            }
        };

        chatRef.addValueEventListener(valueEventListener);


        sendMsgFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userMsgEdt.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this, "please enter your message", Toast.LENGTH_SHORT).show();

                }else {
                    sendMessage();
                }
                String question = userMsgEdt.getText().toString().trim();
                getResponse(question,ChatsModal.USER_KEY);
                callAPI(question);
                userMsgEdt.setText("");


            }
        });
    }

    private void sendMessage() {

        String messageText = userMsgEdt.getText().toString();
        ChatsModal userMessage = new ChatsModal(messageText, "user", System.currentTimeMillis());
        this.chatRef.push().setValue(userMessage);
    }

    void  getResponse(String message,String sender){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatsModalArrayList.add(new ChatsModal(message,sender, System.currentTimeMillis()));
                chatAdapter.notifyDataSetChanged();
                chatsRv.smoothScrollToPosition(chatAdapter.getItemCount());

            }
        });
    }

    void addResponse(String response){
        chatsModalArrayList.remove(chatsModalArrayList.size()-1);
        getResponse(response,ChatsModal.BOT_KEY);
    }

    void callAPI(String question){
        chatsModalArrayList.add(new ChatsModal("typing..",ChatsModal.BOT_KEY, System.currentTimeMillis()));
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model","gpt-3.5-turbo");

            JSONArray messageArr = new JSONArray();
            JSONObject obj = new JSONObject();
            obj.put("role","user");
            obj.put("content",question);
            messageArr.put(obj);

            jsonBody.put("messages",messageArr);
        }catch (JSONException e){
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(jsonBody.toString(),JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization","Bearer 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")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                addResponse("Failed to load response due to"+e.getMessage());

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()){
                    JSONObject jsonObject = null;
                    try {
                     jsonObject = new JSONObject(response.body().string());
                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
                        String result = jsonArray.getJSONObject(0)
                                        .getJSONObject("message")
                                                .getString("content");
                        addResponse(result.trim());

                        replyText(result);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }else {
                    addResponse("Failed to load response due to"+response.body().toString());

                }

            }
        });
    }

    public void replyText(String botResponse){
        ChatsModal botMessage = new ChatsModal(botResponse, "bot", System.currentTimeMillis());
        this.chatRef.push().setValue(botMessage);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void shareApp(){
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "try this bot");
        intent.putExtra(Intent.EXTRA_TEXT, "application link");
        startActivity(Intent.createChooser(intent, "share this app!"));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.shareButton:
                shareApp();
                return true;

            case R.id.menu_logout:
                Toast.makeText(this, "You have been logged out", Toast.LENGTH_SHORT).show();
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
                googleSignInClient.signOut();

                mAuth.signOut();

                Intent i = new Intent(this, LoginActivity.class);
                startActivity(i);
                this.finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }
}
