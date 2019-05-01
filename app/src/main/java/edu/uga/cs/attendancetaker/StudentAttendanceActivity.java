package edu.uga.cs.attendancetaker;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentAttendanceActivity extends AppCompatActivity {


    public static final int STUDENT_ATTENDANCE_ACTIVITY_ID = 2;

    private static final String TAG = "StudentAttendance";
    public static final String KEY_STATUS = "status";

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;


    List<String> genericDataList;// since we are reusing the same recyclerview
    List<String> datesList = new ArrayList<>();
    Context context;
    String crnFromPreviousActivity;


    // firebase stuff
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference attendanceRef = db.collection("attendanceRecords");
    public static final String KEY_CRN = "crn";
    public static final String KEY_CLASS_NAME = "className";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_attendance);

        Intent intent = getIntent();
        crnFromPreviousActivity = intent.getStringExtra("CRN_STRING");
        Log.d(TAG, "onCreate: " + crnFromPreviousActivity);

        getStudentsClasses();
    }


    private void getStudentsClasses() {

        FirebaseUser currentUser = mAuth.getCurrentUser();

        Log.d(TAG, "getStudentsClasses: Current user is " + currentUser.getUid());

        attendanceRef.whereEqualTo("student", currentUser.getUid())
                .whereEqualTo("crn", crnFromPreviousActivity)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<String, Object> dataMap;
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                // Add both, the date and present/absent status

                                dataMap = document.getData();

                                Log.e(TAG, "onComplete: datamap " + dataMap.get(KEY_STATUS), null);


                                HashMap<String, String> dateStatusMap = (HashMap<String, String>) dataMap.get(KEY_STATUS); // get the map of {date: Present/Absent}
                                Log.e(TAG, "onComplete: datestatusmap " + dateStatusMap, null);

                                for (String dateKey : dateStatusMap.keySet()) {
                                    // Add data to make sure that the dates displayed don't exceed the current timestamp
                                    // if date < currentTimestamp then add to list
                                    String formattedStatus = dateKey + "\t" + dateStatusMap.get(dateKey);
                                    datesList.add(formattedStatus);


                                }

                            }

                            genericDataList = datesList;
                            loadRecycleriew();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    private void loadRecycleriew() {
        mRecyclerView = findViewById(R.id.recyclerViewAttendance);

        mRecyclerAdapter = new GenericRecyclerAdapter(getApplicationContext(), genericDataList, STUDENT_ATTENDANCE_ACTIVITY_ID); // -1 means don't use this activity's recycler view element's onclick
        mRecyclerView.setAdapter(mRecyclerAdapter);

        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }
}