package com.fynd.ficto.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fynd.ficto.MainActivity;
import com.fynd.ficto.R;
import com.fynd.ficto.helper.DataHolder;

import java.util.List;

public class beautyMode extends RecyclerView.Adapter<ViewHolder>{

    private RecyclerView parentRecycler;
    private List<DataHolder> data;
    public LinearLayoutManager llm;
    MainActivity mact;
    SharedPreferences sharedpreferences ;
    String MyPREFERENCES="DemoArApp";
    String ScrollKey="ScrollKey";
    SharedPreferences.Editor editor;

    public beautyMode(List<DataHolder> data, MainActivity mainActivity) {
        this.data = data;
        this.mact=mainActivity;
        sharedpreferences= mainActivity.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.beauty_mode_item, parent, false);
        return new ViewHolder(v,2,parentRecycler);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        parentRecycler = recyclerView;
    }
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {


        DataHolder forecast = data.get(position);
        holder.modeName.setText(forecast.getCityName());

        final int pos=position;
        holder.modeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("critemasas"," click "+pos);
                mact.currentBeautyMode=pos;
//                holder.modeName.setTextColor(mact.getResources().getColor(R.color.white));
                parentRecycler.smoothScrollToPosition(pos);
                notifyDataSetChanged();



            }
        });
        if(mact.currentBeautyMode==position){
            holder.modeName.setTextColor(mact.getResources().getColor(R.color.white));
            holder.lContainer.setBackground(mact.getResources().getDrawable(R.drawable.rounded_corner_text));
        }else{
            holder.modeName.setTextColor(mact.getResources().getColor(R.color.textcolor2));
            holder.lContainer.setBackground(mact.getResources().getDrawable(R.drawable.rounded_corner_notselected_text));
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

//    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
//
//        private TextView modeName;
//
//
//        public ViewHolder(View itemView) {
//            super(itemView);
//            modeName = (TextView) itemView.findViewById(R.id.mode_name);
//
//
//            itemView.findViewById(R.id.container).setOnClickListener(this);
//
//        }
//
//
//
////        public void hideText() {
//////            imageView.setColorFilter(ContextCompat.getColor(imageView.getContext(), R.color.grayIconTint));
////            //textView.setVisibility(View.INVISIBLE);
////            imageView.animate().scaleX(1f).scaleY(1f)
////                    .setDuration(200)
////                    .start();
////        }
//
//        @Override
//        public void onClick(View v) {
//
//            parentRecycler.smoothScrollToPosition(getAdapterPosition());
//        }
//
//
//    }

}
