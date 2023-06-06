import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) {
        try {
            Socket serverSocket = new Socket(SERVER_HOST, SERVER_PORT);
            System.out.println("Connected to server: " + serverSocket);

            InputStream inputStream = serverSocket.getInputStream();
            OutputStream outputStream = serverSocket.getOutputStream();

            Thread messageThread = new Thread(() -> {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String message;
                    while ((message = reader.readLine()) != null) {
                        System.out.println("Received message from server: " + message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            messageThread.start();

            Thread commandThread = new Thread(() -> {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                    String command;
                    while ((command = reader.readLine()) != null) {
                        outputStream.write(command.getBytes());
                        outputStream.write('\n');
                        outputStream.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            commandThread.start();

            messageThread.join();
            commandThread.join();

            serverSocket.close();
            System.out.println("Disconnected from server");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
