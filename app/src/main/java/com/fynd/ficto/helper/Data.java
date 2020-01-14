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
                new DataHolder("Normal", R.drawable.ic_nofilter,""),
                new DataHolder("lipstic", R.drawable.ic_lipstick,""),
                new DataHolder("Blush", R.drawable.ic_blush,""),
                new DataHolder("Eyeliner", R.drawable.ic_liner,""),
                new DataHolder("Eyeshadow", R.drawable.ic_eyeshadow,""),
                new DataHolder("Eyeshadow", R.drawable.ic_kajal,"")
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
                new DataHolder("", R.drawable.hair_bg,"#E6E8FA"));
//                new DataHolder("", R.drawable.hair_bg,"#FFE5B4"),
//                new DataHolder("", R.drawable.hair_bg,"#FFA500"));
    }
}
