import java.io.Serializable;

public class SerialTransport implements Serializable {
    private int packetNumber;

    public SerialTransport(){
        packetNumber = 1;
    }
}
