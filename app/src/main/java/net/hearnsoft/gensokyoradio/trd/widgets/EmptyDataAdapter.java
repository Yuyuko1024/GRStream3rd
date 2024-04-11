package net.hearnsoft.gensokyoradio.trd.widgets;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.hearnsoft.gensokyoradio.trd.R;

public class EmptyDataAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private View mItemView;

    public EmptyDataAdapter(){}

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_empty, parent, false);
        return new RecyclerView.ViewHolder(mItemView) {};
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {}

    @Override
    public int getItemCount() {
        return 1;
    }

}
