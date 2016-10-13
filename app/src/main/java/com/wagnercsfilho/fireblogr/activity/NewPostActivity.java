package com.wagnercsfilho.fireblogr.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

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
import com.wagnercsfilho.fireblogr.R;
import com.wagnercsfilho.fireblogr.model.Post;
import com.wagnercsfilho.fireblogr.model.User;

import java.util.Random;

import static android.content.Intent.ACTION_GET_CONTENT;

public class NewPostActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST = 1;

    ImageButton imageMedia;

    EditText editTitle;

    EditText editDescription;

    Uri imageUri;

    StorageReference storageReference;
    DatabaseReference postReference;
    DatabaseReference userReference;

    FirebaseAuth mAuth;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        mAuth = FirebaseAuth.getInstance();

        storageReference = FirebaseStorage.getInstance().getReference();
        postReference = FirebaseDatabase.getInstance().getReference().child("posts");
        userReference = FirebaseDatabase.getInstance().getReference().child("users");

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

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            imageUri = data.getData();
            imageMedia.setImageURI(imageUri);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_post_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_share) {
            createPost(item.getActionView());
        }

        return super.onOptionsItemSelected(item);
    }


    private void createPost(View v) {
        final String title = editTitle.getText().toString().trim();
        final String description = editDescription.getText().toString().trim();

        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(description) && imageMedia != null) {

            progressDialog.setMessage("Posting....");
            progressDialog.show();

            StorageReference filePath = storageReference.child("blog_images").child(imageUri.getLastPathSegment());
            filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {

                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    userReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {

                                Uri downloadUri = taskSnapshot.getDownloadUrl();
                                DatabaseReference newPost = postReference.push();

                                User user = dataSnapshot.getValue(User.class);

                                Post post = new Post();
                                post.setTitle(title);
                                post.setDescription(description);
                                post.setImage(downloadUri.toString());

                                post.getUser().setId(mAuth.getCurrentUser().getUid().toString());
                                post.getUser().setName(user.getName());
                                post.getUser().setImage(user.getImage());

                                newPost.setValue(post).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {
                                            startActivity(new Intent(NewPostActivity.this, MainActivity.class));
                                        }

                                        progressDialog.dismiss();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }
            });
        } else {
            Snackbar.make(v, "Fill all fields", Snackbar.LENGTH_LONG).setAction("Action", null).show();
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
