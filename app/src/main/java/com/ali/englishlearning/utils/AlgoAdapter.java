package com.ali.englishlearning.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ali.englishlearning.R;

import java.util.List;

public class AlgoAdapter extends RecyclerView.Adapter<AlgoViewHolder> {

    private List<Algo> algoList;
    private AlgoListener algoListener;

    public AlgoAdapter(List<Algo> algoList, AlgoListener listener) {
        this.algoList = algoList;
        this.algoListener = listener;
    }

    @NonNull
    @Override
    public AlgoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_icons, parent, false);
        return new AlgoViewHolder(view, algoListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AlgoViewHolder holder, int position) {
        holder.bind(algoList.get(position));
    }

    @Override
    public int getItemCount() {
        return algoList.size();
    }
}
