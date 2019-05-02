package edu.uga.cs.attendancetaker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseInfoActivity extends AppCompatActivity {

    private static final String TAG = "GoogleActivity";

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private GoogleSignInClient mGoogleSignInClient;
    private DocumentReference docIdRef;

    EditText subject;
    EditText crn;
    EditText className;

    Button newBarcode;
    Button signOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_info);

        subject = findViewById(R.id.subject_edit_text);
        crn = findViewById(R.id.crn_edit_text);
        className = findViewById(R.id.class_name_edit_text);
        newBarcode = findViewById(R.id.course_info_new_barcode_button);
        signOut = findViewById(R.id.course_info_sign_out_button);

        newBarcode.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addClassToDatabase(subject.getText().toString(), crn.getText().toString(),
                        className.getText().toString());

                Intent intent = new Intent(CourseInfoActivity.this, NewBarcodeActivity.class);
                intent.putExtra("selectedCrn", crn.getText().toString());
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

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(CourseInfoActivity.this, SplashScreenActivity.class);
                        startActivity(intent);
                    }
                });
    }

    private void addClassToDatabase(final String subject, String crn, final String className) {
        // Access a Cloud Firestore instance from your Activity
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        firebaseUser = mAuth.getCurrentUser();

        docIdRef = db.collection("classes").document(crn);
        docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "Document exists!");
                    } else {
                        Log.d(TAG, "Document does not exist!");
                        Map<String, Object> docData = new HashMap<>();

                        // Testing adding Dates array
                        /*List<String> dates = new ArrayList<String>();
                        dates.add("04-30-19");
                        dates.add("05-02-2019");*/

                        // End Testing

                        docData.put("subject", subject);
                        docData.put("className", className);
                        docData.put("professor", firebaseUser.getUid());
                        //docData.put("Dates", dates);
                        docIdRef.set(docData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully written!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error writing document", e);
                                    }
                                });

                    }
                } else {
                    Log.d(TAG, "Failed with: ", task.getException());
                }
            }
        });
    }

}
