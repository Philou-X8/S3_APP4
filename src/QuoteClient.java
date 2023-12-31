/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * Client
 */
public class QuoteClient {

    private boolean comsInitialized = false;
    private InetAddress addressIP;
    private int addressPort;
    private DatagramSocket socket;

    private ArrayList<String> imagePackets;
    private Integer awaitedPacket;
    private Integer startPacket;

    public static void main(String[] args) throws IOException {

        if (args.length < 1) {
            System.out.println("Usage: java QuoteClient <hostname>");
            return;
        }

        System.out.println("--- starting ---");

        //-------------------------------------------- my code

        QuoteClient client = new QuoteClient(args[1]);
        client.initComs(args);
        System.out.println("--- init complete ---");
        client.sendingComs();
        System.out.println("--- sending complete ---");

        //-------------------------------------------- my code

        // get a datagram socket
        /*
        DatagramSocket socket = new DatagramSocket();

        // send request
        byte[] buf = new byte[256];
        InetAddress address = InetAddress.getByName(args[0]);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 4445);
        socket.send(packet);

        // get response
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);

        // display response
        String received = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Quote of the Moment: " + received);

        socket.close();
        */

    }

    /**
     * Constructor
     * @param file name of file to send
     */
    public QuoteClient(String file){


        imagePackets = GetFragmentedFile(file);
        awaitedPacket = 0;
        startPacket = 1;
    }

    /**
     * Send a packet to the server
     * @param packetJson string of the Json ready to be sent
     */
    private void sendPacket(String packetJson) {
        try{

            //create crc
            packetJson = EncodeCRC(packetJson);


            byte[] buf = packetJson.getBytes(StandardCharsets.UTF_8);



            // generate error ----------------------------------
            Random rand = new Random();
            int n = rand.nextInt(3);
            if(n == 0) {
                buf[3] = 51;
                System.out.println("error generated");
            }
            // -------------------------------------------------

            DatagramPacket packet = new DatagramPacket(buf, buf.length, addressIP, addressPort);
            this.socket.send(packet);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Receive a packet from the server.
     * Returns [null] if there's been an error during communication
     * @return packet formated to JSON
     */
    private String receivePacket(){
        try{
            byte[] buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            this.socket.receive(packet);

            byte[] packetData = packet.getData();
            String packetString = new String(packetData, StandardCharsets.UTF_8);

            // remove CRC
            packetString = ValidateCRC(packetString);

            return packetString;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Establish communication with the server.
     * (Note: this communication is less resilient than data transfer's packet)
     * @param args [0]: IP address, [1]: name of file
     * @throws IOException if the socket can't be created
     */
    private void initComs(String[] args) throws IOException{
        boolean awaitingResponse = true;
        this.socket = new DatagramSocket();
        try{

            addressIP = InetAddress.getByName(args[0]);
            addressPort = 4445;

            String fileName = "Server_file.txt";
            JSONObject content = new JSONObject();
            content.put("pNumber", startPacket);
            content.put("pStart", startPacket + 1);
            content.put("pEnd", startPacket + imagePackets.size());
            content.put("pStatus", 0);
            content.put("mSize", fileName.length());
            content.put("mContent", fileName);

            String serverResponse = null;
            while(serverResponse == null){
                sendPacket(content.toString());
                serverResponse = receivePacket();
            }
            //sendPacket(content.toString());
            //PacketHeader header = new PacketHeader(1, 0,0,fileName.getBytes().length, "First packet");
            //sendPacket(header, fileName);

            //String serverResponse = receivePacket();
            JSONObject responseJSON = new JSONObject(serverResponse);
            awaitedPacket = (Integer)responseJSON.get("pStatus");


            comsInitialized = true;
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    /**
     * Send the file to server in parts
     */
    private void sendingComs(){
        boolean notDone = true;
        while(notDone){
            System.out.println("Sending packet no.: " + awaitedPacket.toString());
            sendFile(0);

            String serverResponse = receivePacket();

            if(serverResponse==null){ //CRC failed
                continue;
            }

            JSONObject responseJSON = new JSONObject(serverResponse);
            awaitedPacket = (Integer)responseJSON.get("pStatus");
            if((awaitedPacket - startPacket) > imagePackets.size()){
                notDone = false;
            }
        }
        JSONObject content = new JSONObject();
        content.put("pNumber", awaitedPacket);
        content.put("pStart", startPacket + 1);
        content.put("pEnd", startPacket + imagePackets.size());
        content.put("pStatus", 1);
        content.put("mSize", 0);
        content.put("mContent", "");
        //sendPacket(content.toString());
    }

    /**
     * Format the JSON of the packet then send it
     * @param status status message
     */
    private void sendFile(int status){

        JSONObject content = new JSONObject();
        content.put("pNumber", awaitedPacket);
        content.put("pStart", startPacket + 1);
        content.put("pEnd", startPacket + imagePackets.size());
        content.put("pStatus", status);
        content.put("mSize", imagePackets.get(awaitedPacket - startPacket - 1).length());
        content.put("mContent", imagePackets.get(awaitedPacket - startPacket - 1));
        sendPacket(content.toString());
    }

    /**
     * Split the file into shorter strings
     * @param fileName name of the file to send
     * @return split file
     */
    public ArrayList<String> GetFragmentedFile(String fileName){
        ArrayList<String> FragmentedPacket = new ArrayList<>();
        String tempCharArray = "";
        int counter = 0;
        try{
            File myObj = new File(fileName);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNext()) {
                if(counter != 20)
                {
                    tempCharArray += myReader.next();
                    counter ++;
                }else {
                    counter = 0;
                    FragmentedPacket.add(tempCharArray);
                    tempCharArray = "";
                }
            }
            myReader.close();
        }catch (Exception e) {
        }
        return FragmentedPacket;
    }

    /**
     * validate and remove the CRC from the packet
     * @param s content of the packet
     * @return [null] if the CRC failed - [Json String] otherwise
     */
    public String ValidateCRC (String s){
        if (s.isEmpty()) {
            return null;
        }

        Logs.AddLogs("Client: receive packet: " + s);

        String codeCRC  = s.substring(0, s.indexOf('{'));
        String JsonObject = s.substring(s.indexOf('{'), s.lastIndexOf('}')+1);

        Checksum crc32 = new CRC32();
        crc32.update(JsonObject.getBytes(StandardCharsets.UTF_8));

        if(Long.valueOf(codeCRC) == crc32.getValue())
        {
            return JsonObject;
        }
        Logs.AddLogs("Client: CRC Failed");
        return null;
    }

    /**
     * Add the CRC to the packet
     * @param JsonObject String of the Json
     * @return formated packet ready to be sent
     */
    public String EncodeCRC (String JsonObject){

        Checksum crc32 = new CRC32();
        crc32.update(JsonObject.getBytes(StandardCharsets.UTF_8));
        Logs.AddLogs("Client: send packet: " + crc32.getValue() + JsonObject);
        return crc32.getValue() + JsonObject;
    }
}