package bots;

import java.net.Socket;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.IOException;

/**
 * This bot attempts to make network connections - should be flagged
 */
public class NetworkBot {

    public void makeMove() {
        try {
            // This should be detected as network access
            Socket socket = new Socket("example.com", 80);
            socket.close();

            // This should also be detected
            URL url = new URL("https://api.example.com/data");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            connection.disconnect();

        } catch (IOException e) {
            System.out.println("Network error");
        }
    }

    public void sendData() {
        // Method names that should trigger detection
        connect();
        send();
        receive();
    }

    private void connect() { }
    private void send() { }
    private void receive() { }
}
