package no.hiof.danielch.eater.ui.form;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.UUID;

import no.hiof.danielch.eater.R;
import no.hiof.danielch.eater.model.Review;

public class ReviewFormActivity extends AppCompatActivity {

    private static final String TAG = "ReviewFormActivity";
    private int PICK_IMAGE_REQUEST = 1;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;
    private FirebaseFirestore db;
    FirebaseStorage storage;
    StorageReference storageRef;
    private String PLACE_ID;
    private Uri imageUri;
    private String imageDownloadUrl;
    double progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_form);

        firebaseAuth = FirebaseAuth.getInstance();
        initFirestore();
        initFirebaseStorage();

        PLACE_ID = getIntent().getStringExtra("placeid");

        Button btn_form_cancel = findViewById(R.id.btn_form_cancel);
        btn_form_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button btn_form_submit = findViewById(R.id.btn_form_submit);
        //Checks if picture is under uploading or no picture is choosen
        btn_form_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (progress == 100 && imageDownloadUrl != null){
                    addReview();
                    finish();
                }
                //If review dont contain picture
                if (progress == 0.0 && imageDownloadUrl == null){
                    addReview();
                    finish();
                }
                if (progress < 99.9 && progress > 0.1){
                    //Toast of progress
                    FancyToast.makeText(ReviewFormActivity.this, "Upload not complete: " + progress, FancyToast.LENGTH_SHORT, FancyToast.WARNING, false).show();
                }


            }
        });

        //Opens picture chooser
        Button btn_select_photo = findViewById(R.id.btn_select_photo);
        btn_select_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Method openFileChooser
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });

    }

    private void initFirebaseStorage() {
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference("Pictures");
    }

    private void initFirestore() {
        db = FirebaseFirestore.getInstance();
    }

    //Add review to db
    private void addReview() {
        RatingBar restaurant_form_rating = findViewById(R.id.restaurant_form_rating);
        EditText restaurant_form_text = findViewById(R.id.restaurant_form_text);

        //Create a new review object
        Review review = new Review(PLACE_ID,
                firebaseAuth.getCurrentUser().getUid(),
                String.valueOf(restaurant_form_rating.getRating()),
                restaurant_form_text.getText().toString(),
                imageDownloadUrl);

        //Adds object to db
        db.collection("reviews")
                .add(review)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                        }
                    });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        imageUri = data.getData();

        //Check if file is selected
        if (requestCode == PICK_IMAGE_REQUEST && data != null){
            imageUri = data.getData();

            ImageView imageUpload = findViewById(R.id.imageUpload);
            Glide.with(this).load(imageUri).into(imageUpload);
            imageUpload.setVisibility(View.VISIBLE);
            uploadImage(imageUri);
        }
    }

    //Return file extension from uri
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    //Uploads image
    private void uploadImage(Uri imageUri) {

        final StorageReference fileRef = storageRef.child(UUID.randomUUID() + "." + getFileExtension(imageUri));
        fileRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        //Get URL from upload
                        Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                //Sets image download url
                                imageDownloadUrl = uri.toString();
                                FancyToast.makeText(ReviewFormActivity.this, "Review is posted", FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false).show();
                                Log.d(TAG, "file uploaded: " + imageDownloadUrl);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        FancyToast.makeText(ReviewFormActivity.this, e.getMessage(), FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        //Updates progressbar
                        progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        ProgressBar uploadProgressBar = findViewById(R.id.uploadProgressBar);
                        uploadProgressBar.setProgress((int) progress);
                    }
                });

        Log.d(TAG, "uploadImage: " + fileRef);
    }


}
