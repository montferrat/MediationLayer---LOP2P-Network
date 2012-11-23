/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package protoSearchDevice;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.endpoint.ByteArrayMessageElement;
import net.jxta.endpoint.EndpointAddress;
import net.jxta.endpoint.Message.ElementIterator;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.StringMessageElement;
import org.bouncycastle.jce.exception.ExtIOException;
import org.json.*;
//import utils.JSONObject;
import protoLowareInterfaceDevice.LIDRequestHandler;
import protoStandardAbstractData.LOP2PMetadata;
import protoTranslatorDevice.*;
import protonetcommunicationdevice.*;


/**
 *
 * @author Rafael de Santiago
 */
public class SDSearch {
    
    //configuration
    private SDConfiguration sdcfg;
    
    //translator
    TrDTranslator translator;
    
    private LIDRequestHandler clientSocket;
    
    /**
     * Constructor of the class SDSearch. This class have the purpose of
     * execute search for metadatas in LOP2P net and retrieve information about
     * requested searchs. 
     * 
     * @param clientSocket socket of loware connection
     * @param sdcfg configuration object of Search Device
     * @param trdcfg configuration object of the Translator Device
     */                    
    public SDSearch (LIDRequestHandler clientSocket, SDConfiguration sdcfg, TrDConfiguration trdcfg){
        this.sdcfg = sdcfg;    
        this.translator = new TrDTranslator(trdcfg);
        this.clientSocket = clientSocket;
    }

    /**
     * Search for Learning Objects. Request received by loware
     * 
     * @param message request message of search containing metadata for search
     */      
    public void search(JSONObject msgJSON) throws FileNotFoundException, IOException, ClassNotFoundException, Exception{

        //send the responses for client
        DataOutputStream toCliente = null;
        
        ArrayList compatibilities = new ArrayList();
        compatibilities.addAll(this.sdcfg.getGDData().searchInMyMetadatas(msgJSON, this.sdcfg.getSimForRecover()));
        compatibilities.addAll(this.sdcfg.getGDData().searchInOthersMetadatas(msgJSON, this.sdcfg.getSimForRecover()));        
        

        //request search outside: others peers
        ArrayList msgPool = this.sdcfg.getNCDCfg().getNCDData().getMessageBufferList();
        Integer nrequests = 0;
        Date date = new Date();
        String msgID = "msgSent" + date.getTime();
        ArrayList otherPeers = sdcfg.getNCDCfg().getNCDData().getKnownPeersList();
        for (int i=0; i<otherPeers.size(); i++){
            
            try {
                String peerTarget = ((EndpointAddress) otherPeers.get(i)).getProtocolAddress();
                String thisPeer = this.sdcfg.getNCDCfg().getNetworkManager().getPeerID().toURI().toString();
                thisPeer = thisPeer.substring(thisPeer.indexOf("uuid"));
                
                LOP2PMessage msgToSend = new LOP2PMessage(thisPeer, peerTarget, this.sdcfg.getNCDCfg().getMessageNamespace());
                msgToSend.addItem("IDMSG", msgID);
                msgToSend.addItem("TYPE", LOP2PMessage.MESSAGE_TYPE.SEARCH.toString());
                msgToSend.addItem("JSON_SEARCH", msgJSON.toString());
                nrequests++;
                
                msgPool.add(msgToSend);
            } catch (Exception ex) {
                otherPeers.remove(i);
                i--;
                System.err.println(ex.toString());
            }
            
        }
        
        
        //send the local messages to loware
        toCliente = clientSocket.getOut();//new DataOutputStream(this.clientSocket.getOutputStream());
        for (int i = 0; i < compatibilities.size(); i++) {
            LOP2PMetadata mtdt = (LOP2PMetadata)compatibilities.get(i);
            //create message
            String msg = (String) translator.translateBack(mtdt, this.sdcfg.getNCDCfg().getNetworkManager().getPeerID().toURI().toString());

            //send message to search requester (responses about this peer)
            synchronized(toCliente){
                toCliente.write(msg.getBytes());
            }
        }
        
        
        //send the responses for client: use time as stop criteria
        Calendar targetCal = new GregorianCalendar();
        targetCal.add(Calendar.SECOND, this.getSDCfg().getExpirationTime2());
        Date target = targetCal.getTime();
        Date now = new Date();
        while ((target.getTime() > now.getTime())){
            LOP2PMessage msg = (LOP2PMessage)this.sdcfg.getNCDCfg().getNCDData().searchForResponseById(msgID);
            if (msg != null){
                //getting metadatas
                JSONObject objetoJSON = new JSONObject(msg.getItem("OBJECTS"));
                //passar isto para fora do laço de repetição, pois deve-se fazer o envio de uma só vez.
                String translatedMessage = objetoJSON.toString();
                synchronized(toCliente){
                    toCliente.write(translatedMessage.getBytes());                        
                }
                
                //decrising number of responses
                //nrequests--; 
            }            
            now = new Date();
        }              
        
        //ending message for requester
        String bye = "GOODBYE";
        synchronized(toCliente){
            toCliente.write(bye.getBytes()); 
            toCliente.close();
        }
   //     clientSocket.close();
    }
    
    /**
     * Return the configuration object of Seach Device. 
     * 
     * @return configuration object of Seach Device
     */      
    public SDConfiguration getSDCfg() {
        return sdcfg;
    }

    /**
     * Search for Learning Objects. Request received by LOP2P network 
     * 
     * @param msg request message of search containing metadata for search
     */     
    public void searchExpose(LOP2PMessage msg) {
        //getting metadatas
        try{
            JSONObject objetoJSON = new JSONObject(msg.getItem("JSON_SEARCH"));
            
             if(objetoJSON  != null){
                ArrayList compatibilities = new ArrayList();
                compatibilities.addAll(this.sdcfg.getGDData().searchInMyMetadatas(objetoJSON, this.sdcfg.getSimForRecover()));
                compatibilities.addAll(this.sdcfg.getGDData().searchInOthersMetadatas(objetoJSON, this.sdcfg.getSimForRecover()));        

                //request search outside: others peers
                ArrayList msgPool = this.sdcfg.getNCDCfg().getNCDData().getMessageBufferList();

                Integer nrequests = 0;

                //send message other
                String msgID = msg.getItem("IDMSG");

                //verifies if this message was received another day
                if (this.sdcfg.getNCDCfg().getNCDData().addIfIsNewMessage(msgID) == false){
                    //preempt the proccess
                    return;
                }

                ArrayList otherPeers = sdcfg.getNCDCfg().getNCDData().getKnownPeersList();
                for (int i=0; i<otherPeers.size(); i++){
                    String peerTarget = ((EndpointAddress) otherPeers.get(i)).getProtocolAddress();
                    if (msg.getItem("peerFrom").equals(peerTarget) != true){
                        
    
                        LOP2PMessage msgToSend = new LOP2PMessage(msg.getItem("peerFrom"), peerTarget, this.sdcfg.getNCDCfg().getMessageNamespace());
                        msgToSend.addItem("IDMSG", msgID);
                        msgToSend.addItem("TYPE", LOP2PMessage.MESSAGE_TYPE.SEARCH.toString());  
                        msgToSend.addItem("JSON_SEARCH", objetoJSON.toString());
                        msgPool.add(msgToSend);

                    }

                }


                //send the local messages to requester peer
                ObjectOutputStream objstream = null;
                for (int i = 0; i < compatibilities.size(); i++) {
                        try {
                            LOP2PMetadata mtdt = (LOP2PMetadata) compatibilities.get(i);
                            //create message
                            String ms = (String) translator.translateBack(mtdt, this.sdcfg.getNCDCfg().getNetworkManager().getPeerID().toURI().toString());
                            //create message
                            LOP2PMessage msgToSend = new LOP2PMessage(msg.getItem("peerDestination"), msg.getItem("peerFrom"), this.sdcfg.getNCDCfg().getMessageNamespace());
                            msgToSend.addItem("IDMSG", msgID);
                            msgToSend.addItem("TYPE", LOP2PMessage.MESSAGE_TYPE.SEARCH_EXPOSE.toString());
                            msgToSend.addItem("OBJECTS", ms);
                            msgToSend.addItem("JSON_SEARCH", objetoJSON.toString());
                            
                            //send message to search requester
                            msgPool.add(msgToSend);
                            System.err.println("Exposing requested search!");
                        } catch (Exception ex) {
                            Logger.getLogger(SDSearch.class.getName()).log(Level.SEVERE, null, ex);

                        }

                }

            
             }
      
        }catch(Exception ex){
            System.err.println("Search Expose: "+ex.toString());
        }                   
    }

    public void searchExposeTorrent(LOP2PMessage msg) {
        //getting metadatas
        try{
            JSONObject objetoJSON = new JSONObject(msg.getItem("JSON_SEARCH"));

             if(objetoJSON  != null){
                ArrayList compatibilities = new ArrayList();
                compatibilities.addAll(this.sdcfg.getGDData().searchInMyMetadatas(objetoJSON, this.sdcfg.getSimForRecover()));
                compatibilities.addAll(this.sdcfg.getGDData().searchInOthersMetadatas(objetoJSON, this.sdcfg.getSimForRecover()));

                //request search outside: others peers
                ArrayList msgPool = this.sdcfg.getNCDCfg().getNCDData().getMessageBufferList();

                Integer nrequests = 0;

                //send message other
                String msgID = msg.getItem("IDMSG");

                //verifies if this message was received another day
                if (this.sdcfg.getNCDCfg().getNCDData().addIfIsNewMessage(msgID) == false){
                    //preempt the proccess
                    return;
                }

                ArrayList otherPeers = sdcfg.getNCDCfg().getNCDData().getKnownPeersList();
                for (int i=0; i<otherPeers.size(); i++){
                    String peerTarget = ((EndpointAddress) otherPeers.get(i)).getProtocolAddress();
                    if (msg.getItem("peerFrom").equals(peerTarget) != true){
                        ObjectOutputStream objstream = null;

                        LOP2PMessage msgToSend = new LOP2PMessage(msg.getItem("peerFrom"), peerTarget, this.sdcfg.getNCDCfg().getMessageNamespace());
                        msgToSend.addItem("IDMSG", msgID);
                        msgToSend.addItem("TYPE", LOP2PMessage.MESSAGE_TYPE.TORRENT_REQUEST.toString());
                        msgToSend.addItem("JSON_SEARCH", objetoJSON.toString());
                        nrequests++;

                        msgPool.add(msgToSend);

                    }

                }


                //send the local messages to requester peer
                ObjectOutputStream objstream = null;
                for (int i = 0; i < compatibilities.size(); i++) {
                        try {
                            LOP2PMetadata mtdt = (LOP2PMetadata) compatibilities.get(i);
                            //create message
                            String ms = (String) translator.translateBack(mtdt, this.sdcfg.getNCDCfg().getNetworkManager().getPeerID().toURI().toString());
                            LOP2PMessage msgToSend = new LOP2PMessage(msg.getItem("peerDestination"), msg.getItem("peerFrom"), this.sdcfg.getNCDCfg().getMessageNamespace());
                            msgToSend.addItem("IDMSG", msgID);
                            msgToSend.addItem("TYPE", LOP2PMessage.MESSAGE_TYPE.TORRENT_SUBMIT.toString());
                            msgToSend.addItem("OBJECTS", ms);
                            msgToSend.addItem("JSON_SEARCH", objetoJSON.toString());

                            //Verify which blocks do we have so we can share with the requester
                            boolean[] totalBlocksDownloaded = mtdt.getBlocks();
                            for (int y = 0; y < totalBlocksDownloaded.length; y++){
                                File bloco = new File(sdcfg.getGDData().getBlocksLOs()+mtdt.getIdentifier()+"/quadro."+((y+1)*512));
                                if (bloco.exists() == true)
                                    totalBlocksDownloaded[y] = true;
                                else
                                    totalBlocksDownloaded[y] = false;
                            }
                            //mtdt.setBlocks(totalBlocksDownloaded);
                            msgToSend.addItem("BLOCKS", totalBlocksDownloaded.toString());

                            nrequests++;

                            //send message to search requester
                            msgPool.add(msgToSend);
                            System.err.println("Exposing requested search!");
                        } catch (Exception ex) {
                            Logger.getLogger(SDSearch.class.getName()).log(Level.SEVERE, null, ex);

                        }

                }



             }

        }catch(Exception ex){
            System.err.println("Search Expose: "+ex.toString());
        }
    }

    
    /**
     * Set the configuration object of Seach Device. 
     * 
     * @param sdcfg  configuration object of Seach Device
     */          

    public void setSDCfg(SDConfiguration sdcfg) {
        this.sdcfg = sdcfg;     
    }
    
    
    
    
}
