
package com.spandan.bittorrent;

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
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.util.List;

public class Utils {
    
    private static String torrentFilePath = "torrentFIles/big-buck-bunny.torrent";
    
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
    public static byte[] getSHA1Hash(String input) {
        try {
            // getInstance() method is called with algorithm SHA-1
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            // digest() method is called
            // to calculate message digest of the input string
            // returned as array of byte
            byte[] messageDigest = md.digest(input.getBytes());

            return messageDigest;
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
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
    
//    public static long size() throws Exception{
//        ObjectMapper objectMapper = new ObjectMapper(); 
//        JsonNode jsonNode = objectMapper.readTree(new File(torrentFilePath)); 
//        JsonNode infoNode = jsonNode.get("info");
//        JsonNode filesNode = infoNode.get("files");
//        
//        long totalSize = 0;
//        if (filesNode.isArray()){
//            for(JsonNode fileNode : filesNode){
//                totalSize += fileNode.get("length").asLong();
//            }
//        }
//        return totalSize;
//    }
    
    
}
