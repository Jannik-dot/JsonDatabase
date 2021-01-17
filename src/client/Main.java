package client;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * This class represents the client.
 * It can accept main line arguments, connect to a server with a json-Database
 * and pass these arguments in json-Format.
 * These arguments include:
 * -t declares the type: get, set , delete, exit
 * -k declares the key under which the object is known in the database
 * -v declares the value in the case of the set-operation
 * It is possible to write a jsonfile in the described format (-t,-k,(-v)) and pass its path
 * to the class via -in command.
 */
public class Main {
    @Parameter(names ={"-t"})
    String type = "";
    @Parameter(names = {"-k"})
    String key = "";
    @Parameter(names = {"-v"})
    String value = "";
    @Parameter(names = {"-in"})
    String filePath = "";


    public static void main(String[] args) {
        System.out.println("Client started!");
        String address = "127.0.0.1";
        String msg = "";

        try (
                Socket socket = new Socket(InetAddress.getByName(address), 23456);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ) {
            Main main = new Main();
            JCommander jCommander = new JCommander(main, args);
            if (!"".equals(main.filePath)) {

                File file = new File(".\\src\\client\\data\\" + main.filePath);
                file.createNewFile();

                try (Scanner scanner = new Scanner(file)) {
                    while (scanner.hasNext()) {
                        msg += scanner.nextLine();
                        output.writeUTF(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                msg = toJson(main.type, main.key, main.value);
                output.writeUTF(msg);
            }

            System.out.println("Sent: " + msg);
            String receiveMsg = input.readUTF();
            System.out.println("Received: " + receiveMsg);


        }catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * @param type Command (get, set, delete)
     * @param key   Acceskey for the database
     * @param value Optional value to be passed, default is ""
     * @return type, key and value together as json
     */
    private static String toJson(String type, String key,  String value) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("type", type);
        map.put("key", key);
        if (!"".equals(value)) {
            map.put("value", value);
        }
        Gson gson = new Gson();
        return gson.toJson(map);
    }
}
