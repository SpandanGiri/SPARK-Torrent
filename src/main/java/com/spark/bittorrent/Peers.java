/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spark.bittorrent;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.*;

/**
 *
 * @author Spandan
 */
public class Peers {
    public static int announceTransactionId;
    public static int connectionId;
    public static int connectionTransactionId;
    
    
    public static byte[] createReqCon(){
        ByteBuffer bb = ByteBuffer.allocate(16);

        //protocol id
        bb.putLong(0, 0x41727101980L);
        
        //action id
        bb.putInt(8, 0);
        
        //random transaction id 12-16
        Random random = new Random();
        byte[] randomByte = new byte[4];
        
        random.nextBytes(randomByte);
        int randomTransactionInt = random.nextInt(10000);
        
        Peers.connectionTransactionId = randomTransactionInt;
        
        //transcation id
        bb.putInt(12,randomTransactionInt);
        
        return bb.array();    
        
    } 
    
    public static int parseConnResp(DatagramPacket receivePacket){ 
        
        byte[] recieiveByte = receivePacket.getData();
        
        for (byte b : recieiveByte) {
            System.out.print(String.format("%02x", b));
        }
                     
        byte dataByte[] = recieiveByte;
        
        byte actionByte[] = Arrays.copyOfRange(dataByte, 0, 4);
        byte transByte[] = Arrays.copyOfRange(dataByte,4,8);
        byte connByte[] = Arrays.copyOfRange(dataByte,8,16);
        
        int actionId = ByteBuffer.wrap(actionByte).getInt();
        int transId = ByteBuffer.wrap(transByte).getInt();
        int connId = ByteBuffer.wrap(connByte).getInt();
                    
        if(Peers.connectionTransactionId == transId){
            Peers.connectionId = connId;           
            return connId;
        }    
        return 0;
    }
    
    
    public static byte[] createAnnounceReq(Map<String,Object> tParser,String torrentFilePath)throws Exception{
        
        System.out.println("Creating Announce Request...");
        ByteBuffer bb = ByteBuffer.allocate(98);
        
        //connId
        bb.putLong(0,connectionId);
        
        //actionid =1 for announce
        bb.putInt(8,1);
        
        //transactionId
        Random random = new Random();
        int transId = random.nextInt(10000);       
        bb.putInt(12,transId);
        
        Peers.announceTransactionId = transId;
        
        //infoHash
        Object infoObj = tParser.get("info");
        Map<String , Object> infoMap = new HashMap();
        infoMap.put("info", infoObj);
        
        byte[] infoHashByte = Utils.getInfoHash(torrentFilePath);
 
        bb.put(16,infoHashByte);
           
        //peer id
        byte[] peerIdByte = Utils.genId();
        bb.put(36,peerIdByte);
        
        //downloaded
        byte[] downloadedByte = new byte[8];
        bb.put(56,downloadedByte);
        
        //left 
        long totalSize = Utils.size(tParser);
        bb.putLong(64,totalSize);
        
        //uploaded
        byte[] uploadedByte = new byte[8];
        bb.put(72,uploadedByte);
        
        //event
        bb.putInt(80,0);
        
        //Ip Adress
        bb.putInt(84,0);
        
        //key
        byte[] randomKeyByte = new byte[4];
        random.nextBytes(randomKeyByte);
        
        bb.put(88,randomKeyByte);
        
        //num want       
        bb.putInt(92,-1);
 
        //port
        //default port for bittorent = 6881
        short port  = 6881; 
        bb.putShort(96,port);
        
        System.out.println("Announce Req:");
        for (byte b : bb.array()) {
            System.out.print(String.format("%02x", b));
        }
        return bb.array();
    }
    
    public static List parseAnnounceResponse(DatagramPacket receivePacket){
        
        System.out.println("Parsing Announce Response...");  
        byte[] receiveByte = receivePacket.getData();
        
        for (byte b : receiveByte) {
            System.out.print(String.format("%02x", b));
        }
        
        byte[] actionByte = Arrays.copyOfRange(receiveByte, 0, 4);
        byte transByte[] = Arrays.copyOfRange(receiveByte,4,8);
        byte intervalByte[] = Arrays.copyOfRange(receiveByte,8,12);
        byte leechersByte[] = Arrays.copyOfRange(receiveByte,12,16);
        byte seedersByte[] = Arrays.copyOfRange(receiveByte, 16, 20);
        
        int ipInfoLength = receivePacket.getLength()-20;
        System.out.println("ipInfoLength: "+ipInfoLength);
        
        List<List> peerList = new ArrayList<>(); 
        
        //Iterating the buffer and adding the ip and port to the peerList
        for(int i=20;i<=ipInfoLength;i=i+6){
            List<String> ipList = new ArrayList<>(); 
            byte ipByte[] = Arrays.copyOfRange(receiveByte, i, i+4);
            byte portByte[] = Arrays.copyOfRange(receiveByte, i+4, i+6);    
            
            String ip = Utils.IpFromBytes(ipByte);
            String port = String.valueOf(ByteBuffer.wrap(portByte).getShort() & 0xFFFF);
            
            ipList.add(ip);
            ipList.add(port);
            
            peerList.add(ipList);
        }
        
            
        
        byte IpPort[] = Arrays.copyOfRange(receiveByte, 24, 26);
        
        int actionId = ByteBuffer.wrap(actionByte).getInt();
        int transId = ByteBuffer.wrap(transByte).getInt();
        int interval = ByteBuffer.wrap(intervalByte).getInt();
        int leechers = ByteBuffer.wrap(leechersByte).getInt();
        int seeders = ByteBuffer.wrap(seedersByte).getInt();
        
        
        System.out.println("\nactionId: "+ actionId);
        System.out.println("transactionId: "+ transId);
        System.out.println("interval: "+ interval);
        System.out.println("leechers: "+ leechers);
        System.out.println("seeders: "+ seeders);
        
        
        if(Peers.announceTransactionId == transId){
            return peerList;
        }
        
        return List.of();
    }
    
    public static void UdpSend(DatagramSocket ds, String url, int port) throws Exception {
        InetAddress addr = InetAddress.getByName(url);
        byte[] connBuffer = createReqCon();

        DatagramPacket packet = new DatagramPacket(connBuffer, connBuffer.length, addr, port);
        ds.send(packet);
              
    }
    
    public static void getPeers(String torrentFilePath) throws Exception {
        DatagramSocket ds = null;
        
        Map<String,Object> tParser = Utils.torrentParser(torrentFilePath);
        
        System.out.println("announce-list: "+ tParser.get("announce-list"));
        
        //null is returned in case of error 
        String announce_url = Utils.extractHostname(tParser.get("announce").toString());    
        int port = Integer.parseInt(Utils.extractPort(tParser.get("announce").toString()));
        String infoNode = tParser.get("info").toString();
              
        System.out.println(announce_url);
        announce_url = "tracker.opentrackr.org";
        //announce_url = "torrent.ubuntu.com";
        port = 1337;
        
        System.out.println("announce url: "+announce_url);
        System.out.println("port: "+port);
        
        try {
            ds = new DatagramSocket();
            //https socket required 
                      
            UdpSend(ds, announce_url, port);

            byte[] ConnBuf = new byte[32];
            DatagramPacket receiveConnPacket = new DatagramPacket(ConnBuf, ConnBuf.length);
            System.out.println("\nWaiting for Connection response...");
            ds.receive(receiveConnPacket);          
            
            int connResp = parseConnResp(receiveConnPacket);
            System.out.println("connResp: "+connResp);
            
            if(connResp!=0){
                
                byte[] announceBuffer = createAnnounceReq(tParser,torrentFilePath);
                 InetAddress addr = InetAddress.getByName(announce_url);            
                DatagramPacket announceRequestPacket = new DatagramPacket(announceBuffer, announceBuffer.length, addr, port);
                
                ds.send(announceRequestPacket); 
                
                byte[] AnnBuf = new byte[500];
                
                DatagramPacket receiveAnnPacket = new DatagramPacket(AnnBuf,AnnBuf.length);
                System.out.println("\nWaiting for Announce response...");
                ds.receive(receiveAnnPacket);     
                
                List peerList = parseAnnounceResponse(receiveAnnPacket);
                
                System.out.println(peerList);
                System.out.println(peerList.size());
            }  
            else{
                System.out.println("Connection Error!!!");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ds != null) {
                ds.close();
            }
        }  
    }
}
