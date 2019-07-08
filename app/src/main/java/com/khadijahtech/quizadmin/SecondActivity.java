package com.khadijahtech.quizadmin;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SecondActivity extends AppCompatActivity {

    private ImageAdapter mImageAdapter;
    private RecyclerView mRecyclerView;

    private DatabaseReference mDatabaseRef;
    private List<UploadImage> mUploads;

    private ProgressBar mProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mProgress = findViewById(R.id.progress_circle);

        mUploads = new ArrayList<>();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        //****READ****//
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //called first time and if new image added to db
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    //deserialize the Data come from the DB into Object
                    UploadImage currentImage = d.getValue(UploadImage.class);
                    mUploads.add(currentImage);
                }
                mImageAdapter = new ImageAdapter(SecondActivity.this,mUploads);
                mRecyclerView.setAdapter(mImageAdapter);
               mProgress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SecondActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                mProgress.setVisibility(View.INVISIBLE);

            }
        });
    }
}
