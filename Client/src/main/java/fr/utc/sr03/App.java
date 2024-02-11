package fr.utc.sr03;

import fr.utc.sr03.client.MessageReceptor;
import fr.utc.sr03.client.MessageSender;

import java.io.IOException;
import java.net.Socket;

public class App {
    public static void main(String[] args) {
        try {
            Socket comm = new Socket("localhost", 8000); // Establishes the connection with the server
            MessageReceptor msgreceptor = new MessageReceptor(comm);
            MessageSender msgsender = new MessageSender(comm);
            // Starts the 2 threads for receiving messages and sending messages
            msgreceptor.start();
            msgsender.start();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

}