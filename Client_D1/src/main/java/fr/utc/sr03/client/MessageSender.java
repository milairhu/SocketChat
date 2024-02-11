package fr.utc.sr03.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MessageSender :
 *  - Gère l'envoi de messages vers le serveur
 *  - Gère le cycle de vie du flux de sortie de données
 *  - Ferme le flux d'entrée de données ainsi que la Socket de communication
 */
public class MessageSender extends Thread {
    private final Socket client; //Socket de communication avec le serveur

    public MessageSender(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            boolean isError =false; //Permet de signaler une déconnexion du serveur et de mettre fin au programme
            Scanner sc=new Scanner(System.in);
            sc.useDelimiter("\n"); //Le délimiteur par défaut étant " ", sans cette instruction, on ne pourrait envoyer que mot par mot
            DataOutputStream outs = new DataOutputStream(client.getOutputStream());
            String pseudo;
            do{ //Vérifie que l'utilisateur entre bien un pseudo
                System.out.println("Entrez votre pseudo : ");
                pseudo = sc.next();
            } while (pseudo.length() == 0 && !client.isClosed());
            try{
                //Envoie le pseudo au serveur
                outs.writeUTF(pseudo);
            }catch (IOException e){
                isError =true;
            }

            if(!isError){
                //si l'envoi du pseudo s'est bien passé, la discussion a lieu tant qu'on n'écrit pas "exit"
                String msg = "";
                while (!msg.equals("exit") && !client.isClosed()) {
                    //envoi du contenu
                    msg = sc.next();
                    try{
                        if(msg != null && !msg.isEmpty()){
                            outs.writeUTF(msg);
                        }
                    }catch (IOException e){
                        isError =true;
                        break;
                    }
                }
                if(!isError){
                    //si serveur ne s'est pas déconnecté intempestivement
                    try {
                        Thread.sleep(20); //Permet d'attendre que la Socket se ferme en premier du côté du serveur
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Socket.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            //Fermeture des flux d'entrée et de sorties de la socket client, et fermeture de la socket
            if(!client.isClosed()){
                outs.close();
                client.close();
            }
        } catch (IOException e) {
            Logger.getLogger(MessageSender.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
