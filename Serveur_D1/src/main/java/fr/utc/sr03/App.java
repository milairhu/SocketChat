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

public class App
{
    public static void main( String[] args )
    {
        try{
            ServerSocket conn = new ServerSocket(8000); //Socket de connexion
            ConcurrentLinkedQueue<Socket> clients = new ConcurrentLinkedQueue<>(); //Pour stocker les différentes sockets de communication
            ConcurrentLinkedQueue<DataOutputStream> outsList = new ConcurrentLinkedQueue<>(); //Pour stocker les flux de sorties
            ConcurrentLinkedQueue<DataInputStream> insList = new ConcurrentLinkedQueue<>(); //Pour stocker les flux d'entrées
            ConcurrentLinkedQueue<String> pseudosList = new ConcurrentLinkedQueue<>(); //Pour stocker les différents pseudos et éviter les redondances
            while(true){
                Socket comm = conn.accept();
                clients.add(comm);
                //Lancer un thread pour chaque communication établie
                MessageHandler msgHandler = new MessageHandler(comm, clients, outsList, insList, pseudosList);
                msgHandler.start();
            }
        }
        catch (IOException e){
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
