package com.fynd.ficto.helper;

import com.fynd.ficto.R;

import java.util.Arrays;
import java.util.List;

/**
 * Created by yarolegovich on 08.03.2017.
 */

public class Data {


    public static Data get() {
        return new Data();
    }

    private Data() {
    }

    public List<DataHolder> getForecasts() {
        return Arrays.asList(
                new DataHolder("Normal", R.drawable.normal_ic,""),
                new DataHolder("lipstic", R.drawable.lipstic_ic,""),
                new DataHolder("Blush", R.drawable.blush_ic,""),
                new DataHolder("Eyeliner", R.drawable.eye_liner_ic,""),
                new DataHolder("Eyeshadow", R.drawable.eyeshadow_ic,"")
               // new DataHolder("Eyeshadow", R.drawable.eyeshadow_ic,""),
              //  new DataHolder("Eyeshadow", R.drawable.eyeshadow_ic,"")
                );
    }

    public List<DataHolder> getMode() {
        return Arrays.asList(
                new DataHolder("FACE", R.drawable.hair_bg,""));
      //  new DataHolder("HAND", R.drawable.hair_bg,"")
//                new DataHolder("Beauty", R.drawable.hair_bg,""),
//                new DataHolder("HAIR", R.drawable.hair_bg,""));
    }
    public List<DataHolder> getColor() {

        return Arrays.asList(
                new DataHolder("", R.drawable.hair_bg,"#8B0000"),
                new DataHolder("", R.drawable.hair_bg,"#800000"),
                new DataHolder("", R.drawable.hair_bg,"#FF6347"),
                new DataHolder("", R.drawable.hair_bg,"#0000A0"),
                new DataHolder("", R.drawable.hair_bg,"#000000"),
                new DataHolder("", R.drawable.hair_bg,"#f88379"),
                new DataHolder("", R.drawable.hair_bg,"#FFE5B4"),
                new DataHolder("", R.drawable.hair_bg,"#FFA500"));
    }
}
