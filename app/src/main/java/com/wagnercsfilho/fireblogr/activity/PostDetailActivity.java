package com.wagnercsfilho.fireblogr.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.wagnercsfilho.fireblogr.R;
import com.wagnercsfilho.fireblogr.model.Post;

public class PostDetailActivity extends AppCompatActivity {


    private String postId;

    private DatabaseReference mDatabaseRefPost;

    private FirebaseAuth mAuth;

    private ValueEventListener mPostListener;
    private ImageView singleImageSelect;
    private TextView singleTitleField;
    private TextView singleDescField;
    private Button singleRemoveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        postId = getIntent().getExtras().getString("postId");

        mDatabaseRefPost = FirebaseDatabase.getInstance().getReference().child("posts").child(postId);

        mAuth = FirebaseAuth.getInstance();

        mPostListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class);

                String imageURL = (String) post.getImage();
                String postTitle = (String) post.getTitle();
                String postDesc = (String) post.getDescription();
                String postUid = (String) post.getUser().getId();

                singleTitleField.setText(postTitle);
                singleDescField.setText(postDesc);

                Picasso.with(PostDetailActivity.this).load(imageURL).fit().centerCrop().into(singleImageSelect);

                if (mAuth.getCurrentUser().getUid().equals(postUid)) {
                    singleRemoveButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        singleImageSelect = (ImageView) findViewById(R.id.singleImageSelect);
        singleTitleField = (TextView) findViewById(R.id.singleTitleField);
        singleDescField = (TextView) findViewById(R.id.singleDescField);
        singleRemoveButton = (Button) findViewById(R.id.singleRemoveButton);


        singleRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabaseRefPost.child(postId).removeValue();
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDatabaseRefPost.addValueEventListener(mPostListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDatabaseRefPost.removeEventListener(mPostListener);
    }
}
