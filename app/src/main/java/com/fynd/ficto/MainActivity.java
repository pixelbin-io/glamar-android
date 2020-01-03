package com.fynd.ficto;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.fynd.artoolkit.common.CameraSource;
import com.fynd.artoolkit.common.CameraSourcePreview;
import com.fynd.artoolkit.common.Constants;
import com.fynd.artoolkit.common.GraphicOverlay;
import com.fynd.artoolkit.java.facedetection.FaceContourDetectorProcessor;
import com.fynd.artoolkit.java.facedetection.FaceDetectionProcessor;
import com.fynd.ficto.adapter.ForecastAdapter;
import com.fynd.ficto.adapter.ViewHolder;
import com.fynd.ficto.adapter.beautyMode;
import com.fynd.ficto.adapter.colorAdapter;
import com.fynd.ficto.customRecycleView.DiscreteScrollView;
import com.fynd.ficto.customRecycleView.DiscreteScrollViewOptions;
import com.fynd.ficto.helper.CameraVideoButton;
import com.fynd.ficto.helper.Data;
import com.fynd.ficto.helper.DataHolder;
import com.fynd.ficto.transform.ScaleTransformer;
import com.fynd.ficto.util.App;
import com.fynd.ficto.util.StorageUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback,
        AdapterView.OnItemSelectedListener,
        CompoundButton.OnCheckedChangeListener, DiscreteScrollView.ScrollStateChangeListener<ViewHolder>,

        DiscreteScrollView.OnItemChangedListener<ViewHolder> ,View.OnClickListener{

    public ToggleButton mMode;
    public ToggleButton mLipstick;
    public ToggleButton mBlush;
    public ToggleButton mEyeshadow;
    public ToggleButton mEyeliner;
    public LinearLayout mModeContainer;
    public LinearLayout mShadeContainer;
    public RecyclerView rvShade;
    public shade_adapter shadeAdapter;
    public ImageView mShadeColor;

    private RecyclerView.LayoutManager layoutManager;
    boolean mode_frount=true;

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUESTS = 1;
    private transient CameraSource cameraSource = null;
    private transient CameraSourcePreview preview;
    private transient GraphicOverlay graphicOverlay;
    private String selectedModel = Constants.FACE_CONTOUR;
    Spinner makeupSelector;
    Spinner spinner;
    ArrayAdapter<String> dataAdapter2;
    ImageView facingSwitch;

    Button colorSelector;
    private FirebaseVisionFaceDetector detector;
    public FaceContourDetectorProcessor faceContourDetectorProcessor ;
    IndicatorSeekBar seekBar;

    public int currentItem=0;
    public int currentBeautyMode=0;
    public int currentColorMode=0;
    public CameraVideoButton videoButton;


    LinearLayout mCameraHeadderContainer;
    LinearLayout mCrossIconContainer;
    LinearLayout mResetIconContainer;
    LinearLayout mHomeIconContainer;
    LinearLayout mEmailIconContainer;
    FrameLayout mCameraFooterContainer;
    FrameLayout mVideoClickContainer;
    FrameLayout mCircularRecyclerFilterContainer;
    FrameLayout mCircularRecyclerShadeContainer;
    ToggleButton mHomeIcon;
    ToggleButton emailButton;
    ImageButton mCameraTryIcon;
    CardView mFaceCard;
    RelativeLayout mContactUsContainer;
    EditText mUserName;
    EditText mUserEmail;
    EditText mUserQuery;
    TextView mSendEmail;
    LinearLayout mSwitchCameraContainer;
    ToggleButton mSwitchCamera;
    CardView contactUsHeader;



    FloatingActionButton mCamera;
    FrameLayout mEmailHomeContainer;
    ScrollView mContentContainer;
    protected DiscreteScrollView cityPicker;
    protected DiscreteScrollView modePicker;
    protected DiscreteScrollView colorPicker;
    private List<DataHolder> forecasts;
    private List<DataHolder> mode;
    private List<DataHolder> color;
    beautyMode tabModeAdapter;
    public View mclickflash;
    ImageView galleryIcon;
    private StorageUtils storageUtils = null;
    public int rotation;
    RelativeLayout contactUsresponse,prograssbarcontainer,walkthrougContainer,walkthroughtSecondImageContainer;
    TextView goHome;
    private ProgressBar progressBar;
    TextView walkthroughtSkip,walkthroughtHeading,walkthroughtText,walkthroughtNext;
    FrameLayout filterFirstWalkthrough,dotImageWalktrough;
    LinearLayout walkthroughtBlankLL;

    SharedPreferences sharedpreferences ;
    String MyPREFERENCES="DemoArApp";
    SharedPreferences.Editor editor;
    private int walhkthroughCount=1;


    colorAdapter mColorAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        faceContourDetectorProcessor = new FaceContourDetectorProcessor(getApplicationContext());
        setContentView(R.layout.main_preview);

        mCamera= findViewById(R.id.base_camera);
        walkthroughtSkip=findViewById(R.id.skip_walkthrough);
        walkthroughtHeading=findViewById(R.id.walkthrought_heading);
        walkthroughtText=findViewById(R.id.walthrought_text);
        walkthroughtNext=findViewById(R.id.walkthought_next);
        walkthroughtSecondImageContainer=findViewById(R.id.walkthroughtSecondImageContainer);
        walkthrougContainer=findViewById(R.id.walkthroughtContainer);
        walkthroughtBlankLL=findViewById(R.id.walkthroughtBlankLL);
        filterFirstWalkthrough=findViewById(R.id.filterFirstWalkthrough);
        dotImageWalktrough=findViewById(R.id.dotImageWalktrough);
        mEmailHomeContainer=findViewById(R.id.base_content_footer);
        mContentContainer=findViewById(R.id.content_home);
        this.videoButton=findViewById(R.id.videobutton);
        contactUsresponse=findViewById(R.id.contactUsresponse);
        goHome=findViewById(R.id.goHome);
        progressBar=findViewById(R.id.progressBar_cyclic);
        prograssbarcontainer=findViewById(R.id.prograssbarcontainer);

        mCameraHeadderContainer=findViewById(R.id.llCameraHedderContainer);
        mCrossIconContainer=findViewById(R.id.llcross);
        mResetIconContainer=findViewById(R.id.llreset);
        mCameraFooterContainer=findViewById(R.id.camera_content_footer);
        mVideoClickContainer=findViewById(R.id.video_container);
        mCircularRecyclerFilterContainer=findViewById(R.id.filter_recycle_container);
        mCircularRecyclerShadeContainer=findViewById(R.id.color_recycle_container);
        mHomeIconContainer=findViewById(R.id.lc_home_icon);
        mEmailIconContainer=findViewById(R.id.ic_email_icon);
        mHomeIcon=findViewById(R.id.home_tb);
        mCameraTryIcon=findViewById(R.id.ic_try_icon);
        mFaceCard=findViewById(R.id.type2_card_view);
        mContactUsContainer=findViewById(R.id.contactUsContainer);
        emailButton=findViewById(R.id.email_tb);

        mUserName=findViewById(R.id.user_name);
        mUserEmail=findViewById(R.id.user_email);
        mUserQuery=findViewById(R.id.user_query);
        mSendEmail=findViewById(R.id.sendEmail);
        mSwitchCameraContainer=findViewById(R.id.mswitchCameraContainer);
        mSwitchCamera=findViewById(R.id.switchCamera);
        contactUsHeader=findViewById(R.id.contact_us_header);
        mclickflash=findViewById(R.id.frame_anim_layer);
        galleryIcon=findViewById(R.id.galleryIcon);

        storageUtils = new StorageUtils(this);
        sharedpreferences= getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            Window w = getWindow(); // in Activity's onCreate() for instance
//            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//        }


        mHomeIcon.setChecked(true);
        mHomeIcon.setEnabled(false);

        mContentContainer.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                return true;
            }
        });

        walkthroughtNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(walhkthroughCount==1){
                    walhkthroughCount=walhkthroughCount+1;
                    walkthroughtHeading.setText(R.string.walkthrough_Long);
                    walkthroughtText.setText(R.string.walkthrough_Long_text);
                }
                else if(walhkthroughCount==2){
                    colorPicker.setVisibility(View.INVISIBLE);
                    cityPicker.scrollToPosition(1);
                    currentItem=1;
                    faceContourDetectorProcessor.setSelectedMakeupIndex(0);

                    walhkthroughCount=walhkthroughCount+1;
//                    View row = modePicker.getLayoutManager().findViewByPosition(0);
//                    int test1[] = new int[2];
//                    row.getLocationInWindow(test1);
//                    Log.d("UIPOInt"," "+test1[0]+" "+test1[1]);
                    walkthroughtSecondImageContainer.setVisibility(View.GONE);
                    dotImageWalktrough.setVisibility(View.VISIBLE);
                    filterFirstWalkthrough.setVisibility(View.VISIBLE);
//                    LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
//                            0,
//                            LinearLayout.LayoutParams.MATCH_PARENT,
//                            1.0f
//                    );
//                    walkthroughtBlankLL.setLayoutParams(param);
                    walkthroughtHeading.setText(R.string.walkthrough_Scroll);
                    walkthroughtText.setText(R.string.walkthrough_Scroll_text);
                    walkthroughtNext.setText(R.string.finish);


                }
                else if(walhkthroughCount==3){
                    colorPicker.setVisibility(View.VISIBLE);
                    walkthrougContainer.setClickable(false);
                    walkthrougContainer.setVisibility(View.GONE);
                    walhkthroughCount=1;


                }
                editor.putBoolean("WalkThrough", false);
                editor.commit();
            }
        });
        walkthroughtSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                walkthrougContainer.setClickable(false);
                walkthrougContainer.setVisibility(View.GONE);

                editor.putBoolean("WalkThrough", false);
                editor.commit();

            }
        });
        galleryIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                StorageUtils.Media media = storageUtils.getLatestMedia();
                Uri pathUri=media.uri;
                Uri uri=null;

                String contentUri=sharedpreferences.getString("lastImageUri","");
                ContentResolver cr = getContentResolver();
                String[] projection = {MediaStore.MediaColumns.DATA};
                Cursor cur = cr.query(Uri.parse(contentUri), projection, null, null, null);
                if (cur != null) {
                    if (cur.moveToFirst()) {
                        String filePath = cur.getString(0);

                        if (new File(filePath).exists()) {
                            // do something if it exists
                            uri=Uri.parse(contentUri);
                        } else {
                            uri=pathUri;
                        }
                    } else {
                        // Uri was ok but no entry found.
                        uri=pathUri;
                    }
                    cur.close();
                } else {
                    // content Uri was invalid or some other error occurred
                    uri=pathUri;
                }

                //uri=Uri.parse(contentUri);
                Log.d("diiff"," uri "+uri+" str "+uri);


                final String REVIEW_ACTION = "com.android.camera.action.REVIEW";
                try {
                    // REVIEW_ACTION means we can view video files without autoplaying
                    Intent intent = new Intent(REVIEW_ACTION, uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplication().startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Log.d(TAG, "REVIEW_ACTION intent didn't work, try ACTION_VIEW");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    // from http://stackoverflow.com/questions/11073832/no-activity-found-to-handle-intent - needed to fix crash if no gallery app installed
                    //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("blah")); // test
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        getApplicationContext().startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(),"No gallery app available",Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });
        goHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactUsresponse.setVisibility(View.GONE);

                contactUsHeader.setVisibility(View.INVISIBLE);
                mContactUsContainer.animate()
                        .translationY(mContactUsContainer.getHeight())
                        .alpha(0.0f)
                        .setDuration(200)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                mContactUsContainer.setVisibility(View.GONE);
                            }
                        });
                mHomeIcon.setChecked(true);
                emailButton.setChecked(false);
                mCameraTryIcon.setClickable(true);
            }
        });
        mSendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                String email=mUserEmail.getText().toString();

                String username=mUserName.getText().toString();


                String message=mUserQuery.getText().toString();


                if (username.matches("")) {
                    Toast.makeText(getApplicationContext(), "Please Enter Email id", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (email.matches("")) {
                    Toast.makeText(getApplicationContext(), "Please Enter Your Name", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (message.matches("")) {
                    Toast.makeText(getApplicationContext(), "Please Enter Query", Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    if(isNetworkConnected()){
                        prograssbarcontainer.setVisibility(View.VISIBLE);
                        postEmail(email,username,message);
                    }else{
                        Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                    }

                }



//                String mailto = "mailto:bob@example.org" +
//                        "?cc=" + "alice@example.com" +
//                        "&subject=" + Uri.encode(subject) +
//                        "&body=" + Uri.encode(message);
//
//                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
//                emailIntent.setData(Uri.parse(mailto));




            }
        });

        mSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Set facing");
                if (cameraSource != null) {
                    if (mode_frount) {
                        mode_frount=false;
                        cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
                    } else {
                        mode_frount=true;
                        cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);

                    }
                }
                preview.stop();
                startCameraSource();
            }
        });
        mSwitchCameraContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Set facing");
                if (cameraSource != null) {
                    if (mode_frount) {
                        mode_frount=false;
                        cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
                    } else {
                        mode_frount=true;
                        cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);

                    }
                }
                preview.stop();
                startCameraSource();
            }
        });
        mHomeIconContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mContactUsContainer.setVisibility(View.GONE);
                contactUsHeader.setVisibility(View.INVISIBLE);
                mContactUsContainer.animate()
                        .translationY(mContactUsContainer.getHeight())
                        .alpha(0.0f)
                        .setDuration(200)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                mContactUsContainer.setVisibility(View.GONE);
                            }
                        });
                mHomeIcon.setChecked(true);
                emailButton.setChecked(false);
                mCameraTryIcon.setClickable(true);


            }
        });
        mHomeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mContactUsContainer.setVisibility(View.GONE);
                contactUsHeader.setVisibility(View.INVISIBLE);
                mContactUsContainer.animate()
                        .translationY(mContactUsContainer.getHeight())
                        .alpha(0.0f)
                        .setDuration(200)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                mContactUsContainer.setVisibility(View.GONE);
                            }
                        });
                mHomeIcon.setChecked(true);
                emailButton.setChecked(false);
                mCameraTryIcon.setClickable(true);
            }
        });

        mEmailIconContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mContactUsContainer.animate()
//                        .translationY(mContactUsContainer.getHeight())
//                        .alpha(0.0f)
//                        .setDuration(600);
                //mContactUsContainer.animate().translationY(0);
                contactUsHeader.setVisibility(View.VISIBLE);
                mContactUsContainer.setVisibility(View.VISIBLE);
                mContactUsContainer.animate()
                        .translationY(0)
                        .alpha(1.0f)
                        .setDuration(200)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                mContactUsContainer.setVisibility(View.VISIBLE);
                            }
                        });
                //mContactUsContainer.animate().alpha(1.0f);

                //mContactUsContainer.setVisibility(View.VISIBLE);
                mHomeIcon.setChecked(false);
                emailButton.setChecked(true);
                mCameraTryIcon.setClickable(false);


            }
        });
        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactUsHeader.setVisibility(View.VISIBLE);
                mContactUsContainer.setVisibility(View.VISIBLE);
                mContactUsContainer.animate()
                        .translationY(0)
                        .alpha(1.0f)
                        .setDuration(200)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                mContactUsContainer.setVisibility(View.VISIBLE);
                            }
                        });
                //mContactUsContainer.animate().translationY(0);
                //mContactUsContainer.animate().alpha(1.0f);
                mHomeIcon.setChecked(false);
                emailButton.setChecked(true);
            }
        });

        mCameraTryIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUICameraClickChange();
            }
        });
        mFaceCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUICameraClickChange();
            }
        });
        mCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUICameraClickChange();

            }
        });
        mCrossIconContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUICrossClickChange();
            }
        });

        colorPicker= (DiscreteScrollView) findViewById(R.id.color_picker);
        color=Data.get().getColor();
        colorPicker.setSlideOnFling(true);
        mColorAdapter=new colorAdapter(color,this);
        colorPicker.setAdapter(mColorAdapter);
        colorPicker.addOnItemChangedListener(this);
        colorPicker.addScrollStateChangeListener(this);
        colorPicker.scrollToPosition(0);
        colorPicker.setItemTransitionTimeMillis(DiscreteScrollViewOptions.getTransitionTime());
        colorPicker.setItemTransformer(new ScaleTransformer.Builder()
                .setMaxScale(1f)
                .setMinScale(1f)

                .build());


        modePicker= (DiscreteScrollView) findViewById(R.id.mode_picker);
        mode = Data.get().getMode();
        modePicker.setSlideOnFling(true);
//        list=new ArrayList<String>();//Creating arraylist
//        list.add("Hand");//Adding object in arraylist
//        list.add("Face");
//        list.add("Beauty");
//        list.add("Hair");
        tabModeAdapter=new beautyMode(mode,this);
        modePicker.setAdapter(tabModeAdapter);
        modePicker.addOnItemChangedListener(this);
        modePicker.addScrollStateChangeListener(this);
        modePicker.scrollToPosition(0);
        modePicker.setItemTransitionTimeMillis(DiscreteScrollViewOptions.getTransitionTime());
        modePicker.setItemTransformer(new ScaleTransformer.Builder()
                .setMaxScale(1f)
                .setMinScale(0.7f)

                .build());

        forecasts = Data.get().getForecasts();
        cityPicker = (DiscreteScrollView) findViewById(R.id.forecast_city_picker);
        cityPicker.setSlideOnFling(true);
        cityPicker.setAdapter(new ForecastAdapter(forecasts,this));
        cityPicker.addOnItemChangedListener(this);
        cityPicker.addScrollStateChangeListener(this);
        //cityPicker.scrollToPosition(2);
        cityPicker.setItemTransitionTimeMillis(DiscreteScrollViewOptions.getTransitionTime());
        cityPicker.setItemTransformer(new ScaleTransformer.Builder()
                .setMaxScale(1f)
                .setMinScale(0.7f)

                .build());
        cityPicker.setClampTransformProgressAfter(2);


        videoButton.setVideoDuration(7000);
        videoButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                videoButton.onLongPressStart();
                return false;
            }
        });

        videoButton.setActionListener(new CameraVideoButton.ActionListener() {
            @Override
            public void onStartRecord() {
                Log.v("TEST", "onstart");
                faceContourDetectorProcessor.smallVideostart=true;

            }

            @Override
            public void onEndRecord() {
                videoButton.setVisibility(View.GONE);
                Log.v("TEST", "onendrecord");
                faceContourDetectorProcessor.smallVideoEnd=true;
                //updateGalleryIcon();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Do something after 100ms
                        setimage(true);

                        enableClick();

                    }
                }, 2000);





                //videoButton.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onDurationTooShortError() {
                Log.v("TEST", "onshort");
                videoButton.setVisibility(View.GONE);

            }

            @Override
            public void onSingleTap() {
                Log.v("TEST", "onsingletap");
                //faceContourDetectorProcessor.click=true;

            }

            @Override
            public void onCancelled() {
                videoButton.setVisibility(View.GONE);
                Log.v("TEST", "onCancelled");
                // videoButton.setVisibility(View.INVISIBLE);
            }
        });



        preview = findViewById(R.id.firePreview);
        graphicOverlay = findViewById(R.id.fireFaceOverlay);
        seekBar = findViewById(R.id.seekBar2);
        mMode = findViewById(R.id.mode_icon);
        mLipstick = findViewById(R.id.lipstick_icon);
        mBlush = findViewById(R.id.blush_icon);
        mEyeshadow = findViewById(R.id.eyeshadow_icon);
        mEyeliner = findViewById(R.id.Eyeliner);
        mModeContainer = findViewById(R.id.Mode);
        mShadeContainer=findViewById(R.id.shade_mode);
        rvShade = findViewById(R.id.rvShade);
        mShadeColor=findViewById(R.id.shade_icon);
        facingSwitch = findViewById(R.id.facingSwitch);
        shadeAdapter = new shade_adapter(this,faceContourDetectorProcessor);
        rvShade.setHasFixedSize(true);
        rvShade.setAdapter(shadeAdapter);
        //layoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL);
        layoutManager =new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvShade.setLayoutManager(layoutManager);


//        ViewPager viewPager = findViewById(R.id.tab_viewpager);
        //viewPager.setAdapter(tabsPagerAdapter);


//        this.tabLayout = findViewById(R.id.tabLayout);
//
//        tabLayout.addTab(tabLayout.newTab().setText("Hand"));
//        tabLayout.addTab(tabLayout.newTab().setText("Face"));
//        tabLayout.addTab(tabLayout.newTab().setText("Beauty"));
//        tabLayout.addTab(tabLayout.newTab().setText("Hair"));
//
//        tabLayout.setupWithViewPager(viewPager);
//
//        TabsPagerAdapter tabsPagerAdapter = new TabsPagerAdapter(this, getSupportFragmentManager());
//
//        viewPager.setAdapter(tabsPagerAdapter);
//
//        tabLayout.setupWithViewPager(viewPager);
//
//        lastTab=tabLayout.getTabCount()-1;
//        for(int i=0; i < tabLayout.getTabCount(); i++) {
//            if(i==lastTab) {
//                View tab = ((ViewGroup) tabLayout.getChildAt(0)).getChildAt(i);
//                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) tab.getLayoutParams();
//                p.setMargins(0, 0, 196, 0);
//                tab.requestLayout();
//            }
//        }

        if (Camera.getNumberOfCameras() == 1) {
            facingSwitch.setVisibility(View.GONE);
        }
        if (preview == null) {
            Log.d(TAG, "Preview is null");
        }
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }
        OpenCVLoader.initDebug();
        if (allPermissionsGranted()) {
            createCameraSource(selectedModel);
        } else {
            getRuntimePermissions();
        }

        facingSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "Set facing");
                if (cameraSource != null) {
                    if (mode_frount) {
                        mode_frount=false;
                        cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
                    } else {
                        mode_frount=true;
                        cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);

                    }
                }
                preview.stop();
                startCameraSource();
            }
        });
        mShadeColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShadeContainer.setVisibility(View.VISIBLE);
                mMode.setChecked(false);
                mModeContainer.setVisibility(View.GONE);

            }
        });
        mMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mMode.isChecked()){
                    mModeContainer.setVisibility(View.VISIBLE);
                    mShadeContainer.setVisibility(View.GONE);
                }else{
                    mShadeContainer.setVisibility(View.VISIBLE);
                    mModeContainer.setVisibility(View.GONE);

                }
            }
        });
        mLipstick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mLipstick.isChecked()){
                    mBlush.setChecked(false);
                    mEyeshadow.setChecked(false);
                    mEyeliner.setChecked(false);
                    Log.d(TAG, "*************** Selected Makeup Index: " + 0);
                    faceContourDetectorProcessor.setSelectedMakeupIndex(0);
                }
                else{
                    mLipstick.setChecked(true);
                }

            }
        });
        mBlush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBlush.isChecked()){
                    mLipstick.setChecked(false);
                    mEyeshadow.setChecked(false);
                    mEyeliner.setChecked(false);
                    Log.d(TAG, "*************** Selected Makeup Index: " + 1);
                    faceContourDetectorProcessor.setSelectedMakeupIndex(1);

                }
                else{
                    mBlush.setChecked(true);
                }
            }
        });
        mEyeshadow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mEyeshadow.isChecked()){
                    mLipstick.setChecked(false);
                    mBlush.setChecked(false);
                    mEyeliner.setChecked(false);
                    Log.d(TAG, "*************** Selected Makeup Index: " + 3);
                    faceContourDetectorProcessor.setSelectedMakeupIndex(3);

                }
                else{
                    mEyeshadow.setChecked(true);
                }
            }
        });
        mEyeliner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mEyeliner.isChecked()){
                    mLipstick.setChecked(false);
                    mEyeshadow.setChecked(false);
                    mBlush.setChecked(false);
                    Log.d(TAG, "*************** Selected Makeup Index: " + 2);
                    faceContourDetectorProcessor.setSelectedMakeupIndex(2);

                }
                else{
                    mEyeliner.setChecked(true);
                }
            }
        });


        seekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {

                faceContourDetectorProcessor.setMakeupOpacitiy(seekParams.progress/100.0f);
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
            }
        });

        updateGalleryIcon();


    }
    public void setorientation(){

        Display display = ((WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        rotation = display.getRotation();
        editor.putInt("Orientation", rotation);
        editor.commit();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
    public void postEmail(String username,String email,String query){

        String url = "https://akruti.addsale.com/contact";

        JSONObject postparams = new JSONObject();
        try {
            postparams.put("username", username);
            postparams.put("email_id", email);
            postparams.put("query", query);
        } catch (JSONException e) {
            Log.d("responseEmail"," fail ");

            e.printStackTrace();
        }

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, postparams,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        String str_message = null;
                        Log.d("responseEmail"," "+response.toString());
                        JSONObject json = null;
                        try {
                            json = new JSONObject(response.toString());
                            str_message=json.getString("message");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if(str_message.equals("Contacted successfully")){
                            Log.d("responseEmail"," send successfully");
                            contactUsresponse.setVisibility(View.VISIBLE);
                            prograssbarcontainer.setVisibility(View.GONE);
                            contactUsHeader.setVisibility(View.GONE);
                            mContactUsContainer.setVisibility(View.GONE);

                            //mContactUsContainer.animate().translationY(0);
                            //mContactUsContainer.animate().alpha(1.0f);


                        }
                    }


                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("responseEmail"," failure "+error);
                        prograssbarcontainer.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(),"Server Error, Try again",Toast.LENGTH_SHORT).show();
                        //Failure Callback
                    }
                });
    // Adding the request to the queue along with a unique string tag
        App.getInstance().addToRequestQueue(jsonObjReq, "postRequest");
    }



    public void startTakePhotoAnimation() {
        AnimationSet animationSet = new AnimationSet(true);

        ScaleAnimation scalAnimation = new ScaleAnimation(0, 1f, 0, 1f, Animation.RELATIVE_TO_SELF, (float) 0.5, Animation.RELATIVE_TO_SELF, (float) 0.5);
        scalAnimation.setDuration(300);
        animationSet.addAnimation(scalAnimation);

        AlphaAnimation alphaAnimation = new AlphaAnimation(0.2f, 1.0f);
        alphaAnimation.setDuration(300);
        animationSet.addAnimation(alphaAnimation);

        mclickflash.startAnimation(animationSet);
        mclickflash.animate().setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mclickflash.setVisibility(View.GONE);
            }
        });
        mclickflash.setVisibility(View.VISIBLE);
        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mclickflash.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

//        ExifInterface old= null;
//        try {
//            Log.d("exf","sta");
//            old = new ExifInterface("/storage/emulated/0/DCIM/ARBeauty/ARBeauty-191217002546.jpg");
//            String type = null;
//            String extension = MimeTypeMap.getFileExtensionFromUrl("/storage/emulated/0/DCIM/ARBeauty/ARBeauty-191217002546.jpg");
//            if (extension != null) {
//                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
//            }
//            Log.d("exf",""+type);
//            Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//            String selection =MediaStore.Images.ImageColumns.MIME_TYPE + "='image/jpeg'";
//            String[] projection = new String[]{
//                    MediaStore.Images.Media._ID,
//                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
//                    MediaStore.Images.Media.DATE_TAKEN
//            };
//            Cursor cursor = null;
//            try {
//                //cursor = context.getContentResolver().query(query, projection, selection, null, order);
//                cursor = getApplicationContext().getContentResolver().query(imageUri, null, selection, null, null);
//                if (cursor != null && cursor.moveToFirst()) {
//
//                    do{
//                        String path = cursor.getString(1);
//                        String one=cursor.getString(0);
//                        cursor.getColumnCount();
//                        Log.d("allfile",""+path+" "+one);
//                    }while (cursor.moveToNext());
//                }
//            }
//            catch (SQLiteException e){
//                Log.d("allfile","exception");
//            }
//            ;
//
////            old.setAttribute(MediaStore.Images.ImageColumns.MIME_TYPE,"image/jpeg");
////            old.setAttribute(MediaStore.Images.ImageColumns.DATE_TAKEN, Long.toString(System.currentTimeMillis()));
////            old.setAttribute(MediaStore.Images.ImageColumns.DATA, "/storage/emulated/0/DCIM/ARBeauty/ARBeauty-191217002546.jpg");
////            old.saveAttributes();
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        old= null;
//        try {
//            old = new ExifInterface("/storage/emulated/0/DCIM/ARBeauty/ARBeauty-191217002546.jpg");
//            old.getAttribute(MediaStore.Images.ImageColumns.MIME_TYPE);
//            old.getAttribute(MediaStore.Images.ImageColumns.DATE_TAKEN);
//            old.getAttribute(MediaStore.Images.ImageColumns.DATA);
//            Log.d("exf",""+old.getAttribute(MediaStore.Images.ImageColumns.MIME_TYPE));
//            Log.d("exf",""+old.getAttribute(MediaStore.Images.ImageColumns.DATE_TAKEN));
//            Log.d("exf",""+old.getAttribute(MediaStore.Images.ImageColumns.DATA));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }



    }


    public void updateGalleryIcon(final Bitmap thumbnail) {

        if (thumbnail != null) {


            Animation animation = new ScaleAnimation(0, 1f, 0, 1f, Animation.RELATIVE_TO_SELF, (float) 0.5, Animation.RELATIVE_TO_SELF, (float) 0.5);
            animation.setDuration(500);
            animation.setFillAfter(true);
            galleryIcon.startAnimation(animation);

            galleryIcon.setImageBitmap(thumbnail);


            // Log.clickedPositionThreadSafe("ishroid","thumb size wxh" +gallery_bitmap.getWidth() +" x "+gallery_bitmap.getHeight());
       /* galleryIcon.post(new Runnable() {
            @Override
            public void run() {
                applyAnim(galleryIcon);
            }
        });*/
        } else {
            updateGalleryIcon();
        }


    }


    public void disableClick(){
        colorPicker.setVisibility(View.GONE);
        mCrossIconContainer.setVisibility(View.GONE);
        galleryIcon.setClickable(false);
        mSwitchCamera.setClickable(false);
        mSwitchCameraContainer.setClickable(false);

    }
    public void enableClick(){
        colorPicker.setVisibility(View.VISIBLE);
        mCrossIconContainer.setVisibility(View.VISIBLE);
        galleryIcon.setClickable(true);
        mSwitchCamera.setClickable(true);
        mSwitchCameraContainer.setClickable(true);

    }


    public void setimage( boolean video){

        String  id=sharedpreferences.getString("lastImageUi","");

        Glide.with(getApplicationContext())
                .load(id)
                .into(galleryIcon);

        storageUtils.getVideoURI(storageUtils.getImageFolder(id), true, true, sharedpreferences);
        Log.d("diiff", " uri " + sharedpreferences.getString("lastImageUri", "no"));
        if(!video){
            callforUri();
        }
        else if(video){
            callforVideoUri();
        }

        //editor.putString("lastImageUri",storageUtils.getVideoURI(storageUtils.getImageFolder(id),true,true).toString());


           }
   public void callforUri(){
       final Handler handler2 = new Handler();
       handler2.postDelayed(new Runnable() {
           @Override
           public void run() {
               //Do something after 100ms
               Uri pastUri=storageUtils.getURI();
               Log.d("diiff"," inside uri "+pastUri.toString());
               editor.putString("lastImageUri",pastUri.toString());
               editor.apply();
               //sharedpreferences.getString("lastImageUri","not");
               Log.d("videoPath"," image call "+pastUri);
               Log.d("diiff"," inside uri sh  "+sharedpreferences.getString("lastImageUri","not"));


           }
       }, 500);


   }
    public void callforVideoUri(){
        final Handler handler2 = new Handler();
        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                Uri pastUri=storageUtils.getURI();
                Log.d("diiff"," inside uri "+pastUri.toString());
                editor.putString("lastImageUri",pastUri.toString());
                editor.apply();
                //sharedpreferences.getString("lastImageUri","not");
                Log.d("videoPath"," video call "+pastUri);
                Log.d("diiff"," inside uri sh  "+sharedpreferences.getString("lastImageUri","not"));


            }
        }, 1000);


    }
    public void updateGalleryIcon() {

        long time_s = System.currentTimeMillis();
        StorageUtils.Media media = storageUtils.getLatestMedia();
        Bitmap thumbnail = null;
        if (media != null && getContentResolver() != null) {
            // check for getContentResolver() != null, as have had reported Google Play crashes
            if (media.video) {
                thumbnail = MediaStore.Video.Thumbnails.getThumbnail(getContentResolver(), media.id, MediaStore.Video.Thumbnails.MINI_KIND, null);
            } else {
                thumbnail = MediaStore.Images.Thumbnails.getThumbnail(getContentResolver(), media.id, MediaStore.Images.Thumbnails.MINI_KIND, null);
            }
//            if (thumbnail != null) {
//                if (media.orientation != 0) {
//                    if (true)
//                        Log.d(TAG, "thumbnail size is " + thumbnail.getWidth() + " x " + thumbnail.getHeight());
//                    Matrix matrix = new Matrix();
//                    matrix.setRotate(media.orientation, thumbnail.getWidth() * 0.5f, thumbnail.getHeight() * 0.5f);
//                    try {
//                        Bitmap rotated_thumbnail = Bitmap.createBitmap(thumbnail, 0, 0, thumbnail.getWidth(), thumbnail.getHeight(), matrix, true);
//                        // careful, as rotated_thumbnail is sometimes not a copy!
//                        if (rotated_thumbnail != thumbnail) {
//                            thumbnail.recycle();
//                            thumbnail = rotated_thumbnail;
//                        }
//                    } catch (Throwable t) {
//                        if (true)
//                            Log.d(TAG, "failed to rotate thumbnail");
//                    }
//                }
//            }
        }

        // since we're now setting the thumbnail to the latest media on disk, we need to make sure clicking the Gallery goes to this
        storageUtils.clearLastMediaScanned();
        if (thumbnail != null) {
            if (true)
                Log.d(TAG, "set gallery button to thumbnail");
            updateGalleryIcon(thumbnail);
        } else {
            if (true)
                Log.d(TAG, "set gallery button to blank");
            //updateGalleryIconToBlank();
        }


    }


    public void setUICameraClickChange(){

        if(sharedpreferences.getBoolean("WalkThrough", true)){
            walkthrougContainer.setVisibility(View.VISIBLE);
        }else{
            walkthrougContainer.setVisibility(View.GONE);
        }

        mHomeIcon.setChecked(false);
        mCamera.setVisibility(View.GONE);
        mContentContainer.setVisibility(View.GONE);
        mEmailHomeContainer.setVisibility(View.GONE);
        mCameraHeadderContainer.setVisibility(View.VISIBLE);
        mCameraFooterContainer.setVisibility(View.VISIBLE);
        mVideoClickContainer.setVisibility(View.VISIBLE);
        mCircularRecyclerFilterContainer.setVisibility(View.VISIBLE);
        mCircularRecyclerShadeContainer.setVisibility(View.VISIBLE);
        mContactUsContainer.setVisibility(View.GONE);
    }
    public void setUICrossClickChange(){
        mHomeIcon.setChecked(true);

        mCameraHeadderContainer.setVisibility(View.GONE);
        mCameraFooterContainer.setVisibility(View.GONE);
        mVideoClickContainer.setVisibility(View.GONE);
        mCircularRecyclerFilterContainer.setVisibility(View.GONE);
        mCircularRecyclerShadeContainer.setVisibility(View.GONE);
        mContactUsContainer.setVisibility(View.GONE);
        emailButton.setChecked(false);
        mCamera.setVisibility(View.VISIBLE);
        mContentContainer.setVisibility(View.VISIBLE);
        mEmailHomeContainer.setVisibility(View.VISIBLE);
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }
    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }
    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }
    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "Permission granted!");
        if (allPermissionsGranted()) {
            createCameraSource(selectedModel);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

    private void createCameraSource(String model) {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, graphicOverlay);
        }

        try {
            switch (model) {
                case Constants.FACE_DETECTION:
                    Log.i(TAG, "Using Face Detector Processor");
                    cameraSource.setMachineLearningFrameProcessor(new FaceDetectionProcessor(getResources()));
                    break;
                case Constants.FACE_CONTOUR:
                    Log.i(TAG, "Using Face Contour Detector Processor");
                    cameraSource.setMachineLearningFrameProcessor(faceContourDetectorProcessor);
                    break;
                default:
                    Log.e(TAG, "Unknown model: " + model);
            }
        } catch (Exception e) {
            Log.e(TAG, "Can not create image processor: " + model, e);
            Toast.makeText(
                    getApplicationContext(),
                    "Can not create image processor: " + e.getMessage(),
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (preview == null) {
                    Log.d(TAG, "resume: Preview is null");
                }
                if (graphicOverlay == null) {
                    Log.d(TAG, "resume: graphOverlay is null");
                }
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }
    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }

    @Override
    public void onScrollStart(@NonNull ViewHolder currentItemHolder, int adapterPosition) {

    }

    @Override
    public void onScrollEnd(@NonNull ViewHolder currentItemHolder, int adapterPosition) {
        if(currentItemHolder.viewMode==2){

            tabModeAdapter.notifyDataSetChanged();
        }
        if(currentItemHolder.viewMode==3){

            mColorAdapter.notifyDataSetChanged();
        }


    }

    @Override
    public void onScroll(float scrollPosition, int currentPosition, int newPosition, @Nullable ViewHolder currentHolder, @Nullable ViewHolder newCurrent) {

    }

    @Override
    public void onCurrentItemChanged(@Nullable ViewHolder viewHolder, int adapterPosition) {

        if(viewHolder.viewMode==1){
            currentItem=adapterPosition;
            faceContourDetectorProcessor.setSelectedMakeupIndex(adapterPosition-1);
            Log.d("adapterpos",""+adapterPosition);
            if(adapterPosition==0){
                colorPicker.setVisibility(View.INVISIBLE);
            }
            else{
                colorPicker.setVisibility(View.VISIBLE);
            }
            View row = modePicker.getLayoutManager().findViewByPosition(0);
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) row.getLayoutParams();
            Rect rectf = new Rect();

//For coordinates location relative to the parent
            row.getLocalVisibleRect(rectf);

            int test1[] = new int[2];
            row.getLocationOnScreen(test1);
            Log.d("UIPOInt"," "+rectf.left+" "+rectf.right+" "+rectf.top+" "+rectf.bottom);



        }
        else if(viewHolder.viewMode==2){
            currentBeautyMode=adapterPosition;

        }
        else if(viewHolder.viewMode==3){
            currentColorMode=adapterPosition;

            faceContourDetectorProcessor.setSelectedColor(color.get(adapterPosition).getColorName());

        }
    }


    @Override
    public void onBackPressed() {

      if(emailButton.isChecked()){
          mHomeIcon.performClick();

      }
      else if(mHomeIcon.isChecked() & mCameraHeadderContainer.getVisibility()==View.GONE){
          this.finish();
      }
      else if(mCameraHeadderContainer.getVisibility()==View.VISIBLE){
          mCrossIconContainer.performClick();

      }
    }
}

