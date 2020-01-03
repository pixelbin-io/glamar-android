package com.fynd.ficto.helper;

/**
 * Created by yarolegovich on 08.03.2017.
 */

public class DataHolder {

    private final String Name;
    private final String Color;
    private final int Icon;


    public DataHolder(String Name, int Icon, String Color) {
        this.Name = Name;
        this.Icon = Icon;
        this.Color=Color;

    }

    public String getCityName() {
        return Name;
    }

    public String getColorName() {
        return Color;
    }

    public int getCityIcon() {
        return Icon;
    }


}
