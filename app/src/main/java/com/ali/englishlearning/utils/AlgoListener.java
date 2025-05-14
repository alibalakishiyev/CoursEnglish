package com.ali.englishlearning.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ali.englishlearning.R;

import java.util.List;


class AlgoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ImageView iconImageView;
    private TextView algoTextView;
    private AlgoListener algoListener;
    private Algo algo;

    public AlgoViewHolder(@NonNull View itemView, AlgoListener algoListener) {
        super(itemView);
        itemView.setOnClickListener(this);
        this.algoListener = algoListener;

        iconImageView = itemView.findViewById(R.id.iconImageView);
        algoTextView = itemView.findViewById(R.id.algoTextView);
    }

    public void bind(Algo algo) {
        this.algo = algo;
        iconImageView.setImageResource(algo.algoImage);
        algoTextView.setText(algo.algoText);
    }

    @Override
    public void onClick(View v) {
        if (algoListener != null) {
            algoListener.onAlgoSelected(algo);
        }
    }
}

public interface AlgoListener {
    void onAlgoSelected(Algo algo);
}

