/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package protoSubmitDevice;

import protoStandardAbstractData.*;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import net.jxta.endpoint.Message.ElementIterator;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.StringMessageElement;
import protoLowareInterfaceDevice.LIDRequestHandler;
import protoSearchDevice.SDConfiguration;
import protoStandardAbstractData.LOP2PMetadata;
import protoStatisticsDevice.StDData;
import protoTranslatorDevice.*;
import protonetcommunicationdevice.*;


/**
 *
 * @author Rafael de Santiago
 */
public class SuDSubmit {
    
    //configuration
    private SuDConfiguration sdcfg;
    
    //translator
    TrDTranslator translator;
    
    private LIDRequestHandler clientSocket;
    private StDData statisticData;
    private SDConfiguration searchCfg;
    
    /**
     * Constructor of the class SuDSubmit. This class have the purpose of
     * represent Submit/Store Device.
     * 
     * @param clientSocket socket with loware application
     * @param sdcfg configuration object of the Search Device
     * @param trdcfg configuration object of the Translator Device
     */                  
    public SuDSubmit (LIDRequestHandler clientSocket, SuDConfiguration sdcfg, TrDConfiguration trdcfg, SDConfiguration sdcong){
        this.sdcfg = sdcfg;    
        this.translator = new TrDTranslator(trdcfg);
        this.clientSocket = clientSocket;
        this.searchCfg = sdcong;
    }

    
    /**
     * Treat the Store requisition from loware.
     * 
     * @param message message from loware
     */      
    public void store(String message) throws FileNotFoundException, IOException, ClassNotFoundException, Exception{

        //send the responses for client
        DataOutputStream toCliente = null;
        //toCliente = new DataOutputStream(this.clientSocket.getOutputStream());
        toCliente = this.clientSocket.getOut();
        
        
        ArrayList results = translator.translateStoreRequest(message);
        String peerid = null;
        String loid   = null;
        if (results.size() == 2) {
            loid = (String) results.get(0);
            peerid = (String) results.get(1);
        } else {
            //generate exception
            String error = "The message sent not correspond to a valid Store communication.";
            synchronized(toCliente){
                toCliente.write(error.getBytes());                     
            }
            String bye = "O Download dos blocos foi iniciado, verifique a GUI para status";
            synchronized(toCliente){
                toCliente.write(bye.getBytes());         
                toCliente.close();
            }
            //clientSocket.close();
           throw new Exception(error);
        }

        //ending message for requester (LOWare)
        String bye = "O Download dos blocos foi iniciado, verifique a GUI para status";
        synchronized(toCliente){
            toCliente.write(bye.getBytes());
            toCliente.close();
        }

        //it runs the download thread
        SuDDownloadHandler sdh = new SuDDownloadHandler(loid, this.sdcfg, this.statisticData, this.translator, this.searchCfg);
        sdh.start();
/*
            String translatedMessage = "";
            //if the peer is that, send the object, if not, search!
            //send message
            ArrayList msgPool = this.sdcfg.getNCDCfg().getNCDData().getMessageBufferList();
            Date date = new Date();
            String msgID = "msgSentStore" + this.sdcfg.getNCDCfg().NT_INSTANCE_NAME + date.getTime();       
            String peerTarget = peerid;
            peerTarget = peerTarget.substring(peerTarget.indexOf("uuid"));
            String thisPeer = this.sdcfg.getNCDCfg().getNetworkManager().getPeerID().toURI().toString();
            thisPeer = thisPeer.substring(thisPeer.indexOf("uuid"));
            LOP2PMessage msgToSend = new LOP2PMessage(thisPeer, peerTarget, this.sdcfg.getNCDCfg().getMessageNamespace());
            msgToSend.addItem("IDMSG", msgID);
            msgToSend.addItem("TYPE", LOP2PMessage.MESSAGE_TYPE.TORRENT_DOWNLOAD.toString());
            msgToSend.addItem("LOID", loid);


            msgPool.add(msgToSend);
            
            //insert new mapping for download
            this.statisticData = StDData.instanceOfStDData();
            this.statisticData.addNewDownload(msgID, peerTarget);
            
            //wait response for some seconds
            Calendar targetCal = new GregorianCalendar();
            targetCal.add(Calendar.SECOND, this.getSuDCfg().getExpirationTime());
            Date target = targetCal.getTime();
            Date now = new Date();
            translatedMessage = "";
            Double downloadPerc = 0.0;
            boolean aptoAParar = false; //stop only if the message was recived
            while (aptoAParar != true){//(target.getTime() > now.getTime())){
                LOP2PMessage msg = (LOP2PMessage)this.sdcfg.getNCDCfg().getNCDData().searchForResponseById(msgID);
                if (msg != null){
                    aptoAParar = true;
                    //getting metadatas
                    ElementIterator itMsg =  msg.getMessageElements();

                    while(itMsg.hasNext()){
                        MessageElement el = itMsg.next();
                        if (el.getElementName().equals("fileRefence")){
                            //recover the element
                            StringMessageElement sme = 
                                                            (StringMessageElement) el;                    
                            String filePath = new String(sme.getBytes(true));

                            //copy object to repository peer                
                            File loFile = new File(filePath);
                            String newName = "LOP2P" + now.getTime()+loFile.getName();
                            String newPath = this.getSuDCfg().getLearningObjectsDirectory()+"/"+newName;
                            File newLO = new File (newPath);
                            copyFile(loFile, newLO);
                            
                            //return the location in this computer
                            translatedMessage = newLO.getAbsolutePath();//(String) translator.translateBackStore(newLO.getAbsolutePath());
                            break;
                        }                    
                    }
                    //send statistics if necessary
                    Double downloadAux = this.statisticData.getDownloadByIDs(msgID, peerTarget).getProgress();
                    if (downloadPerc != downloadAux){
                        downloadPerc = downloadAux;
                        //send statistic
                        synchronized(toCliente){
                            toCliente.write(downloadPerc.toString().getBytes());                        
                        }
                    }

                }            
                now = new Date();
            }  
            //erase statistic
            this.statisticData.finishDownload(msgID, peerTarget);  

*/
        

        //clientSocket.close();
    }

    /**
     * return the configuration object of the Submit/Store Device.
     * 
     * @return configuration object of the Submit/Store Device
     */        
    public SuDConfiguration getSuDCfg() {
        return sdcfg;
    }

    /**
     * Receive a message from another peer and forward the Learning Object file.
     * 
     * @param msg message from requester (peer requester)
     */          
    public void storeSubmit(LOP2PMessage msg) {
        try{
            //getting metadatas

            String loid = msg.getItem("LOID");

            String msgid = msg.getItem("IDMSG");

            String block = msg.getItem("BLOCK");
            //verifies if this message was received another day
            if (this.sdcfg.getNCDCfg().getNCDData().addIfIsNewMessage(msgid) == false){
                //preempt the proccess
                return;
            }


            //retrieving metadata from object
            LOP2PMetadata mtdt = this.sdcfg.getGDData().getMyMetadataByID(loid);
            String location = this.sdcfg.getGDData().getBlocksLOs()+mtdt.getIdentifier()+"/quadro."+block;


            ArrayList msgPool = this.sdcfg.getNCDCfg().getNCDData().getMessageBufferList();

            //create message
            LOP2PMessage msgToSend = new LOP2PMessage(msg.getItem("peerDestination"), msg.getItem("peerFrom"), this.sdcfg.getNCDCfg().getMessageNamespace());
            msgToSend.addItem("IDMSG", msgid);
            msgToSend.addItem("TYPE", LOP2PMessage.MESSAGE_TYPE.TORRENT_UPLOAD.toString());
            //pegar bloco
            File Block = new File(location);
            msgToSend.addAttachment(Block);

            //send message to search requester
            msgPool.add(msgToSend);
            System.err.println("Sending Block "+block+"!");
        }catch(Exception ex){
            System.out.println("SubmitiStore: "+ex.toString());
        }            

    }


    void copyFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();

    }
    
    /**
     * Set the configuration object of the Submit/Store Device to be used.
     * 
     * @param sdcfg configuration object of the Submit/Store Device to be used
     */          
    
    public void setSuDCfg(SuDConfiguration sdcfg) {
        this.sdcfg = sdcfg;
    }
    
    
    
    
}
