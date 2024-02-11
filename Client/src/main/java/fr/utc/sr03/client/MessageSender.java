package fr.utc.sr03.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MessageSender :
 * - Manages the sending of messages to the server
 * - Manages the lifecycle of the data output stream
 * - Closes the data input stream and the communication socket
 */
public class MessageSender extends Thread {
    private final Socket client; // Communication socket with the server

    public MessageSender(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            boolean isError = false; // Used to signal a server disconnection and end the program
            Scanner sc = new Scanner(System.in);
            sc.useDelimiter("\n"); // The default delimiter is " ", without this instruction, we could only send
                                   // word by word
            DataOutputStream outs = new DataOutputStream(client.getOutputStream());
            String pseudo;
            do { // Checks that the user enters a pseudo
                System.out.println("Enter your username: ");
                pseudo = sc.next();
            } while (pseudo.length() == 0 && !client.isClosed());
            try {
                // Sends the username to the server
                outs.writeUTF(pseudo);
            } catch (IOException e) {
                isError = true;
            }

            if (!isError) {
                // If the username was sent successfully, the discussion takes place as long as
                // we do not write "exit"
                String msg = "";
                while (!msg.equals("exit") && !client.isClosed()) {
                    // Sending the content
                    msg = sc.next();
                    try {
                        if (msg != null && !msg.isEmpty()) {
                            outs.writeUTF(msg);
                        }
                    } catch (IOException e) {
                        isError = true;
                        break;
                    }
                }
                if (!isError) {
                    // If the server has not disconnected unexpectedly
                    try {
                        Thread.sleep(20); // Allows to wait for the Socket to close first on the server side
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Socket.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            // Closing the input and output streams of the client socket, and closing the
            // socket
            if (!client.isClosed()) {
                outs.close();
                client.close();
            }
        } catch (IOException e) {
            Logger.getLogger(MessageSender.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}