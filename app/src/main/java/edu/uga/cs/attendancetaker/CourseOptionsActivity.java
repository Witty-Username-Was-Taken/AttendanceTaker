package edu.uga.cs.attendancetaker;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CourseOptionsActivity extends AppCompatActivity {

    private static final String TAG = "CourseOptionsActivity";
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter mRecyclerAdapter;

    List<String> genericDataList;// since we are reusing the same recyclerview
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_options);

        // 1. get data from the database and populate an Arraylist
        // 2. populate the arraylist data into the reyclerview
        context = getApplicationContext();

        new AsyncCourseOptionsTask().execute();

    }

    private List<String> loadDataSource() {
        List<String> list;
        list = new ArrayList<String>();
        list.add("Course 1");
        list.add("Course 2");
        list.add("Course 3");
        list.add("Course 4");
//        Log.d(TAG, "loadDataSource: Loaded data " + Arrays.asList(genericDataList));
        return list;
    }


    private class AsyncCourseOptionsTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... params) {
            genericDataList = loadDataSource();
            return genericDataList;
        }

        @Override
        protected void onPostExecute(List<String> strings) {
            super.onPostExecute(strings);
            mRecyclerView = findViewById(R.id.recyclerView);

            mRecyclerAdapter = new GenericRecyclerAdapter(getApplicationContext(), genericDataList);
            mRecyclerView.setAdapter(mRecyclerAdapter);

            mLayoutManager = new LinearLayoutManager(context);
            mRecyclerView.setLayoutManager(mLayoutManager);
        }
    }
}
