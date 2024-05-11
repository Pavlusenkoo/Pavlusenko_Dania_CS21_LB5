package UDP;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPServer {
    private ActiveUsers userList;
    private DatagramSocket socket;
    private DatagramPacket packet;
    private InetAddress address;
    private int port;

    public UDPServer(int serverPort) {
        try {
            socket = new DatagramSocket(serverPort);
        } catch (SocketException e) {
            System.out.println("Error: " + e);
        }
        userList = new ActiveUsers();
    }

    public void work(int bufferSize) {
        try {
            System.out.println("Server started...");
            while (true) {
                getUserData(bufferSize);
                log(address, port);
                sendUserData();
            }
        } catch (IOException e) {
            System.out.println("Error: " + e);
        } finally {
            System.out.println("Server ended...");
            socket.close();
        }
    }

    private void log(InetAddress address, int port) {
        System.out.println("Request from: " + address.getHostAddress() + " port: " + port);
    }

    private void clear(byte[] arr) {
        for (int i = 0; i < arr.length; i++) {
            arr[i] = 0;
        }
    }

    private void getUserData(int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        address = packet.getAddress();
        port = packet.getPort();
        User user = new User(address, port);
        if (userList.isEmpty()) {
            userList.add(user);
        } else if (!userList.contains(user)) {
            userList.add(user);
        }
        clear(buffer);
    }

    private void sendUserData() throws IOException {
        byte[] buffer;
        for (int i = 0; i < userList.size(); i++) {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bout);
            out.writeObject(userList.get(i));
            buffer = bout.toByteArray();
            packet = new DatagramPacket(buffer, buffer.length, address, port);
            socket.send(packet);
        }
        buffer = "end".getBytes();
        packet = new DatagramPacket(buffer, 0, address, port);
        socket.send(packet);
    }

    public static void main(String[] args) {
        (new UDPServer(1501)).work(256);
    }
}