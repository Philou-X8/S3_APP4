import java.io.*;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

import org.json.*;
public class Connection {
    private String UUID;
    private ArrayList<String> imagePackets;
    private int totalPacket;
    private Integer currentPacket;
    private Integer startPacket;
    private Integer finishPacket;
    private Integer errorCount;
    private String fileName;

    /**
     * Establish the parameters of the connexion (eg: number of packet expected)
     * @param packetString First packet of the communication received from the client
     */
    public Connection(String packetString){
        imagePackets = new ArrayList<>();

        JSONObject content = new JSONObject(packetString);

        currentPacket = (Integer)content.get("pNumber") + 1;
        startPacket = (Integer)content.get("pStart");
        finishPacket = (Integer)content.get("pEnd");
        fileName = content.get("mContent").toString();
        errorCount = 0;

    }

    /**
     * Handles the json of a newly received packet
     * @param packetString packet formated to json
     * @return True if the server is expecting more packets - False if the file is complete
     * @throws TransissionErrorException when 3 packets in a row are corrupted or miss ordered
     */
    public boolean receive(String packetString) throws TransissionErrorException{
        JSONObject content = new JSONObject(packetString);

        if((Integer)content.get("pStatus") == 1) return false; // terminate if client ask so

        Integer newPacketNumber = (Integer)content.get("pNumber");
        System.out.println("received packet number: " + newPacketNumber + ", currentPacket: " + currentPacket);
        if(newPacketNumber.equals(currentPacket)){

            imagePackets.add(currentPacket - startPacket, content.get("mContent").toString());
            currentPacket += 1;
            errorCount = 0;

        } else {
            errorCount += 1;
            System.out.println("--- error count: " + errorCount);
            if(errorCount >= 3) throw new TransissionErrorException();
        }

        return (currentPacket <= finishPacket);
    }

    /**
     * Generate the response json that will be sent to the client
     * @return json String
     */
    public String generateResponse(){

        JSONObject content = new JSONObject();
        content.put("pNumber", currentPacket);
        content.put("pStart", startPacket);
        content.put("pEnd", finishPacket);
        content.put("pStatus", currentPacket);
        content.put("mSize", 0);
        content.put("mContent", "");
        return content.toString();
    }

    /**
     * Build and save file from the received data
     */
    public void SaveFile(){
        try{
            FileWriter saveFile = new FileWriter(fileName);
            for(String s : imagePackets){
                saveFile.write(s);
                saveFile.write('\n');
            }
            saveFile.close();
            System.out.println("File Saved");

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }
}

