package com.fynd.artoolkit.java.facedetection;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.fynd.artoolkit.common.CameraImageGraphic;
import com.fynd.artoolkit.common.Constants;
import com.fynd.artoolkit.common.FrameMetadata;
import com.fynd.artoolkit.common.GraphicOverlay;
import com.fynd.artoolkit.java.VisionProcessorBase;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import org.jcodec.api.android.AndroidSequenceEncoder;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Rational;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Face Contour Demo.
 */
public class FaceContourDetectorProcessor extends VisionProcessorBase<List<FirebaseVisionFace>> {

    // neeraj
//    public native void ConvertRGBtoGray(long matAddrInput, long matAddrResult, float[] upperLipTopX);
    public native void AugmentFace(long matAddrInput, long matAddrResult,
                                        float[] faceX, float[] faceY,
                                        float[] leftEyebrowTopX, float[] leftEyebrowTopY,
                                        float[] leftEyebrowBottomX, float[] leftEyebrowBottomY,
                                        float[] rightEyebrowTopX, float[] rightEyebrowTopY,
                                        float[] rightEyebrowBottomX, float[] rightEyebrowBottomY,
                                        float[] leftEyeX, float[] leftEyeY, float[] rightEyeX, float[] rightEyeY,
                                        float[] noseBridgeX, float[] noseBridgeY, float[] noseBottomX, float[] noseBottomY,
                                        float[] upperLipTopX, float[] upperLipTopY,
                                        float[] upperLipBottomX, float[] upperLipBottomY,
                                        float[] lowerLipTopX, float[] lowerLipTopY,
                                        float[] lowerLipBottomX, float[] lowerLipBottomY,
                                        int selectedColorR, int selectedColorG, int selectedColorB,
                                        int selectedMakeupIndex, float colorAlpha);


    private static final String TAG = "FaceContourDetectorProc";

    private final FirebaseVisionFaceDetector detector;

    Context context;

    private String selectedColor = Constants.colorsHexList.get(0);
    private Integer selectedMakeupIndex = 0;
    private Float makeupOpacitiy = 0.2f;
    public boolean click=false;
    public boolean smallVideo=false;
    public boolean smallVideoEnd=false;
    public boolean smallVideostart=false;
    public int videoCreatedMode;
    FileChannelWrapper out = null;
    AndroidSequenceEncoder encoder;
    File sdCard;
    File file;
    SharedPreferences sharedpreferences ;
    String MyPREFERENCES="DemoArApp";
    SharedPreferences.Editor editor;
    transient Bitmap bmp;
    transient Bitmap bmp32;
    ConcurrentLinkedQueue<Bitmap> bitmapQueue = new ConcurrentLinkedQueue<Bitmap>();

    public File dir;
    // neeraj
    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

    public FaceContourDetectorProcessor(  Context context) {



        //FirebaseVision fVision=FirebaseVision.getInstance();
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                        .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                        .build();

        detector =FirebaseVision.getInstance().getVisionFaceDetector(options);


        this.context = context;
        sharedpreferences= context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
    }

    public void setSelectedColor(String selectedColor){
        this.selectedColor = selectedColor;
    }

    public void setSelectedMakeupIndex(int selectedMakeupIndex){
        this.selectedMakeupIndex = selectedMakeupIndex;
    }

    public void setMakeupOpacitiy(float makeupOpacitiy){
        this.makeupOpacitiy = makeupOpacitiy;
    }

    @Override
    public void stop() {
        try {
            detector.close();

        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Face Contour Detector: " + e);
        }
    }

    @Override
    protected Task<List<FirebaseVisionFace>> detectInImage(FirebaseVisionImage image) {
        return detector.detectInImage(image);
    }

    boolean getModifiedAlpha(int color) {
        return ColorUtils.calculateLuminance(color) < 0.3;
    }

    @Override
    protected void onSuccess(
            @Nullable  Bitmap originalCameraImage,
            @NonNull List<FirebaseVisionFace> faces,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        if (originalCameraImage != null) {
            // neeraj start
            Mat matInput = new Mat();
            bmp32 = originalCameraImage.copy(Bitmap.Config.ARGB_8888, true);
            Utils.bitmapToMat(bmp32, matInput);


            int w = bmp32.getWidth();
            int h = bmp32.getHeight();

            Mat matResult = new Mat(matInput.size(), matInput.type());

            if(faces.size() >0) {
                for (int i = 0; i < faces.size(); ++i) {
                    FirebaseVisionFace face = faces.get(i);
                    FaceContourGraphic faceGraphic = new FaceContourGraphic(graphicOverlay, face);
                    //Log.d("HELLLLO", "onSuccess: ppppp "+ faceGraphic.scaleX() + " ,  " + faceGraphic.scaleY() );


                    float[] faceX = faceGraphic.getContourCoordinateArray(face, FirebaseVisionFaceContour.FACE, 'x');
                    float[] faceY = faceGraphic.getContourCoordinateArray(face, FirebaseVisionFaceContour.FACE, 'y');

                    float[] leftEyebrowTopX = faceGraphic.getContourCoordinateArray(face, FirebaseVisionFaceContour.LEFT_EYEBROW_TOP, 'x');
                    float[] leftEyebrowTopY = faceGraphic.getContourCoordinateArray(face, FirebaseVisionFaceContour.LEFT_EYEBROW_TOP, 'y');

                    float[] leftEyebrowBottomX = faceGraphic.getContourCoordinateArray(face, FirebaseVisionFaceContour.LEFT_EYEBROW_BOTTOM, 'x');
                    float[] leftEyebrowBottomY = faceGraphic.getContourCoordinateArray(face, FirebaseVisionFaceContour.LEFT_EYEBROW_BOTTOM, 'y');

                    float[] rightEyebrowTopX = faceGraphic.getContourCoordinateArray(face, FirebaseVisionFaceContour.RIGHT_EYEBROW_TOP, 'x');
                    float[] rightEyebrowTopY = faceGraphic.getContourCoordinateArray(face, FirebaseVisionFaceContour.RIGHT_EYEBROW_TOP, 'y');

                    float[] rightEyebrowBottomX = faceGraphic.getContourCoordinateArray(face, FirebaseVisionFaceContour.RIGHT_EYEBROW_BOTTOM, 'x');
                    float[] rightEyebrowBottomY = faceGraphic.getContourCoordinateArray(face, FirebaseVisionFaceContour.RIGHT_EYEBROW_BOTTOM, 'y');

                    float[] leftEyeX = faceGraphic.getContourCoordinateArray(face, FirebaseVisionFaceContour.LEFT_EYE, 'x');
                    float[] leftEyeY = faceGraphic.getContourCoordinateArray(face, FirebaseVisionFaceContour.LEFT_EYE, 'y');

                    float[] rightEyeX = faceGraphic.getContourCoordinateArray(face, FirebaseVisionFaceContour.RIGHT_EYE, 'x');
                    float[] rightEyeY = faceGraphic.getContourCoordinateArray(face, FirebaseVisionFaceContour.RIGHT_EYE, 'y');

                    float[] noseBridgeX = faceGraphic.getContourCoordinateArray(face, FirebaseVisionFaceContour.NOSE_BRIDGE, 'x');
                    float[] noseBridgeY = faceGraphic.getContourCoordinateArray(face, FirebaseVisionFaceContour.NOSE_BRIDGE, 'y');

                    float[] noseBottomX = faceGraphic.getContourCoordinateArray(face, FirebaseVisionFaceContour.NOSE_BOTTOM, 'x');
                    float[] noseBottomY = faceGraphic.getContourCoordinateArray(face, FirebaseVisionFaceContour.NOSE_BOTTOM, 'y');

                    float[] upperLipTopX = faceGraphic.getContourCoordinateArray(face, FirebaseVisionFaceContour.UPPER_LIP_TOP, 'x');
                    float[] upperLipTopY = faceGraphic.getContourCoordinateArray(face, FirebaseVisionFaceContour.UPPER_LIP_TOP, 'y');

                    float[] upperLipBottomX = faceGraphic.getContourCoordinateArray(face, FirebaseVisionFaceContour.UPPER_LIP_BOTTOM, 'x');
                    float[] upperLipBottomY = faceGraphic.getContourCoordinateArray(face, FirebaseVisionFaceContour.UPPER_LIP_BOTTOM, 'y');

                    float[] lowerLipTopX = faceGraphic.getContourCoordinateArray(face, FirebaseVisionFaceContour.LOWER_LIP_TOP, 'x');
                    float[] lowerLipTopY = faceGraphic.getContourCoordinateArray(face, FirebaseVisionFaceContour.LOWER_LIP_TOP, 'y');

                    float[] lowerLipBottomX = faceGraphic.getContourCoordinateArray(face, FirebaseVisionFaceContour.LOWER_LIP_BOTTOM, 'x');
                    float[] lowerLipBottomY = faceGraphic.getContourCoordinateArray(face, FirebaseVisionFaceContour.LOWER_LIP_BOTTOM, 'y');

                    int  selectedColorR =  Integer.valueOf( selectedColor.substring( 1, 3 ), 16 );
                    int  selectedColorG =  Integer.valueOf( selectedColor.substring( 3, 5 ), 16 );
                    int  selectedColorB =  Integer.valueOf( selectedColor.substring( 5, 7 ), 16 );

                    Log.d(TAG, "onSuccess: R: " + selectedColorR + " " + selectedColorG + " " + selectedColorB + " ");

                    AugmentFace(matInput.getNativeObjAddr(), matResult.getNativeObjAddr(),
                            faceX, faceY, leftEyebrowTopX, leftEyebrowTopY,
                            leftEyebrowBottomX, leftEyebrowBottomY,
                            rightEyebrowTopX, rightEyebrowTopY,
                            rightEyebrowBottomX, rightEyebrowBottomY,
                            leftEyeX, leftEyeY, rightEyeX, rightEyeY,
                            noseBridgeX, noseBridgeY, noseBottomX, noseBottomY,
                            upperLipTopX, upperLipTopY, upperLipBottomX, upperLipBottomY,
                            lowerLipTopX, lowerLipTopY, lowerLipBottomX, lowerLipBottomY,
                            selectedColorR, selectedColorG, selectedColorB,
//                            selectedMakeupIndex, Constants.colorAlphaList.get(selectedMakeupIndex));
                    selectedMakeupIndex, makeupOpacitiy);

                }
            }

            if(faces.size()!=0) {
                bmp = Bitmap.createBitmap(matInput.cols(), matInput.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(matResult, bmp);
                originalCameraImage = bmp;
            }
            // neeraj end
            if(click) {

                File sdCard = Environment.getExternalStorageDirectory();
                dir = new File(sdCard.getAbsolutePath() + "/DCIM/ARBeauty");
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String timeStamp = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
                //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String ImageFile = "ARBeauty-" + timeStamp + ".jpg"; //".png";

                File file = new File(dir, ImageFile);
                //File file = new File( ImageFile);
                //String path= Environment.getExternalStorageState().toString()+"out.jpg";
                try (FileOutputStream out = new FileOutputStream(file)) {

                    long generatedLong = new Random().nextLong();
                    originalCameraImage.compress(Bitmap.CompressFormat.PNG, 100, out);
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, file.getPath());
                    values.put(MediaStore.Images.Media.ORIENTATION, sharedpreferences.getInt("Orientation",90));
                    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis()); // DATE HERE
                    //values.put(MediaStore.Images.Media._ID, generatedLong);
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.MediaColumns.DATA, file.getPath());

                    context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                    editor.putString("lastImageUi", file.getPath());
                    editor.apply();

//                    ExifInterface old=new ExifInterface(file.getPath());
//
//                    old.setAttribute(MediaStore.Images.ImageColumns.MIME_TYPE,"image/jpeg");
//                    old.setAttribute(MediaStore.Images.ImageColumns.DATE_TAKEN, Long.toString(System.currentTimeMillis()));
//                    old.setAttribute(MediaStore.Images.ImageColumns.DATA, file.getPath());
//                    old.saveAttributes();

                    click=false;
                    Log.d("criteasms","saved");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            if(smallVideo){
                Log.d("callme1as"," set first "+smallVideo);

                if(smallVideostart){
                    bitmapQueue = new ConcurrentLinkedQueue<Bitmap>();
                    smallVideostart=false;
                    bitmapQueue.add(originalCameraImage);
                    new FrametoVideo().execute(1);
                }
                else if(smallVideoEnd) {
                    smallVideoEnd=false;
                    smallVideo=false;
                    bitmapQueue.add(originalCameraImage);
                    new FrametoVideo().execute(3);
                }
                else{
                    bitmapQueue.add(originalCameraImage);
                    new FrametoVideo().execute(2);
                }


//                if(smallVideostart){
//                    smallVideostart=false;
//                    out = null;
//                    sdCard = Environment.getExternalStorageDirectory();
//                    dir = new File(sdCard.getAbsolutePath() +  "/DCIM/ARBeauty");
//                    if (!dir.exists()) {
//                        dir.mkdirs();
//                    }
//                    String timeStamp = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
//                    String ImageFile = "ARBeauty-" + timeStamp + ".mp4"; //".png";
//                    //File file = new File(dir, ImageFile);
//
//                    file = new File(dir, ImageFile);
//
//                    try {
//                        out = NIOUtils.writableFileChannel(file.getAbsolutePath());
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    }
//                    try {
//                        encoder = new AndroidSequenceEncoder(out, Rational.R(4, 1));
//                        encoder.encodeImage(originalCameraImage);
//                        Log.d("callme"," set first ");
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//
//
//                }
//
//                if(smallVideoEnd) {
//                    smallVideoEnd=false;
//                    smallVideo=false;
//                }
//                else if(smallVideoEnd){
//                    smallVideoEnd=false;
//                    smallVideo=false;
//                    try {
//                        encoder.encodeImage(originalCameraImage);
//                        encoder.finish();
//                        ContentValues values = new ContentValues();
//                        values.put(MediaStore.Video.VideoColumns.TITLE, file.getPath());
//                        values.put(MediaStore.Video.VideoColumns.ORIENTATION, sharedpreferences.getInt("Orientation",90));
//                        values.put(MediaStore.Video.VideoColumns.DATE_TAKEN, System.currentTimeMillis()); // DATE HERE
//                        values.put(MediaStore.Video.VideoColumns.MIME_TYPE, "");
//                        values.put(MediaStore.Video.VideoColumns.DATA, file.getPath());
//                        //values.put(MediaStore.Video.Media.DATA, file.getPath());
//                        editor.putString("lastImageUi", file.getPath());
//                        editor.commit();
//                        Log.d("videoPath"," set end "+file.getPath());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                    NIOUtils.closeQuietly(out);
//
//                }
//                else{
//                    try {
//                        encoder.encodeImage(originalCameraImage);
//                        Log.d("callme"," set mid ");
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                }



            }
            CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay, originalCameraImage);
            graphicOverlay.add(imageGraphic);
        }
/*
      for (int i = 0; i < faces.size(); ++i) {
           FirebaseVisionFace face = faces.get(i);
           FaceContourGraphic faceGraphic = new FaceContourGraphic(graphicOverlay, face);
           graphicOverlay.add(faceGraphic);
      }
*/
        graphicOverlay.postInvalidate();
    }


    private class FrametoVideo extends AsyncTask<Integer, Void, Void> {


        @Override
        protected Void doInBackground(Integer... integers) {
            if(integers[0]==1){
                out = null;
                sdCard = Environment.getExternalStorageDirectory();
                dir = new File(sdCard.getAbsolutePath() +  "/DCIM/ARBeauty");
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                String timeStamp = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
                String ImageFile = "ARBeauty-" + timeStamp + ".mp4"; //".png";
                //File file = new File(dir, ImageFile);

                file = new File(dir, ImageFile);

                try {
                    out = NIOUtils.writableFileChannel(file.getAbsolutePath());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    videoCreatedMode=1;
                    encoder = new AndroidSequenceEncoder(out, Rational.R(14, 1));
                    encoder.encodeImage(bitmapQueue.poll());
                    Log.d("videoPath"," set first ");
                } catch (IOException e) {
                    e.printStackTrace();
                }



            }else if(integers[0]==2){
                try {
                    encoder.encodeImage(bitmapQueue.poll());
                    Log.d("videoPath"," set mid ");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(integers[0]==3){

                try {
                        encoder.encodeImage(bitmapQueue.poll());
                        encoder.finish();
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Video.VideoColumns.TITLE, file.getPath());
                        values.put(MediaStore.Video.VideoColumns.ORIENTATION, sharedpreferences.getInt("Orientation",90));
                        values.put(MediaStore.Video.VideoColumns.DATE_TAKEN, System.currentTimeMillis()); // DATE HERE
                        values.put(MediaStore.Video.VideoColumns.MIME_TYPE, "");
                        values.put(MediaStore.Video.VideoColumns.DATA, file.getPath());
                        //values.put(MediaStore.Video.Media.DATA, file.getPath());
                        editor.putString("lastImageUi", file.getPath());
                        editor.apply();
                        videoCreatedMode=10;

                        Log.d("videocreated","videocreated set end "+file.getPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    NIOUtils.closeQuietly(out);
            }
            return null;
        }

        protected void onPreExecute() {
            // Runs on the UI thread before doInBackground
            // Good for toggling visibility of a progress indicator

        }





    }

    public static void saveBitmap(final Bitmap bitmap, final String filename) {
        final String root =
                Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "imagesegmenter";
        //Log.i("save", bitmap.getWidth(), bitmap.getHeight(), root);
        final File myDir = new File(root);

        if (!myDir.mkdirs()) {
            Log.i("save","Make dir failed");
        }

        final String fname = filename;
        final File file = new File(myDir, fname);
        if (file.exists()) {
            file.delete();
        }
        try {
            final FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 99, out);
            out.flush();
            out.close();
        } catch (final Exception e) {
            Log.e("save", "Exception!");
        }
    }
    public static interface ClickCallback {
        void onCameraClickCallback();
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Face detection failed " + e);
    }


}

