package fr.utc.sr03.messageReceptor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MessageHandler :
 * - Manages the reception of messages coming from the communication Socket
 * "client"
 * - Manages the sending of messages to the communication Socket "client"
 * - Manages the uniqueness of client pseudonyms with the "pseudos" collection
 * - Manages unexpected disconnections of a client by notifying other clients
 */
public class MessageHandler extends Thread {

    private final Socket client;
    private final ConcurrentLinkedQueue<Socket> clients;
    private final ConcurrentLinkedQueue<DataOutputStream> outsList;
    private final ConcurrentLinkedQueue<DataInputStream> insList;
    private final ConcurrentLinkedQueue<String> pseudosList;

    public MessageHandler(Socket client, ConcurrentLinkedQueue<Socket> clients,
            ConcurrentLinkedQueue<DataOutputStream> outsList,
            ConcurrentLinkedQueue<DataInputStream> insList,
            ConcurrentLinkedQueue<String> pseudosList) {

        this.client = client;
        this.clients = clients;
        this.insList = insList;
        this.outsList = outsList;
        this.pseudosList = pseudosList;
    }

    @Override
    public void run() {
        try {
            boolean isError = false; // Indicates whether an error has occurred or not.
            // If yes, some instruction blocks are avoided later, and the client's
            // disconnection is signaled to other clients

            final String ERROR_PSEUDO_MESSAGE = "Server said: Pseudo already used, enter another pseudonym.";
            String msg = "";
            String pseudo;

            DataOutputStream outs = new DataOutputStream(client.getOutputStream());
            DataInputStream ins = new DataInputStream(client.getInputStream());

            // Register pseudo
            pseudo = ins.readUTF();
            // Check uniqueness of the pseudo
            while (pseudosList.contains(pseudo)) {
                outs.writeUTF(ERROR_PSEUDO_MESSAGE);
                pseudo = ins.readUTF();
            }
            outs.writeUTF("-------------------");

            // Add streams and pseudo to collections managing concurrent accesses
            pseudosList.add(pseudo);
            outsList.add(outs);
            insList.add(ins);

            // Send pseudo to everyone :
            final String finalPseudo = pseudo;
            outsList.forEach((outputStream) -> {
                try {
                    if (!outputStream.equals(outs))
                        outputStream.writeUTF(finalPseudo + " joined the conversation");

                } catch (IOException e) {
                    Logger.getLogger(MessageHandler.class.getName()).log(Level.SEVERE, null, e);
                }
            });

            // Read message coming from the client
            try {
                msg = ins.readUTF();
            } catch (IOException e) {
                // If unexpected disconnection between entering the pseudo and entering the
                // first message, signal the error
                isError = true;
            }
            if (!isError) {
                while (!msg.equals("exit")) {
                    // Send message
                    try {
                        String finalMsg = msg;
                        outsList.forEach((outputStream) -> {
                            try {
                                if (!outputStream.equals(outs))
                                    outputStream.writeUTF(finalPseudo + " said: " + finalMsg);
                            } catch (IOException e) {
                                Logger.getLogger(MessageHandler.class.getName()).log(Level.SEVERE, null, e);
                            }
                        });
                        msg = ins.readUTF();
                    } catch (IOException e) {
                        // To manage unexpected disconnection
                        isError = true; // to not execute the rest if an error occurred
                        break;
                    }
                }
            }
            if (!isError) { // if everything went well, and when user entered "exit"
                outsList.forEach((outputStream) -> {
                    try {
                        if (!outputStream.equals(outs))
                            outputStream.writeUTF(finalPseudo + " left the conversation");
                    } catch (IOException e) {
                        Logger.getLogger(MessageHandler.class.getName()).log(Level.SEVERE, null, e);
                    }
                });
                outs.writeUTF("exit"); // to close InputStream of client's MessageReceptor
            }

            // Removal of client's streams and pseudos
            insList.remove(ins);
            outsList.remove(outs);
            ins.close();
            outs.close();
            clients.remove(client);
            client.close();
            pseudosList.remove(pseudo);

            if (isError) {
                // Notify clients of the disconnection :
                outsList.forEach((outputStream) -> {
                    try {
                        outputStream.writeUTF(finalPseudo + " left the conversation");
                    } catch (IOException e) {
                        Logger.getLogger(MessageHandler.class.getName()).log(Level.SEVERE, null, e);
                    }
                });
            }

        } catch (IOException e) {
            // Handles the case where a client disconnection occurs before entering a pseudo
            outsList.forEach((outputStream) -> {
                try {
                    outputStream.writeUTF(" Unknown user left the conversation");

                } catch (IOException err) {
                    Logger.getLogger(MessageHandler.class.getName()).log(Level.SEVERE, null, err);
                }

            });
            try {
                // We remove the communication socket with the client
                clients.remove(client);
                client.close();
            } catch (IOException err) {
                Logger.getLogger(MessageHandler.class.getName()).log(Level.SEVERE, null, e);

            }
        }
    }
}