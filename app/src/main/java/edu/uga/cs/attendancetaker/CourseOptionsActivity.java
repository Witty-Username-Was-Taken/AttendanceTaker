package edu.uga.cs.attendancetaker;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CourseOptionsActivity extends AppCompatActivity {

    public static final int COURSE_OPTIONS_ACTIVITY_ID = 1;

    private static final String TAG = "CourseOptionsActivity";
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    List<String> genericDataList;// since we are reusing the same recyclerview
    Context context;

    public static String SELECTED_CLASS;

    // firebase stuff
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference attendanceRef = db.collection("attendanceRecords");
    public static final String KEY_CRN = "crn";
    public static final String KEY_CLASS_NAME = "className";

    private CollectionReference classesRef = db.collection("classes");


    List<String> classList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_options);

        // 1. get data from the database and populate an Arraylist
        // 2. populate the arraylist data into the reyclerview
        context = getApplicationContext();

        Intent intent = getIntent();

        if (intent.getStringExtra("from").equalsIgnoreCase("studentActivity")) {
            getStudentsClasses();
        } else if (intent.getStringExtra("from").equalsIgnoreCase("professorActivity")){
            getProfessorsClasses();
        }

    }

    private void loadRecycleriew() {
        mRecyclerView = findViewById(R.id.recyclerView);

        mRecyclerAdapter = new GenericRecyclerAdapter(getApplicationContext(), genericDataList, COURSE_OPTIONS_ACTIVITY_ID);
        mRecyclerView.setAdapter(mRecyclerAdapter);

        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }



    private void getStudentsClasses() {

        FirebaseUser currentUser = mAuth.getCurrentUser();

        Log.d(TAG, "getStudentsClasses: Current user is " + currentUser.getUid());

        attendanceRef.whereEqualTo("student", currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                String crn = document.getString(KEY_CRN);
                                String className = document.getString(KEY_CLASS_NAME);
                                classList.add(crn + " | " + className);

                            }

                            genericDataList = classList;
                            loadRecycleriew();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    private void getProfessorsClasses() {

        FirebaseUser currentUser = mAuth.getCurrentUser();

        Log.d(TAG, "getStudentsClasses: Current user is " + currentUser.getUid());

        classesRef.whereEqualTo("professor", currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                String className = document.getString(KEY_CLASS_NAME);
                                classList.add(className);

                            }

                            Log.d(TAG, "onComplete: Received professor's classes: " + classList);
                            genericDataList = classList;
                            loadRecycleriew();

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

}
