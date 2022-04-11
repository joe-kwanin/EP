package com.example.vvuexampermitapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private TextInputLayout email,password;
    private Button signin;
    private Boolean isvalidpassword=false;
    private Boolean isvalidemail = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

        email = (TextInputLayout) findViewById(R.id.email);
        password = (TextInputLayout) findViewById(R.id.password);
        signin = (Button) findViewById(R.id.signin);

        signin.setEnabled(false);

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signInWithEmailAndPassword(email.getEditText().getText().toString().trim(),password.getEditText().getText().toString().trim()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Intent intent = new Intent(Login.this,Dashboard.class);
                        startActivity(intent);

                    }
                });

            }
        });

        email.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                validateemail(editable);

            }
        });

        email.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                validateemail(((EditText)view).getText());
            }
        });


        password.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                validatepassword(editable);

            }
        });

        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                validatepassword(((EditText)view).getText());
            }
        });

    }

    private void validatepassword(Editable editable) {
        if (TextUtils.isEmpty(editable)){
            password.setError("");
            signin.setEnabled(false);
        }else {
            password.setError(null);
            isvalidpassword=true;
            if (isvalidpassword & isvalidemail){
                signin.setEnabled(true);
            }
        }
    }

    private void validateemail(Editable editable) {
        if (TextUtils.isEmpty(editable)){
            email.setError("");
            signin.setEnabled(false);
        }else {
            email.setError(null);
            isvalidemail = true;
            if (isvalidemail & isvalidpassword){
                signin.setEnabled(true);
            }
        }
    }
}