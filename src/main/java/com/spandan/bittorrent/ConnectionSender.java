package com.spandan.bittorrent;

import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

public class ConnectionSender {

    public static void UdpSend(DatagramSocket ds, String url, int port) throws Exception
    {
        InetAddress addr = InetAddress.getByName(url);
        byte[] connBuffer = createReqCon();

        DatagramPacket packet = new DatagramPacket(connBuffer, connBuffer.length, addr, port);
        ds.send(packet);
    }

    public static void HttpSend(String urlString) throws Exception
    {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        byte[] connBuffer = createReqCon();

        try (OutputStream os = conn.getOutputStream()) {
            os.write(connBuffer, 0, connBuffer.length);
            os.flush();
        }

        int responseCode = conn.getResponseCode();
        System.out.println("HTTP Response Code: " + responseCode);

        conn.disconnect();
    }

    private static byte[] createReqCon() {
        // TODO: Implement your logic to create the request content.
        // This is a placeholder implementation.
        return "Your request data".getBytes();
    }

    public static void main(String[] args) {
        try {
            DatagramSocket ds = new DatagramSocket();
            UdpSend(ds, "E:\\SPARK Torrent\\SPARK-Torrent\\torrentFIles\\big-buck-bunny.torrent", 12345);

            HttpSend("http://example.com");
            HttpSend("https://example.com");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
