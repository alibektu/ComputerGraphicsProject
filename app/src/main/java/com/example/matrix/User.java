package com.example.matrix;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by cjoo on 4/25/15.
 */
public class User
{

    // cjoo: id
    String id;
    CharSequence name;

    User()
    {
        id = "";
        name = "";
        mySpaceId = "";

        try
        {
            serverAddr = InetAddress.getByName("114.70.9.118"); //out of UNIST
            //serverAddr = InetAddress.getByName("10.20.17.4");//My lab MAC server in UNIST
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
            isConnected = false;
        }

        serverPort = 8002;
        isConnected = false;
    }


    InetAddress myAddr;
    InetAddress serverAddr;
    int serverPort;
    boolean isConnected;

    String mySpaceId;

    public void getLocalIpAddress()
    {
        try
        {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces)
            {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs)
                {
                    if (!addr.isLoopbackAddress())
                    {
                        myAddr = addr;
                    }
                }
            }
        }
        catch (Exception ex) { } // for now eat exceptions
        return;
    }

    public void send( final DatagramSocket skt, final String msg ) {
        if( serverAddr == null || serverPort == 0 ) return;
        final DatagramPacket pkt;
        pkt = new DatagramPacket( msg.getBytes(), msg.length(), serverAddr, serverPort );

        new Thread(new Runnable() {
            public void run() {
                try {
                    skt.send( pkt );
                } catch ( IOException e ) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
};

class Users
{
    List<User> uList = new ArrayList<User>();
};

