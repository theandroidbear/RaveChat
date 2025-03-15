package com.ravemaster.ravechat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ravemaster.ravechat.databinding.ActivityLogInBinding;
import com.ravemaster.ravechat.utilities.Constants;
import com.ravemaster.ravechat.utilities.PreferenceManager;


public class LogInActivity extends AppCompatActivity {

    private ActivityLogInBinding binding;

    String email = "";
    String password = "";
    EditText one, two;

    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLogInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(this);
        getEditTexts();

        binding.btnGoToCreateAccount.setOnClickListener(v->{
            Intent intent = new Intent(this, SignUpActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        binding.btnLogIn.setOnClickListener(v->{
            checkDetails();
        });
    }

    private void getEditTexts(){
        one = binding.logInUsername.getEditText();
        two = binding.logInPassword.getEditText();
    }

    private void checkDetails(){
        if (checkEmpty()){
            signIn();
        } else {
            showToasts("Please fill in all fields");
        }

    }

    private void signIn() {
        isLoading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL,one.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,two.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().getDocuments().isEmpty()){
                        DocumentSnapshot snapshot = task.getResult().getDocuments().get(0);

                        preferenceManager.putString(Constants.KEY_USER_ID,snapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME,snapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE,snapshot.getString(Constants.KEY_IMAGE));

                        showToasts("Welcome back "+email);

                        Intent intent = new Intent(this, MainActivity.class );
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else{
                        isLoading(false);
                        showToasts("Unable to log in, check your credentials");
                    }
                });
    }


    private boolean checkEmpty(){
        email = one.getText().toString();
        password = two.getText().toString();

        if (email.isEmpty()){
            setErrors(binding.logInUsername,"Field empty!");
            return false;
        } else if (password.isEmpty()){
            setErrors(binding.logInPassword,"Field empty!");
            return false;
        } else {
            removeErrors();
            return true;
        }
    }

    private void setErrors(TextInputLayout layout, String error){
        layout.setErrorEnabled(true);
        layout.setError(error);
    }

    private void removeErrors(){
        binding.logInUsername.setErrorEnabled(false);
        binding.logInPassword.setErrorEnabled(false);
    }

    private void showToasts(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void isLoading(boolean loading){
        if (loading){
            binding.btnLogIn.setVisibility(View.GONE);
            binding.logInProgress.setVisibility(View.VISIBLE);
            binding.logInProgress.setIndeterminate(true);
        } else {
            binding.btnLogIn.setVisibility(View.VISIBLE);
            binding.logInProgress.setVisibility(View.GONE);

        }
    }

}