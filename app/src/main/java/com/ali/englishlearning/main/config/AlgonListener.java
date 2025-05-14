package com.ali.englishlearning.main.config;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ali.englishlearning.R;


class AlgonViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ImageView iconImageView;
    private TextView algoTextView;
    private AlgonListener algonListener;
    private Algon algon;

    public AlgonViewHolder(@NonNull View itemView, AlgonListener algonListener) {
        super(itemView);
        itemView.setOnClickListener(this);
        this.algonListener = algonListener;

        iconImageView = itemView.findViewById(R.id.iconImageView);
        algoTextView = itemView.findViewById(R.id.algoTextView);
    }

    public void bind(Algon algon) {
        this.algon = algon;
        iconImageView.setImageResource(algon.algoImage);
        algoTextView.setText(algon.algoText);
    }

    @Override
    public void onClick(View v) {
        if (algonListener != null) {
            algonListener.onAlgoSelected(algon);
        }
    }
}

public interface AlgonListener {
    void onAlgoSelected(Algon algon);
}

