/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package protonetcommunicationdevice;

import java.io.*;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.document.*;
import net.jxta.endpoint.*;

/**
 *
 * @author Rafael de Santiago
 */
public class LOP2PMessage extends Message{
    static private final long serialVersionUID = 42L;

    /**
     * Return the namespace of the message.
     * 
     * @return namespace
     */            
    public String getNamespace() {
        return namespace;
    }

    /**
     * Set the namespace of the message.
     * 
     * @param namespace namespace
     */        
    public void setNamespace(String namespace) {
        this.namespace = namespace;
        
    }

    //type of that message based in IMS DRI
    public enum MESSAGE_TYPE {
        GATHER, GATHER_EXPOSE, 
        SEARCH, SEARCH_EXPOSE,
        STORE, STORE_SUBMIT,
        REQUEST, REQUEST_SUBMIT,
        ALERT, ALERT_EXPOSE,
        STATISTICS_EXPOSE
    }
    
    private String namespace;
    
    
    /**
     * Constructor of the class LOP2PMessage. This is the class of messages 
     * that are sent by the LOP2P network. Contructor for a new message.
     * 
     * @param peerFrom source peer of the message
     * @param peerDestination destination peer of the message
     * @param namespace namespace of the message
     */         
    public LOP2PMessage(String peerFrom,String peerDestination, String namespace){
        //configuration object
        this.namespace = namespace;
        
        //to prevent the defaultNamespace serialization error
        this.addMessageElement(null, new StringMessageElement("default", "default", null));                
        
         // adding peer from of the message
        StringMessageElement smeFrom = new StringMessageElement("peerFrom", peerFrom, null);
        this.addMessageElement(this.getNamespace(), smeFrom);        
        
         // adding destination of the message
        StringMessageElement smeDest = new StringMessageElement("peerDestination", peerDestination, null);
        this.addMessageElement(this.getNamespace(), smeDest);
        
    }
    

    /**
     * Constructor of the class LOP2PMessage. This is the class of messages 
     * that are sent by the LOP2P network. Message objects instantied by that 
     * constructor are created throw messages recoved from a file. 
     * 
     * @param file file to be retrieve
     * @param pathSourceRepository path of the source repository
     * @param pathTargetRepository path of the target repository
     */        
    public LOP2PMessage(File file, String pathSourceRepository, String pathTargetRepository) throws IOException, ClassNotFoundException{
        //getting message
        ObjectInputStream objstream = null;
        try {
            objstream = new ObjectInputStream(new FileInputStream(pathSourceRepository+'/'+file.getName()));
            LOP2PMessage msgFromFile = (LOP2PMessage) objstream.readObject();
            objstream.close();
            
            //iterator to catch elements
            ElementIterator itMsg =  msgFromFile.getMessageElements();
            String lastString = ".unk";
            while(itMsg.hasNext()){
                MessageElement el = itMsg.next();
                
                if (el.getElementName().equals("fileBytes")){
                    //store file
                    ByteArrayMessageElement myByteArrayMessageElement = 
                                                    (ByteArrayMessageElement) el;
                    Random randomPart = new Random();
                    Date dt = new Date();
                    String fileName = el.getElementName()
                                     +(randomPart.nextInt() %1000)
                                     +dt.getTime()
                                     +lastString;
                    
                    String targetDestination = pathTargetRepository + '/' + fileName;
                    FileOutputStream fout = 
                            new FileOutputStream(targetDestination);
                    
                    fout.write(myByteArrayMessageElement.getBytes());
                    fout.close();
                    
                    //attaching in a message
                    StringMessageElement smeType = 
                            new StringMessageElement("fileRefence", targetDestination, null);
                    this.addMessageElement(this.getNamespace(), smeType);                        
                    
                }else{
                    //add element to the message
                    ByteArrayMessageElement myByteArrayMessageElement = 
                                                    (ByteArrayMessageElement) el;                    
                    String ns = el.getElementName();
                    lastString = new String(myByteArrayMessageElement.getBytes());
                    this.addItem(ns, lastString);                    
                }
                
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(LOP2PMessage.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                objstream.close();
            } catch (IOException ex) {
                Logger.getLogger(LOP2PMessage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }

    
    /**
     * Add a string item in the message. 
     * 
     * @param tagName tag name of the item
     * @param content content of the item
     */            
    public void addItem(String tagName, String content){
         // adding string in the message
        StringMessageElement smeType = new StringMessageElement(tagName, content, null);
        this.addMessageElement(this.getNamespace(), smeType);        
    }
    
    /**
     * get a string item of the message. 
     * 
     * @param tagName tag name of the item
     * 
     * @return content of the item
     */       
    public String getItem(String tagName){
        MessageElement smeType = this.getMessageElement(tagName);
        String result = null;
        if (smeType.getClass() == StringMessageElement.class){
            StringMessageElement s = (StringMessageElement) smeType;
            result = new String (s.getBytes(true));
        }
        if (smeType.getClass() == ByteArrayMessageElement.class){
            ByteArrayMessageElement s = (ByteArrayMessageElement) smeType;
            result = new String (s.getBytes(true));
        }
        return result;
         
    }
    
        
    
    /**
     * Add a file item in the message
     * 
     * @param file file that will be attached
     */         
    public void addAttachment(File file){
         // adding string in the message
        StringMessageElement smeType = 
                new StringMessageElement("fileName", file.getName(), null);
        this.addMessageElement(this.getNamespace(), smeType); 
        
        try {
             //convert file in bytes
            InputStream inputStream;
            if (file.exists() && file.canRead()) {
                inputStream = new FileInputStream(file);
            } else {
                inputStream = null;
            }     
            byte[] myFile = new byte[(int)file.length()];
                        
            inputStream.read(myFile);
            
            //attaching in a message
            String tagName = "fileBytes";
            ByteArrayMessageElement MyByteArrayMessageElement = 
                    new ByteArrayMessageElement(tagName, MimeMediaType.AOS, myFile, null);
            this.addMessageElement(this.getNamespace(), MyByteArrayMessageElement);
            
        } catch (IOException ex) {
            Logger.getLogger(LOP2PMessage.class.getName()).log(Level.SEVERE, null, ex);
        }
                    
    }
    
    
    
}
