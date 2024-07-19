
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
        String torrentFilePath2 = "torrentFIles/edubuntu-23.10-desktop-amd64.iso.torrent";
        
        //List<List> peers = Peers.getPeers(torrentFilePath);
        HttpTrackerClient.getPeers("http://torrent.ubuntu.com:6969/announce");
        
        
        //downloading from peers
        //Download.download(peers);
        
        Map<String,Object> tParser = Utils.torrentParser("torrentFIles/big-buck-bunny.torrent");
        
        Utils.putBlocksInfo(tParser);
        int lastBlockLen = Integer.parseInt(tParser.get("lastBlockLength").toString());
        System.out.println(lastBlockLen);
        
    
    }
      
}

