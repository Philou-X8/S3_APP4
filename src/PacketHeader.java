import java.io.ByteArrayOutputStream;

public class PacketHeader {
    private Integer packetNumber;
    private Integer startPacket;
    private Integer endPacket;
    private Integer messageSize;
    private String headerMessage;
    public PacketHeader(
            Integer number,
            Integer start,
            Integer end,
            Integer size,
            String message)
    {
        packetNumber = number;
        startPacket = start;
        endPacket = end;
        if(size < 0){
            messageSize = 0;
        } else if(size > 128){
            messageSize = 128;
        } else {
            messageSize = size;
        }
        headerMessage = message;
    }
    public void setHeader(Integer number){
        packetNumber = number;
    }
    public byte[] getBytes(){

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(packetNumber);
        outputStream.write(startPacket);
        outputStream.write(endPacket);
        outputStream.write(messageSize);
        byte[] mappedMessage= new byte[124];
        System.arraycopy(headerMessage.getBytes(), 0, mappedMessage, 0, Math.min(124, headerMessage.getBytes().length));
        outputStream.write(mappedMessage, 0, 124);

        return outputStream.toByteArray();
    }
    public int messageSize(){

        if(messageSize < 0){
            return 0;
        } else if(messageSize > 128){
            return 128;
        } else {
            return messageSize;
        }
    }
}
