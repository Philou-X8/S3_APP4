import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.util.ArrayList;
import org.json.*;
public class Connection {
    private String UUID;
    private ArrayList<String> imagePackets;
    private int totalPacket;
    private Integer currentPacket;
    private int startPacket;
    private int finishPacket;
    private int errorCount;
    private String fileName;

    public Connection(DatagramPacket packet){
        byte[] packetData = packet.getData();
        String packetString = String.valueOf(packetData);


    }

    public boolean receive(DatagramPacket packet){
        byte[] packetData = packet.getData();

        return true;
    }
}

