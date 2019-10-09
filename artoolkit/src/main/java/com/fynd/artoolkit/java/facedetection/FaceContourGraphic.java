package com.fynd.artoolkit.java.facedetection;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.fynd.artoolkit.common.GraphicOverlay;
import com.fynd.artoolkit.common.GraphicOverlay.Graphic;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/** Graphic instance for rendering face contours graphic overlay view. */
public class FaceContourGraphic extends Graphic {

  private static final float FACE_POSITION_RADIUS = 4.0f;
  private static final float ID_TEXT_SIZE = 30.0f;
  private static final float ID_Y_OFFSET = 80.0f;
  private static final float ID_X_OFFSET = -70.0f;
  private static final float BOX_STROKE_WIDTH = 5.0f;

  private final Paint facePositionPaint;
  private final Paint idPaint;
  private final Paint boxPaint;

  private volatile FirebaseVisionFace firebaseVisionFace;

  public FaceContourGraphic(GraphicOverlay overlay, FirebaseVisionFace face) {
    super(overlay);

    this.firebaseVisionFace = face;
    final int selectedColor = Color.WHITE;

    facePositionPaint = new Paint();
    facePositionPaint.setColor(selectedColor);

    idPaint = new Paint();
    idPaint.setColor(selectedColor);
    idPaint.setTextSize(ID_TEXT_SIZE);

    boxPaint = new Paint();
    boxPaint.setColor(selectedColor);
    boxPaint.setStyle(Paint.Style.STROKE);
    boxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
  }

/*
  */
/** Draws the face annotations for position on the supplied canvas. */

  @Override
  public void draw(Canvas canvas) {
    FirebaseVisionFace face = firebaseVisionFace;
    if (face == null) {
      return;
    }

    // Draws a circle at the position of the detected face, with the face's track id below.
    float x = translateX(face.getBoundingBox().centerX());
    float y = translateY(face.getBoundingBox().centerY());
    canvas.drawCircle(x, y, FACE_POSITION_RADIUS, facePositionPaint);
    canvas.drawText("id: " + face.getTrackingId(), x + ID_X_OFFSET, y + ID_Y_OFFSET, idPaint);

    // Draws a bounding box around the face.
    float xOffset = scaleX(face.getBoundingBox().width() / 2.0f);
    float yOffset = scaleY(face.getBoundingBox().height() / 2.0f);
    float left = x - xOffset;
    float top = y - yOffset;
    float right = x + xOffset;
    float bottom = y + yOffset;
    canvas.drawRect(left, top, right, bottom, boxPaint);

    FirebaseVisionFaceContour contour = face.getContour(FirebaseVisionFaceContour.ALL_POINTS);
    for (com.google.firebase.ml.vision.common.FirebaseVisionPoint point : contour.getPoints()) {
      float px = translateX(point.getX());
      float py = translateY(point.getY());
      canvas.drawCircle(px, py, FACE_POSITION_RADIUS, facePositionPaint);
    }

    if (face.getSmilingProbability() >= 0) {
      canvas.drawText(
          "happiness: " + String.format("%.2f", face.getSmilingProbability()),
          x + ID_X_OFFSET * 3,
          y - ID_Y_OFFSET,
          idPaint);
    }

    if (face.getRightEyeOpenProbability() >= 0) {
      canvas.drawText(
          "right eye: " + String.format("%.2f", face.getRightEyeOpenProbability()),
          x - ID_X_OFFSET,
          y,
          idPaint);
    }
    if (face.getLeftEyeOpenProbability() >= 0) {
      canvas.drawText(
          "left eye: " + String.format("%.2f", face.getLeftEyeOpenProbability()),
          x + ID_X_OFFSET * 6,
          y,
          idPaint);
    }
    FirebaseVisionFaceLandmark leftEye = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE);
    if (leftEye != null && leftEye.getPosition() != null) {
      canvas.drawCircle(
          translateX(leftEye.getPosition().getX()),
          translateY(leftEye.getPosition().getY()),
          FACE_POSITION_RADIUS,
          facePositionPaint);
    }
    FirebaseVisionFaceLandmark rightEye = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE);
    if (rightEye != null && rightEye.getPosition() != null) {
      canvas.drawCircle(
          translateX(rightEye.getPosition().getX()),
          translateY(rightEye.getPosition().getY()),
          FACE_POSITION_RADIUS,
          facePositionPaint);
    }

    FirebaseVisionFaceLandmark leftCheek = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_CHEEK);
    if (leftCheek != null && leftCheek.getPosition() != null) {
      canvas.drawCircle(
          translateX(leftCheek.getPosition().getX()),
          translateY(leftCheek.getPosition().getY()),
          FACE_POSITION_RADIUS,
          facePositionPaint);
    }
    FirebaseVisionFaceLandmark rightCheek =
        face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_CHEEK);
    if (rightCheek != null && rightCheek.getPosition() != null) {
      canvas.drawCircle(
          translateX(rightCheek.getPosition().getX()),
          translateY(rightCheek.getPosition().getY()),
          FACE_POSITION_RADIUS,
          facePositionPaint);
    }
  }


  public float[] getContourCoordinateArray(@NotNull FirebaseVisionFace face, int CONTOUR_TYPE, char coordinate_type){
    List<Float> contourList = new ArrayList<Float>();
    FirebaseVisionFaceContour contour = face.getContour(CONTOUR_TYPE);

    for (com.google.firebase.ml.vision.common.FirebaseVisionPoint point : contour.getPoints()) {
      float px = translateX_opencv(point.getX());
      if(coordinate_type == 'x') {
        contourList.add(px);
      }
      else if(coordinate_type == 'y') {
      float py = translateY_opencv(point.getY());
        contourList.add(py);
      }
    }

    float[] contourArray = new float[contourList.size()];

    int i = 0;
    for (Float f : contourList) {
      contourArray[i++] = f;
    }
    Log.d("TTTTTTTTTTT", "getContourCoordinateArray: %f" + contourList);
    return contourArray;
  }
/*
  @Override
  public void draw(Canvas canvas) {

    FirebaseVisionFace face = firebaseVisionFace;
    if (face == null) {
      return;
    }
  }*/
}
