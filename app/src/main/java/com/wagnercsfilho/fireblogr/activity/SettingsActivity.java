package com.wagnercsfilho.fireblogr.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wagnercsfilho.fireblogr.R;

import butterknife.BindView;
import butterknife.OnClick;

public class SettingsActivity extends AppCompatActivity {

    public static final int GALLERY_REQUEST = 1;

    @BindView(R.id.edit_user_name)
    EditText editName;

    @BindView(R.id.image_user_avatar)
    ImageButton imageUserAvatar;

    Uri imageUserAvatarURI = null;

    FirebaseAuth firebaseAuth;

    DatabaseReference databaseReference;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        progressDialog = new ProgressDialog(this);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
    }

    @OnClick(R.id.image_user_avatar)
    public void changeUserAvatar(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST);
    }

    @OnClick(R.id.button_save)
    public void saveUserInfo(View v) {
        String name = editName.getText().toString().trim();

        if (!TextUtils.isEmpty(name) && imageUserAvatarURI != null) {

            progressDialog.setMessage("");

            String userId = firebaseAuth.getCurrentUser().getUid();

            databaseReference.child(userId).child("name").setValue(name);
            databaseReference.child(userId).child("image").setValue("default");
        } else {

        }
    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                imageUserAvatarURI = result.getUri();

                imageUserAvatar.setImageURI(imageUserAvatarURI);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
