package fr.utc.sr03.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MessageReceptor :
 *  - Gère la réception de messages venant du serveur en les affichant sur la console
 *  - Instancie le flux d'entrée de données
 *  - Gère la déconnexion intempestive du serveur en envoyant un message à l'utilisateur
 */
public class MessageReceptor extends Thread {
    private final Socket client; //Socket de communication avec le serveur

    public MessageReceptor(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            DataInputStream ins = new DataInputStream(client.getInputStream());
            String msg = "";
            while (!msg.equals("exit")) {
                //lecture du contenu
                try{
                    msg = ins.readUTF();
                    System.out.println(" "+msg);
                }catch (IOException e){
                    System.out.println("Erreur : déconnexion du serveur");
                    break;
                }
            }
            ins.close();
        } catch (IOException e) {
            Logger.getLogger(MessageReceptor.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
