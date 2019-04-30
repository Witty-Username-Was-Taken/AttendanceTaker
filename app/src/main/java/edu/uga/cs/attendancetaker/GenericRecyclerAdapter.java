package edu.uga.cs.attendancetaker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

public class GenericRecyclerAdapter extends RecyclerView.Adapter<GenericRecyclerAdapter.GenericViewHolder> {

    private static final String TAG = "GenericRecyclerAdapter";
    private List<String> mList;
    private LayoutInflater mInflater;

    public GenericRecyclerAdapter(Context context, List<String> ccaList) {
        mList = ccaList;
        mInflater = LayoutInflater.from(context);
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

        public GenericViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRecyclerItem = itemView.findViewById(R.id.tvRecyclerItem);
            Log.d(TAG, "GenericViewHolder: In viewholder constructor");
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.e(TAG, "onClick: Clicked item" + tvRecyclerItem.getText().toString() + " Position: " + getAdapterPosition(), null);
                }
            });
        }

    }
}
