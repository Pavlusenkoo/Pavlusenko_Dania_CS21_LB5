package server;

import interfaces.Executable;
import interfaces.Result;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends JFrame {
    private JTextArea logArea;
    private JTextField portField;
    private ServerSocket serverSocket;
    private boolean isRunning;

    public Server() {
        setTitle("TCP Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JPanel portPanel = new JPanel();
        portPanel.add(new JLabel("Working Port:"));
        portField = new JTextField(6);
        portField.setText("12345");
        portPanel.add(portField);
        mainPanel.add(portPanel);

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        mainPanel.add(scrollPane);

        JPanel buttonPanel = new JPanel();
        JButton startButton = new JButton("Start Server");
        startButton.addActionListener(e -> startServer());
        buttonPanel.add(startButton);

        JButton stopButton = new JButton("Stop Server");
        stopButton.addActionListener(e -> stopServer());
        stopButton.setEnabled(false);
        buttonPanel.add(stopButton);

        JButton exitButton = new JButton("Exit Server");
        exitButton.addActionListener(e -> System.exit(0));
        buttonPanel.add(exitButton);

        mainPanel.add(buttonPanel);
        add(mainPanel);
        setVisible(true);
    }

    private void startServer() {
        try {
            int port = Integer.parseInt(portField.getText());
            serverSocket = new ServerSocket(port);
            isRunning = true;
            logArea.append("The server is waiting for connections...\n");

            new Thread(() -> {
                while (isRunning) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        logArea.append("Connection " + clientSocket.getInetAddress().getHostAddress() + " starting execution.\n");
                        handleClient(clientSocket);
                        logArea.append("Connection " + clientSocket.getInetAddress().getHostAddress() + " finished execution.\n");
                    } catch (IOException e) {
                        logArea.append("Error: " + e.getMessage() + "\n");
                    }
                }
            }).start();
        } catch (IOException e) {
            logArea.append("Error: " + e.getMessage() + "\n");
        }
    }

    private void stopServer() {
        try {
            isRunning = false;
            serverSocket.close();
            logArea.append("The server stops working.\n");
        } catch (IOException e) {
            logArea.append("Error: " + e.getMessage() + "\n");
        }
    }

    private void handleClient(Socket clientSocket) throws IOException {
        try{
        ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
        String classFile = (String) in.readObject();
        classFile = classFile.replaceFirst("client", "server");
        byte[] b = (byte[]) in.readObject();
        FileOutputStream fos = new FileOutputStream(classFile);
        fos.write(b);
        fos.close();

        Executable ex = (Executable) in.readObject();
        double startTime = System.nanoTime();
        Object output = ex.execute();
        double endTime = System.nanoTime();
        double completionTime = endTime - startTime;

        ResultImpl result = new ResultImpl(output, completionTime);

        ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
        classFile = "out/production/lb5/server/ResultImpl.class";
        out.writeObject(classFile);
        FileInputStream fis = new FileInputStream(classFile);
        b = new byte[fis.available()];
        fis.read(b);
        out.writeObject(b);
        out.writeObject(result);
        out.flush();
    } catch (IOException | ClassNotFoundException e) {
        logArea.append("Error: " + e.getMessage() + "\n");
    }
}

    public static void main(String[] args) {
        new Server();
    }
}