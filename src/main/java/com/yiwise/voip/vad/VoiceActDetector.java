package com.yiwise.voip.vad;


import com.google.common.primitives.Shorts;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author annpeter.it@gmail.com
 * @date 15/06/2018
 */
public class VoiceActDetector {
    protected static int DEFAULT_MUTE_VALUE = 2300; // 静音判断的默认值
    protected static int MIN_VOICE_COUNT = 5; // 最小声音片段数

    private int minRecordTime = 0;   // 最短录音时间
    private int maxRecordTime = 30000; // 最长录音时间
    private int gateMuteCount = 35; // 连续静音片段个数阈值，超过该值可以认为发现了静默空白
    private int gateVoiceCount = 5; // 连续有声片段个数阈值，超过该值可以认为检测到说话
    private int frameCount = 0; // 累计的片段个数
    private int lastVoiceEndFrameCount = 0; // 最近一次的voice end frame count 值
    private int gateMuteValue; // 静音阈值，低于此值的被认为是静音
    private int gateFrameMuteCount; // 片段中静音判断的点数阈值，有声点数超过该值，整个片段被判断为有声，否则是静音
    private int nVoiceContinuous; // 当前连续的有声片段个数
    private boolean mIsDetectStop; // 是否检测说话结束
    private int nMuteCount; // 当前连续的静音片段个数
    private int nVoiceCount; // 有声片段总数
    private int mRecordTime; // 累计的片段的时间，单位毫秒
    private boolean isSaying = false; // 是否在说话中
    private VoiceActDetectorCallback mCallback;

    private List<Integer> frameMuteValuePre = new ArrayList<>();
    private List<Integer> frameAvgValuePre = new ArrayList<>();
    private List<Integer> gradPre = new ArrayList<>();
    private int index = 0;
    private int preAvg;
    private int preMute;

    public VoiceActDetector(VoiceActDetectorCallback callback) {
        this.gateMuteValue = DEFAULT_MUTE_VALUE;
        this.gateFrameMuteCount = 35;
        this.nVoiceContinuous = 0;
        this.mIsDetectStop = true;
        this.nMuteCount = 0;
        this.nVoiceCount = 0;
        this.mRecordTime = 0;
        this.mCallback = callback;
    }

    public void reset() {
        this.gateMuteValue = DEFAULT_MUTE_VALUE;
        this.nMuteCount = 0;
        this.mRecordTime = 0;
        this.frameCount = 0;
        this.nVoiceCount = 0;
        this.frameMuteValuePre = new ArrayList<>();
        this.gradPre = new ArrayList<>();
        this.frameAvgValuePre = new ArrayList<>();
    }

    public void setGateMuteValue(int gateMuteValue) {
        this.gateMuteValue = gateMuteValue;
    }

    public void setGateFrameMuteCount(int gateFrameMuteCount) {
        this.gateFrameMuteCount = gateFrameMuteCount;
    }

    public void detectStopEnabled(boolean isDetectStop) {
        this.mIsDetectStop = isDetectStop;
    }

    public void setMuteGate(int gateCount) {
        gateMuteCount = gateCount;
    }

    public void setMaxVoiceTime(int maxVoiceTime) {
        int maxVoiceGate = maxVoiceTime / 20;
        setVoiceGate(maxVoiceGate);
    }

    public void setVoiceGate(int gateCount) {
        gateVoiceCount = gateCount;
    }

    public void setMaxStallTime(int maxStallTime) {
        int maxStallGate = maxStallTime / 20;
        setMuteGate(maxStallGate);
    }

    public int getRecordTime() {
        return this.mRecordTime;
    }

    private boolean needStopWhenAutoStop() {
        if (this.nMuteCount <= gateMuteCount || this.mRecordTime < minRecordTime && this.nVoiceCount <= MIN_VOICE_COUNT) {
            return false;
        } else {
            return true;
        }
    }

    private boolean needStopNormal() {
        if (this.mRecordTime > maxRecordTime) {
            return true;
        } else {
            return false;
        }
    }

    public boolean update(byte[] bufferFrame) {
        short[] shorts = AudioHandleUtils.getShorts(bufferFrame);
        return update(shorts);
    }

    public boolean update(short[] bufferFrame) {
        boolean bRet = false;
        if (bufferFrame.length != 1600) {
            return bRet;
        }
        this.updateValue(bufferFrame);
        //log();
        this.isMute(bufferFrame);
        if (this.needStopNormal() || (this.mIsDetectStop && this.needStopWhenAutoStop())) {
            this.lastVoiceEndFrameCount = this.frameCount;
            if (this.isSaying) {
                this.isSaying = false;
                if (this.mCallback != null) {
                    this.mCallback.onVoiceEnd(this.frameCount);
                }
            }
        }
        if (lastVoiceEndFrameCount != 0 && (this.frameCount > this.lastVoiceEndFrameCount) && !isSaying) {
            isSaying = true;
            if (this.mCallback != null) {
                this.mCallback.onVoiceStart();
            }
        }
        return bRet;
    }

    public void log(){
        System.out.println("************avg start***********");
        /*StringBuffer sb1 = new StringBuffer();
        for(Integer item : frameAvgValuePre){
            sb1.append(item).append(";");
        }
        System.out.println(sb1.toString());*/
        StringBuffer sb2 = new StringBuffer();
        for(Integer item : frameMuteValuePre){
            sb2.append(item).append(";");
        }
        System.out.println(sb2.toString());
        System.out.println("************avg end***********");
        System.out.println("______________________________");
    }

    public void updateValue(short[] wave){
        int sumValue = 0;
        int nLen = wave.length;
        if(nLen == 0){
            return;
        }
        List<Short> shorts = Shorts.asList(wave);
        shorts.sort(new Comparator<Short>() {
            @Override
            public int compare(Short o1, Short o2) {
                Short o1_tmp = o1;
                Short o2_tmp = o2;
                if(o1 < 0){
                    o1_tmp = (short)-o1;
                }
                if(o2 < 0){
                    o2_tmp = (short)-o2;
                }
                return o1_tmp.compareTo(o2_tmp);
            }
        });
        List<Short> subList = shorts.subList(0, nLen/2);

        for (int i = 0; i < nLen/2; ++i) {
            short v = subList.get(i);
            if (v < 0) {
                v = (short) (-v);
            }
            sumValue += v;
        }
        /*frameAvgValuePre.add(sumValue/ nLen);
        if(index >= 2){
            frameMuteValuePre.add((frameMuteValuePre.get(index - 1) - frameMuteValuePre.get(index - 2)) *
                    (frameAvgValuePre.get(index) - frameAvgValuePre.get(index - 1)) / (frameAvgValuePre.get(index - 1) - frameAvgValuePre.get(index -2))
            + frameMuteValuePre.get(index - 1));
            frameMuteValuePre.add(frameAvgValuePre.get(index) * frameMuteValuePre.get(index - 1) / frameAvgValuePre.get(index - 1));
        }
        if(index == 1){
            //初始第一个
            frameMuteValuePre.add(frameMuteValuePre.get(index - 1) * frameAvgValuePre.get(1)/frameAvgValuePre.get(0));
        }
        if(index == 0){
            frameMuteValuePre.add(this.gateMuteValue);
        }*/

        if(index == 0){
            this.preAvg = sumValue/(nLen/2);
            this.preMute = this.gateMuteValue;
        }else {
            int avg = sumValue/(nLen/2);
            this.preMute = this.preMute * avg / this.preAvg;
            this.preAvg = avg;
        }
        System.out.println("当前音强:" + this.preAvg + ";当前静音阈值: " + this.preMute);

        index++;
    }

    public boolean isMute(short[] wave) {
        ++this.frameCount;
        this.mRecordTime += 20;
        int nLen = wave.length;
        int nCount = 0; // 有声点数
        int mCount = 0; // 无声点数
        int gateFrameVoiceCount = nLen - this.gateFrameMuteCount;

        for (int i = 0; i < nLen; ++i) {
            short v = wave[i];
            if (v < 0) {
                v = (short) (-v);
            }

            if (v > this.gateMuteValue) {
                ++nCount;
            } else {
                ++mCount;
            }

            if (nCount > this.gateFrameMuteCount) {
                ++this.nVoiceContinuous;
                if (this.nVoiceContinuous > gateVoiceCount) {
                    this.nMuteCount = 0;
                } else {
                    ++this.nMuteCount;
                }

                ++this.nVoiceCount;
                return false;
            }
            if (mCount > gateFrameVoiceCount) {
                ++this.nMuteCount;
                this.nVoiceContinuous = 0;
                return true;
            }
        }

        if (nCount > this.gateFrameMuteCount) {
            ++this.nVoiceContinuous;
            if (this.nVoiceContinuous > gateVoiceCount) {
                this.nMuteCount = 0;
            } else {
                ++this.nMuteCount;
            }

            ++this.nVoiceCount;
            return false;
        } else {
            ++this.nMuteCount;
            this.nVoiceContinuous = 0;
            return true;
        }
    }

    public int getFrameCount() {
        return frameCount;
    }

    public int getMinRecordTime() {
        return minRecordTime;
    }

    public void setMinRecordTime(int minRecordTime) {
        this.minRecordTime = minRecordTime;
    }

    public int getMaxRecordTime() {
        return maxRecordTime;
    }

    public void setMaxRecordTime(int maxRecordTime) {
        this.maxRecordTime = maxRecordTime;
    }
}
