package fr.utc.sr03;

import fr.utc.sr03.messageReceptor.MessageHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {
    public static void main(String[] args) {
        try {
            ServerSocket conn = new ServerSocket(8000); // Connection socket
            ConcurrentLinkedQueue<Socket> clients = new ConcurrentLinkedQueue<>(); // To store the different
                                                                                   // communication sockets
            ConcurrentLinkedQueue<DataOutputStream> outsList = new ConcurrentLinkedQueue<>(); // To store the output
                                                                                              // streams
            ConcurrentLinkedQueue<DataInputStream> insList = new ConcurrentLinkedQueue<>(); // To store the input
                                                                                            // streams
            ConcurrentLinkedQueue<String> pseudosList = new ConcurrentLinkedQueue<>(); // To store the different
                                                                                       // pseudonyms and avoid
                                                                                       // redundancies
            while (true) {
                Socket comm = conn.accept();
                clients.add(comm);
                // Launch a thread for each established communication
                MessageHandler msgHandler = new MessageHandler(comm, clients, outsList, insList, pseudosList);
                msgHandler.start();
            }
        } catch (IOException e) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}