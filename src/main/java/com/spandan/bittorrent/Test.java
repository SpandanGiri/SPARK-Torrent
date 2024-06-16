
package com.spandan.bittorrent;

import java.io.*; 
import java.net.MalformedURLException;
import java.net.URL;
import java.io.IOException; 
import java.util.*;
import com.dampcake.bencode.*;
import java.nio.charset.StandardCharsets;
import com.turn.ttorrent.bcodec.BEValue;
import com.turn.ttorrent.bcodec.BEncoder;
import com.turn.ttorrent.bcodec.BDecoder;



public class Test {
    
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
        public static void main(String[] args) throws Exception{
        try {
            String torrentFilePath = "torrentFiles/big-buck-bunny.torrent";
            String infoFilePath = "info.txt";

            // Read the torrent file
            FileInputStream torrentInputStream = new FileInputStream(torrentFilePath);
            byte[] torrentData = torrentInputStream.readAllBytes();
            torrentInputStream.close();

            // Decode the torrent file
            
            Map<String, BEValue> torrentMap = BDecoder.bdecode(new ByteArrayInputStream(torrentData)).getMap();
            
            // Extract the 'info' dictionary
            BEValue infoValue = torrentMap.get("info");
            Map<String, BEValue> infoMap = infoValue.getMap();
            

            // Bencode the 'info' dictionary
            ByteArrayOutputStream encodedInfoStream = new ByteArrayOutputStream();
            BEncoder.bencode(infoValue, encodedInfoStream);
            byte[] encodedInfo = encodedInfoStream.toByteArray();


            // For testing: Print the info hash
            byte[] infoHash = Utils.getSHA1Hash(encodedInfo);
            System.out.println("Info Hash: " + bytesToHex(infoHash));

        } catch (Exception e) {
            e.printStackTrace();
        }
            
            
            
            
            
            
            
            
            
            
            
            
//            for(Map.Entry<String,Object> e:dict.entrySet())
//                    System.out.println(e.getKey() + " : " + e.getValue());
            
//            String announce = dict.get("announce").toString();
//            
//            String port = Utils.extractPort(announce);
//            
//            System.out.println(Integer.parseInt(port));
//            
//            Object infoObject = dict.get("info");
//            
//            
//        if (infoObject instanceof Map) {
//            Map<String, Object> infoMap = (Map<String, Object>) infoObject;
//            Object filesObject = infoMap.get("files");
//            
//            if (filesObject instanceof List) {
//                List<Map<String, Object>> filesList = (List<Map<String, Object>>) filesObject;
//                
//                for (Map<String, Object> file : filesList) {
//                    Long length = (Long) file.get("length");
//                    List<String> path = (List<String>) file.get("path");
//                    
//                    System.out.println("File length: " + length);
//                    System.out.println("File path: " + String.join("/", path));
//                }
//            }
//        }
            
//            for(Map.Entry<String,Object> e:dict.entrySet())
//                    System.out.println("key: "+e.getKey() + "value" + e.getValue());
        }
}
