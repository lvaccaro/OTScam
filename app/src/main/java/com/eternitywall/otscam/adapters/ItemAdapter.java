package com.eternitywall.otscam.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eternitywall.otscam.R;
import java.util.LinkedHashMap;
import java.util.Map;


public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private Map<String,String> mDataset = new LinkedHashMap<>();
    OnItemClickListener mItemClickListener;

// Provide a reference to the views for each data item
// Complex data items may need more than one view per item, and
// you provide access to all the views for a data item in a view holder

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // each data item is just a string in this case
        public TextView mTvKey;
        public TextView mTvValue;

        public ViewHolder(final View v) {
            super(v);
            mTvKey = v.findViewById(R.id.tvKey);
            mTvValue = v.findViewById(R.id.tvValue);
            mTvKey.setOnClickListener(this);
            mTvValue.setOnClickListener(this);
        }
        @Override
        public void onClick(final View v) {
            final String key = mTvKey.getText().toString();
            if (mItemClickListener != null)
                mItemClickListener.onItemClick(v, getAdapterPosition(), key);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ItemAdapter(final Map<String,String> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ItemAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent,
                                                     final int viewType) {
        // create a new view
        final View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        final ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mTvKey.setText("");
        holder.mTvValue.setText("");
        synchronized (mDataset) {
            try {
                holder.mTvKey.setText(mDataset.keySet().toArray()[position].toString());
                holder.mTvValue.setText(mDataset.values().toArray()[position].toString());
            }catch (final Exception e){
                e.printStackTrace();
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position, String id);
    }

    public void setOnItemClickListener(final OnItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}