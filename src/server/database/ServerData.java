package server.database;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class handles the database. The incoming data will be saved to the db.file and
 * on request it can be accessed.
 */
public class ServerData extends Thread{
    public JsonObject database;
    private final Gson gson;
    private final File file;
    Lock readLock;
    Lock writeLock;
    String type = "";
    String key = "";
    ArrayList<String> keys;
    String finalKey = "";
    JsonElement value;

    /**
     * The constructor of the ServerData class.
     * This initialize most fields and creates the database db.json file.
     * No input needed.
     */
    public ServerData() {
        this.database = new JsonObject();
        this.gson = new Gson();

        this.file = new File(".\\src\\server\\data\\db.json");
        try {
            file.createNewFile();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        ReadWriteLock lock = new ReentrantReadWriteLock();
        this.readLock = lock.readLock();
        this.writeLock = lock.writeLock();

    }

    /**
     * This method outputs the responses and values depending on the incoming requests.
     * @param output this is the outputsstream of the serversocket
     */
    public void processData(DataOutputStream output) {
        LinkedTreeMap<String, Object> feedback = new LinkedTreeMap<>();
        innitDatabase();
        switch (type) {
            case "get":
                if (!"".equals(key)) {
                    if (database.has(key)) {
                        feedback.put("response", "OK");
                        feedback.put("value", database.get(key));
                    }

                } else {
                    JsonObject placeHolder = database
                            .getAsJsonObject();
                    JsonObject truePlaceHolder = new JsonObject();
                    for (String keyKeys: keys) {

                        if (placeHolder.has(keyKeys)) {
                            if (!keyKeys.equals(finalKey)) {
                                truePlaceHolder.add(keyKeys, placeHolder.get(keyKeys)
                                        .getAsJsonObject());
                                placeHolder = placeHolder.get(keyKeys)
                                        .getAsJsonObject();

                            }
                        } else {
                            break;
                        }
                    }
                    if (placeHolder.has(finalKey)) {
                        feedback.put("response", "OK");
                        feedback.put("value", placeHolder.get(finalKey));
                    }

                }
                if (feedback.isEmpty()) {
                    feedback.put("response", "ERROR");
                    feedback.put("reason", "No such key");
                }

                break;

            case "set":

            case "delete":
                if (!"".equals(key)) {
                    database.add(key, value);
                } else {
                    recursionJson(database, keys.size());
                }
                feedback.put("response", "OK");
                updateDatabase();
                break;
            default:
                feedback.put("response", "ERROR");
                feedback.put("reason", "No such key");
                break;
        }
        try {
            output.writeUTF(gson.toJson(feedback));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }


    }


    /**
     * This method initializes the database by reading the db.file and saving it to the
     * database variable. While this process goes on a lock is placed. This ensures that
     * another thread cant change the database while the database is being read.
     */
    private void innitDatabase() {
        readLock.lock();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNext()) {
                String input = scanner.nextLine();
                database = gson.fromJson(input, database.getClass());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        readLock.unlock();
    }

    /**
     * This method updates the db.file by writing the database variable to it.
     * While this process is being done a lock is placed to ensure that no other Thread
     * can read or write the database.
     */
    private void updateDatabase() {
        writeLock.lock();
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(gson.toJson(database) + " ");
            writer.flush();

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        key = "";
        writeLock.unlock();

    }

    /**
     * This method transforms the incoming json to more actionable data.
     * The incming json need to be of the format: type: , key: , and optional
     * value: .
     * @param input this is the incoming json as string
     */
    public void inputToJson(String input) {
        JsonQuery query = new JsonQuery();
        query = gson.fromJson(input, query.getClass());

        value = query.value;
        type = query.type;
        if (query.key.getClass().equals(String.class)) {
            key = (String) query.key;
        } else {
            keys = (ArrayList) query.key;
            finalKey = keys.get(keys.size() - 1);
        }

    }


    /**
     * This method is used to traverse the database and change entries. This works with an
     * unknown depth and unknown structure. It uses the amount of keys to determine the
     * required depth.
     * @param object This is the database
     * @param x This is the amount of keys, namely the size of the keys arraylist
     */
    private void recursionJson(JsonObject object, int x) {
        try {
            if (x > 1) {
                if (object.get(keys.get(keys.size() - x)).isJsonObject()) {
                    recursionJson(object.get(keys.get(keys.size() - x))
                            .getAsJsonObject(), x - 1);
                }
            } else {
                if ("delete".equals(type)) {
                    object.remove(finalKey);
                } else {
                    object.add(finalKey, value);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * getter for type
     * @return type String
     */
    public String getType() {
        return type;
    }

    /**
     * needed for gson to pull apart the incoming json
     */
    private class JsonQuery {
        JsonElement value;
        String type;
        Object key;
    }
}



