package com.yiwise.voip.vad;

/**
 * @author annpeter.it@gmail.com
 * @date 15/06/2018
 */
public interface VoiceActDetectorCallback {
    void onVoiceStart();

    void onVoiceEnd(long frameCount);
}
