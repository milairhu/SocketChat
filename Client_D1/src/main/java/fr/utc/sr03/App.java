package fr.utc.sr03;

import fr.utc.sr03.client.MessageReceptor;
import fr.utc.sr03.client.MessageSender;

import java.io.IOException;
import java.net.Socket;

public class App 
{
    public static void main( String[] args )
    {
        try{
            Socket comm = new Socket("localhost",8000); //Etablie la connexion avec le serveur
            MessageReceptor msgreceptor = new MessageReceptor(comm);
            MessageSender msgsender = new MessageSender(comm);
            //Démarre les 2 threads de réception de messages et d'envoi de message
            msgreceptor.start();
            msgsender.start();
        }
        catch (IOException e){
            System.out.println(e);
        }
    }

}
