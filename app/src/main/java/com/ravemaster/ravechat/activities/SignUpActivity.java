package com.ravemaster.ravechat.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ravemaster.ravechat.R;
import com.ravemaster.ravechat.databinding.ActivitySignUpBinding;
import com.ravemaster.ravechat.utilities.Constants;
import com.ravemaster.ravechat.utilities.PreferenceManager;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;

    String encodedImage = "";
    String name = "";
    String email = "";
    String password = "";
    String repeatPassword = "";
    EditText one, two, three, four;

    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(this);
        getEditTexts();

        binding.btnGoToLogIn.setOnClickListener(v->{
            Intent intent = new Intent(this, LogInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        binding.btnCreateAccount.setOnClickListener(v->{
            createAccount();
        });

        binding.pickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                pickImage.launch(intent);
            }
        });
    }

    private void createAccount(){
        if (checkEmpty()){
            if (checkPasswords()){
                signUp();
            } else {
                showToasts("Passwords do not match!");
            }
        } else {
            showToasts("Please fill in all fields!");
        }

    }

    private void getEditTexts(){
        one = binding.createName.getEditText();
        two = binding.createEmail.getEditText();
        three = binding.createPassword.getEditText();
        four = binding.repeatPassword.getEditText();
    }

    private void signUp(){
        isLoading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, one.getText().toString());
        user.put(Constants.KEY_EMAIL, two.getText().toString());
        user.put(Constants.KEY_PASSWORD, four.getText().toString());
        user.put(Constants.KEY_IMAGE, encodedImage);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    isLoading(false);

                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                    preferenceManager.putString(Constants.KEY_USER_ID,documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME,one.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE,encodedImage);

                    showToasts("Welcome "+name);

                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class );
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    isLoading(false);
                    showToasts(e.getMessage());
                });
    }

    private boolean checkEmpty(){
        name = one.getText().toString();
        email = two.getText().toString();
        password = three.getText().toString();
        repeatPassword = four.getText().toString();

        if (name.isEmpty()){
            setErrors(binding.createName,"Field empty!");
            return false;
        } else if (email.isEmpty()){
            setErrors(binding.createEmail,"Field empty!");
            return false;
        } else if (password.isEmpty()){
            setErrors(binding.createPassword,"Field empty!");
            return false;
        } else if (repeatPassword.isEmpty()){
            setErrors(binding.repeatPassword,"Field empty!");
            return false;
        } else if(encodedImage.isEmpty()) {
            showToasts("Please pick profile image.");
           return false;
        }
        else {
            removeErrors();
            return true;
        }
    }

    private void setErrors(TextInputLayout layout, String error){
        layout.setErrorEnabled(true);
        layout.setError(error);
    }

    private void removeErrors(){
        binding.createName.setErrorEnabled(false);
        binding.createEmail.setErrorEnabled(false);
        binding.repeatPassword.setErrorEnabled(false);
        binding.createPassword.setErrorEnabled(false);
    }

    private boolean checkPasswords(){
        if (password.equals(repeatPassword)){
            removeErrors();
            return true;
        } else {
            setErrors(binding.repeatPassword,"Passwords do not match!");
            return false;
        }
    }

    private void showToasts(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void isLoading(boolean loading){
        if (loading){
            binding.btnCreateAccount.setVisibility(View.GONE);
            binding.createProgress.setVisibility(View.VISIBLE);
            binding.createProgress.setIndeterminate(true);
        } else {
            binding.btnCreateAccount.setVisibility(View.VISIBLE);
            binding.createProgress.setVisibility(View.GONE);

        }
    }

    private ActivityResultLauncher<Intent> pickImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult o) {
            if (o.getResultCode() == RESULT_OK){
                if (o.getData() != null){
                    Uri imageUri = o.getData().getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        binding.chooseImage.setImageBitmap(bitmap);
                        encodedImage = getEncodedImage(bitmap);
                    } catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                }
            }
        }
    });

    private String getEncodedImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth/bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,outputStream);
        byte[] bytes = outputStream.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);
    }


}