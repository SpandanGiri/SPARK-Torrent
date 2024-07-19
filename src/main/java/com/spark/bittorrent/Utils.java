
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
import static com.spark.bittorrent.Test.bytesToHex;
import com.turn.ttorrent.bcodec.BDecoder;
import com.turn.ttorrent.bcodec.BEValue;
import com.turn.ttorrent.bcodec.BEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.util.List;

public class Utils {
    
    private static String torrentFilePath = "torrentFIles/big-buck-bunny.torrent";
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
        int lastBlockLength = (pieceLength%blockLength ==0) ?    blockLength:pieceLength%blockLength;
        
        tp.put("pieceLength", pieceLength);
        tp.put("blocksPerPiece", blocksPerPiece);
        tp.put("lastPieceIndex", lastPieceIndex);
        tp.put("lastPieceIndexLength", lastPieceIndexLength);
        tp.put("lastBlockLength", lastBlockLength);
        
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
    
    
}
