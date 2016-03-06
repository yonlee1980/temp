package com.jikexueyuan.model;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Created by Administrator on 2016/2/27.
 */
public class IpGet {
    public static String getLocalIpAdress(){
        StringBuilder sb = new StringBuilder();
        try{
            for(Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();){
                NetworkInterface intf =en.nextElement();
                for(Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();enumIpAddr.hasMoreElements();){
                    InetAddress inetAddress =enumIpAddr.nextElement();
                    if(!inetAddress.isLoopbackAddress()&&!inetAddress.isLinkLocalAddress()){
                        if(inetAddress.getHostAddress().matches("\\d+.\\d+.\\d+.\\d+")){
//                            if(!inetAddress.getHostAddress().startsWith("192")){
                                return inetAddress.getHostAddress();
//                            }
                        }
                    }
                }
            }
            return sb.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
