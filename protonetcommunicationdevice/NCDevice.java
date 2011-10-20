/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package protonetcommunicationdevice;

import protoLowareInterfaceDevice.LIDConfiguration;

/**
 *
 * @author Rafael de Santiago
 */
public class NCDevice {
    
    protected NCDData netdata;
    
    private NCDConfiguration ncdcfg;
    
    //private NCDPeerInterface ncdPeerInterface;
    
    private LIDConfiguration lidcfg;

    
    /**
     * Constructor for the NCDevice. Class that represent the Network 
     * Commnication Device
     * 
     */     
    public NCDevice(){
        //data storage for network
        netdata = new NCDData();         
                        
        //configuration object for LOP2P net
        ncdcfg = new NCDConfiguration(this.netdata);  
        
    /*    //creates peer interface for that device
        this.ncdPeerInterface = new NCDPeerInterface(this.ncdcfg);
      */  
    }

    /**
     * Execution method of the thread NCDevice.
     */     
    public void start(){
        
        //discovery other peers
        NCDDiscovery dscvry = new NCDDiscovery(this.getNCDCfg());
        dscvry.start();    
        
        //adverstise to others peers
        NCDAdvertisements advertmts = new NCDAdvertisements(this.getNCDCfg());
        advertmts.start(); 
        
        //receiver message component
        NCDMessageReceiver mreceiver = new NCDMessageReceiver(this.getNCDCfg());
        mreceiver.start();
        
        //sender message
        NCDMessagesSender msender = new NCDMessagesSender(this.getNCDCfg());
        msender.start();
        
        //treat messages receiveds
        NCDMessagesReceiverVerifier mrverifier = new NCDMessagesReceiverVerifier(this.getLIDCfg());
        mrverifier.start();
        
    }
    
    
    /**
     * Return the configuration object of the Network Communication Device.
     * 
     * @return configuration object of the Network Communication Device
     */      
    public NCDConfiguration getNCDCfg() {
        return ncdcfg;
    }

    /**
     * Set the configuration object of the Network Communication Device.
     * 
     * @param ncdcfg configuration object of the Network Communication Device
     */         
    public void setNCDCfg(NCDConfiguration ncdcfg) {
        this.ncdcfg = ncdcfg;
    }


    /**
     * Return the configuration object of the Loware Interface Device.
     * 
     * @return configuration object of the Loware Interface Device
     */ 
    public LIDConfiguration getLIDCfg() {
        return lidcfg;
    }
    
    /**
     * Set the configuration object of the Loware Interface Device.
     * 
     * @param lidcfg configuration object of the Loware Interface Device
     */
    public void setLIDCfg(LIDConfiguration lidcfg) {
        this.lidcfg = lidcfg;
    }
    
    
}
