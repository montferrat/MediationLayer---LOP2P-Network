/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package protoLowareInterfaceDevice;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import protoGatherDevice.*;

import protoSearchDevice.SDConfiguration;
import protoSearchDevice.SDSearch;
import protoSearchDevice.SDTorrentRequest;
import protoStatisticsDevice.StDStatistics;
import protoSubmitDevice.SuDConfiguration;
import protoSubmitDevice.SuDSubmit;
import protonetcommunicationdevice.LOP2PMessage;

/**
 *
 * @author Rafael de Santiago
 */
public class LIDRequestHandler extends Thread{
    
    private LIDConfiguration lidcfg;
    private Socket socket;
    static Vector handlers = new Vector(10);
    private BufferedReader in;
    private DataOutputStream out;
 

    
    /**
     * Constructor of the class LIDRequestHandler. This class have the 
     * purpose treat the lowares requests
     * 
     * @param lidcfg configuration object of the Loware Interface Device
     * @param socket socket object of the connection
     */       
    public LIDRequestHandler(LIDConfiguration lidcfg, Socket socket){
        try {
            this.socket = socket;
            this.lidcfg = lidcfg;
            
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.out = new DataOutputStream(this.socket.getOutputStream());
            
            this.setPriority(MIN_PRIORITY);
        } catch (IOException ex) {
            Logger.getLogger(LIDRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Execution method of the thread LIDReceiveConnection
     * 
     */           
    @Override
    public void run(){
        //receive the message
    /*    synchronized(handlers) {
            handlers.addElement(this);
            // add() not found in Vector class
        }
        for(int i = 0; i < handlers.size(); i++) {
            synchronized(handlers) {*/
                LIDRequestHandler handler = this;//(LIDRequestHandler)handlers.elementAt(i);
                
                BufferedReader doCliente = null;
                try {
                    doCliente = handler.getIn();//new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                    String bruteMessage = "";
                    char tmp = ' ' ;
                    sleep(1000L);
                    while (doCliente.ready()) {
                        synchronized(doCliente){
                            tmp = (char) doCliente.read();
                        }
                        bruteMessage += tmp;
                    }
                    DataOutputStream toCliente = handler.getOut();//new DataOutputStream(this.socket.getOutputStream());

                    //working with that message
                    String messageType = bruteMessage.substring(0, 25);
                    String message = bruteMessage.substring(25);        

        /*            LIDThreadServices lidts = new LIDThreadServices(message, messageType, lidcfg, socket, toCliente, doCliente);
                    lidts.start();*/
                    //identify the type of request and sending to specific handling
                    switch(LOP2PMessage.MESSAGE_TYPE.valueOf(messageType.trim())){
                        case GATHER:
                            //calls handler for that function
                            GDGather gather = new GDGather(lidcfg.getNCDCfg().NT_INSTANCE_NAME, this.lidcfg.getGdCfg(), this.lidcfg.getTrdCfg());
                            gather.receiveMetadataFromLoware(message);
                            String msgReturn = "LOP2P: Congratulations, metadata list stored with success!";
                            synchronized(toCliente){
                                toCliente.write(msgReturn.getBytes());                            
                            }
                            break;        

                        case SEARCH:
                            //calls handler for that function
                            //System.out.println (message);
                            SDSearch search = new SDSearch(this, new SDConfiguration(this.lidcfg.getGdCfg(), lidcfg.getNCDCfg()), this.lidcfg.getTrdCfg());
                            search.search(message);
                            break;

                        case STORE:
                            
                            //calls handler for that function
                            //(LIDRequestHandler clientSocket, SuDConfiguration sdcfg, TrDConfiguration trdcfg, SDConfiguration sdcong)
                            SuDSubmit submit = new SuDSubmit(this, new SuDConfiguration(this.lidcfg.getGdCfg(), lidcfg.getNCDCfg()), this.lidcfg.getTrdCfg(), new SDConfiguration(this.lidcfg.getGdCfg(), this.lidcfg.getNCDCfg()));
                            submit.store(message);

                            break;


//                        case TORRENT_REQUEST:
//                             //calls handler for that function
//                            SDTorrentRequest searchTorrentRequest = new SDTorrentRequest(this, new SDConfiguration(this.lidcfg.getGdCfg(), lidcfg.getNCDCfg()), this.lidcfg.getTrdCfg());
//                            searchTorrentRequest.search(message);
//                            break;

                        case TORRENT_SUBMIT:

                            //Adicionar

                            break;

                        case TORRENT_DOWNLOAD:

                            //Adicionar

                            break;

                        case TORRENT_UPLOAD:

                            //Adicionar

                            break;


                        case STATISTICS_EXPOSE:
                            //calls handler to expose all downloads of mediation layer
                            StDStatistics statistics = new StDStatistics(this);
                            statistics.sendStatisticsOfDownloadsAndUploads();
                            break;

                    }



                } catch (FileNotFoundException ex) {
                    Logger.getLogger(LIDRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(LIDRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(LIDRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(LIDRequestHandler.class.getName()).log(Level.SEVERE, null, ex);

                } finally {
                    try {
                        doCliente.close();
                    } catch (IOException ex) {
                        Logger.getLogger(LIDRequestHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }finally{
                     /*  synchronized(handlers) {
                           handlers.removeElement(this);
                       }
                    }*/
                }
            // } for
        }

        System.out.println("LOWARE connection closed.");
        
        
    }

    public BufferedReader getIn() {
        return in;
    }

    public void setIn(BufferedReader in) {
        this.in = in;
    }

    public DataOutputStream getOut() {
        return out;
    }

    public void setOut(DataOutputStream out) {
        this.out = out;
    }
}
