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

public class QuoteServerThread extends Thread {

    protected DatagramSocket socket = null;
    protected BufferedReader in = null;
    protected boolean moreQuotes = true;

    private InetAddress addressIP;
    private int addressPort;

    public QuoteServerThread() throws IOException {
        this("QuoteServerThread");
    }

    public QuoteServerThread(String name) throws IOException {
        super(name);
        socket = new DatagramSocket(4445);

        try {
            in = new BufferedReader(new FileReader("one-liners.txt"));
        } catch (FileNotFoundException e) {
            System.err.println("Could not open quote file. Serving time instead.");
        }
    }

    public void run() {

        while (moreQuotes) {
            String packetContent = receivePacket();
            Connection connection = new Connection(packetContent);

            sendPacket(connection.generateResponse());

            /*
            loop the reception of packets here
             */
            boolean FileRemaining = true;
            while(FileRemaining){
                String filePacket = receivePacket();
                try{
                    FileRemaining = connection.receive(filePacket);
                } catch (TransissionErrorException e) {
                    break;
                }
                System.out.println("file remaining");
                sendPacket(connection.generateResponse());
                System.out.println("response sent from server");
            }
            connection.SaveFile();


            /*
            try {

                byte[] buf = new byte[256];

                // receive request
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                System.out.println("packet received on server");

                // figure out response
                String dString = null;
                if (in == null) {
                    dString = new Date().toString();
                }
                else {
                    dString = getNextQuote();
                }

                buf = dString.getBytes();

                // send the response to the client at "address" and "port"
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                packet = new DatagramPacket(buf, buf.length, address, port);

                System.out.println("about to send packet on server");
                socket.send(packet);
                System.out.println("about to send packet on server -- done");

            } catch (IOException e) {
                e.printStackTrace();
                moreQuotes = false;

            }
            */

        }
    }

    protected String getNextQuote() {
        String returnValue = null;
        try {
            if ((returnValue = in.readLine()) == null) {
                in.close();
                moreQuotes = false;
                returnValue = "No more quotes. Goodbye.";
            }
        } catch (IOException e) {
            returnValue = "IOException occurred in server.";
        }
        return returnValue;
    }


    private String receivePacket(){
        try{
            byte[] buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            this.socket.receive(packet);
            this.addressIP = packet.getAddress();
            this.addressPort = packet.getPort();

            byte[] packetData = packet.getData();
            String packetString = new String(packetData, StandardCharsets.UTF_8);

            // remove CRC
            packetString = ValidateCRC(packetString);

            System.out.println("receivePacket(): " + packetString);
            return packetString;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void sendPacket(String packetString) {
        try{

            //create crc
            packetString = EncodeCRC(packetString);

            byte[] buf = packetString.getBytes(StandardCharsets.UTF_8);


            DatagramPacket packet = new DatagramPacket(buf, buf.length, this.addressIP, this.addressPort);
            this.socket.send(packet);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String ValidateCRC (String s){
        if (s.isEmpty()) {
            return null;
        }

        String codeCRC  = s.substring(0, s.indexOf('{'));
        String JsonObject = s.substring(s.indexOf('{'), s.lastIndexOf('}')+1);

        Checksum crc32 = new CRC32();
        crc32.update(JsonObject.getBytes(StandardCharsets.UTF_8));

        if(Long.valueOf(codeCRC) == crc32.getValue())
        {
            return JsonObject;
        }
        return null;
    }

    public String EncodeCRC (String JsonObject){
        Checksum crc32 = new CRC32();
        crc32.update(JsonObject.getBytes(StandardCharsets.UTF_8));
        return crc32.getValue() + JsonObject;
    }

}

