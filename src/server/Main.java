package server;

/**
 * This is the main class, which passes the port and address to the Server class
 * and then starts it.
 */
public class Main {

    public static void main(String[] args) {
        Server server = new Server(23456, "127.0.0.1");
        server.start();
    }

}
