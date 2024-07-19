
package com.spark.bittorrent;

import java.nio.*;
import java.util.List;


public class BitTorrent {
    
    public static int serverPort = 998;
    public static int clientPort = 999;
    public static int buffer_size = 1024;
    

    public static void main(String[] args)throws Exception{
        
        String torrentFilePath = "torrentFIles/big-buck-bunny.torrent";
        String torrentFilePath2 = "torrentFIles/edubuntu-23.10-desktop-amd64.iso.torrent";
        

        Peers.getPeers(torrentFilePath);
        //HttpTrackerClient.getPeers("http://torrent.ubuntu.com:6969/announce");

        
        //downloading from peers
        //Download.download(peers);
        
    
    }
      
}

