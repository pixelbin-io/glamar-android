package com.fynd.ficto.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fynd.ficto.MainActivity;
import com.fynd.ficto.R;
import com.fynd.ficto.helper.DataHolder;

import java.util.List;

public class colorAdapter extends RecyclerView.Adapter<ViewHolder>{

    private RecyclerView parentRecycler;
    private List<DataHolder> data;
    public LinearLayoutManager llm;
    MainActivity mact;
    SharedPreferences sharedpreferences ;
    String MyPREFERENCES="DemoArApp";
    String ScrollKey="ScrollKey";
    SharedPreferences.Editor editor;
    GradientDrawable bgShape;
    int mSelectedPos = -1;


    public colorAdapter(List<DataHolder> data, MainActivity mainActivity) {
        this.data = data;
        this.mact=mainActivity;
        sharedpreferences= mainActivity.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.color_card, parent, false);
        ImageView selection =(ImageView) v.findViewById(R.id.view_divider);
        return new ViewHolder(v,3,selection,parentRecycler);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        parentRecycler = recyclerView;
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        DataHolder forecast = data.get(position);

        bgShape = (GradientDrawable)holder.imageView.getBackground();
        bgShape.setColor(Color.parseColor(forecast.getColorName()));
        if (mact.currentColorMode == position)
            holder.imageselectView.setVisibility(View.VISIBLE);
        else
            holder.imageselectView.setVisibility(View.INVISIBLE);

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                mSelectedPos = position;
                mact.currentColorMode=position;
                parentRecycler.smoothScrollToPosition(position);
                notifyDataSetChanged();

            }
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
