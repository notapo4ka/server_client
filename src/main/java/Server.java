import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final int PORT = 8080;
    private static final List<ClientConnection> activeConnections = new ArrayList<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                ClientConnection clientConnection = new ClientConnection(clientSocket);
                activeConnections.add(clientConnection);
                broadcastMessage("[SERVER] " + clientConnection.getName() + " successfully connected.");

                Thread clientThread = new Thread(clientConnection);
                clientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientConnection implements Runnable {
        private final Socket clientSocket;
        private final String name;
        private final long connectionTime;

        public ClientConnection(Socket clientSocket) {
            this.clientSocket = clientSocket;
            this.name = "client-" + (activeConnections.size() + 1);
            this.connectionTime = System.currentTimeMillis();
        }

        public String getName() {
            return name;
        }

        @Override
        public void run() {
            try {
                InputStream inputStream = clientSocket.getInputStream();
                OutputStream outputStream = clientSocket.getOutputStream();

                while (true) {
                    byte[] buffer = new byte[1024];
                    int bytesRead = inputStream.read(buffer);
                    if (bytesRead == -1) {
                        break;
                    }

                    String command = new String(buffer, 0, bytesRead).trim();
                    System.out.println("Received command from " + name + ": " + command);

                    if (command.equals("-exit")) {
                        handleExitCommand();
                        break;
                    } else if (command.startsWith("-file ")) {
                        String filePath = command.substring("-file ".length());
                        handleFileCommand(filePath);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                    activeConnections.remove(this);
                    broadcastMessage("[SERVER] " + name + " disconnected.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void handleExitCommand() {

        }

        private void handleFileCommand(String filePath) {

        }
    }

    private static void broadcastMessage(String message) {
        System.out.println(message);

        for (ClientConnection connection : activeConnections) {
            try {
                OutputStream outputStream = connection.clientSocket.getOutputStream();
                outputStream.write(message.getBytes());
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
