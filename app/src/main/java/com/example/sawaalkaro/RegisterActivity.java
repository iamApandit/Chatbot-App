package com.example.sawaalkaro;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.questionpaperprovider.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText userNameEdt, pwdEdt, cnfPwdEdt;
    public Button registerBtn;
    private ProgressBar loadingPB;
    private FirebaseAuth mAuth;
    public TextView loginTV;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userNameEdt = findViewById(R.id.idEDTUserName);
        pwdEdt = findViewById(R.id.idEdtPassword);
        cnfPwdEdt = findViewById(R.id.idEdtConfirmPassword);
        registerBtn = findViewById(R.id.buttonRegister);
        loadingPB = findViewById(R.id.PBLoading);
        loginTV = findViewById(R.id.textView);
        mAuth = FirebaseAuth.getInstance();
        loginTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(i);
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingPB.setVisibility(View.VISIBLE);
                String userName = Objects.requireNonNull(userNameEdt.getText()).toString();
                String pwd = Objects.requireNonNull(pwdEdt.getText()).toString();
                String cnfPwd = Objects.requireNonNull(cnfPwdEdt.getText()).toString();
                if (!pwd.equals(cnfPwd)){
                    Toast.makeText(RegisterActivity.this, "Please Check your password", Toast.LENGTH_SHORT).show();

                }else if (TextUtils.isEmpty(userName) && TextUtils.isEmpty(pwd) && TextUtils.isEmpty(cnfPwd)){
                    Toast.makeText(RegisterActivity.this, "Please add your credentials..", Toast.LENGTH_SHORT).show();
                }else {
                    mAuth.createUserWithEmailAndPassword(userName,pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                loadingPB.setVisibility(View.GONE);
                                Toast.makeText(RegisterActivity.this, "User Registered Successfully", Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(RegisterActivity.this,LoginActivity.class);
                                startActivity(i);
                                finish();
                            }else {
                                loadingPB.setVisibility(View.GONE);
                                Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }
            }
        });

    }
}