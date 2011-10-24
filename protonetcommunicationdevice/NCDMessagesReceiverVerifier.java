/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package protonetcommunicationdevice;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.endpoint.ByteArrayMessageElement;
import net.jxta.endpoint.Message.ElementIterator;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.StringMessageElement;
import protoLowareInterfaceDevice.LIDConfiguration;
import protoSearchDevice.SDConfiguration;
import protoSearchDevice.SDSearch;
import protoStandardAbstractData.LOP2PMetadata;
import protoSubmitDevice.SuDConfiguration;
import protoSubmitDevice.SuDSubmit;

/**
 *
 * @author Rafael de Santiago
 */
public class NCDMessagesReceiverVerifier extends Thread{
    private NCDConfiguration ncdcfg;
    
    private LIDConfiguration lidcfg;

    
    /**
     * Constructor for the NCDMessagesReceiverVerifier. Objects from this class 
     * verify if a new message arrives and request treatment by the 
     * NCDMessageReceiver
     * 
     * @param lidcfg configuration object for Loware Interface Device
     */        
    public NCDMessagesReceiverVerifier(LIDConfiguration lidcfg){
        this.setPriority(MIN_PRIORITY);  
        this.ncdcfg = lidcfg.getNCDCfg();
        this.lidcfg = lidcfg;
    }
    
    /**
     * Execution Method of the thread NCDMessagesReceiverVerifier.
     */    
    public void run() {
        while(true){
            try {
                
                ArrayList msgsReceiveds = this.ncdcfg.getNCDData().getMessageReceiverBufferList();
                
                
                int size;
                synchronized(msgsReceiveds){
                    size = msgsReceiveds.size();
                }                
                for (int i = 0; i < size; i++) {
                    LOP2PMessage msg;
                    synchronized(msgsReceiveds){
                         msg = (LOP2PMessage) msgsReceiveds.get(i);
                    }
                    if (msg.getItem("TYPE").equals(LOP2PMessage.MESSAGE_TYPE.SEARCH.toString())) {
                        synchronized(msgsReceiveds){
                            msgsReceiveds.remove(i);
                        }
                        i--;
                        try {
                            //calls handler for that function
                            SDSearch search = new SDSearch(null, new SDConfiguration(this.lidcfg.getGdCfg(), this.ncdcfg), this.lidcfg.getTrdCfg());
                            search.searchExpose(msg);
                        }catch(NullPointerException ex){
                            System.err.println("NCDMessagesReceiverVerifier error[NullPointerException 1]: "+ex.toString());
                            Logger.getLogger(NCDMessagesReceiverVerifier.class.getName()).log(Level.SEVERE, null, ex);        
                        }
                    }
                    
                    if (msg.getItem("TYPE").equals(LOP2PMessage.MESSAGE_TYPE.TORRENT_DOWNLOAD.toString())) {
                        synchronized(msgsReceiveds){
                            msgsReceiveds.remove(i);
                        }
                        i--;
                        try {
                            //calls handler for that function
                            SuDSubmit submit = new SuDSubmit(null, new SuDConfiguration(this.lidcfg.getGdCfg(), this.ncdcfg), this.lidcfg.getTrdCfg(), new SDConfiguration(this.lidcfg.getGdCfg(), this.lidcfg.getNCDCfg()));
                            submit.storeSubmit(msg);
                        }catch(NullPointerException ex){
                            System.err.println("NCDMessagesReceiverVerifier error[NullPointerException: 2]: "+ex.toString());
                            Logger.getLogger(NCDMessagesReceiverVerifier.class.getName()).log(Level.SEVERE, null, ex);        
                        }
                    }



                    //TORRENT_DOWNLOAD
//
//                    if (msg.getItem("TYPE").equals(LOP2PMessage.MESSAGE_TYPE.TORRENT_DOWNLOAD.toString())) {
//                        synchronized(msgsReceiveds){
//                            msgsReceiveds.remove(i);
//                        }
//                        i--;
//                        try {
//                            //calls handler for that function
//                            SuDSubmit submit = new SuDSubmit(null, new SuDConfiguration(this.lidcfg.getGdCfg(), this.ncdcfg), this.lidcfg.getTrdCfg());
//                            submit.storeSubmit(msg);
//                        }catch(NullPointerException ex){
//                            System.err.println("NCDMessagesReceiverVerifier error[NullPointerException: 2]: "+ex.toString());
//                            Logger.getLogger(NCDMessagesReceiverVerifier.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                    }
//
//
//                    //TORRENT_UPLOAD
//
//                    if (msg.getItem("TYPE").equals(LOP2PMessage.MESSAGE_TYPE.TORRENT_UPLOAD.toString())) {
//                        synchronized(msgsReceiveds){
//                            msgsReceiveds.remove(i);
//                        }
//                        i--;
//                        try {
//                            //calls handler for that function
//                            SuDSubmit submit = new SuDSubmit(null, new SuDConfiguration(this.lidcfg.getGdCfg(), this.ncdcfg), this.lidcfg.getTrdCfg());
//                            submit.storeSubmit(msg);
//                        }catch(NullPointerException ex){
//                            System.err.println("NCDMessagesReceiverVerifier error[NullPointerException: 2]: "+ex.toString());
//                            Logger.getLogger(NCDMessagesReceiverVerifier.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                    }


                    // TORRENT_SUBMIT
//desativado: a procura por novos pedaços ocorrerá de forma conjunta com o download
//                    if (msg.getItem("TYPE").equals(LOP2PMessage.MESSAGE_TYPE.TORRENT_SUBMIT.toString())) {
//                        synchronized(msgsReceiveds){
//                            msgsReceiveds.remove(i);
//                        }
//                        i--;
//                        try {
//                            //calls handler for that function
//                            SuDSubmit submit = new SuDSubmit(null, new SuDConfiguration(this.lidcfg.getGdCfg(), this.ncdcfg), this.lidcfg.getTrdCfg());
//                            submit.storeSubmitTorrentSubmit(msg);
//                        }catch(NullPointerException ex){
//                            System.err.println("NCDMessagesReceiverVerifier error[NullPointerException: 2]: "+ex.toString());
//                            Logger.getLogger(NCDMessagesReceiverVerifier.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                    }


//
//                    // TORRENT_REQUEST
//
                    if (msg.getItem("TYPE").equals(LOP2PMessage.MESSAGE_TYPE.TORRENT_REQUEST.toString())) {
                        synchronized(msgsReceiveds){
                            msgsReceiveds.remove(i);
                        }
                        i--;
                        try {
                            //calls handler for that function
                            SDSearch search = new SDSearch(null, new SDConfiguration(this.lidcfg.getGdCfg(), this.ncdcfg), this.lidcfg.getTrdCfg());
                            search.searchExposeTorrent(msg);
                        }catch(NullPointerException ex){
                            System.err.println("NCDMessagesReceiverVerifier error[NullPointerException 1]: "+ex.toString());
                            Logger.getLogger(NCDMessagesReceiverVerifier.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                    synchronized(msgsReceiveds){
                        size = msgsReceiveds.size();
                    }
                }
                sleep(this.ncdcfg.getThreadSleepTime());
            } catch (InterruptedException ex) {
                System.err.println("NCDMessagesReceiverVerifier error [InterruptedException]: "+ex.toString());
                Logger.getLogger(NCDMessagesReceiverVerifier.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
