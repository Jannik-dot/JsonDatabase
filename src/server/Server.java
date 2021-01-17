package server;

import server.database.ServerData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * This class represents the Server. It continuously looks for a connection.
 * Once the connection is established it listens for an input and passes the request
 * to the ServerData class. The program can handle multiple request at once.
 */
public class Server {
    private final int port;
    private final String address;
    private ServerData database;

    /**
     *
     * @param port This is the port the client will connect to
     * @param address This is the address the client will connect to
     */
    public Server(int port, String address) {
        this.port = port;
        this.address = address;
        this.database = new ServerData();

    }

    public void start() {
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        try (ServerSocket server = new ServerSocket(port, 50, InetAddress.getByName(address))) {
            System.out.println("Server started!");
            while (true) {
                Socket socket = server.accept();
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output  = new DataOutputStream(socket.getOutputStream());
                String msg = input.readUTF();
                try {
                    database.inputToJson(msg);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                if ("".equals(database.getType())) {
                    output.writeUTF("No Command");
                } else {
                    if ("exit".equals(database.getType())) {
                        output.writeUTF("{\"response\":\"OK\"}");
                        server.close();
                        break;
                    }
                    executorService.submit(() -> database.processData(output));
                }
            }
    } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
