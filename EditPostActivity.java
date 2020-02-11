package com.infenodesigns.feedsapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;

public class EditPostActivity extends AppCompatActivity {

    private Toolbar mPostToolBar;

    private EditText mEditPostDescription;
    private ProgressBar mUploadProgressBar;

    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseAuth mAuth;

    private String currentUserId;
    private String blogPostId;

    private FrameLayout blurLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        mPostToolBar = findViewById(R.id.edit_post_toolbar);
        setSupportActionBar(mPostToolBar);
        getSupportActionBar().setTitle("Edit Post");

        blogPostId = getIntent().getStringExtra("blogPostId");

        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid();

        mEditPostDescription = findViewById(R.id.edit_post_description);
        blurLayout = findViewById(R.id.blurScreen);

        mFirebaseFirestore.collection("posts").document(blogPostId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String desc = document.getString("desc");
                        mEditPostDescription.setText(desc);
                    } else {
                        Log.d("EditPostActivity", "No such document");
                    }
                } else {
                    Log.d("EditPostActivity", "get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.new_post_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.post_button) {
            editPost();
        }

        return true;
    }

    private void editPost() {

        String editPost = mEditPostDescription.getText().toString();

        if (!TextUtils.isEmpty(editPost)) {
            blurLayout.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            mFirebaseFirestore.collection("posts").document(blogPostId)
                    .update("desc", editPost)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(EditPostActivity.this, "Post updated", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditPostActivity.this, "Error while update", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(EditPostActivity.this, "Please add a description", Toast.LENGTH_SHORT).show();
        }

    }
}
