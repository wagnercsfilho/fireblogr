package com.wagnercsfilho.fireblogr.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.wagnercsfilho.fireblogr.R;
import com.wagnercsfilho.fireblogr.model.User;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends AppCompatActivity {

    public static final int GALLERY_REQUEST = 1;
    private final int CAMERA_REQUEST = 1;

    @BindView(R.id.edit_user_name)
    EditText mNameEdit;

    @BindView(R.id.image_user_avatar)
    ImageButton mUserAvatarImage;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabaseRefUsers;

    private StorageReference mStorageRefProfileImages;

    private ProgressDialog mProgressDialog;

    private Uri imageUserAvatarURI = null;

    private String userId;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        mProgressDialog = new ProgressDialog(this);

        mDatabaseRefUsers = FirebaseDatabase.getInstance().getReference().child("users");
        mStorageRefProfileImages = FirebaseStorage.getInstance().getReference().child("profile_images");

        ButterKnife.bind(this);

        mDatabaseRefUsers.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    mNameEdit.setText(user.getName());
                    if (!TextUtils.isEmpty(user.getImage())) {
                        Uri uriUserAvatar = Uri.parse(user.getImage());
                        Picasso.with(SettingsActivity.this).load(uriUserAvatar).into(mUserAvatarImage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callCameraIntent();
            } else {
                Toast.makeText(SettingsActivity.this, "You need set permission Camera to use this App", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @OnClick(R.id.image_user_avatar)
    public void changeUserAvatar(View v) {

        final String OPTION_CAMERA = "Camera";
        final String OPTION_GALLERY = "Gallery";

        final CharSequence cameraTypes[] = new CharSequence[]{OPTION_CAMERA, OPTION_GALLERY};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a camera");
        builder.setItems(cameraTypes, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (cameraTypes[which].equals(OPTION_CAMERA)) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
                    } else {
                        callCameraIntent();
                    }
                } else if (cameraTypes[which].equals(OPTION_GALLERY)) {
                    callGalleryIntent();
                }
            }
        });
        builder.show();
    }

    private void callGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST);
    }


    private void callCameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, GALLERY_REQUEST);
        }
    }

    @OnClick(R.id.button_save)
    public void saveUserInfo(View v) {
        final String name = mNameEdit.getText().toString().trim();

        if (!TextUtils.isEmpty(name) && imageUserAvatarURI != null) {

            mProgressDialog.setMessage("Loading...");
            mProgressDialog.show();

            mStorageRefProfileImages.putFile(imageUserAvatarURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(final UploadTask.TaskSnapshot task) {
                    String userId = mAuth.getCurrentUser().getUid();

                    User user = new User();
                    user.setName(name);
                    user.setImage(task.getDownloadUrl().toString());

                    mDatabaseRefUsers.child(userId).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mProgressDialog.dismiss();

                            if (task.isSuccessful()) {

                                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);

                            } else {
                                Toast.makeText(SettingsActivity.this, "Failed!", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            });

        } else {
            Toast.makeText(SettingsActivity.this, "Fill all fields", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Uri imageUri = null;

            if (requestCode == GALLERY_REQUEST) {
                imageUri = data.getData();
            }

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);

        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                imageUserAvatarURI = result.getUri();

                mUserAvatarImage.setImageURI(imageUserAvatarURI);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
