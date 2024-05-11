package UDP;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class UDPClient {
    private ActiveUsers userList;
    private DatagramSocket socket;
    private DatagramPacket packet;
    private int serverPort;
    private InetAddress serverAddress;

    public UDPClient(String address, int port) {
        userList = new ActiveUsers();
        serverPort = port;
        try {
            serverAddress = InetAddress.getByName(address);
            socket = new DatagramSocket();
            socket.setSoTimeout(1000);
        } catch (UnknownHostException e) {
            System.out.println("Error: " + e);
        } catch (SocketException e) {
            System.out.println("Error: " + e);
        }
    }

    public void work(int bufferSize) throws ClassNotFoundException {
        byte[] buffer = new byte[bufferSize];
        try {
            packet = new DatagramPacket(buffer, buffer.length, serverAddress, serverPort);
            socket.send(packet);
            System.out.println("Sending request");
            while (true) {
                packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                if (packet.getLength() == 0) break;
                ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(packet.getData(), 0, packet.getLength()));
                User user = (User) in.readObject();
                userList.add(user);
                clear(buffer);
            }
        } catch (SocketTimeoutException e) {
            System.out.println("Server is unreachable: " + e);
        } catch (IOException e) {
            System.out.println("Error: " + e);
        } finally {
            socket.close();
        }
        System.out.println("Registered users: " + userList.size());
        System.out.println(userList);
    }

    private void clear(byte[] arr) {
        for (int i = 0; i < arr.length; i++) {
            arr[i] = 0;
        }
    }

    public static void main(String[] args) throws ClassNotFoundException {
        (new UDPClient("127.0.0.1", 1501)).work(256);
    }
}