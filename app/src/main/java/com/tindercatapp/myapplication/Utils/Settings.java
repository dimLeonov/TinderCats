package com.tindercatapp.myapplication.Utils;

public class Settings {

    private boolean isMute;


    public boolean isMute() {
        return isMute;
    }

    public void setMute(boolean mute) {
        isMute = mute;
    }

    public Settings(boolean isMute) {
        this.isMute = isMute;
    }
}
