package com.fynd.ficto;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fynd.artoolkit.common.CameraSource;
import com.fynd.artoolkit.common.CameraSourcePreview;
import com.fynd.artoolkit.common.Constants;
import com.fynd.artoolkit.common.GraphicOverlay;
import com.fynd.artoolkit.java.facedetection.FaceContourDetectorProcessor;
import com.fynd.artoolkit.java.facedetection.FaceDetectionProcessor;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import org.opencv.android.OpenCVLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback,
        AdapterView.OnItemSelectedListener,
        CompoundButton.OnCheckedChangeListener, View.OnClickListener{

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
    private CameraSource cameraSource = null;
    private CameraSourcePreview preview;
    private GraphicOverlay graphicOverlay;
    private String selectedModel = Constants.FACE_CONTOUR;
    Spinner makeupSelector;
    Spinner spinner;
    ArrayAdapter<String> dataAdapter2;
    ImageView facingSwitch;

    Button colorSelector;
    private FirebaseVisionFaceDetector detector;
    FaceContourDetectorProcessor faceContourDetectorProcessor ;
    IndicatorSeekBar seekBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        faceContourDetectorProcessor = new FaceContourDetectorProcessor();
        setContentView(R.layout.main_preview);
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
}

