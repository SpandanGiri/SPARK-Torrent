
package com.spark.bittorrent;

import java.nio.*;
import java.util.List;
import java.util.Map;


public class BitTorrent {
    
    public static int serverPort = 998;
    public static int clientPort = 999;
    public static int buffer_size = 1024;
    

    public static void main(String[] args)throws Exception{
               
        String torrentFilePath = "torrentFIles/big-buck-bunny.torrent";
        String torrentFilePath2 = "torrentFIles/ubuntu-16.04.1-server-amd64.iso.torrent";
              
        //List<List>peers = Peers.getPeers(torrentFilePath);
       
        //downloading from peers
        
        Map<String,Object> tParser = Utils.torrentParser(torrentFilePath);
        Utils.putBlocksInfo(tParser);
        
        Map<String,Integer> fileInfoMap = Utils.getFileInfoMap(tParser);
        System.out.println(fileInfoMap);
        
        Map<String,Integer> fileOffsetMap = Utils.getFileOffsetMap(tParser);
        System.out.println(fileOffsetMap);
        
        
//        Download d = new Download(torrentFilePath,peers);
//        Download.startDownload();

    }
      
}

