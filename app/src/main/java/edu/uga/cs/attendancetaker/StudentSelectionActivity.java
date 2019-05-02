package edu.uga.cs.attendancetaker;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Document;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentSelectionActivity extends AppCompatActivity {

    public static final int STUDENT_SELECTION_ACTIVITY_ID = 3;

    private static final int VERTICAL_ITEM_SPACE = 35;

    private static final String TAG = "StudentSelection";
    public static final String KEY_STATUS = "status";

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;


    List<String> genericDataList = new ArrayList<>();
    Context context;

    public static Map<String, String> studentUID = new HashMap<>();

    // firebase stuff
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private GoogleSignInClient mGoogleSignInClient;
    private CollectionReference attendanceRef = db.collection("attendanceRecords");
    public static final String KEY_CRN = "crn";
    public static final String KEY_CLASS_NAME = "className";

    Button home;
    Button signOut;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_selection);

        home = findViewById(R.id.student_selection_home_button);
        signOut = findViewById(R.id.student_selection_sign_out_button);

        home.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(StudentSelectionActivity.this, ProfessorMainScreenActivity.class);
                startActivity(intent);
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                signOut();
            }
        });

        Intent intent = getIntent();
        String classNameFromPreviousActivity = intent.getStringExtra("className");

        getStudentsOfSelectedClass(classNameFromPreviousActivity);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

    }

    private void getStudentsOfSelectedClass(String classNameFromPreviousActivity) {
        if (checkProfessor() == true) {
            Log.e(TAG, "onCreate: FROM STUDENT SELECTION", null);
            attendanceRef.whereEqualTo("className", classNameFromPreviousActivity)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                Map<String, Object> dataMap;
                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    dataMap = document.getData();
                                    Log.d(TAG, "onComplete: datamap " + dataMap);


                                    db.collection("students")
                                            .document((String)dataMap.get("student"))
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {

                                                        DocumentSnapshot documentSnapshot = task.getResult();
                                                        Log.d(TAG, "onComplete: LOAD STUDENTS " + documentSnapshot.getString("name"));
                                                        studentUID.put(documentSnapshot.getString("name"), documentSnapshot.getId());
                                                        genericDataList.add(documentSnapshot.getString("name"));
                                                    }

                                                    loadRecycleriew();
                                                }
                                            });
                                }

                            }
                        }
                    });
        }

    }


    private void loadRecycleriew() {
        mRecyclerView = findViewById(R.id.recyclerViewStudentSelection);

        mRecyclerAdapter = new GenericRecyclerAdapter(getApplicationContext(), genericDataList, STUDENT_SELECTION_ACTIVITY_ID); // -1 means don't use this activity's recycler view element's onclick
        mRecyclerView.setAdapter(mRecyclerAdapter);

        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }



    private boolean checkProfessor() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        boolean isProfessor = false;
        String[] professor_emails = getResources().getStringArray(R.array.professor_emails);
        for(String email : professor_emails) {
            if(user.getEmail().equals(email)) {
//                isProfessor = true;
                return true;
            }
        }
        return false;
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(StudentSelectionActivity.this, SplashScreenActivity.class);
                        startActivity(intent);
                    }
                });
    }


}
