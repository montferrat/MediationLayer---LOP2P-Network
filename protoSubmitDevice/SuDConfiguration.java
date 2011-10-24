/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package protoSubmitDevice;

import java.io.File;
import protoGatherDevice.*;
import protonetcommunicationdevice.NCDConfiguration;


/**
 *
 * @author Rafael de Santiago
 */


public class SuDConfiguration {
    //configuration of Gather Device. Important to locate Gather
    private GDConfiguration gdcfg;
    
    //configuration of Netword Communication Device configuration
    private NCDConfiguration ncfcfg;
    
    //similarity requested for recover metadatas in the field
    private Double simForRecover = 99.9998;
    
    //max time
    private Integer expirationTime = 60;//one minutes

    //max time for Torrent Download and Upload
    private Integer expirationTimeTorrent = 20;//20 seconds

    //directory to store the Learning Objects retrieved
    private String learningObjectsDirectory = "./retrievedLOs";
    
    /**
     * Constructor of the class SuDConfiguration. This class have the purpose of
     * store configuration settings of the Submit/Store Device.
     * 
     * @param gdcfg configuration object of the Gather Device
     * @param ncfcfg configuration object of the Network Communication Device
     */          
    public SuDConfiguration(GDConfiguration gdcfg, NCDConfiguration ncfcfg){
        this.gdcfg = gdcfg;
        this.ncfcfg = ncfcfg;
        
        this.createDiretory(this.getLearningObjectsDirectory());
    }

    /**
     * Creates a new directory in file system.
     * 
     * @param path directory that will be created
     */       
    private void createDiretory(String path){
        File dir = new File(path);
        if (dir.exists() == false){
            dir.mkdir();
        }
    }     

    
    /**
     * Return the data object of the Gather Device.
     * 
     * @return data object of the Gather Device
     */           
    public GDData getGDData() {
        return getGDCfg().getGdData();
    }

    /**
     * Sets the data object of the Gather Device.
     * 
     * @param gddata data object of the Gather Device
     */         
    public void setGDData(GDData gddata) {
        getGDCfg().setGdData(gddata);
    }

    /**
     * Return the similarity of the Search Device.
     * 
     * @return similarity of the Search Device
     */         
    public Double getSimForRecover() {
        return simForRecover;
    }

    /**
     * Set the similarity.
     * 
     * @param simForRecover similarity level
     */       
    public void setSimForRecover(Double simForRecover) {
        this.simForRecover = simForRecover;
    }

    /**
     * Return the configuration object of the Gather Device.
     * 
     * @return configuration object of the Gather Device
     */             
    public GDConfiguration getGDCfg() {
        return gdcfg;
    }

    /**
     * Set the configuration object of the Gather Device used.
     * 
     * @param gdcfg configuration object of the Gather Device used
     */       
    public void setGDCfg(GDConfiguration gdcfg) {
        this.gdcfg = gdcfg;
    }
    
    /**
     * Return the configuration object of the Network Communication Device.
     * 
     * @return configuration object of the Network Communication Device
     */   
    public NCDConfiguration getNCDCfg() {
        return ncfcfg;
    }

    /**
     * Set the configuration object of the Network Communication Device used.
     * 
     * @param ncfcfg configuration object of the Network Communication Device used
     */   
    public void setNCDCfg(NCDConfiguration ncfcfg) {
        this.ncfcfg = ncfcfg;
    }

    /**
     * Return the standard expiration time of the Submit/Store.
     * 
     * @return standard expiration time of the Submit/Store
     */   
    public Integer getExpirationTime() {
        return expirationTime;
    }

    /**
     * Set the standard expiration time of the Submit/Store.
     * 
     * @param expirationTime expiration time of the Submit/Store
     */       
    public void setExpirationTime(Integer expirationTime) {
        this.expirationTime = expirationTime;
    }

    /**
     * Return the standard expiration time of the Submit/Store.
     *
     * @return standard expiration time of the Submit/Store TORRENT
     */
    public Integer getExpirationTimeTorrent() {
        return expirationTimeTorrent;
    }

    /**
     * Set the standard expiration time of the Submit/Store.
     *
     * @param expirationTimeTorrent expiration time of the Submit/Store
     */
    public void setExpirationTimeTorrent(Integer expirationTimeTorrent) {
        this.expirationTimeTorrent = expirationTimeTorrent;
    }

    /**
     * Return the location in the file system that the Learning Objects 
     * transmitted will be stored.
     * 
     * @return path to store
     */       
    public String getLearningObjectsDirectory() {
        return learningObjectsDirectory;
    }

    /**
     * Set the location in the file system that the Learning Objects will be store 
     * 
     * @param learningObjectsDirectory location in the file system that the Learning Objects will be store 
     */           
    public void setLearningObjectsDirectory(String learningObjectsDirectory) {
        this.learningObjectsDirectory = learningObjectsDirectory;
    }
}
