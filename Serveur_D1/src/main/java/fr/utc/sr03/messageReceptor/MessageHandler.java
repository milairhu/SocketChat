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
 *  - Gère la réception de messages venant provenant de la Socket de communication "client"
 *  - Gère l'envoi de message vers la Socket de communication "client"
 *  - Gère l'unicité des pseudonymes des clients avec la collection "pseudos"
 *  - Gère les déconnexions intempestives d'un client en prévenant les autres clients
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
                          ConcurrentLinkedQueue<String> pseudosList
                          ){

        this.client = client;
        this.clients = clients;
        this.insList = insList;
        this.outsList = outsList;
        this.pseudosList = pseudosList;
    }

    @Override
    public void run(){
        try{
            boolean isError = false; //rend compte de si une erreur a eu lieu ou pas.
            // Si oui, des blocs d'instructions sont évités ultérieurement, et on signale la déconnexion du client aux autres clients

            final String ERROR_PSEUDO_MESSAGE = "Server a dit : Pseudo déjà utilisé, entrez un autre pseudonyme.";
            String msg = "" ;
            String pseudo;


            DataOutputStream outs = new DataOutputStream(client.getOutputStream());
            DataInputStream ins = new DataInputStream(client.getInputStream());

            //Enregistre pseudo
            pseudo  = ins.readUTF();
            //Vérifie unicité du pseudo
            while(pseudosList.contains(pseudo)){
                outs.writeUTF(ERROR_PSEUDO_MESSAGE);
                pseudo = ins.readUTF();
            }
            outs.writeUTF("-------------------");

            //Ajout des flux et du pseudo dans les collections gérant les accès concurrents
            pseudosList.add(pseudo);
            outsList.add(outs);
            insList.add(ins);

            //Envoie pseudo à tout le monde :
            final String finalPseudo = pseudo;
            outsList.forEach((outputStream)->{
                try{
                    if(!outputStream.equals(outs))
                        outputStream.writeUTF(finalPseudo +" a rejoint la conversation");

                }catch (IOException e){
                    Logger.getLogger(MessageHandler.class.getName()).log(Level.SEVERE, null, e);
                }
            });

            //Lit message venant du client
            try{
                msg = ins.readUTF();
            }catch (IOException e){
                //Si déconnexion innatendue entre entrée du pseudo et entrée du premier message, on signale l'erreur
                isError = true;
            }
            if(!isError){
                while (!msg.equals("exit") ){
                    //Envoi message
                    try{
                        String finalMsg = msg;
                        outsList.forEach((outputStream)->{
                            try{
                                if(!outputStream.equals(outs))
                                    outputStream.writeUTF(finalPseudo +" a dit : " + finalMsg);
                            }catch (IOException e){
                                Logger.getLogger(MessageHandler.class.getName()).log(Level.SEVERE, null, e);
                            }
                        });
                        msg = ins.readUTF();
                    }catch (IOException e){
                        //Pour gérer déconnection inattendue
                        isError =true; //pour ne pas exécuter la suite si une erreur s'est passé
                        break;
                    }
                }
            }
            if(!isError){ //si tout s'est bien passé, et quand utilisateur a entré "exit"
                outsList.forEach((outputStream)->{
                    try{
                        if(!outputStream.equals(outs))
                            outputStream.writeUTF(finalPseudo + " a quitté la conversation");
                    }catch (IOException e){
                        Logger.getLogger(MessageHandler.class.getName()).log(Level.SEVERE, null, e);
                    }
                });
                outs.writeUTF("exit"); //pour fermer InputStream de MessageReceptor de client
            }

            //Suppression des flux et du pseudos du client
            insList.remove(ins);
            outsList.remove(outs);
            ins.close();
            outs.close();
            clients.remove(client);
            client.close();
            pseudosList.remove(pseudo);

            if(isError){
                //Prévenir les clients de la déconnexion :
                outsList.forEach((outputStream)->{
                    try{
                        outputStream.writeUTF(finalPseudo + " a quitté la conversation");
                    }catch (IOException e){
                        Logger.getLogger(MessageHandler.class.getName()).log(Level.SEVERE, null, e);
                    }
                });
            }

        }
        catch (IOException e){
            //Gère le cas où une déconnexion du client a lieu avant d'avoir entré un pseudo
            outsList.forEach((outputStream)->{
                try{
                    outputStream.writeUTF( " Utilisateur inconnu a quitté la conversation");

                }catch (IOException err){
                    Logger.getLogger(MessageHandler.class.getName()).log(Level.SEVERE, null, err);
                }

            });
            try{
                // On supprime la socket communicante avec le client
                clients.remove(client);
                client.close();
            }catch (IOException err){
                Logger.getLogger(MessageHandler.class.getName()).log(Level.SEVERE, null, e);

            }
        }
    }
}
