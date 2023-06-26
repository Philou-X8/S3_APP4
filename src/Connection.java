import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

    public Connection(String packetString){

        JSONObject content = new JSONObject(packetString);

        currentPacket = (Integer)content.get("pNumber");
        System.out.println(currentPacket);
        startPacket = (Integer)content.get("pStart");
        finishPacket = (Integer)content.get("pEnd");
        fileName = content.get("mContent").toString();


    }

    public boolean receive(String packetString){
        JSONObject content = new JSONObject(packetString);
        Integer newPacketNumber = (Integer)content.get("pNumber");
        if(newPacketNumber.equals(currentPacket)){

            imagePackets.add(content.get("mContent").toString());
            currentPacket += 1;
            return true;

        } else {

            errorCount += 1;
            return false;

        }
    }
}

