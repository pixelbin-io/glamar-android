package com.fynd.ficto.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.fynd.ficto.MainActivity;
import com.fynd.ficto.R;
import com.fynd.ficto.helper.DataHolder;

import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by yarolegovich on 08.03.2017.
 */

public class ForecastAdapter extends RecyclerView.Adapter<ViewHolder> {

    private RecyclerView parentRecycler;
    private List<DataHolder> data;
    public LinearLayoutManager llm;
    MainActivity mact;
    public int displayedfirstposition;
    public int displayedlastposition;
    SharedPreferences sharedpreferences ;
    String MyPREFERENCES="DemoArApp";
    String ScrollKey="ScrollKey";
    SharedPreferences.Editor editor;
    boolean isvideoButtonLongPressed;

    public ForecastAdapter(List<DataHolder> data, MainActivity mainActivity) {
        this.data = data;
        this.mact=mainActivity;
        sharedpreferences= mainActivity.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        parentRecycler = recyclerView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_h, parent, false);

        return new ViewHolder(v,1,parentRecycler);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {


        DataHolder forecast = data.get(position);
        Glide.with(holder.itemView.getContext())
                .load(forecast.getCityIcon())
                .bitmapTransform(new CropCircleTransformation(holder.itemView.getContext()))

                .into(holder.imageView);
//        Glide.with(holder.itemView.getContext())
//                .load(forecast.getCityIcon())
//                .listener(new TintOnLoad(holder.imageView, iconTint))
//                .into(holder.imageView);
//        holder.textView.setText(forecast.getCityName());
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(position==mact.currentItem){


                    mact.faceContourDetectorProcessor.click=true;
                    //mact.mclickflash.setVisibility(View.GONE);
                    mact.startTakePhotoAnimation();
                    mact.setorientation();
                    Log.d("criteasms","click");

                    //mact.updateGalleryIcon();
                    if(!mact.faceContourDetectorProcessor.click){

                        mact.setimage(false);
                    }
                    else{
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                //Do something after 100ms
                                mact.setimage(false);
                            }
                        }, 300);

                    }



                }
                if(!mact.faceContourDetectorProcessor.smallVideo){
                    parentRecycler.smoothScrollToPosition(position);
                }


//                if()

            }
        });
        holder.imageView.setOnTouchListener(speakTouchListener);

        holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(position==mact.currentItem){
                    editor.putBoolean(ScrollKey, false);
                    editor.commit();
                    mact.disableClick();
                    Log.d("critems","long");
                    //mact.videoButton.setVisibility(View.VISIBLE);

//                    mact.cityPicker.setOverScrollEnabled(true);
                    //mact.cityPicker.setdisablescroll(false);
                    isvideoButtonLongPressed = true;
                    mact.videoButton.setVisibility(View.VISIBLE);
                    mact.faceContourDetectorProcessor.smallVideo=true;

                    mact.videoButton.onLongPressStart();


                    //mact.cityPicker.setVisibility(View.GONE);
                    return true;
                }
                return false;

            }
        });
    }

    private View.OnTouchListener speakTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View pView, MotionEvent pEvent) {
            pView.onTouchEvent(pEvent);
            // We're only interested in when the button is released.
            if (pEvent.getAction() == MotionEvent.ACTION_UP) {
                // We're only interested in anything if our speak button is currently pressed.
                if (isvideoButtonLongPressed) {
                    // Do something when the button is released.
                    mact.videoButton.onLongPressEnd();
                    editor.putBoolean(ScrollKey, true);
                    editor.commit();
                    isvideoButtonLongPressed = false;
                    mact.videoButton.setVisibility(View.GONE);

                }
            }
            return false;
        }
    };

    @Override
    public int getItemCount() {
        return data.size();
    }


//    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
//
//        private ImageView imageView;
//
//
//        public ViewHolder(View itemView) {
//            super(itemView);
//            imageView = (ImageView) itemView.findViewById(R.id.city_image);
//
//
//            itemView.findViewById(R.id.container).setOnClickListener(this);
//
//        }
//
////        public void showText() {
////            int parentHeight = ((View) imageView.getParent()).getHeight();
////            float scale = (parentHeight - textView.getHeight()) / (float) imageView.getHeight();
////            imageView.setPivotX(imageView.getWidth() * 0.5f);
////            imageView.setPivotY(0);
////            imageView.animate().scaleX(scale)
////                    .withEndAction(new Runnable() {
////                        @Override
////                        public void run() {
////                            textView.setVisibility(View.VISIBLE);
////                            imageView.setColorFilter(Color.BLACK);
////                        }
////                    })
////                    .scaleY(scale).setDuration(200)
////                    .start();
////        }
//
//        public void hideText() {
////            imageView.setColorFilter(ContextCompat.getColor(imageView.getContext(), R.color.grayIconTint));
//            //textView.setVisibility(View.INVISIBLE);
//            imageView.animate().scaleX(1f).scaleY(1f)
//                    .setDuration(200)
//                    .start();
//        }
//
//        @Override
//        public void onClick(View v) {
//
//            parentRecycler.smoothScrollToPosition(getAdapterPosition());
//        }
//
//
//    }

    private static class TintOnLoad implements RequestListener<Integer, GlideDrawable> {

        private ImageView imageView;
        private int tintColor;

        public TintOnLoad(ImageView view, int tintColor) {
            this.imageView = view;
            this.tintColor = tintColor;
        }

        @Override
        public boolean onException(Exception e, Integer model, Target<GlideDrawable> target, boolean isFirstResource) {
            return false;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, Integer model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
//            imageView.setColorFilter(tintColor);
            return false;
        }
    }
}
