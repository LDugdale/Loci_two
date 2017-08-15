package com.lauriedugdale.loci.ui.activity.auth;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.ui.activity.MainActivity;

/**
 * Contains the UI logic for the login activity
 *
 * @author Laurie Dugdale
 */
public class LoginActivity extends AppCompatActivity {

    private EditText mEditTextEmail;
    private EditText mEditTextPassword;
    private FirebaseAuth mAuth;
    private ProgressBar mProgressBar;
    private Button mButtonSignup;
    private Button mButtonLogin;
    private Button mButtonReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Get Firebase auth instance
        mAuth = FirebaseAuth.getInstance();

        // if current user is not null meaning that they are already signed in, load the MainActivity
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        // find the UI elements
        mEditTextEmail = (EditText) findViewById(R.id.email);
        mEditTextPassword = (EditText) findViewById(R.id.password);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mButtonSignup = (Button) findViewById(R.id.btn_signup);
        mButtonLogin = (Button) findViewById(R.id.btn_login);
        mButtonReset = (Button) findViewById(R.id.btn_reset_password);

        mAuth = FirebaseAuth.getInstance();

        onSignupButtonClicked();
        onResetButtonClicked();
        onLoginButtonClicked();
    }

    /**
     * Listener for the signup button
     */
    private void onSignupButtonClicked(){
        mButtonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });
    }

    /**
     *  Listener for the reset button
     */
    private void onResetButtonClicked(){
        mButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });
    }

    /**
     * Listener for the login button
     */
    private void onLoginButtonClicked(){

        mButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEditTextEmail.getText().toString();
                final String password = mEditTextPassword.getText().toString();

                // if email address is empty
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Please enter your email address!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // if password is empty
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Please enter a password!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // make progress bar visible
                mProgressBar.setVisibility(View.VISIBLE);

                // use fire base auth API to sign in with email and password
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // set progress bar to invisible
                        mProgressBar.setVisibility(View.GONE);

                        // if sign in was unsuccessful notify user
                        if (!task.isSuccessful()) {
                            // there was an error
                            if (password.length() < 6) {
                                mEditTextPassword.setError(getString(R.string.minimum_password));
                            } else {
                                Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                            }
                        // else if sign in was successful start MainActivity
                        } else {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            }
        });
    }
}
