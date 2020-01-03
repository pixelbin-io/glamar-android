package com.fynd.ficto.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fynd.ficto.R;

public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    protected ImageView imageView;
    protected ImageView imageselectView;
    protected TextView modeName;
    protected RecyclerView parentRecycler;
    public int viewMode;
    protected LinearLayout lContainer;

    public ViewHolder(@NonNull View itemView, int viewMode, RecyclerView parentRecycler) {
        super(itemView);
        this.parentRecycler=parentRecycler;
        this.viewMode=viewMode;
        imageView = (ImageView) itemView.findViewById(R.id.city_image);
        modeName = (TextView) itemView.findViewById(R.id.mode_name);
        lContainer=(LinearLayout) itemView.findViewById(R.id.container) ;

        lContainer.setOnClickListener(this);

    }

    public ViewHolder(View itemView, int viewMode, ImageView selection, RecyclerView parentRecycler) {
        super(itemView);
        this.parentRecycler=parentRecycler;
        this.viewMode=viewMode;
        imageselectView=(ImageView) itemView.findViewById(R.id.view_divider);
        imageView = (ImageView) itemView.findViewById(R.id.city_image);
        modeName = (TextView) itemView.findViewById(R.id.mode_name);
        lContainer=(LinearLayout) itemView.findViewById(R.id.container) ;

        lContainer.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        parentRecycler.smoothScrollToPosition(getAdapterPosition());
    }
}
