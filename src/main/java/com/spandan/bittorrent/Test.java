
package com.spandan.bittorrent;

import java.io.*; 
import java.net.MalformedURLException;
import java.net.URL;
import java.io.IOException; 
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;
import com.dampcake.bencode.*;


public class Test {

    
        public static void main(String[] args) throws Exception{
            
            String torrentFilePath = "torrentFIles/big-buck-bunny.torrent";
            File torrentFile = new File(torrentFilePath);
            
            FileInputStream fl = new FileInputStream(torrentFile);
            byte[] byteArr = new byte[(int)torrentFile.length()];
            
            fl.read(byteArr);
            fl.close();
            
            //System.out.println(byteArr);
            
            ByteArrayInputStream in = new ByteArrayInputStream(byteArr);
            BencodeInputStream bencode = new BencodeInputStream(in);

            Type type = bencode.nextType(); // Returns Type.DICTIONARY
            Map<String, Object> dict = bencode.readDictionary();
            
            String announce = dict.get("announce").toString();
            
            String port = Utils.extractPort(announce);
         
            System.out.println(Integer.parseInt(port));
            
            Object infoObject = dict.get("info");
        if (infoObject instanceof Map) {
            Map<String, Object> infoMap = (Map<String, Object>) infoObject;
            Object filesObject = infoMap.get("files");
            
            if (filesObject instanceof List) {
                List<Map<String, Object>> filesList = (List<Map<String, Object>>) filesObject;
                
                for (Map<String, Object> file : filesList) {
                    Long length = (Long) file.get("length");
                    List<String> path = (List<String>) file.get("path");
                    
                    System.out.println("File length: " + length);
                    System.out.println("File path: " + String.join("/", path));
                }
            }
        }
            
//            for(Map.Entry<String,Object> e:dict.entrySet())
//                    System.out.println("key: "+e.getKey() + "value" + e.getValue());
        }
}
