package com.wagnercsfilho.fireblogr.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wagnercsfilho.fireblogr.R;
import com.wagnercsfilho.fireblogr.model.User;

public class SignUpActivity extends AppCompatActivity {

    EditText editName;
    EditText editEmail;
    EditText editPassword;
    Button buttonSignUp;

    FirebaseAuth firebaseAuth;
    DatabaseReference mDatabaseRefUser;

    ProgressDialog progressDialog;
    private FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        progressDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRefUser = FirebaseDatabase.getInstance().getReference().child("users");

        editEmail = (EditText) findViewById(R.id.edit_email);
        editName = (EditText) findViewById(R.id.edit_name);
        editPassword = (EditText) findViewById(R.id.edit_password);
        buttonSignUp = (Button) findViewById(R.id.button_signup);

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp(view);
            }
        });

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        };

    }

    private void signUp(final View view) {
        final String name = editName.getText().toString().trim();
        final String email = editEmail.getText().toString().trim();
        final String password = editPassword.getText().toString().trim();

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

            progressDialog.setMessage("Signing Up...");
            progressDialog.show();

            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        String userId = firebaseAuth.getCurrentUser().getUid();

                        User user = new User();
                        user.setName(name);
                        user.setImage("default");

                        mDatabaseRefUser.child(userId).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                progressDialog.dismiss();

                                if (task.isSuccessful()) {
                                    Toast.makeText(SignUpActivity.this, "You need to setup your account.", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(SignUpActivity.this, SettingsActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                } else {
                                    Snackbar.make(view, "Sign Up Failed! Try again.", Snackbar.LENGTH_SHORT)
                                            .setAction("Action", null)
                                            .show();
                                }
                            }
                        });

                    } else {
                        progressDialog.dismiss();

                        Snackbar.make(view, "Sign Up Failed! Try again.", Snackbar.LENGTH_SHORT)
                                .setAction("Action", null)
                                .show();
                    }
                }
            });

        } else {
            Snackbar.make(view, "Fill all fields", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null)
                    .show();
        }
    }
}
