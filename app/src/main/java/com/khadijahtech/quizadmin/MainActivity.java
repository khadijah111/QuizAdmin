package com.khadijahtech.quizadmin;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private static final int RC_PHOTO_PICKER = 1;


    private Button mButtonChooseImage;
    private Button mButtonUpload;
    private TextView mTextViewShowUploads;
    private EditText mEditTextFileName;
    private ImageView mImageView;
    private ProgressBar mProgressBar;

    private Uri mImageUri;

    //firebase DB
    private DatabaseReference mDatabaseReference;

    //firebase Storage
    private StorageReference mStorageRef;

    private StorageTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonChooseImage = findViewById(R.id.button_choose_image);
        mButtonUpload = findViewById(R.id.button_upload);
        mTextViewShowUploads = findViewById(R.id.text_view_show_uploads);
        mEditTextFileName = findViewById(R.id.edit_text_file_name);
        mImageView = findViewById(R.id.image_view);
        mProgressBar = findViewById(R.id.progress_bar);

        //get a reference to the root node(Firebase) in DB
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("uploads");
        mStorageRef = FirebaseStorage.getInstance().getReference().child("uploads");


        mButtonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, RC_PHOTO_PICKER);

            }
        });

        //Upload image to Fire base Storage WRITE
        mButtonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(MainActivity.this, "Uplaod in progress, please wait", Toast.LENGTH_LONG).show();
                } else {
                    uploadFileToFireBase();
                }
            }

        });

        mTextViewShowUploads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageActivity();
            }
        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            //handling the photo picker result
            mImageUri = data.getData();//get the image uri

            //put the image to the image view
            Picasso.get().load(mImageUri).into(mImageView);

            /*  */
        }

    }


    private void openImageActivity() {
        Intent i = new Intent(this, SecondActivity.class);
        startActivity(i);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void uploadFileToFireBase() {

        if (mImageUri != null) {
            // Get a reference to store file at this directory chat_photos/<FILENAME> in firebase STORAGE
            final StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + "." +
                    getFileExtension(mImageUri));

            // Upload file to Firebase STORAGE
            mUploadTask = fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setProgress(0);
                                }
                            }, 500);

                            Toast.makeText(MainActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // Got the download URL for 'users/me/profile.png'
                                    //save a new image object
                                    UploadImage uploadedImage = new UploadImage(mEditTextFileName.getText().toString().trim(),
                                            uri.toString());//****

                                    Toast.makeText(MainActivity.this, uri.toString(), Toast.LENGTH_LONG).show();

                                    //store the new object to DB
                                    //String uploadId = mDatabaseReference.push().getKey(); //create new entry to DB
                                    //mDatabaseReference.child(uploadId).setValue(uploadedImage);

                                    mDatabaseReference.push().setValue(uploadedImage);
                                }
                            });

                        }//end of on sucess 1
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mProgressBar.setProgress((int) progress);

                        }
                    });

        } else {
            Toast.makeText(this, "No file selected!!", Toast.LENGTH_LONG).show();
        }
    }
}
