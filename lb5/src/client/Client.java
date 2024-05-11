package client;

import interfaces.Executable;
import interfaces.Result;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class Client extends JFrame {
    private JTextArea resultArea;
    private JTextField ipField, portField, nField;

    public Client() {
        setTitle("TCP Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JPanel ipPortPanel = new JPanel();
        ipPortPanel.add(new JLabel("IP Address:"));
        ipField = new JTextField(10);
        ipField.setText("localhost");
        ipPortPanel.add(ipField);
        ipPortPanel.add(new JLabel("Port:"));
        portField = new JTextField(5);
        portField.setText("12345");
        ipPortPanel.add(portField);
        mainPanel.add(ipPortPanel);

        JPanel nPanel = new JPanel();
        nPanel.add(new JLabel("N:"));
        nField = new JTextField(10);
        nField.setText("27");
        nPanel.add(nField);
        mainPanel.add(nPanel);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        mainPanel.add(scrollPane);

        JPanel buttonPanel = new JPanel();
        JButton calculateButton = new JButton("Calculate");
        calculateButton.addActionListener(e -> calculate());
        buttonPanel.add(calculateButton);

        JButton clearButton = new JButton("Clear Result");
        clearButton.addActionListener(e -> resultArea.setText(""));
        buttonPanel.add(clearButton);

        JButton exitButton = new JButton("Exit Program");
        exitButton.addActionListener(e -> System.exit(0));
        buttonPanel.add(exitButton);

        mainPanel.add(buttonPanel);
        add(mainPanel);
        setVisible(true);
    }

    private void calculate() {
        try {
            String host = ipField.getText();
            int port = Integer.parseInt(portField.getText());
            Socket client = new Socket(host, port);

            ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
            String classFile = "out/production/lb5/client/JobOne.class";
            out.writeObject(classFile);
            FileInputStream fis = new FileInputStream(classFile);
            byte[] b = new byte[fis.available()];
            fis.read(b);
            out.writeObject(b);

            int num = Integer.parseInt(nField.getText());
            Executable aJob = new JobOne(num);
            out.writeObject(aJob);
            out.flush();

            ObjectInputStream in = new ObjectInputStream(client.getInputStream());
            classFile = (String) in.readObject();
            b = (byte[]) in.readObject();
            FileOutputStream fos = new FileOutputStream(classFile);
            fos.write(b);
            fos.close();

            Result r = (Result) in.readObject();
            resultArea.append("result = " + r.output() + ", time taken = " + r.scoreTime() + "ns\n");

            client.close();
        } catch (IOException | ClassNotFoundException ex) {
            resultArea.append("Error: " + ex.getMessage() + "\n");
        }
    }
    public static void main(String[] args) {
        new Client();
    }
}