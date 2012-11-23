/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package protoGatherDevice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import protoStandardAbstractData.LOP2PMetadata;
import protonetcommunicationdevice.LOP2PMessage;

/**
 *
 * @author Rafael de Santiago
 */

    
        

public class GDData {
    private ArrayList myMetadatas;
    private ArrayList othersMetadatas;
    private String myMetadataPath;
    private String othersMetadatasPath;
    private String blocksLOs = "./blocks/";
    
    /**
     * Constructor of the class GDData. This class have the purpose to store 
     * important data for Gather Device: for example known metadatas.
     * 
     * @param   myMetadataPath          Directory where the loware's metadatas are stored
     * @param   othersMetadatasPath     Directory where known others peers metadatas are stored
     */    
    public GDData(String myMetadataPath, String othersMetadatasPath){
        //if not exists, create directories
        this.createDiretory(myMetadataPath);
        this.createDiretory(othersMetadatasPath);
        this.createDiretory("./blocks");
        
        this.myMetadataPath = myMetadataPath+"/MyMetadatas.ser";
        this.othersMetadatasPath = othersMetadatasPath+"/OthersMetadatas.ser";
        
        //try to load the data,
        // if not exists create a new data
        this.restoreMyMetadatas();
        this.restoreOthersMetadatas();
        
        if (this.getMyMetadatas() == null){
            this.setMyMetadatas(new ArrayList());
        }
        if (this.getOthersMetadatas() == null){
            this.setOthersMetadatas(new ArrayList());
        }        
    }
    
    /**
     * Create diretory in file system
     * 
     * @param   path          Directory that will be create
     */        
    private void createDiretory(String path){
        File dir = new File(path);
        if (dir.exists() == false){
            dir.mkdir();
        }
    }        
    
    /**
     * Retrieve all loware's metadatas to this class.
     * 
     */          
    private void restoreMyMetadatas(){
        String file = this.myMetadataPath;
        try {
            ObjectInputStream objstream = null;

            objstream = new ObjectInputStream(new FileInputStream(file));
            this.setMyMetadatas((ArrayList) objstream.readObject());
            objstream.close();
            return;
        } catch (IOException ex) {
            Logger.getLogger(GDData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(GDData.class.getName()).log(Level.SEVERE, null, ex);      
        }        
        this.setMyMetadatas(new ArrayList());
    }
    
    /**
     * Retrieve all others known metadatas to this class.
     * 
     */            
    private void restoreOthersMetadatas(){
        String file = this.othersMetadatasPath;
        try {
            ObjectInputStream objstream = null;

            objstream = new ObjectInputStream(new FileInputStream(file));
            this.setOthersMetadatas((ArrayList) objstream.readObject());
            objstream.close();
            return;
        } catch (IOException ex) {
            Logger.getLogger(GDData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(GDData.class.getName()).log(Level.SEVERE, null, ex);      
        }        
        this.setOthersMetadatas(new ArrayList());
    }    

    /**
     * Save the loware's metadatas in file system.
     * 
     */      
    public void saveMyMetadata(){
        try {
            ObjectOutputStream objstream = new ObjectOutputStream(new FileOutputStream(this.myMetadataPath));
            objstream.writeObject(this.getMyMetadatas());
            objstream.close();
        } catch (IOException ex) {
            Logger.getLogger(GDData.class.getName()).log(Level.SEVERE, null, ex);
        }
    }   

    /**
     * Save the others known metadatas in file system.
     * 
     */           
    public void saveOthersMetadatas(){
         try {
            ObjectOutputStream objstream = new ObjectOutputStream(new FileOutputStream(this.othersMetadatasPath));
            objstream.writeObject(this.getOthersMetadatas());
            objstream.close();
        } catch (IOException ex) {
            Logger.getLogger(GDData.class.getName()).log(Level.SEVERE, null, ex);
        }       
    }
    
    /**
     * Search for a metadata in the list of loware's metadatas using a ID.
     * 
     * @param loid  ID of the metadata
     * @return      Return the metadata located. If no metadata is located, null is returned
     */          
    public LOP2PMetadata getMyMetadataByID (String loid){
        ArrayList metadts = this.getMyMetadatas();
        for (int i=0; i<metadts.size(); i++){
            LOP2PMetadata mtdt = (LOP2PMetadata) metadts.get(i);
            if (mtdt.getIdentifier().equals(loid)){
                return mtdt;
            }
        }
        return null;
    }
    
    
    
     /**
     * Save all lists of metadatas (lowares or known) in file system.
     * 
     */      
    public void saveAll(){
        this.saveMyMetadata();
        this.saveOthersMetadatas();
    }

    /**
     * Return the list of loware's metadatas.
     * 
     * @return      list of loware's metadatas
     */      
    public ArrayList getMyMetadatas() {
        return myMetadatas;
    }

    /**
     * Set the list of loware's metadatas.
     * 
     * @param myMetadatas the new metadatas list
     */     
    public void setMyMetadatas(ArrayList myMetadatas) {
        this.myMetadatas = myMetadatas;
    }

    /**
     * Return the list of the others known metadatas.
     * 
     * @return      list of others known metadatas
     */          
    public ArrayList getOthersMetadatas() {
        return othersMetadatas;
    }

    
    /**
     * Set the list of others known metadatas.
     * 
     * @param othersMetadatas the new metadatas list of others known metadatas
     */         
    public void setOthersMetadatas(ArrayList othersMetadatas) {
        this.othersMetadatas = othersMetadatas;
    }
    /**
     * Search for compatibles metadatas in list of loware's metadatas.
     * 
     * @param mtdt          metadata that will be compared with others
     * @param simForRecover similarity level expected for retrieve metadata
     * 
     * @return              list of metadatas compatible with the parameter mtdt
     */             
    public ArrayList searchInMyMetadatas(JSONObject msgJSON, Double simForRecover){
        ArrayList results = new ArrayList();
        for(int i=0; i<this.myMetadatas.size(); i++){
            LOP2PMetadata myMtdt = (LOP2PMetadata) this.myMetadatas.get(i);
            Double similarity = myMtdt.compare(msgJSON);
            if (similarity >= simForRecover){
                results.add(myMtdt);
            }
        }
       // results = this.myMetadatas;
       // System.out.println(results);
        return results;
    }
    
    /**
     * Search for compatibles metadatas in list of known metadatas.
     * 
     * @param mtdt          metadata that will be compared with others
     * @param simForRecover similarity level expected for retrieve metadata
     * 
     * @return              list of metadatas compatible with the parameter mtdt
     */             
    public ArrayList searchInOthersMetadatas(JSONObject msgJSON, Double simForRecover){
        ArrayList results = new  ArrayList();
        for(int i=0; i<this.othersMetadatas.size(); i++){
            LOP2PMetadata otherMtdt = (LOP2PMetadata) this.othersMetadatas.get(i);
            Double similarity = otherMtdt.compare(msgJSON);
            if (similarity >= simForRecover){
                results.add(otherMtdt);
            }
        }
        return results;
    }

    /**
     * @return the blocksLOs
     */
    public String getBlocksLOs() {
        return blocksLOs;
    }

    /**
     * @param blocksLOs the blocksLOs to set
     */
    public void setBlocksLOs(String blocksLOs) {
        this.blocksLOs = blocksLOs;
    }
}
