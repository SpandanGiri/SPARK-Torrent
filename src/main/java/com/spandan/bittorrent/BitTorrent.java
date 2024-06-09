
package com.spandan.bittorrent;

import java.nio.*;

public class BitTorrent {
    
    public static int serverPort = 998;
    public static int clientPort = 999;
    public static int buffer_size = 1024;
    

    public static void main(String[] args)throws Exception{
        //Peers peers = new Peers();
        String torrentFilePath = "torrentFIles/big-buck-bunny.json";
        String url2 = "tracker.opentrackr.org";
        String url = "tracker.leechers-paradise.org";
        
        int port = 1337;
        int port2 = 6969; 
        
        Peers.getPeers(torrentFilePath);
    
    }
      
}

