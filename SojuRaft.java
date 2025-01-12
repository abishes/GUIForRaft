import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;

import com.google.gson.Gson;


class SojuRaft extends JFrame implements ActionListener {

    private JTextField textField1;
    private JTextField textField2;
    private JTextArea textArea;

    public SojuRaft() {
        
        setTitle("Soju Raft");
        setExtendedState(JFrame.MAXIMIZED_BOTH);  
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        
        setLayout(new BorderLayout());

        
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));

        JLabel label1 = new JLabel("Key");
        JLabel label2 = new JLabel("Value");
        JLabel label3 = new JLabel("Action");

        textField1 = new JTextField(20);
        textField2 = new JTextField(20); 

        JButton set = new JButton("set");
        set.addActionListener(this); 
        JButton get = new JButton("get");
        get.addActionListener(this);
        JButton delete = new JButton("delete");
        delete.addActionListener(this);

        // Add components to the top panel
        topPanel.add(label1);
        topPanel.add(textField1);
        topPanel.add(label2);
        topPanel.add(textField2);
        topPanel.add(label3);
        topPanel.add(set);
        topPanel.add(get);
        topPanel.add(delete);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
        getAll();
    }

    public static void main(String[] args) {
        // Ensure GUI creation runs on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SojuRaft(); // Create and display the frame
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Get the key and value from the text fields
        String key = textField1.getText();
        String value = textField2.getText();

        // Perform actions based on the button clicked
        if (e.getActionCommand().equals("set")) {
            setKeyValue(key, value, "set");
        }
        else if (e.getActionCommand().equals("delete")) {
            setKeyValue(key, value, "delete");
        }
        
        else if (e.getActionCommand().equals("get")) {
            get(key);
        }
    }

    void setKeyValue(String key, String value, String setOrDelete) {
        try {
            Random random = new Random();
            URI uri = new URI("https://sojuraft.osac.org.np/set");
            URL url = uri.toURL();

            
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String messString = "id=" + random.nextInt(1000) + "&action="+setOrDelete +"&key=" + key;
            if(setOrDelete == "set"){
                messString += "&value=" + value;
            }

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = messString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int code = connection.getResponseCode();
            if (code == 200) //HttpURLConnection.HTTP_OK
            {   if(setOrDelete == "set")
                    textArea.setText("Data sent successfully:\nKey = " + key + ": Value = " + value);
                else
                    textArea.setText("Key deleted successfully:\nKey = " + key);
            } else {
                textArea.setText("Failed to send data. HTTP Code: " + code);
            }

            connection.disconnect();

        } catch (Exception ex) {
            ex.printStackTrace();
            textArea.setText("Error: " + ex.getMessage());
        }
    }
    
    void get(String key) {
        try {
            URI uri = new URI("https://sojuraft.osac.org.np/get/" + key);
            URL url = uri.toURL();

            
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int code = connection.getResponseCode();
            if (code == 200) //HttpURLConnection.HTTP_OK
            {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    textArea.setText("Data received successfully:\nKey = "+key+" : Value = " + response.toString());
                }
                catch(Exception e ){
                    textArea.setText("Could not get data");
                }
            } else {
                textArea.setText("Failed to get data. HTTP Code: " + code);
            }

            connection.disconnect();

        } catch (Exception ex) {
            ex.printStackTrace();
            textArea.setText("Error: " + ex.getMessage());
        }
    }

    void getAll(){
        try {
            URI uri = new URI("https://sojuraft.osac.org.np/getall");
            URL url = uri.toURL();

            
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int code = connection.getResponseCode();
            if (code == 200) //HttpURLConnection.HTTP_OK
            {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    
                    // Parse the JSON string using Gson
                    Gson gson = new Gson();
                    Map<String, Object> jsonMap = gson.fromJson(response.toString(), Map.class);
                    String str = "";

                    // Print the key-value pairs
                    for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
                        str +=entry.getKey() + ": " + entry.getValue() + "\n";
                    }
                    textArea.setText("Data received successfully:\n" + str);
                }
                catch(Exception e ){
                    textArea.setText("Could not get data");
                }
            } else {
                textArea.setText("Failed to get data. HTTP Code: " + code);
            }

            connection.disconnect();

        } catch (Exception ex) {
            ex.printStackTrace();
            textArea.setText("Error: " + ex.getMessage());
        }
    }
}
