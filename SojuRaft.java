import javax.swing.*;
import javax.swing.border.LineBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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


class SojuRaft extends JFrame{

    private JTextField textField1;
    private JTextField textField2;
    private JPanel panel;

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
        // set.addActionListener(this); 
        JButton get = new JButton("get");
        // get.addActionListener(this);
        JButton delete = new JButton("delete");
        // delete.addActionListener(this);

        // Add components to the top panel
        topPanel.add(label1);
        topPanel.add(textField1);
        topPanel.add(label2);
        topPanel.add(textField2);
        topPanel.add(label3);
        topPanel.add(set);
        topPanel.add(get);
        topPanel.add(delete);

        set.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String key = textField1.getText();
                String value = textField2.getText();
                setKeyValue(key, value, "set");
            }
        });

        get.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String key = textField1.getText();
                get(key);
            }
        });

        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String key = textField1.getText();
                setKeyValue(key, "", "delete");
            }
        });

        panel = new JPanel();
        panel.setLayout(new GridLayout(0, 3));
         // Wrap the panel inside a JScrollPane to make it scrollable
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Text area to display get response
        textArea = new JTextArea(5, 40);
        textArea.setEditable(false);
        add(new JScrollPane(textArea), BorderLayout.SOUTH);

        setVisible(true);
    }

    public static void main(String[] args) {
        // Ensure GUI creation runs on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SojuRaft sj = new SojuRaft(); // Create and display the frame
                sj.getAll();
            }
        });
    }

    // @Override
    // public void actionPerformed(ActionEvent e) {
    //     // Get the key and value from the text fields
    //     String key = textField1.getText();
    //     String value = textField2.getText();

    //     // Perform actions based on the button clicked
    //     if (e.getActionCommand().equals("set")) {
    //         setKeyValue(key, value, "set");
    //     }
    //     else if (e.getActionCommand().equals("delete")) {
    //         setKeyValue(key, value, "delete");
    //     }
        
    //     else if (e.getActionCommand().equals("get")) {
    //         get(key);
    //     }
    // }

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
            if(setOrDelete.equals("set")){
                messString += "&value=" + value;
            }
            // Send data
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = messString.getBytes();
                os.write(input, 0, input.length);
            }

            // Get response code
            int code = connection.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                String str;
                if(setOrDelete.equals("set"))
                    str = "Set Request successful:\nKey = " + key + " : value = " + value;
                else
                    str = "Delete Request successful:\nKey = " + key;

                textArea.setText(str);
                getAll();
            } else {
                    textArea.setText("Error occured: " + code);
            }

            connection.disconnect();

        } catch (Exception ex) {
            ex.printStackTrace();
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
            if (code == 200) { // HTTP_OK
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    // Parse the JSON response using Gson
                    Gson gson = new Gson();
                    Map<String, Object> jsonMap = gson.fromJson(response.toString(), Map.class);
                    
                    panel.removeAll();

                    // Add components for each key-value pair
                    for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
                        // Create a text field for the key
                        JTextField keyField = new JTextField(entry.getKey());
                        keyField.setEditable(false);  // Make the key non-editable
                        keyField.setBackground(new Color(127,255,0));
                        keyField.setForeground(Color.WHITE);
                        keyField.setFont(new Font("Arial", Font.BOLD, 16));


                        // Create a text field for the value
                        JTextArea valueArea = new JTextArea(entry.getValue().toString());
                        valueArea.setEditable(false); 
                        valueArea.setLineWrap(true);  
                        valueArea.setWrapStyleWord(true);
                        valueArea.setBorder(new LineBorder(Color.BLACK)); 
                        valueArea.setPreferredSize(new Dimension(200, 50)); 

                        // Create a delete button for the row
                        JButton deleteButton = new JButton("Delete");
                        deleteButton.setBackground(new Color(237,41,57));
                        deleteButton.setForeground(Color.WHITE);

                        deleteButton.setFont(new Font("Arial", Font.BOLD, 16));
                        deleteButton.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                        deleteButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                // Call setKeyValue with the "delete" action
                                setKeyValue(entry.getKey(), "", "delete");
                                panel.remove(keyField);
                                panel.remove(valueArea);  // Remove the JScrollPane around JTextArea
                                panel.remove(deleteButton);

                                // Refresh the panel layout
                                panel.revalidate();
                                panel.repaint();
                            }
                        });

                        deleteButton.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseEntered(MouseEvent e) {
                                deleteButton.setBackground(new Color(240, 84, 97)); 
                            }
                
                            @Override
                            public void mouseExited(MouseEvent e) {
                                deleteButton.setBackground(new Color(237,41,57));
                            }
                        });

                        // Add components to the panel
                        panel.add(keyField);
                        panel.add(valueArea);
                        panel.add(deleteButton);

                
                    }

                    // Refresh the panel to display the components
                    panel.revalidate();
                    panel.repaint();
                }
                catch(Exception e){
                    System.out.println("Error in reading: " + e.toString());
                }
            } else {
                System.out.println("Error: Failed to get data");
            }

            connection.disconnect();

        } 
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}

