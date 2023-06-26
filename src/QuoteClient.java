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

import java.io.*;
import java.net.*;
import java.util.*;
public class QuoteClient {

    private boolean comsInitialized = false;
    private InetAddress addressIP;
    private int addressPort;
    private DatagramSocket socket;

    public static void main(String[] args) throws IOException {

        if (args.length < 1) {
            System.out.println("Usage: java QuoteClient <hostname>");
            return;
        }

        System.out.println("trying my init");

        //-------------------------------------------- my code
        QuoteClient client = new QuoteClient();
        client.initComs(args);
        //-------------------------------------------- my code

        System.out.println("my init done");
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
    private void sendPacket(PacketHeader header, String message) {
        try{

            // create byte array for message
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(header.getBytes(), 0, 128);
            outputStream.write(message.getBytes(), 0, header.messageSize());
            byte[] buf = outputStream.toByteArray();

            DatagramPacket packet = new DatagramPacket(buf, buf.length, addressIP, addressPort);
            this.socket.send(packet);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String receivePacket(){
        try{
            byte[] buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            this.socket.receive(packet);

            // display response
            String received = new String(packet.getData(), 0, packet.getLength());
            System.out.println("receivePacket(): " + received);
            return received;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "failed to receive packet";
    }

    private void initComs(String[] args) throws IOException{
        boolean awaitingResponse = true;
        this.socket = new DatagramSocket();
        try{

            addressIP = InetAddress.getByName(args[0]);
            addressPort = 4445;

            String fileName = args[1];
            PacketHeader header = new PacketHeader(1, 0,0,fileName.getBytes().length, "First packet");
            sendPacket(header, fileName);

            receivePacket();

            comsInitialized = true;
        }catch (IOException e){
            e.printStackTrace();
        }
        this.socket.close();

    }
}