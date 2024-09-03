
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
        Map<String,Object> tParser = Utils.torrentParser("torrentFIles/ubuntu-16.04.1-server-amd64.iso.torrent");
        
 
        String announce_url = Utils.extractHostname(tParser.get("announce").toString());
        
        System.out.println(announce_url);
        
    }
}
