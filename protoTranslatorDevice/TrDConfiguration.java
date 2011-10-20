/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package protoTranslatorDevice;

/**
 *
 * @author Rafael de Santiago
 */
public class TrDConfiguration {
    //lor messages format standard
    private String lorMessagesType = "JSON";
    
    //lor main metadata standard
    private String lorMetadataType = "LOM";

    /**
     * Constructor of the class TrDConfiguration. This class have the purpose of
     * represent configuration object of the Translator Device.
     * 
     */        
    public TrDConfiguration(){
        
    }

    /**
     * Return loware messages type
     * 
     * @return loware messages type
     */          
    public String getLorMessagesType() {
        return lorMessagesType;
    }

    /**
     * Set loware messages type
     * 
     * @param lorMessagesType loware messages type
     */          
    public void setLorMessagesType(String lorMessagesType) {
        this.lorMessagesType = lorMessagesType;
    }

    /**
     * Return loware metadata type
     * 
     * @return loware metadata type
     */        
    public String getLorMetadataType() {
        return lorMetadataType;
    }

    /**
     * Set loware metadata type
     * 
     * @param lorMetadataType loware metadata type
     */              
    public void setLorMetadataType(String lorMetadataType) {
        this.lorMetadataType = lorMetadataType;
    }
}
