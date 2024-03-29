package edu.uga.cs.attendancetaker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScanBarcodeActivity extends AppCompatActivity {

    private static final String TAG = "GoogleActivity";

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private DocumentReference docIdRef;
    private DocumentReference docIdRef2;

    Button scanButton;
    Button home;
    Button signOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_barcode);

        scanButton = findViewById(R.id.scan_barcode_button);
        home = findViewById(R.id.scan_barcode_home_button);
        signOut = findViewById(R.id.scan_barcode_sign_out_button);

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startQRScanner();
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ScanBarcodeActivity.this, StudentMainScreenActivity.class);
                startActivity(intent);
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                signOut();
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    private void startQRScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                addAttendanceRecordToDatabase(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void addAttendanceRecordToDatabase(final String crn) {
        final DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy"); // Was MM/dd/yyyy hh:mm a
        //DateFormat dateFormat1 = new SimpleDateFormat("MM-dd-yyyy");
        Date d = new Date();
        final String date = dateFormat.format(d);
        //String date1 = dateFormat1.format(d);

        // Access a Cloud Firestore instance from your Activity
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        Map<String, Object> docData = new HashMap<>();


        // Start code here
        docIdRef = db.collection("attendanceRecords").document(user.getDisplayName() + " " + crn);
        docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (!document.exists()) {
                        Log.d(TAG, "Document Does not exist! Looking for: " + crn);
                        Log.d(TAG, "Document Does not exist!");
                        docIdRef2 = db.collection("classes").document(crn);
                        docIdRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentSnapshot = task.getResult();

                                    Map<String, Object> docData = new HashMap<>();
                                    docData.put("student", user.getUid());
                                    docData.put("crn", crn);

//<<<<<<< Updated upstream
                                    docData.put("className", documentSnapshot.getString(CourseOptionsActivity.KEY_CLASS_NAME)); // Added by SJ

                                    List<Timestamp> dates = (List<Timestamp>) documentSnapshot.get("Dates");
                                    List<String> stringDates = new ArrayList<String>();

                                    final Map<String, Object> statusMap = new HashMap();
                                    Map<String, Object> dateStatusMap = new HashMap<>();

                                    for (Timestamp s : dates) {
                                        Log.d(TAG, "Found date: " + s);
                                        Date newDate = s.toDate();
                                        stringDates.add(dateFormat.format(s.toDate()));
                                        String sDate = dateFormat.format(newDate);
                                        //docData.put(sDate, "Absent");
                                        dateStatusMap.put(sDate, "Absent");
                                    }

                                    //statusMap.put("status", dateStatusMap);
                                    docData.put("status", dateStatusMap);
                                    docIdRef.set(statusMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "onComplete: Statusmap " + statusMap);
                                                System.err.println("STATUSMAP" + statusMap);
                                            }
                                        }
                                    });

                                    if (stringDates.contains(date)) {

                                        Log.d(TAG, "Today's date found!");
                                        dateStatusMap.put(date, "Present");
                                        statusMap.put("status", dateStatusMap);
                                        docIdRef.set(statusMap, SetOptions.merge());
                                    } else {
                                        Log.d(TAG, "Today's date not found");
                                    }

                                    db.collection("attendanceRecords").document(user.getDisplayName() + " "
                                            + crn).set(docData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot successfully written for new Record!");
                                        }
                                    })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w(TAG, "Error writing document", e);
                                                }
                                            });
                                } else {
                                    Log.d(TAG, "Failed");
                                }
                            }
                        });
                    } else {
                        Log.d(TAG, "Document exists");

                        docIdRef2 = db.collection("classes").document(crn);
                        docIdRef2.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentSnapshot = task.getResult();

                                    List<Timestamp> dates = (List<Timestamp>) documentSnapshot.get("Dates");
                                    List<String> dateStrings = new ArrayList<>();
                                    for (Timestamp d : dates) {
                                        dateStrings.add(dateFormat.format(d.toDate()));
                                    }

                                    if (dateStrings.contains(date)) {
                                        Log.d(TAG, "Right day!");
                                        Map<String, Object> docData = new HashMap<>();
                                        Log.e(TAG, "onComplete: documentsnapshot " + documentSnapshot.contains(date), null);
                                        // TODO: UPDATE status map with absents


                                        Map<String, Object> dateStatusMap = new HashMap<>();
                                        Map<String, Object> statusMap = new HashMap<>();

                                        dateStatusMap.put(date, "Present"); // Added by sj

                                        statusMap.put("status", dateStatusMap);
                                        docIdRef.set(statusMap, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "onComplete: Appended map to db");
                                                }
                                            }
                                        });

                                        docData.put(date, "Present");


                                        docIdRef.update(docData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "Successfully updated " + date);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "Failed to update " + date);
                                            }
                                        });
                                    } else {
                                        Log.d(TAG, "Wrong day!");
                                    }
                                }
                            }
                        });

                    }


                } else {
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }
        });
        // End new code here
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(ScanBarcodeActivity.this, SplashScreenActivity.class);
                        startActivity(intent);
                    }
                });
    }


}
