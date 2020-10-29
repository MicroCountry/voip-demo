package com.yiwise.voip;

import com.yiwise.voip.vad.VoiceActDetector;
import com.yiwise.voip.vad.VoiceActDetectorCallback;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

/**
 * @Author: wangguomin
 * @Date: 2019-03-27 16:31
 */
public class TestSoundVad {

    public static void main(String[] args) throws Exception{
        AudioFormat audioFormat = new AudioFormat(8000, 16, 1, true, false);
        DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
        TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
        targetDataLine.open(audioFormat);
        targetDataLine.start();

        VoiceActDetector voiceActDetector = new VoiceActDetector(new VoiceActDetectorCallback() {
            @Override
            public void onVoiceStart() {
            }

            @Override
            public void onVoiceEnd(long frameCount) {
            }
        });
        voiceActDetector.setGateMuteValue(1500);
        int nByte = 0;
        final int bufSize = 3200;
        byte[] buffer = new byte[bufSize];
        while (nByte != -1) {
            nByte = targetDataLine.read(buffer, 0, bufSize);
            voiceActDetector.update(buffer);
        }
    }
}
