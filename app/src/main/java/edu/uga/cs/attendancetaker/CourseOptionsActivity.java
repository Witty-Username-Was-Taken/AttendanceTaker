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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
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

    private static final int VERTICAL_ITEM_SPACE = 35;

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
    private GoogleSignInClient mGoogleSignInClient;
    private CollectionReference attendanceRef = db.collection("attendanceRecords");
    public static final String KEY_CRN = "crn";
    public static final String KEY_CLASS_NAME = "className";

    private CollectionReference classesRef = db.collection("classes");


    List<String> classList = new ArrayList<>();

    Button home;
    Button signOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_options);

        home = findViewById(R.id.course_options_home_button);
        signOut = findViewById(R.id.course_options_sign_out_button);

        home.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent;
                if(checkProfessor()) {
                    intent = new Intent(CourseOptionsActivity.this, ProfessorMainScreenActivity.class);
                } else {
                    intent = new Intent(CourseOptionsActivity.this, StudentMainScreenActivity.class);
                }
                startActivity(intent);
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                signOut();
            }
        });

        // 1. get data from the database and populate an Arraylist
        // 2. populate the arraylist data into the reyclerview
        context = getApplicationContext();

        Intent intent = getIntent();

        if (intent.getStringExtra("from").equalsIgnoreCase("studentActivity")) {
            getStudentsClasses();
        } else if (intent.getStringExtra("from").equalsIgnoreCase("professorActivity")){
            getProfessorsClasses();
        }

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

    }

    private void loadRecycleriew() {
        mRecyclerView = findViewById(R.id.recyclerView);

        mRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));

        mRecyclerView.setHasFixedSize(true);

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

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(CourseOptionsActivity.this, SplashScreenActivity.class);
                        startActivity(intent);
                    }
                });
    }

    private boolean checkProfessor() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        boolean isProfessor = false;
        String[] professor_emails = getResources().getStringArray(R.array.professor_emails);
        for(String email : professor_emails) {
            if(user.getEmail().equals(email)) {
                isProfessor = true;
                return isProfessor;
            }
        }
        return isProfessor;
    }

}