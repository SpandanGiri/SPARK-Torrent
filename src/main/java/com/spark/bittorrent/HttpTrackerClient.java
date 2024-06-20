
package com.spark.bittorrent;

import com.turn.ttorrent.bcodec.BDecoder;
import com.turn.ttorrent.bcodec.BEValue;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class HttpTrackerClient {

    public static List<List> getPeers(String trackerUrl) {
        try {
            //trackerUrl = "http://torrent.ubuntu.com:6969/announce";
            trackerUrl = "wss://wstracker.online";
            //String infoHash = "dd8255ecdc7ca55fb0bbf81323d87062db1f6d1c";
            String infoHash = "c99047a907bf9941365c6a1974bc142a181460ad";
            String peerId = "-TR2940-6wfG2wk6wFOu";  // Example peer ID, use a unique one for your client
            int port = 6881;
            long uploaded = 0;
            long downloaded = 0;
            long left = 1000000;  // Example value, should be the size of the file(s) you have yet to download

            String encodedInfoHash = URLEncoder.encode(new String(hexStringToByteArray(infoHash), StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1.toString());
            String encodedPeerId = URLEncoder.encode(peerId, StandardCharsets.ISO_8859_1.toString());

            String urlString = String.format(
                    "%s?info_hash=%s&peer_id=%s&port=%d&uploaded=%d&downloaded=%d&left=%d",
                    trackerUrl,
                    encodedInfoHash,
                    encodedPeerId,
                    port,
                    uploaded,
                    downloaded,
                    left
            );

            System.out.println("Request URL: " + urlString);  

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            // Close connections
            in.close();
            conn.disconnect();

            System.out.println("Tracker Response: " + content.toString());

            // Parse the response using BDecoder
            
            ByteArrayInputStream responseStream = new ByteArrayInputStream(content.toString().getBytes(StandardCharsets.ISO_8859_1));
            BDecoder bDecoder = new BDecoder(responseStream);
            Map<String, BEValue> response = bDecoder.bdecode().getMap();
 
            List<BEValue> peers = response.get("peers").getList();
            List<List> peerList = new ArrayList();

            for(BEValue peer:peers){
                Map<String, BEValue> peerMap = peer.getMap();
                String ip = peerMap.get("ip").getString();
                String peerPort = peerMap.get("port").getString();      
                
                List<String> ipList = new ArrayList();
                ipList.add(ip);
                ipList.add(peerPort);
                
                peerList.add(ipList);
                               
            }
            System.out.println(peerList);
            return peerList;
           
        }

         catch (Exception e) {
            e.printStackTrace();
        }
        
        return List.of();
    }

    // Utility function to convert hex string to byte array
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
