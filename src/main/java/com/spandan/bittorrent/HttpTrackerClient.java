package com.spandan.bittorrent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.Type;

public class HttpTrackerClient {

    public static void main(String[] args) {
        try {
            String trackerUrl = "http://torrent.ubuntu.com:6969/announce";
            String infoHash = "c99047a907bf9941365c6a1974bc142a181460ad";
            String peerId = "-TR2940-6wfG2wk6wFOu";  // Example peer ID, use a unique one for your client
            int port = 6881;
            long uploaded = 0;
            long downloaded = 0;
            long left = 1000000;  // Example value, should be the size of the file(s) you have yet to download

            String encodedInfoHash = URLEncoder.encode(new String(hexStringToByteArray(infoHash), StandardCharsets.ISO_8859_1), StandardCharsets.ISO_8859_1.toString());
            String encodedPeerId = URLEncoder.encode(peerId, StandardCharsets.ISO_8859_1.toString());

            String urlString = String.format(
                    "%s?info_hash=%s&peer_id=%s&port=%d&uploaded=%d&downloaded=%d&left=%d&compact=1",
                    trackerUrl,
                    encodedInfoHash,
                    encodedPeerId,
                    port,
                    uploaded,
                    downloaded,
                    left
            );

            System.out.println("Request URL: " + urlString);  // Print the URL for debugging

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

            // Parse the response using Bencode
            Bencode bencode = new Bencode();
            Map<String, Object> response = bencode.decode(content.toString().getBytes(StandardCharsets.ISO_8859_1), Type.DICTIONARY);

            // Extract peer information
            if (response.containsKey("peers")) {
                Object peersObj = response.get("peers");
                if (peersObj instanceof List) {
                    List<Map<String, Object>> peers = (List<Map<String, Object>>) peersObj;
                    for (Map<String, Object> peer : peers) {
                        String peerIp = new String((byte[]) peer.get("ip"), StandardCharsets.ISO_8859_1);
                        int peerPort = ((Number) peer.get("port")).intValue();
                        System.out.println("Peer IP: " + peerIp + ", Peer Port: " + peerPort);
                    }
                } else if (peersObj instanceof byte[]) {
                    byte[] peersBytes = (byte[]) peersObj;
                    for (int i = 0; i < peersBytes.length; i += 6) {
                        String peerIp = (peersBytes[i] & 0xFF) + "." + (peersBytes[i + 1] & 0xFF) + "." + (peersBytes[i + 2] & 0xFF) + "." + (peersBytes[i + 3] & 0xFF);
                        int peerPort = ((peersBytes[i + 4] & 0xFF) << 8) | (peersBytes[i + 5] & 0xFF);
                        System.out.println("Peer IP: " + peerIp + ", Peer Port: " + peerPort);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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
