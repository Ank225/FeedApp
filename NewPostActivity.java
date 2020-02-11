package com.infenodesigns.feedsapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ServerValue;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.victor.loading.rotate.RotateLoading;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {

    private static final int MAX_LENGTH = 50;
    private Toolbar mPostToolBar;

    private ImageView mNewPostImage;
    private EditText mNewPostDescription;
    private ProgressBar mUploadProgressBar;

    private FloatingActionMenu floatingActionMenu;
    private FloatingActionButton floatingActionButton;

    private StorageReference mStorageReference;
    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseAuth mAuth;

    private String currentUserId;

    private FrameLayout blurLayout;

    private Uri mPostImageUri = null;
    private byte[] mImageByte;

    private static final int GALLERY_PICK = 1;

    private String check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        mPostToolBar = findViewById(R.id.new_post_toolbar);
        setSupportActionBar(mPostToolBar);
        getSupportActionBar().setTitle("Add a NewPost");

        mStorageReference = FirebaseStorage.getInstance().getReference();
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        currentUserId = mAuth.getCurrentUser().getUid();

        mNewPostImage = findViewById(R.id.new_post_image);
        mNewPostDescription = findViewById(R.id.new_post_description);
        blurLayout = findViewById(R.id.blurScreen);

        floatingActionMenu = findViewById(R.id.floating_action_menu);
        floatingActionButton = findViewById(R.id.floating_action_select_photo);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(NewPostActivity.this, "Add photo", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "SELECT IMAGE"), GALLERY_PICK);
            }
        });

        /*mNewPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "SELECT IMAGE"), GALLERY_PICK);

            }
        });*/

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setMinCropWindowSize(500, 500)
                    .start(NewPostActivity.this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                mPostImageUri = result.getUri();
                mNewPostImage.setImageURI(mPostImageUri);

                File imagePath = new File(mPostImageUri.getPath());
                ByteArrayOutputStream imageBaos = new ByteArrayOutputStream();

                try {
                    Bitmap imageBitmap = new Compressor(this)
                            .setQuality(50)
                            .compressToBitmap(imagePath);

                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, imageBaos);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                mImageByte = imageBaos.toByteArray();

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
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
            makePost();
        }

        return true;
    }

    private void makePost() {

        final String newPost = mNewPostDescription.getText().toString();

        if (!TextUtils.isEmpty(newPost) && mPostImageUri != null) {

            blurLayout.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            String randomName = UUID.randomUUID().toString();

            final StorageReference filePath = mStorageReference.child("post_images").child(randomName + ".jpg");

            UploadTask imageUploadTask = filePath.putBytes(mImageByte);
            imageUploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {

                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            if (task.isSuccessful()) {
                                String downloadUri = uri.toString();

                                Map<String, Object> postMap = new HashMap<>();
                                postMap.put("image_uri", downloadUri);
                                postMap.put("desc", newPost);
                                postMap.put("user_id", currentUserId);
                                postMap.put("post_type", "image");
                                postMap.put("timestamp", FieldValue.serverTimestamp());

                                mFirebaseFirestore.collection("posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {

                                        if (task.isSuccessful()) {

                                            Snackbar snackbar = Snackbar
                                                    .make(mNewPostImage, "NewPost added successfully...", Snackbar.LENGTH_LONG);
                                            snackbar.show();
                                            Toast.makeText(NewPostActivity.this, "NewPost added successfully...", Toast.LENGTH_LONG).show();
                                            finish();

                                        } else {
                                            Toast.makeText(NewPostActivity.this, "Error while post...", Toast.LENGTH_SHORT).show();
                                        }
                                        //mUploadProgressBar.setVisibility(View.INVISIBLE);
                                    }
                                });

                            } else {
                                //mUploadProgressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });

                }
            });
            //blurLayout.setVisibility(View.GONE);
            //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else if (TextUtils.isEmpty(newPost) && mPostImageUri != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(NewPostActivity.this);
            builder.setMessage("Please add a description for the image");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            });
            // Create and show the AlertDialog
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else if (!TextUtils.isEmpty(newPost) && mPostImageUri == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(NewPostActivity.this);
            builder.setMessage("Are you sure want to upload post with out image?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (dialog != null) {
                        uploadTextPsot();
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            });
            // Create and show the AlertDialog
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else if (TextUtils.isEmpty(newPost) && mPostImageUri == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(NewPostActivity.this);
            builder.setMessage("Please add a image and description for post");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            });
            // Create and show the AlertDialog
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }


    }

    private void uploadTextPsot() {

        String newTextPost = mNewPostDescription.getText().toString();

        blurLayout.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        Map<String, Object> postMap = new HashMap<>();
        postMap.put("image_uri", "default");
        postMap.put("desc", newTextPost);
        postMap.put("user_id", currentUserId);
        postMap.put("post_type", "text");
        postMap.put("timestamp", FieldValue.serverTimestamp());

        mFirebaseFirestore.collection("posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {

                if (task.isSuccessful()) {

                    Toast.makeText(NewPostActivity.this, "Post added", Toast.LENGTH_SHORT).show();
                    finish();

                } else {
                    Toast.makeText(NewPostActivity.this, "Error while post", Toast.LENGTH_SHORT).show();
                }
                //mUploadProgressBar.setVisibility(View.INVISIBLE);
            }
        });

    }

    @Override
    public void onBackPressed() {

        check = mNewPostDescription.getText().toString();

        if (mPostImageUri != null || !TextUtils.isEmpty(check)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(NewPostActivity.this);
            builder.setMessage("Are you sure want to go back?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            });
            // Create and show the AlertDialog
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else {
            finish();
        }

    }
}

