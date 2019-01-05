package com.yiwise.voip;

import com.yiwise.voip.config.OperationEnum;
import com.yiwise.voip.javaxsound.JavaxSoundManager;
import net.sourceforge.peers.Config;
import net.sourceforge.peers.FileLogger;
import net.sourceforge.peers.JavaConfig;
import net.sourceforge.peers.Logger;
import net.sourceforge.peers.media.MediaMode;
import net.sourceforge.peers.sip.Utils;
import net.sourceforge.peers.sip.core.useragent.SipListener;
import net.sourceforge.peers.sip.core.useragent.UserAgent;
import net.sourceforge.peers.sip.syntaxencoding.SipUriSyntaxException;
import net.sourceforge.peers.sip.transactionuser.Dialog;
import net.sourceforge.peers.sip.transactionuser.DialogManager;
import net.sourceforge.peers.sip.transport.SipRequest;
import net.sourceforge.peers.sip.transport.SipResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @Author: wangguomin
 * @Date: 2018-12-13 22:21
 */
public class AppB implements SipListener {

    private UserAgent userAgent;
    private SipRequest sipRequest;

    public AppB() throws SocketException {
        Config config = new JavaConfig();
        config.setUserPart("1001");
        config.setDomain("192.168.199.195:5060");
        config.setPassword("1234");
        config.setMediaMode(MediaMode.captureAndPlayback);
        InetAddress inetAddress = null;
        try {
            // if you have only one active network interface, getLocalHost()
            // should be enough
            //inetAddress = InetAddress.getLocalHost();
            // if you have several network interfaces like I do,
            // select the right one after running ipconfig or ifconfig
            inetAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();

        }
        config.setLocalInetAddress(inetAddress);
        Logger logger = new FileLogger(null);
        JavaxSoundManager javaxSoundManager = new JavaxSoundManager(false, logger, null);
        userAgent = new UserAgent(this, config, logger, javaxSoundManager);
        new Thread() {
            @Override
            public void run() {
                try {
                    userAgent.register();
                    System.out.println("B : 注册成功" );
                } catch (SipUriSyntaxException e) {
                    e.printStackTrace();
                    System.out.println("B : 注册失败" );
                }
            }
        }.start();

        InputStreamReader inputStreamReader = new InputStreamReader(System.in);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        while (true) {
            String command;
            try {
                command = bufferedReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
            command = command.trim();
            if (command.startsWith(OperationEnum.CALL)) {
                //call sip:1000@47.100.166.61:5060
                String callee = command.substring(
                        command.lastIndexOf(' ') + 1);
                call(callee);
            } else if (command.startsWith(OperationEnum.HANGUP)) {
                hangup();
            } else if (command.startsWith(OperationEnum.PICKUP)){
                SipRequest uasRequest = userAgent.getUas().getSipRequest();
                if(uasRequest != null) {
                    String callId = Utils.getMessageCallId(uasRequest);
                    DialogManager dialogManager = userAgent.getDialogManager();
                    Dialog dialog = dialogManager.getDialog(callId);
                    userAgent.acceptCall(uasRequest, dialog);
                    sipRequest = uasRequest;
                    System.out.println("B 接听成功");
                }else {
                    System.out.println("uas sipRequest is null");
                }
            }else {
                System.out.println("unknown command " + command);
            }
        }
    }


    // commands methods
    public void call(final String callee) {
        new Thread() {
            @Override
            public void run() {
                try {
                    sipRequest = userAgent.invite(callee, null);
                } catch (SipUriSyntaxException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void hangup() {
        new Thread() {
            @Override
            public void run() {
                userAgent.terminate(sipRequest);
            }
        }.start();
    }


    // SipListener methods

    @Override
    public void registering(SipRequest sipRequest) { }

    @Override
    public void registerSuccessful(SipResponse sipResponse) { }

    @Override
    public void registerFailed(SipResponse sipResponse) { }

    @Override
    public void incomingCall(SipRequest sipRequest, SipResponse provResponse) { }

    @Override
    public void remoteHangup(SipRequest sipRequest) { }

    @Override
    public void ringing(SipResponse sipResponse) { }

    @Override
    public void calleePickup(SipResponse sipResponse) { }

    @Override
    public void error(SipResponse sipResponse) { }

    public static void main(String[] args) {
        try {
            new AppB();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
