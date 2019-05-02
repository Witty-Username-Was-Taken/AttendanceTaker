package edu.uga.cs.attendancetaker;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class GenericRecyclerAdapter extends RecyclerView.Adapter<GenericRecyclerAdapter.GenericViewHolder> {

    private static final String TAG = "GenericRecyclerAdapter";
    private List<String> mList;
    private LayoutInflater mInflater;

    private int whichActivity;

    public GenericRecyclerAdapter(Context context, List<String> ccaList, int activityId) {
        mList = ccaList;
        mInflater = LayoutInflater.from(context);
        whichActivity = activityId;
        Log.d(TAG, "GenericRecyclerAdapter: " + Arrays.asList(mList));
    }

    @NonNull
    @Override
    public GenericViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = mInflater.inflate(R.layout.recycler_item, parent, false);
        return new GenericViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenericViewHolder holder, int i) {
        String currentItem = mList.get(i);
        Log.d(TAG, "onBindViewHolder: Current item: " + currentItem);
        holder.tvRecyclerItem.setText(currentItem);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class GenericViewHolder extends RecyclerView.ViewHolder {

        TextView tvRecyclerItem; // can be course or student or attendance
        private final Context context;


        private boolean checkProfessor() {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            boolean isProfessor = false;
            String[] professor_emails = context.getResources().getStringArray(R.array.professor_emails);
            for (String email : professor_emails) {
                if (user.getEmail().equals(email)) {
                    isProfessor = true;
                    return isProfessor;
                }
            }
            return isProfessor;
        }

        public GenericViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            tvRecyclerItem = itemView.findViewById(R.id.tvRecyclerItem);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Log.e(TAG, "onClick: Clicked item: " + tvRecyclerItem.getText().toString() + " Position: " + getAdapterPosition(), null);

                    // since this is meant to be a generic recycler view, we choose the new
                    // activity to be started depending on the activity the data item is being clicked from

                    // set colors for the textviews
                    if (whichActivity == StudentAttendanceActivity.STUDENT_ATTENDANCE_ACTIVITY_ID) {
                        if (tvRecyclerItem.getText().toString().split("\\s+")[1].equalsIgnoreCase("Present")) {
                            // then color green
                            Log.e(TAG, "onClick: Clicked recyclerview ", null);
                            tvRecyclerItem.setBackgroundResource(R.color.present);
                        } else {
                            // color red
                            tvRecyclerItem.setBackgroundResource(R.color.absent);
                        }
                    }
                    Log.e(TAG, "onClick: Context.getClass(): " + context.getClass(), null);

                    callRespectiveActivity();

                }
            });
        }

        private void callRespectiveActivity() {

            if (checkProfessor() != true) { // student
                Intent intent = new Intent(context, StudentAttendanceActivity.class); //default, just for testing. might remove later
                switch (whichActivity) {

                    case CourseOptionsActivity.COURSE_OPTIONS_ACTIVITY_ID: // 1
                        intent = new Intent(context, StudentAttendanceActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("CRN_STRING", tvRecyclerItem.getText().toString().split("\\s+")[0]);
                        context.startActivity(intent);
                        break;

                    case StudentAttendanceActivity.STUDENT_ATTENDANCE_ACTIVITY_ID:
                        Log.e(TAG, "onClick: Clicked recyclerview " + tvRecyclerItem.getText().toString(), null);

                        break;

                    default:
                        break;
                }

            } else if (checkProfessor() == true) {

                Log.d(TAG, "onClick: checkProfessor() == true");
                Intent intent = new Intent(context, StudentAttendanceActivity.class); //default, just for testing. might remove later
                switch (whichActivity) {

                    case CourseOptionsActivity.COURSE_OPTIONS_ACTIVITY_ID: // 1
                        Log.d(TAG, "callRespectiveActivity: Before getting exception");
                        intent = new Intent(context, StudentSelectionActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        CourseOptionsActivity.SELECTED_CLASS = tvRecyclerItem.getText().toString();
                        intent.putExtra("className", tvRecyclerItem.getText().toString());
                        context.startActivity(intent);
                        break;

                    case StudentAttendanceActivity.STUDENT_ATTENDANCE_ACTIVITY_ID: //2
                        Log.e(TAG, "onClick: Clicked recyclerview " + tvRecyclerItem.getText().toString(), null);

                        break;

                    case StudentSelectionActivity.STUDENT_SELECTION_ACTIVITY_ID: // 3
                        Log.d(TAG, "callRespectiveActivity: Going to attendance activity");
                        intent = new Intent(context, StudentAttendanceActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("STUDENT_NAME", tvRecyclerItem.getText().toString());
                        context.startActivity(intent);
                        break;

                    default:
                        break;
                }

            }
        }

    }
}
