
package com.spark.bittorrent;
import java.nio.ByteBuffer;
/**
 *
 * @author Spandan
 */
public class Message {
    
    public static String torrentFilePath= "torrentFIles/big-buck-bunny.torrent";
    
    public static byte[] buildHandShake() throws Exception{
        ByteBuffer bb = ByteBuffer.allocate(68);
        
        //protocol string length
        bb.put((byte) 19);
        
        //protocol string
        String prtlstr = "BitTorrent protocol";
        bb.put(1, prtlstr.getBytes());
        
        //reserved bytes
        bb.putLong(20,0);
     
        //info Hash
        byte[] infoHashByte = Utils.getInfoHash(torrentFilePath);
        bb.put(28,infoHashByte);
        
        //peer-id
        bb.put(48,Utils.genId());
        
        return bb.array();
    }
    
/*
    index > piece index
    begin > strating index of block
    length> length of block
    
    */   

public static byte[] buildRequest(int index, int begin, int length) {
    ByteBuffer bb = ByteBuffer.allocate(17);
    
    // length prefix (4 bytes)
    bb.putInt(13);
    
    // message ID (1 byte)
    bb.put((byte) 6);
    
    // index (4 bytes)
    bb.putInt(index);
    
    // begin (4 bytes)
    bb.putInt(begin);
    
    // length (4 bytes)
    bb.putInt(length);

    return bb.array();
}
    
    public static byte[] buildPiece(int blockLength,int index,int begin,byte[] block){
        
        //length
        ByteBuffer bb = ByteBuffer.allocate(9+blockLength);
        
        //id
        bb.put(4,(byte)7);
        
        //index
        bb.putInt(5,index);
         
        //begin
        bb.putInt(9,begin);
        
        bb.put(13,block);
        
        return bb.array();
    }
    
    public static byte[] buildChoke(){
        ByteBuffer bb = ByteBuffer.allocate(5);
        
        //length
        bb.putInt(0,1);
        
        //id
        bb.put(4,(byte)0);
        return bb.array();
    }
    
    public static byte[] buildUnChoke(){
        ByteBuffer bb = ByteBuffer.allocate(5);
        
        //length
        bb.putInt(0,1);
        
        //id
        bb.put(4,(byte)1);
        return bb.array();
    }
    
    public static byte[] buildInterested(){
        ByteBuffer bb = ByteBuffer.allocate(5);
        
        //length
        bb.putInt(0,1);
        
        //id
        bb.put(4,(byte)2);
        
        System.out.println("HandShake Req:");

        return bb.array();
    }
    
    public static byte[] buildNotInterested(){
        ByteBuffer bb = ByteBuffer.allocate(5);
        
        //length
        bb.putInt(0,1);
        
        //id
        bb.put(4,(byte)3);
        return bb.array();
    }
    
    public static byte[] buildHave(int payload){
        ByteBuffer bb = ByteBuffer.allocate(6);
        
        //length
        bb.putInt(0,5);
        
        //id
        bb.put(4,(byte)4);
        
        //piece index
        bb.putInt(5,payload);
        
        return bb.array();
    }
    
    public static byte[] buildCancel(int payload){
        ByteBuffer bb = ByteBuffer.allocate(17);
        //length
        bb.putInt(0,13);
        
        //id=8
        bb.put(4,(byte)8);
        
        //piece index
        bb.putInt(5,payload);
        
        //
                
        
        return bb.array();
    }
    
    public static byte[] buildPort(){
        
        ByteBuffer bb = ByteBuffer.allocate(7);
        
        //length
        bb.putInt(7);
        
        //id 
        bb.putInt(4,9);
        
         
        
        
        return bb.array();
    }
    
    
}
