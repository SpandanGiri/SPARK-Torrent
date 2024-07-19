/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spark.bittorrent;

import java.util.*;
import java.net.Socket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.nio.ByteBuffer;


/**
 *
 * @author Spandan
 */
public class Download {
    
    
    public static void download(List<List>peers){
        if(!peers.isEmpty()){            
            System.out.println("Inside Download function");
            for(List peer:peers){
                String ip = peer.get(0).toString();
                int port = Integer.parseInt(peer.get(1).toString());
                //downloadPeer(ip,port);
            }
        }
        else{
            System.out.println("No Peers found");
        }
    }
    
    public static void downloadPeer(String ip,int port,Map<String,Object> tParser){
        
        try{
        
        System.out.println("Requesting pieces from " + "ip: "+ ip +" port: "+port);
        InetAddress Ipaddr = InetAddress.getByName(ip);
        
        Socket ss = new Socket(Ipaddr,port);
       
        DataOutputStream ds = new DataOutputStream(ss.getOutputStream());
        byte[] handshakeByte = Message.buildHandShake();
        ds.write(handshakeByte);
        
        System.out.println("Handshake sent..");
        DataInputStream dis=new DataInputStream(ss.getInputStream());  
        
        
        while(true){
            byte[] responseByte = new byte[4096];
            int responseByteRead = dis.read(responseByte);
            if(responseByteRead>0){
                msgHandler(responseByte,ds,dis,tParser);
            }else{
                System.out.println("No more data from server, closing connection.");
                break;
            }
        }
        
        ss.close();    
        }catch(Exception e){
            e.printStackTrace();
        }    
    }
    
    public static void msgHandler(byte[] peerResponse,DataOutputStream ds,DataInputStream dis,Map<String,Object> tParser)throws Exception{

        System.out.println("Inside Message Handler");  
        //First check for handshake message 
        
        if(isHandShake(peerResponse)){
            handshakeHandler(ds);      
        }else{
        
        //Then heck for message id and pass to the corresponding handler
            byte[] messageIdByte = Arrays.copyOfRange(peerResponse, 4, 5);
            
            int messageId = messageIdByte[0];

            System.out.println("messageId: "+messageId);

            if(messageId==0){
                chokeHandler();
            }
            else if(messageId==1){
                unChokeHandler(ds,dis,tParser);
            }   
            else if(messageId==2){
                interestedHandler();
            }
            else if(messageId==3){
                unInterestedHandler();
            }   
            else if(messageId==4){
                haveHandler(peerResponse);
            }
            else if(messageId==5){
                bitfieldHandler(peerResponse);
            }          
            else if(messageId==7){
                pieceHandler(peerResponse);
            }
            else{
                System.out.println("Unkown messageId : "+messageId);
            }
        }
    }
    
    public static void handshakeHandler(DataOutputStream ds)throws Exception{
        System.out.println("Inside Handshake Handler");
        
        byte[] interestedMessage = Message.buildInterested();               
        ds.write(interestedMessage);                
        System.out.println("Interested message sent to peer: ");   
    }
    
    public static void chokeHandler(){
        
    }
    public static void unChokeHandler(DataOutputStream ds,DataInputStream dis,Map<String,Object> tParser) throws Exception{
        System.out.println("Inside Unchoke handler");
        
        int lastPieceIndex = Integer.parseInt(tParser.get("lastPieceIndex").toString());
        System.out.println("lastPieceIndex: "+ lastPieceIndex);
        
 
        
        sendPieceRequest(dis,ds,0,tParser);
        
    }
    public static void interestedHandler(){
        System.out.println("Inside Interested handler");
    }
    public static void unInterestedHandler(){
        System.out.println("Inside un-Interested handler");
    }
    
    public static void haveHandler(byte[] peerResponse){
        System.out.println("Inside Have handler");
    }
    
    public static void bitfieldHandler(byte[] pperResponse){
        System.out.println("Inside BitfieldHandler handler");
    }
    
    public static boolean isHandShake(byte[] handShakeResponseByte)throws Exception{
        byte[] peerPtrlStrByte = Arrays.copyOfRange(handShakeResponseByte, 1, 20);
        String peerPtrlStr = new String(peerPtrlStrByte,"UTF-8");
        
        System.out.println(peerPtrlStr);
        
        return peerPtrlStr.equals("BitTorrent protocol"); 
    }
    
    public static void pieceHandler(byte[] pieceResponse){
        System.out.println("Inside pieceHandler");
        System.out.println("Received data: " + pieceResponse.length + " bytes");
        
        byte[] pieceIndexByte = Arrays.copyOfRange(pieceResponse, 5, 9);
        
        //offset
        byte[] beginByte = Arrays.copyOfRange(pieceResponse, 9, 13);
        
        ByteBuffer respPieceWrapped = ByteBuffer.wrap(pieceIndexByte);
        ByteBuffer beginByteWrapped = ByteBuffer.wrap(beginByte);
        
        int respPieceIndex = respPieceWrapped.getInt();
        int beginOffset = beginByteWrapped.getInt();
        
        
        System.out.println("Will start to write file in offset "+beginOffset);
        
        
    }
    
    public static boolean isUnChoke(byte[] interestedResponseByte)throws Exception{
        byte[] messageIdByte = Arrays.copyOfRange(interestedResponseByte, 4, 5);
        
        //int messageId = ByteBuffer.wrap(messageIdByte).getShort();
        int messageId = messageIdByte[0];
        if(messageId==1)    return true;
             
        return false;
    }

    
    public static void sendPieceRequest(DataInputStream dis,DataOutputStream ds,int pieceIndex,Map<String,Object> tParser) throws Exception{
        
        int blocksPerPiece = Integer.parseInt(tParser.get("blocksPerPiece").toString());
        int blockLen = Utils.blockLength;
        
        for(int i=0;i<blocksPerPiece;i++){
            
            byte[] requestBlockBytes;
            //for last block
            if(i==blocksPerPiece-1){
                
                int lastBlockLen = Integer.parseInt(tParser.get("lastBlockLength").toString());
                requestBlockBytes = Message.buildRequest(pieceIndex,i*blockLen,lastBlockLen);
            }
            else{
                requestBlockBytes = Message.buildRequest(pieceIndex,i*blockLen,blockLen);   
            }
            
            ds.write(requestBlockBytes);
            ds.flush();
            
            System.out.println("Request sent for block:"+ (i+1));
            ByteArrayOutputStream pieceResponseStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096]; // larger buffer to handle piece response
            int bytesRead = 0;
            
            
            while ((bytesRead = dis.read(buffer)) != -1) {

                pieceResponseStream.write(buffer, 0, bytesRead);

                //piece: <len=0009+X><id=7><index><begin><block>
                // If we read the expected amount of data, break the loop
                if (pieceResponseStream.size() >= 16384 + 13) { // 13 bytes for the header
                    break;
                }
            }

            if (pieceResponseStream.size() == 0) {
                System.out.println("No data received from server.");
            } else {
                byte[] pieceResponse = pieceResponseStream.toByteArray();
                pieceHandler(pieceResponse);

            }  

            //Thread.sleep(100);
        }
    }
    
    
    
    public static void main(String args[]){
        List<List> peerList = new ArrayList();
        //
        String ip = "129.151.215.49";
        int port = 6881;
        try{
        Map<String,Object> tParser = Utils.torrentParser("torrentFIles/big-buck-bunny.torrent");
        
        Utils.putBlocksInfo(tParser);
        downloadPeer(ip,port,tParser);
        
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }
}
 