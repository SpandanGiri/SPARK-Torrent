
package com.spandan.bittorrent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    
    private static String torrentFilePath = "torrentFIles/big-buck-bunny.json";

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
    
    
    //encrypt input string using SHA1 hashing
    public static String encrypString(String input){
        try {
            // getInstance() method is called with algorithm SHA-1
            MessageDigest md = MessageDigest.getInstance("SHA-1");
 
            // digest() method is called
            // to calculate message digest of the input string
            // returned as array of byte
            byte[] messageDigest = md.digest(input.getBytes());
 
            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);
 
            // Convert message digest into hex value
            String hashtext = no.toString(16);
 
            // Add preceding 0s to make it 32 bit
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
 
            // return the HashText
            return hashtext;
        }
 
        // For specifying wrong message digest algorithms
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }    
    
    public static byte[] genId()throws Exception{
        
        //creating a random 20 byte     
        Random random = new Random();
        byte[] idByte = new byte[20];
        random.nextBytes(idByte);
             
        //copying my client id code to the random buffer
        String clientCode = "-TT0001-";
        byte[] clientCodeByte = clientCode.getBytes("UTF-8");       
        System.arraycopy(clientCodeByte,0,idByte,0,clientCodeByte.length);
             
        return clientCodeByte;
    } 
    
    public static long size() throws Exception{
        ObjectMapper objectMapper = new ObjectMapper(); 
        JsonNode jsonNode = objectMapper.readTree(new File(torrentFilePath)); 
        JsonNode infoNode = jsonNode.get("info");
        JsonNode filesNode = infoNode.get("files");
        
        long totalSize = 0;
        if (filesNode.isArray()){
            for(JsonNode fileNode : filesNode){
                totalSize += fileNode.get("length").asLong();
            }
        }
        return totalSize;
    }
    
    
}
