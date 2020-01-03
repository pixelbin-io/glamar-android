package com.fynd.artoolkit.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Constants {

    public static final String FACE_DETECTION = "Face Detection";
    public static final String FACE_CONTOUR = "Face Contour";

    public static final ArrayList<String> colorsHexList = new ArrayList<>(Arrays.asList(
            "#8B0000", "#800000", "#FF6347", "#0000A0", "#000000","#f88379","#FFE5B4","#FFA500","#7a3d3a","#D2691E"));

    // Dont change order or Below list since same order is used in native-lib
    public static final List<String> makeUpList = new ArrayList<>(Arrays.asList("Lipstick", "Blush", "Eyeliner", "Eyeshadow","earing","kajal"));
    public static final List<Float> colorAlphaList = new ArrayList<>(Arrays.asList(0.89f, 0.91f, 0.8f, 0.93f));

}
