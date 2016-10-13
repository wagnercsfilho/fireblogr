package com.wagnercsfilho.fireblogr.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.wagnercsfilho.fireblogr.R;
import com.wagnercsfilho.fireblogr.model.Post;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    DatabaseReference mDatabaseRefPosts;

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;

    @BindView(R.id.list_post)
    RecyclerView mPostList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, NewPostActivity.class));
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ButterKnife.bind(this);


        mDatabaseRefPosts = FirebaseDatabase.getInstance().getReference().child("posts");
        mPostList.setHasFixedSize(true);
        mPostList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            case R.id.action_logout:
                logout();
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation mView item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            fetchBlogPosts();
        }
    }

    private void fetchBlogPosts() {
        FirebaseRecyclerAdapter<Post, PostViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(
                Post.class,
                R.layout.list_post,
                PostViewHolder.class,
                mDatabaseRefPosts
        ) {
            @Override
            protected void populateViewHolder(PostViewHolder viewHolder, Post model, int position) {

                final String postId = getRef(position).toString();

                viewHolder.setTitle(model.getTitle());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setUser(getApplicationContext(), model.getUser());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(MainActivity.this, "You clicked a View: " + postId, Toast.LENGTH_LONG).show();
                    }
                });
            }
        };

        mPostList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {

        View mView;

        TextView mPostTitleText;
        TextView mPostDescText;
        ImageView mPostMediaImage;
        TextView mUserFullNameText;
        ImageView mUserAvatarImage;

        public PostViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            mPostTitleText = (TextView) mView.findViewById(R.id.text_post_title);
            mPostDescText = (TextView) mView.findViewById(R.id.text_post_description);
            mPostMediaImage = (ImageView) mView.findViewById(R.id.image_post_media);
            mUserFullNameText = (TextView) mView.findViewById(R.id.text_user_name);
            mUserAvatarImage = (ImageView) mView.findViewById(R.id.image_user_avatar);

            mPostTitleText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.v("CLICK", "CLICKOU");
                }
            });
        }

        public void setTitle(String title) {
            mPostTitleText.setText(title);
        }

        public void setDescription(String description) {
            mPostDescText.setText(description);
        }

        public void setImage(final Context context, final String image) {
            Picasso.with(context).load(image).into(mPostMediaImage);
        }

        public void setUser(final Context context, final Post.User user) {
            mUserFullNameText.setText(user.getName());

            Transformation transformation = new RoundedTransformationBuilder()
                    .borderColor(Color.BLACK)
                    .borderWidthDp(0)
                    .cornerRadiusDp(30)
                    .oval(false)
                    .build();

            Picasso.with(context).load(user.getImage()).fit()
                    .transform(transformation).into(mUserAvatarImage);
        }
    }

}
