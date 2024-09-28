
package com.spark.bittorrent;

import java.nio.*;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class BitTorrent {
    
    public static int serverPort = 998;
    public static int clientPort = 999;
    public static int buffer_size = 1024;
    

    public static void main(String[] args)throws Exception{
               
        BitTorrentWindow torrentWindow = new BitTorrentWindow();
        String torrentFilePath;
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                torrentWindow.setVisible(true);
            }
        });
        
        
        while(torrentWindow.filepath.isEmpty()){
            Thread.sleep(100);
        }
        
        torrentFilePath = torrentWindow.filepath;
       
//        torrentFilePath = "torrentFIles/sintel.torrent";
        System.out.println("FilePath: "+torrentFilePath);
                
        List<List>peers = Peers.getPeers(torrentFilePath);
       
        //downloading from peers
        Map<String,Object> tParser = Utils.torrentParser(torrentFilePath);
        Utils.putBlocksInfo(tParser);
        
        LinkedHashMap<String,Integer> fileInfoMap = Utils.getFileInfoMap(tParser);

        LinkedHashMap<String,Integer> fileOffsetMap = Utils.getFileOffsetMap(tParser);
             
        List<List> masterList = Utils.getmasterInfo(fileOffsetMap, fileInfoMap);
        
        System.out.println(masterList);
        
        Download d = new Download(torrentFilePath,peers);
        Download.startDownload();

    }
      
}

