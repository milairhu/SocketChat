package fr.utc.sr03.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MessageReceptor :
 *  - Manages the reception of messages coming from the server by displaying them on the console
 *  - Instantiates the data input stream
 *  - Handles unexpected server disconnection by sending a message to the user
 */
public class MessageReceptor extends Thread {
    private final Socket client; // Communication socket with the server

    public MessageReceptor(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            DataInputStream ins = new DataInputStream(client.getInputStream());
            String msg = "";
            while (!msg.equals("exit")) {
                // reading the content
                try{
                    msg = ins.readUTF();
                    System.out.println(" "+msg);
                }catch (IOException e){
                    System.out.println("Error: server disconnection");
                    break;
                }
            }
            ins.close();
        } catch (IOException e) {
            Logger.getLogger(MessageReceptor.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
