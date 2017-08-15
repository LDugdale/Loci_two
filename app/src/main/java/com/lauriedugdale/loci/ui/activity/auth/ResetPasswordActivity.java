package com.lauriedugdale.loci.ui.activity.auth;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.lauriedugdale.loci.R;

/**
 * Contains the UI logic for the reset password activity
 *
 * @author Laurie Dugdale
 */
public class ResetPasswordActivity extends AppCompatActivity {

    private EditText mEditTextEmail;
    private Button mButtonReset;
    private Button mButtonBack;
    private FirebaseAuth mAuth;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // find UI elements
        mEditTextEmail = (EditText) findViewById(R.id.email);
        mButtonReset = (Button) findViewById(R.id.btn_reset_password);
        mButtonBack = (Button) findViewById(R.id.btn_back);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        mButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        onResetButtonClicked();
    }

    /**
     * Listener for the reset button
     */
    private void onResetButtonClicked(){
        mButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // get email String
                String email = mEditTextEmail.getText().toString().trim();

                // if email is empty
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplication(), "Please enter the email you registered an account with", Toast.LENGTH_SHORT).show();
                    return;
                }

                // show progress bar
                mProgressBar.setVisibility(View.VISIBLE);

                // use firebase auth API to sent the reset password email
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ResetPasswordActivity.this, "We have sent you the instructions to reset your password!!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ResetPasswordActivity.this, "We were not able to send the reset email!", Toast.LENGTH_SHORT).show();
                                }

                                // hide the progress bar
                                mProgressBar.setVisibility(View.GONE);
                            }
                        });
            }
        });
    }

}
