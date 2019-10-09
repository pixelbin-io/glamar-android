package com.fynd.ficto;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fynd.artoolkit.java.facedetection.FaceContourDetectorProcessor;


public class shade_adapter extends RecyclerView.Adapter<shade_adapter.PhotoFilterViewHolder>  {
    public View view;
    ImageView filterImage;
    GradientDrawable bgShape;
    FaceContourDetectorProcessor faceContourDetectorProcessor;
    String[] LipsticShadeColor = {"#8B0000", "#800000", "#FF6347", "#0000A0", "#000000","#f88379","#FFE5B4","#FFA500","#7a3d3a","#D2691E"};
    int FilterCount=LipsticShadeColor.length;
    private Context context;

    public shade_adapter(Context context, FaceContourDetectorProcessor faceContourDetectorProcessor) {
        this.faceContourDetectorProcessor=faceContourDetectorProcessor;
        this.context = context;


    }
    @NonNull
    @Override
    public PhotoFilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(context).inflate(R.layout.item_shade, parent, false);
        filterImage = view.findViewById(R.id.filters);

        return new shade_adapter.PhotoFilterViewHolder(view, filterImage);

    }

    int mSelectedPos = -1;

    public void setSelectedPos(int pos){
        mSelectedPos =pos;
        notifyDataSetChanged();
    }
    @Override
    public void onBindViewHolder(@NonNull final PhotoFilterViewHolder holder, final int position) {
        //holder.filterImage.setBackgroundColor(Color.parseColor(LipsticShadeColor[position]));
        bgShape = (GradientDrawable)holder.filterImage.getBackground();
        bgShape.setColor(Color.parseColor(LipsticShadeColor[position]));
        //((GradientDrawable)holder.filterImage.getBackground()).setColor(Color.parseColor(LipsticShadeColor[position]));
        //holder.filterImage.setImageResource(R.drawable.ic_filterapply);

        if (mSelectedPos == position)
            holder.divider.setVisibility(View.VISIBLE);
        else
            holder.divider.setVisibility(View.INVISIBLE);
        holder.filterImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                mSelectedPos = holder.getAdapterPosition();
                faceContourDetectorProcessor.setSelectedColor(LipsticShadeColor[mSelectedPos]);
                notifyDataSetChanged();

            }
        });

    }

    @Override
    public int getItemCount() {
        return  FilterCount;
    }

    public static class PhotoFilterViewHolder extends RecyclerView.ViewHolder {
        public ImageView filterImage;
        public ImageView divider;

        public PhotoFilterViewHolder(View view, ImageView filterImage) {
            super(view);
            this.filterImage = filterImage;
            this.divider = view.findViewById(R.id.view_divider);
        }


    }
}
