package com.stabilise.network.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.stabilise.util.annotation.Incomplete;

@Incomplete
public class UDPConnection {
    
    public UDPConnection() {
        
    }
    
    
    
    
    
    @SuppressWarnings("resource")
    public static void server() throws IOException {
        DatagramSocket serverSocket = new DatagramSocket(9999);
        byte[] inBuf = new byte[1024];
        byte[] outBuf = new byte[1024];
        while(true) {
            DatagramPacket inPacket = new DatagramPacket(inBuf, 1024);
            serverSocket.receive(inPacket);
            System.out.println("[SERVER] RECEIVED: " + inPacket.getData());
            InetAddress ip = inPacket.getAddress();
            int port = inPacket.getPort();
            DatagramPacket outPacket = new DatagramPacket(outBuf, 1024, ip, port);
            serverSocket.send(outPacket);
        }
    }
    
    public static void client() throws IOException {
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress ip = InetAddress.getLocalHost();
        byte[] inBuf = new byte[1024];
        byte[] outBuf = new byte[1024];
        DatagramPacket outPacket = new DatagramPacket(outBuf, 1024, ip, 9999);
        clientSocket.send(outPacket);
        DatagramPacket inPacket = new DatagramPacket(inBuf, 1024);
        clientSocket.receive(inPacket);
        System.out.println("[CLIENT] RECEIVED: " + inPacket.getData());
        clientSocket.close();
    }
    
    private static void doServer() {
        try {
            server();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void doClient() {
        try {
            client();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        new Thread(() -> doServer()).start();
        new Thread(() -> doClient()).start();
        new Thread(() -> doClient()).start();
        new Thread(() -> doClient()).start();
        new Thread(() -> doClient()).start();
    }
    
}
