/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package protoLowareInterfaceDevice;





import protoGatherDevice.GDConfiguration;
import protoTranslatorDevice.TrDConfiguration;
import protonetcommunicationdevice.*;
/**
 *
 * @author Rafael de Santiago
 */
public class LIDConfiguration {
    //sockets configurations
    private int portServerSocket = 6000;
    
    //devices
    private GDConfiguration gdcfg = null;
    
    private TrDConfiguration trdcfg = null;
    private NCDData ncddata;
    
    private NCDConfiguration ncdcfg;
    
    
    
    /**
     * Constructor of the class LIDConfiguration. This class have the purpose of
     * maintain the data for configuration of the Loware Interface Device. 
     * 
     * @param   ncdcfg      configuration object of the Network Communication Device
     */         
    public LIDConfiguration( NCDConfiguration ncdcfg ){
        this.gdcfg = new GDConfiguration();
        this.ncdcfg = ncdcfg;
        this.ncddata = ncdcfg.getNCDData();
        this.trdcfg = new TrDConfiguration();
        this.trdcfg.setLorMessagesType("JSON");
    }

    
    /**
     * Return the number of the port that are listening for loware connection.
     * 
     * @return   number of the port that are listening for loware connection
     */       
    public int getPortServerSocket() {
        return portServerSocket;
    }

    /**
     * Set the number of the port that will be listening for loware connection.
     * 
     * @param portServerSocket   number of the port that will be listening for loware connection
     */           
    public void setPortServerSocket(int portServerSocket) {
        this.portServerSocket = portServerSocket;
    }

    /**
     * Return the configuration object of the Gather Device.
     * 
     * @return configuration object of the Gather Device
     */       
    public GDConfiguration getGdCfg() {
        return gdcfg;
    }
    /**
     * Set the configuration object Gather Device that are used in Loware Interface Device.
     * 
     * @param gdcfg configuration object Gather Device that are used in Loware Interface Device
     */   
    public void setGdCfg(GDConfiguration gdcfg) {
        this.gdcfg = gdcfg;
    }

    /**
     * Return the configuration object of the Translator Device.
     * 
     * @return configuration object of the Translator Device
     */       
    public TrDConfiguration getTrdCfg() {
        return trdcfg;
    }
     
    
    /**
     * Set the configuration object Translator Device that are used in Loware Interface Device.
     * 
     * @param trdcfg configuration object Translator Device that are used in Loware Interface Device
     */      
    public void setTrdCfg(TrDConfiguration trdcfg) {
        this.trdcfg = trdcfg;
    }

    
    /**
     * Return the data object of the Network Communication Device.
     * 
     * @return  Return the data object of the Network Communication Device
     */          
    public NCDData getNCDData() {
        return ncddata;
    }
    
    /**
     * Set the data object of the Network Communication Device for purpose of 
     * use by Loware Interface Device.
     * 
     * @param ncddata  data object of the Network Communication Device
     */              
    public void setNcddata(NCDData ncddata) {
        this.ncddata = ncddata;
    }

    
    /**
     * Return the configuration object of the Network Communication Device.
     * 
     * @return  Return the configuration object of the Network Communication Device
     */     
    public NCDConfiguration getNCDCfg() {
        return ncdcfg;
    }

    /**
     * Set the configuration object of the Network Communication Device for purpose of 
     * use by Loware Interface Device.
     * 
     * @param ncdcfg  configuration object of the Network Communication Device
     */        
    public void setNcdcfg(NCDConfiguration ncdcfg) {
        this.ncdcfg = ncdcfg;
    }
}
