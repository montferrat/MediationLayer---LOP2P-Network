/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package protoGatherDevice;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import protoStandardAbstractData.LOP2PMetadata;
import protoTranslatorDevice.*;

/**
 *
 * @author Rafael de Santiago
 */
public class GDGather {
    
    private GDConfiguration gdcfg;
    
    private TrDTranslator translator;
    String peername;
    
    
    /**
     * Constructor of the class GDGather. This class have the purpose to 
     * represent the Gather Device. 
     * 
     * @param   peername    name of the the peer
     * @param   gdcfg       configuration object of the Gather Device
     * @param   trdcfg      configuration object of the Translator Device
     */        
    public GDGather(String peername, GDConfiguration gdcfg, TrDConfiguration trdcfg){
        this.peername = peername;
        this.gdcfg = gdcfg;
        this.translator = new TrDTranslator(trdcfg);
    }
    
    /**
     * Receive the message of the loware with the purpose to store the local 
     * metadatas. 
     * 
     * @param   message    message received from loware
     */      
    
    public void receiveMetadataFromLoware(String message){
        ArrayList metadatas = translator.translate(message);
        this.gdcfg.getGdData().getMyMetadatas().clear();
        for(int i=0; i<metadatas.size(); i++){
            //setNewId
            LOP2PMetadata mtdt = (LOP2PMetadata)metadatas.get(i);
            Date now = new Date();
            mtdt.setNewLOP2PID(this.peername, now.getTime(), i);
            
            //include in myMetadata
            this.gdcfg.getGdData().getMyMetadatas().add(metadatas.get(i));
        }
        this.gdcfg.getGdData().saveAll();
    }
    

    
}
