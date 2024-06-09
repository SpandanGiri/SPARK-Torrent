
package com.spandan.bittorrent;

import java.io.File; 
import java.net.MalformedURLException;
import java.net.URL;
import java.io.IOException; 
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {

    public static String extractHostnameHelper(String url) {
        String regex = "^(?:udp|http|https)://([^:/]+)(?::\\d+)?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }
    
        public static void main(String[] args) {
        String inputUrl = "udp://tracker.leechers-paradise.org:6969";
        String hostname = extractHostnameHelper(inputUrl);
        
        
        
        if (hostname != null) {
            System.out.println("Hostname: " + hostname);
        } else {
            System.out.println("Invalid URL");
        }
        }
}
