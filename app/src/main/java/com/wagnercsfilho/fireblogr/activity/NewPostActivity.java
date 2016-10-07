package com.wagnercsfilho.fireblogr.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.wagnercsfilho.fireblogr.R;

import java.net.URI;
import java.util.Random;

import static android.content.Intent.ACTION_GET_CONTENT;

public class NewPostActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST = 1;

    ImageButton imageMedia;

    EditText editTitle;

    EditText editDescription;

    Button buttonCreate;

    Uri imageUri;

    StorageReference storageReference;
    DatabaseReference databaseReference;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        storageReference = FirebaseStorage.getInstance().getReference();
    databaseReference = FirebaseDatabase.getInstance().getReference().child("blog");

        progressDialog = new ProgressDialog(this);

        editTitle = (EditText) findViewById(R.id.edit_title);
        editDescription = (EditText) findViewById(R.id.edit_description);

        imageMedia = (ImageButton) findViewById(R.id.image_media);
        imageMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_REQUEST);
            }
        });

        buttonCreate = (Button) findViewById(R.id.button_create);
        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String title = editTitle.getText().toString().trim();
                final String description = editDescription.getText().toString().trim();

                if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(description) && imageMedia != null) {

                    progressDialog.setMessage("Posting....");
                    progressDialog.show();

                    StorageReference filePath = storageReference.child("blog_images").child(imageUri.getLastPathSegment());
                    filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri downloadUri = taskSnapshot.getDownloadUrl();

                            DatabaseReference newPost = databaseReference.push();

                            newPost.child("title").setValue(title);
                            newPost.child("description").setValue(description);
                            newPost.child("image").setValue(downloadUri.toString());


                            progressDialog.dismiss();
                        }
                    });
                } else {
                    Snackbar.make(v, "Fill all fields", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            imageUri = data.getData();
            imageMedia.setImageURI(imageUri);
        }
    }

    private static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(20);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}
