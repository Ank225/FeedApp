package com.infenodesigns.feedsapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.infenodesigns.feedsapp.adapters.CommentsRecyclerAdapter;
import com.infenodesigns.feedsapp.models.Comments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class CommentsActivity extends AppCompatActivity {

    private EditText commentsField;
    private Button commentsPostButton;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;

    private RecyclerView commentRecyclerLists;
    private CommentsRecyclerAdapter commentsRecyclerAdapter;
    private List<Comments> commentLists;

    private String blogPostId;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        firebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        blogPostId = getIntent().getStringExtra("blog_post_id");

        currentUserID = mAuth.getCurrentUser().getUid();
        commentsField = findViewById(R.id.comments_field);
        commentsPostButton = findViewById(R.id.comments_post_button);
        commentRecyclerLists = findViewById(R.id.comments_list);

        //Recycler  firebase list
        commentLists = new ArrayList<>();
        commentsRecyclerAdapter = new CommentsRecyclerAdapter(commentLists);
        commentRecyclerLists.setHasFixedSize(true);
        commentRecyclerLists.setLayoutManager(new LinearLayoutManager(this));
        commentRecyclerLists.setAdapter(commentsRecyclerAdapter);

        firebaseFirestore.collection("posts/" + blogPostId + "/comments").orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    String commentId = doc.getDocument().getId();
                                    Comments comments = doc.getDocument().toObject(Comments.class);
                                    commentLists.add(comments);
                                    commentsRecyclerAdapter.notifyDataSetChanged();

                                }

                            }

                        }

                    }
                });

        commentsPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String commentMessage = commentsField.getText().toString();

                if (!TextUtils.isEmpty(commentMessage)) {

                    Map<String, Object> commentsMap = new HashMap<>();
                    commentsMap.put("message", commentMessage);
                    commentsMap.put("user_id", currentUserID);
                    commentsMap.put("timestamp", FieldValue.serverTimestamp());

                    firebaseFirestore.collection("posts/" + blogPostId + "/comments")
                            .add(commentsMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {

                            if (task.isSuccessful()) {
                                commentsField.setText("");
                                //Toast.makeText(CommentsActivity.this, "Post comment successfully", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }
}

