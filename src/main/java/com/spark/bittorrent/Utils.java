
package com.spark.bittorrent;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.logging.*;
import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.BencodeInputStream;
import com.dampcake.bencode.Type;
import com.turn.ttorrent.bcodec.BDecoder;
import com.turn.ttorrent.bcodec.BEValue;
import com.turn.ttorrent.bcodec.BEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.List;
import java.io.IOException;  
import java.io.RandomAccessFile; 
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;

public class Utils {
    
    public static int blockLength = 16384;
    
    //returns Torrent Parser Object contains info about torrent file
    public static Map<String,Object> torrentParser(String torrentFilePath) throws Exception{
        
            File torrentFile = new File(torrentFilePath);
            
            FileInputStream fl = new FileInputStream(torrentFile);
            byte[] byteArr = new byte[(int)torrentFile.length()];            
            
            fl.read(byteArr);
            fl.close();
            
            ByteArrayInputStream in = new ByteArrayInputStream(byteArr);
            BencodeInputStream bencode = new BencodeInputStream(in);

            Type type = bencode.nextType(); // Returns Type.DICTIONARY
            Map<String, Object> torrentParser = bencode.readDictionary();
            
            return torrentParser;
    }
    
    //returns the protocol of the url
    public static String extractProtocol(String url) {
        String regex = "^(udp|http|https|wss)://";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);  // This will return the protocol (e.g., "udp", "http", "https", "wss")
        } else {
            return null;  // Return null if no protocol is found
        }
    }
    
    //returns only the hostname from the announce url
    public static String extractHostname(String url) {
        String regex = "^(?:udp|http|https)://([^:/]+)(?::\\d+)?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }
    
    public static String extractPort(String url) {
        String regex = "^(?:udp|http|https)://[^:/]+(?::(\\d+))?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find() && matcher.group(1) != null) {
            return matcher.group(1);
        } else {
            return null;
        }
    }
    
    //hash string in sha1    
    public static byte[] getSHA1Hash(byte[] input) {
        try {
           
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return md.digest(input);          
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /*
    returns the infoHash of the the torrent File using com.turn.ttorrent.bcodec 
    Created a sha1 hash of the info part of the torrent file
    */
    
    public static byte[] getInfoHash(String torrentFilePath) throws Exception{
            
            // Read the torrent file
            FileInputStream torrentInputStream = new FileInputStream(torrentFilePath);
            byte[] torrentData = torrentInputStream.readAllBytes();
            torrentInputStream.close();

            // Decode the torrent file
            
            Map<String, BEValue> torrentMap = BDecoder.bdecode(new ByteArrayInputStream(torrentData)).getMap();
            
            
            // Extract the 'info' dictionary
            BEValue infoValue = torrentMap.get("info");
            
            // Bencode the 'info' dictionary
            ByteArrayOutputStream encodedInfoStream = new ByteArrayOutputStream();
            BEncoder.bencode(infoValue, encodedInfoStream);
            byte[] encodedInfo = encodedInfoStream.toByteArray();
            
            byte[] infoHash = Utils.getSHA1Hash(encodedInfo);
            System.out.println("Info Hash: " + bytesToHex(infoHash));
            return infoHash;
            
    }
    
    public static byte[] genId()throws Exception{
        
        //creating a random 20 byte     
        Random random = new Random();
        byte[] idByte = new byte[20];
        random.nextBytes(idByte);
             
        //copying my client id code to the random buffer
        String clientCode = "-TT0001-";
        byte[] clientCodeByte = clientCode.getBytes("UTF-8");       
        System.arraycopy(clientCodeByte,0,idByte,0,clientCodeByte.length);
             
        return clientCodeByte;
    } 
    
    //Returns the total size of all the files in bytes
    public static long size(Map<String,Object>tp) {
        long size = 0;
        Object infoObject = tp.get("info");
        if (infoObject instanceof Map) {
            Map<String, Object> infoMap = (Map<String, Object>) infoObject;
            Object filesObject = infoMap.get("files");
            
            if (filesObject instanceof List) {
                List<Map<String, Object>> filesList = (List<Map<String, Object>>) filesObject;
                
                for (Map<String, Object> file : filesList) {
                    Long length = (Long) file.get("length");
                    size +=length;
                }
            }
        }
        System.out.println(size);
        return size;
    }
    
    //directly puts the blocks and pieces info in torrent parser Map
    public static Map<String,Object> putBlocksInfo(Map<String,Object>tp){
        int totalSize = (int)size(tp);
        int pieceLength = 0 ;
        Object infoObject = tp.get("info");
        
        if (infoObject instanceof Map) {
            Map<String,Object> infoMap = (Map<String,Object>) infoObject;
            pieceLength = Integer.parseInt(infoMap.get("piece length").toString());   
        }
        
        int blocksPerPiece = pieceLength/blockLength;
        int lastPieceIndex = (int)Math.ceil(totalSize/pieceLength);
        
        int lastPieceIndexLength = (totalSize%pieceLength == 0)? pieceLength:totalSize%pieceLength;
      //  int lastBlockLength = (pieceLength%blockLength ==0) ?    blockLength:pieceLength%blockLength;
      
        System.out.println(totalSize + " "+ pieceLength +" " );
      
        int lastBlockLength = (lastPieceIndexLength %blockLength ==0) ?    blockLength:lastPieceIndexLength%blockLength;
        int lastBlockIndex = totalSize/blockLength;
        
        tp.put("pieceLength", pieceLength);
        tp.put("blocksPerPiece", blocksPerPiece);
        tp.put("lastPieceIndex", lastPieceIndex);
        tp.put("lastBlockIndex",lastBlockIndex);
        tp.put("lastPieceIndexLength", lastPieceIndexLength);
        tp.put("lastBlockLength", lastBlockLength);
        
        //tp.put("lastBlockLength", 16384); 
   
        
        return tp;
        
    }
    
    
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    
    public static String IpFromBytes(byte[] ipByte){
        String ip = (ipByte[0] & 0xFF) + "." +
                     (ipByte[1] & 0xFF) + "." +
                     (ipByte[2] & 0xFF) + "." +
                     (ipByte[3] & 0xFF);
        
        return ip;
    }
    
    //copies bytes to the traget file strating from offset , ued in writing the file 
    public static void writeBytesAtOffset(String filePath,byte[] data,int offset){
        try {  
            RandomAccessFile file = new RandomAccessFile(filePath, "rw");  
            file.seek(offset);  
            file.write(data);  
            file.close();     
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
        
    //saves the filename and cummulative file ending in Map
    //Linked Hash Map is used to maintain the order
    public static LinkedHashMap<String,Integer> getFileInfoMap(Map<String,Object> tp){
        
        LinkedHashMap<String, Integer> fileInfo = new LinkedHashMap<String, Integer>();
 
        Object infoObject = tp.get("info");
        if (infoObject instanceof Map) {
            Map<String, Object> infoMap = (Map<String, Object>) infoObject;
            Object filesObject = infoMap.get("files");
            
            if (filesObject instanceof List) {
                List<Map<String, Object>> filesList = (List<Map<String, Object>>) filesObject;
                
                int cumFileLength = 0;
                for (Map<String, Object> file : filesList) {     
                    Object pathObject = file.get("path");

                    String filename = "";
                    if (pathObject instanceof List) {
                        List<String> pathSegments = (List<String>) pathObject;
                        
                        filename = String.join("/", pathSegments);
                    } else {
                        filename = pathObject.toString();
                    }

                    Integer fileLength = Integer.parseInt(file.get("length").toString());
                    cumFileLength +=fileLength;
                    fileInfo.put(filename, cumFileLength);
                }
            }
        }
        return fileInfo;
    }
    
    //saves the filename and cummulative file  starting in Map 
    //Linked Hash Map is used to maintain the order
    public static LinkedHashMap<String,Integer> getFileOffsetMap(Map<String,Object> tp){
        LinkedHashMap<String, Integer> fileInfo = new LinkedHashMap<String, Integer>();
 
        Object infoObject = tp.get("info");
        if (infoObject instanceof Map) {
            Map<String, Object> infoMap = (Map<String, Object>) infoObject;
            Object filesObject = infoMap.get("files");
            
            if (filesObject instanceof List) {
                List<Map<String, Object>> filesList = (List<Map<String, Object>>) filesObject;
                
                int cumFileLength = 0;
                for (Map<String, Object> file : filesList) {     
                    Object pathObject = file.get("path");

                    String filename = "";
                    if (pathObject instanceof List) {
                        List<String> pathSegments = (List<String>) pathObject;
                        
                        filename = String.join("/", pathSegments);
                    } else {
                        filename = pathObject.toString();
                    }

                    Integer fileLength = Integer.parseInt(file.get("length").toString());        
                    fileInfo.put(filename, cumFileLength);
                    cumFileLength +=fileLength;
                }
            }
        }
        return fileInfo;
    }
    
    //combins the fileoffset and fileINfo map 
    //will comine the getFIleInfoMap and getFIleOffsetMap to this function
    public static List<List> getmasterInfo(LinkedHashMap<String, Integer>fileOffset,LinkedHashMap<String, Integer>fileInfo){
        List<List> master= new ArrayList<List>();
        
        for(Map.Entry<String,Integer>entry:fileOffset.entrySet()){
            
            List list = new ArrayList<>();
            //l.add(entry.getKey());
            list.add(entry.getValue());
            master.add(list);
        }
        
        int index=0;
        for(Map.Entry<String,Integer>entry:fileInfo.entrySet()){
            
            List list = master.get(index++);
            list.add(entry.getValue());
            list.add(entry.getKey());
        }
        return master;
    }
    
}
