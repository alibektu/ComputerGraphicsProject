package com.example.matrix;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.EditText;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by netlab on 6/3/16.
 */
public class Receive implements Runnable {


    private SensorManager mSensorManager;
    private Sensor mSensor;


    MainActivity activity;
    User MyState;
    Users uList;
    DatagramSocket rSocket = null;
    DatagramPacket rPacket = null;
    byte[] rMessage = new byte[12000];

    private volatile boolean stopRequested;

    private List<ZaxisListener> listeners = new ArrayList<ZaxisListener>();

    double Z,X;

    public Receive(DatagramSocket sck, User state, Users uList, MainActivity myActivity, SensorManager SM, Sensor S)
    {

        mSensorManager = SM;

        mSensor = S;

        this.rSocket = sck;
        this.MyState = state;
        this.uList = uList;
        stopRequested = false;
        activity = myActivity;
    }

    public User getUser()
    {
        return MyState;
    }

    public void run()
    {
        while (stopRequested == false)
        {
            try {
                rPacket = new DatagramPacket(rMessage, rMessage.length);
                rSocket.receive(rPacket);
                handlePacket(rPacket);
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void handlePacket(DatagramPacket pkt)
    {
        String msg;

        msg = new String(rPacket.getData(), 0, pkt.getLength());


        if (msg.indexOf("Z-Location") >= 0)
        {

            for (ZaxisListener hl : listeners)
            {
                hl.ReceivedFromSensorZ();
            }

        }

        else if (msg.indexOf("X-Location_R") >= 0)
        {

            for (ZaxisListener hl : listeners)
            {
                hl.ReceivedFromSensorX_R();
            }
        }

        else if (msg.indexOf("X-Location_L") >= 0)
        {

            for (ZaxisListener hl : listeners)
            {
                hl.ReceivedFromSensorX_L();
            }
        }

    }

    public void addListener(ZaxisListener toAdd)
    {
        listeners.add(toAdd);
    }
}