package com.ali.englishlearning.main.config;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ali.englishlearning.R;

import java.util.List;

public class AlgonAdapter extends RecyclerView.Adapter<AlgonViewHolder> {

    private List<Algon> algonList;
    private AlgonListener algonListener;

    public AlgonAdapter(List<Algon> algonList, AlgonListener listener) {
        this.algonList = algonList;
        this.algonListener = listener;
    }

    @NonNull
    @Override
    public AlgonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_icons, parent, false);
        return new AlgonViewHolder(view, algonListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AlgonViewHolder holder, int position) {
        holder.bind(algonList.get(position));
    }

    @Override
    public int getItemCount() {
        return algonList.size();
    }
}
