
package com.spark.bittorrent;

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
        Map<String,Object> tp = Utils.torrentParser("torrentFIles/big-buck-bunny.torrent");
        
        Map<String,Integer> fileInfo = new HashMap<String,Integer>();
 
        Object infoObject = tp.get("info");
        if (infoObject instanceof Map) {
            Map<String, Object> infoMap = (Map<String, Object>) infoObject;
            Object filesObject = infoMap.get("files");
            
            if (filesObject instanceof List) {
                List<Map<String, Object>> filesList = (List<Map<String, Object>>) filesObject;
                
                for (Map<String, Object> file : filesList) {     
                    String filename = file.get("path").toString();
                    Integer fileLength = Integer.parseInt(file.get("length").toString());
                    //System.out.println(file);
                    
                    fileInfo.put(filename, fileLength);
                }
            }
        }
        
        System.out.println(fileInfo);
    }
}
