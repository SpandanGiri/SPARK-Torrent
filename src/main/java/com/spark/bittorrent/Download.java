
package com.spark.bittorrent;

import java.util.*;
import java.net.Socket;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.ByteArrayOutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.*;
import org.apache.log4j.Logger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;
/**
 *
 * @author Spandan
 */
public class Download {
    
    private static Set<Integer> notRecievedPieces = new HashSet<>();
    private static Set<Integer>receivedPieces = new HashSet<>();
    private static String torrentFilePath;
    
    private static int blocksPerPiece;
    private static Map<String,Object> tParser;
    private static int lastPieceIndex;
    private static List<Integer> requestPieceQueue = new ArrayList<>();
    private static List<Integer> alreadyRequestedPiece = new ArrayList<>();
    
    private static int peerCounter = 0;    
    private static List<List>peers = new ArrayList<>();
    private static List<List>reliablePeers = new ArrayList<>();
    
    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private static int pieceLength;
    private static final Logger logger = Logger.getLogger(Download.class);
    
    private static TreeMap<Integer,String> fileOffset = new TreeMap<Integer,String>(); 
    private static Map<String,Integer> fileInfo = new HashMap<String,Integer>(); 

    
    Download(String torrentFilePath,List<List>peers)throws Exception{
        
        this.torrentFilePath = torrentFilePath;
        tParser = Utils.torrentParser(torrentFilePath);
        Utils.putBlocksInfo(tParser);
        blocksPerPiece = Integer.parseInt(tParser.get("blocksPerPiece").toString());
        lastPieceIndex =  Integer.parseInt(tParser.get("lastPieceIndex").toString());
        pieceLength = Integer.parseInt(tParser.get("pieceLength").toString());
        
        //for(int pieceIndex=0;pieceIndex<=lastPieceIndex;pieceIndex++)    requestPieceQueue.add(pieceIndex);
        requestPieceQueue.add(1053);
        requestPieceQueue.add(1054);
        
        
        this.peers = peers;
        
        //Adding some peers for testing 
        
//        peers.add(Arrays.asList("129.151.215.49","6881"));
//        peers.add(Arrays.asList("185.252.232.129","9358"));
//        peers.add(Arrays.asList("109.206.201.132","6881"));
//        peers.add(Arrays.asList("129.151.173.61","6881"));

        fileOffset.put(0,"Big Buck Bunny.en.srt");
        fileOffset.put(140,"Big Buck Bunny.mp4");
        fileOffset.put(276134947,"poster.jpg");
        
        fileInfo.put("Big Buck Bunny.en.srt", 140);
//        fileInfo.put("Big Buck Bunny.mp4", 276134947);
//        fileInfo.put("poster.jpg",310380);
        fileInfo.put("Big Buck Bunny.mp4", 276135087);
        fileInfo.put("poster.jpg",276445467);
        
    }
    
    //will return the next peer
    private static List nextPeer(){
        if(peerCounter>peers.size()-1)  return peers.get(peerCounter++%(peers.size()));
        return peers.get(peerCounter++);
    }
    
    public static void startDownload()throws Exception{
        
        if(!peers.isEmpty()){            
            System.out.println("Inside Download function");
            for(List peer:peers){
                String ip = peer.get(0).toString();
                int port = Integer.parseInt(peer.get(1).toString());
                executorService.submit(()->{
                    log("startdonwload:");
                    downloadPeer(ip,port,tParser);
                    log("Finished:");
                });         
            }
            System.out.println("reliablePeers : "+reliablePeers);
        }
        else{
            System.out.println("No Peers found");
        }
        executorService.shutdown();
    }
    
    public static void downloadPeer(String ip,int port,Map<String,Object> tParser){
        
        try{
        Message m = new Message(torrentFilePath);
        System.out.println("Requesting pieces from " + "ip: "+ ip +" port: "+port);
        InetAddress Ipaddr = InetAddress.getByName(ip);
        
        Socket ss = new Socket(Ipaddr,port);
        ss.setSoTimeout(5000);
       
        DataOutputStream ds = new DataOutputStream(ss.getOutputStream());
        byte[] handshakeByte = Message.buildHandShake();
        ds.write(handshakeByte);
        
        System.out.println("Handshake sent..");
        DataInputStream dis=new DataInputStream(ss.getInputStream());  
           
        while(true){
            byte[] responseByte = new byte[4096];
            int responseByteRead = dis.read(responseByte);
            if(responseByteRead>0){

                reliablePeers.add(Arrays.asList(ip,String.valueOf(port)));
                msgHandler(responseByte,ds,dis,tParser);
            }else{
                System.out.println("No more data from server, closing connection.");
                break;
            }
        }
        
        
        
        ss.close();    
        }catch(Exception e){
            e.printStackTrace();

            if(e instanceof ConnectException){
                //remove the ip from peer        
                List<String>peerdel = Arrays.asList(ip,Integer.toString(port));
                peers.remove(peerdel);
                System.out.println("removing "+peerdel);
                
                // cehck if it is reliable peer re attempt another connection !!!
                downloadNextPeer();  
            }
            else if(e instanceof SocketTimeoutException)    downloadNextPeer();          
        }    
    }
 
    public static void downloadNextPeer(){
        
        List<String> ipList = nextPeer();
        String nextIp = ipList.get(0);
        int nextPort = Integer.parseInt(ipList.get(1));

        downloadPeer(nextIp,nextPort,tParser);      
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
        System.out.println("Choked");         
    }
    
    public static void unChokeHandler(DataOutputStream ds,DataInputStream dis,Map<String,Object> tParser) throws Exception{
        System.out.println("Inside Unchoke handler");
        
        System.out.println("lastPieceIndex: "+ lastPieceIndex);

        sendPieceRequest(dis,ds,requestPieceQueue.get(0),tParser);  //return the first piece in queue
        
        System.out.println("Received Pieces:");
        for(int i: receivedPieces){
            System.out.print(i+" ");
        }
        System.out.println("\nNot Received Pieces:");
        
        for(int i: notRecievedPieces){
            System.out.print(i+" ");
        }     
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
    
    public static void bitfieldHandler(byte[] peerResponse){
        
        System.out.println("Inside BitfieldHandler handler");
        
        byte[] lengthBytes = Arrays.copyOfRange(peerResponse, 0, 4);
        int length = ByteBuffer.wrap(lengthBytes).getInt();
        byte messageId = peerResponse[4];

        System.out.println("Message Length: " + length);
        System.out.println("Message ID: " + messageId);

        // Extract the bitfield
        byte[] bitFieldBytes = Arrays.copyOfRange(peerResponse, 5, 4 + length);
        System.out.println("Bitfield Length: " + bitFieldBytes.length);

        // Print out the bitfield in binary
        for (byte b : bitFieldBytes) {
            System.out.print(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0') + " ");
        }
    }
    
    public static boolean isHandShake(byte[] handShakeResponseByte)throws Exception{
        byte[] peerPtrlStrByte = Arrays.copyOfRange(handShakeResponseByte, 1, 20);
        String peerPtrlStr = new String(peerPtrlStrByte,"UTF-8");
        
        System.out.println(peerPtrlStr);
        return peerPtrlStr.equals("BitTorrent protocol"); 
    }
    
    public static void pieceHandler(byte[] pieceResponse){
        
        System.out.println("pieceResp len :" + pieceResponse.length);
        byte[] pieceIndexByte = Arrays.copyOfRange(pieceResponse, 5, 9);    //pieceIndex
        byte[] beginByte = Arrays.copyOfRange(pieceResponse, 9, 13);    //offset
        byte[] blockBytes = Arrays.copyOfRange(pieceResponse, 13, (pieceResponse.length)-1);    //block
         
        ByteBuffer respPieceWrapped = ByteBuffer.wrap(pieceIndexByte);
        ByteBuffer beginByteWrapped = ByteBuffer.wrap(beginByte);
        
        int respPieceIndex = respPieceWrapped.getInt();
        int beginOffset = beginByteWrapped.getInt();
        int globalOffset = (pieceLength * respPieceIndex) + beginOffset;    //starting offset of the stream of bytes
        int i = fileOffset.floorKey(globalOffset);
        int localOffset = 0;
        
        String targetFile = fileOffset.get(fileOffset.floorKey(globalOffset));  //find the file 
        int targetFileLength = fileInfo.get(targetFile);
        
        //int localOffset = globalOffset - i;     //starting offset of file where the block is going to be copied
        File file = new File(targetFile);
        
        if(file.exists())   localOffset = (int)file.length(); 
         
        System.out.println(globalOffset+" "+localOffset);
             
        if(targetFileLength < globalOffset + Utils.blockLength){
            //write the file till the targetFileLength
            
            //int endOffset = targetFileLength - localOffset;     //how much of bytes are  left till targetFile
            int endOffset = 15675;
            System.out.println(pieceResponse.length);
            System.out.println("endOffset: "+endOffset);
            
            byte[] firstBlockBytes = Arrays.copyOfRange(pieceResponse, 13, endOffset+13);
            byte[] secondBlockBytes = Arrays.copyOfRange(pieceResponse, endOffset+13,(pieceResponse.length));
            
            System.out.println("Will start to write in "+ targetFile +" at offset " + localOffset + " till " + endOffset);
            Utils.writeBytesAtOffset(targetFile, firstBlockBytes, localOffset);
            
            //globalOffset += targetFileLength;
            globalOffset += (firstBlockBytes.length)-1;
            
            //NextFile file
            targetFile = fileOffset.get(fileOffset.floorKey(globalOffset));
            
            localOffset = 0;
            
            System.out.println("Will start to write in "+ targetFile +" at offset " + localOffset + " till " + secondBlockBytes.length);
            Utils.writeBytesAtOffset(targetFile, secondBlockBytes, localOffset);
            
            globalOffset += (secondBlockBytes.length)-1;
            
        }else{
            System.out.println("Will start to write in "+ targetFile +" at offset " + localOffset);
            Utils.writeBytesAtOffset(targetFile, blockBytes, localOffset);
        }
        
    }
    
    
    public static boolean isUnChoke(byte[] interestedResponseByte)throws Exception{
        byte[] messageIdByte = Arrays.copyOfRange(interestedResponseByte, 4, 5);
        
        //int messageId = ByteBuffer.wrap(messageIdByte).getShort();
        int messageId = messageIdByte[0];
        if(messageId==1)    return true;             
        return false;
    }

    
    public static void sendPieceRequest(DataInputStream dis,DataOutputStream ds,int pieceIndex,Map<String,Object> tParser) throws Exception{
        
        if(receivedPieces.contains(pieceIndex) || pieceIndex==lastPieceIndex+1 )    return;
         
        System.out.println("Requesting blocks for piece:"+ pieceIndex);
        int blockLen = Utils.blockLength;
        
        for(int i=0;i<blocksPerPiece;i++){
            
            byte[] requestBlockBytes;
            
            //for last block
            int blockIndex = pieceIndex * 16 + i;
            System.out.println(blockIndex);
            
            if(blockIndex == Integer.parseInt(tParser.get("lastBlockIndex").toString())){
                System.out.println("last block");
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
                notRecievedPieces.add(pieceIndex);
                break;
            } else {
                byte[] pieceResponse = pieceResponseStream.toByteArray();
                pieceHandler(pieceResponse);
                
                if(i==blocksPerPiece-1){
                    receivedPieces.add(pieceIndex);
                    //removing the recieved piece from queue
                    requestPieceQueue.remove(0);
                }
            }      
        }
        
        //calling another peer
        List<String> ipList = nextPeer();
        String ip = ipList.get(0);
        int port = Integer.parseInt(ipList.get(1));
        
        //downloadPeer(ip,port,tParser);
        
        sendPieceRequest(dis,ds,requestPieceQueue.get(0),tParser);
 
    }
    
    public static void main(String args[]){
        List<List> peerList = new ArrayList();
        
//        peers.add(Arrays.asList("129.151.173.61","6881"));
        peers.add(Arrays.asList("129.151.215.49","6881"));
//        peers.add(Arrays.asList("188.6.133.191","6881"));
//        peers.add(Arrays.asList("123.201.8.164","6881"));
        
        try{ 
        Map<String,Object> tParser = Utils.torrentParser("torrentFIles/big-buck-bunny.torrent");
        
        Utils.putBlocksInfo(tParser);
        
        Download d = new Download("torrentFIles/big-buck-bunny.torrent",peers);
        //d.downloadPeer(ip,port,tParser);
        //System.out.println("lastBlockLength : "+ tParser.get("lastBlockLength"));
        //System.out.println("lastPieceLength : "+ tParser.get("lastPieceIndexLength"));
        Download.startDownload();
        
        
        
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private static void log(String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        System.out.println(sdf.format(new Date()) + " - " + message);
    }
}
 