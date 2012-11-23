/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package  protoSearchDevice;

import protoGatherDevice.*;
import protonetcommunicationdevice.NCDConfiguration;


/**
 *
 * @author Rafael de Santiago
 */


public class SDConfiguration {
    //configuration of Gather Device. Important to locate Gather
    private GDConfiguration gdcfg;
    
    //configuration of Netword Communication Device configuration
    private NCDConfiguration ncfcfg;
    
    //similarity requested for recover metadatas in the field
    private Double simForRecover = 99.9998;
    
    //max time
    private Integer expirationTime = 30;//240; //four minutes
    
    private Integer expirationTime2 = 7;//seven seconds //120; //two minutes

    private Integer expirationTimeTorrent = 60;//60; //sixty seconds
    
    /**
     * Constructor of the class SDConfiguration. This class have the purpose of
     * maintain the configuration for the Search Device. 
     * 
     * @param   gdcfg       configuration object of the Gather Device
     * @param   ncfcfg      configuration object of the Network Communication Device
     */           
    public SDConfiguration(GDConfiguration gdcfg, NCDConfiguration ncfcfg){
        this.gdcfg = gdcfg;
        this.ncfcfg = ncfcfg;
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
     * Set the data object of the Gather Device.
     * 
     * @param gddata data object of the Gather Device
     */     
    public void setGDData(GDData gddata) {
        getGDCfg().setGdData(gddata);
    }
    
    /**
     * Return the similarity range for acceptance of a metadata when a seach 
     * happens.
     * 
     * @return the similarity range for metadata search
     */ 
    public Double getSimForRecover() {
        return simForRecover;
    }

    /**
     * Set the similarity range for acceptance of a metadata when a seach 
     * happens.
     * 
     * @param simForRecover the new similarity range for metadata search
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
     * Set the configuration object of the Gather Device.
     * 
     * @param gdcfg configuration object of the Gather Device
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
     * Set the configuration object of the Network Communication Device.
     * 
     * @param ncfcfg configuration object of the Network Communication Device
     */      
    public void setNCDCfg(NCDConfiguration ncfcfg) {
        this.ncfcfg = ncfcfg;
    }

    /**
     * Return the expiration time for a search.
     * 
     * @return expiration time for a search.
     */          
    public Integer getExpirationTime() {
        return expirationTime;
    }

    /**
     * Set the expiration time for a search.
     * 
     * @param expirationTime new expiration time for a search
     */  
    public void setExpirationTime(Integer expirationTime) {
        this.expirationTime = expirationTime;
    }

    public Integer getExpirationTime2() {
        return expirationTime2;
    }

    public void setExpirationTime2(Integer expirationTime2) {
        this.expirationTime2 = expirationTime2;
    }

    public Integer getExpirationTimeTorrent() {
        return expirationTimeTorrent;
    }

    public void setExpirationTimeTorrent(Integer expirationTimeTorrent) {
        this.expirationTimeTorrent = expirationTimeTorrent;
    }

}
