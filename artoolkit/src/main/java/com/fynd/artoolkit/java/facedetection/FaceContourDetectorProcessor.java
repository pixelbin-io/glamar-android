package com.fynd.artoolkit.java.facedetection;

import android.graphics.Bitmap;
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

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.List;


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

    private String selectedColor = Constants.colorsHexList.get(0);
    private Integer selectedMakeupIndex = 0;
    private Float makeupOpacitiy = 0.2f;

    // neeraj
    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

    public FaceContourDetectorProcessor() {

        //FirebaseVision fVision=FirebaseVision.getInstance();
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                        .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                        .build();

        detector =FirebaseVision.getInstance().getVisionFaceDetector(options);;
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
            @Nullable Bitmap originalCameraImage,
            @NonNull List<FirebaseVisionFace> faces,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        if (originalCameraImage != null) {
            // neeraj start
            Mat matInput = new Mat();
            Bitmap bmp32 = originalCameraImage.copy(Bitmap.Config.ARGB_8888, true);
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
                Bitmap bmp = Bitmap.createBitmap(matInput.cols(), matInput.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(matResult, bmp);
                originalCameraImage = bmp;
            }
            // neeraj end

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

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Face detection failed " + e);
    }


}
