package com.lauriedugdale.loci.ui.activity.auth;

import android.content.Intent;
import android.os.Bundle;
import android.sax.StartElementListener;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.lauriedugdale.loci.data.UserDatabase;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.ui.activity.MainActivity;

/**
 * Contains the UI logic for the sign up activity
 *
 * @author Laurie Dugdale
 */
public class SignupActivity extends AppCompatActivity {

    private EditText mEditTextUsername;
    private EditText mEditTextEmail;
    private EditText mEditTextPassword;
    private Button mButtonSignIn;
    private Button mButtonSignUp;
    private Button mButtonResetPassword;

    private ProgressBar mProgressBar;
    private FirebaseAuth mAuth;
    private UserDatabase mUserDatabase;

    private String mUsername;
    private String mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // access database methods
        mUserDatabase = new UserDatabase(this);
        //Get Firebase auth instance
        mAuth = FirebaseAuth.getInstance();

        // find ui elements
        mButtonSignIn = (Button) findViewById(R.id.sign_in_button);
        mButtonSignUp = (Button) findViewById(R.id.sign_up_button);
        mEditTextUsername = (EditText) findViewById(R.id.username);
        mEditTextEmail = (EditText) findViewById(R.id.email);
        mEditTextPassword = (EditText) findViewById(R.id.password);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mButtonResetPassword = (Button) findViewById(R.id.btn_reset_password);

        // finish activity and go back to LoginActivity
        mButtonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        onSignUpButtonClicked();
        onResetButtonClicked();
    }

    /**
     * Start ResetPasswordActivity on click
     */
    private void onResetButtonClicked(){
        mButtonResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, ResetPasswordActivity.class));
            }
        });
    }

    /**
     * The sign up button logic
     */
    private void onSignUpButtonClicked(){
        mButtonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mUsername = mEditTextUsername.getText().toString().trim();
                mEmail = mEditTextEmail.getText().toString().trim();
                String password = mEditTextPassword.getText().toString().trim();

                // if email is empty
                if (TextUtils.isEmpty(mEmail)) {
                    Toast.makeText(getApplicationContext(), "Please enter an email address!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // if password is empty
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Please enter a password!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // if password is too small
                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "The password is too short, please enter a minimum of 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // show the progress bar
                mProgressBar.setVisibility(View.VISIBLE);
                // access the firebase auth API to create a user with email address.
                mAuth.createUserWithEmailAndPassword(mEmail, password).addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // hide the progress bar
                                mProgressBar.setVisibility(View.GONE);

                                // if the tsk is not successful
                                if (!task.isSuccessful()) {
                                    Toast.makeText(SignupActivity.this, "Authentication has failed! Please try again!", Toast.LENGTH_SHORT).show();
                                }}
                        }).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        // on success start the MainActivity
                        startActivity(new Intent(SignupActivity.this, MainActivity.class));
                        // upload the user details to the database
                        mUserDatabase.uploadNewUser(mUsername, mEmail);
                        // finish the activity
                        finish();
                    }
                });

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mProgressBar.setVisibility(View.GONE);
    }
}