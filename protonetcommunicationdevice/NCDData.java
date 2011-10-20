/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package protonetcommunicationdevice;

import java.util.ArrayList;
import net.jxta.endpoint.EndpointAddress;
import protoStatisticsDevice.StDData;

/**
 *
 * @author Rafael de Santiago
 */
public class NCDData {
    
    //messages buffers
    private ArrayList messageSenderBufferList;
    private ArrayList messageReceiverBufferList;
    
    //messages
    private ArrayList messagesRead;
    
    //know peerlist
    private ArrayList knownPeersList;

    
    /**
     * Constructor for the NCDData. Objects from this class are 
     * responsible for maintain the data from the Network Communicatio Device.
     * 
     */       
    public NCDData(){
        this.knownPeersList = new ArrayList();
        this.messageSenderBufferList = new ArrayList();
        this.messageReceiverBufferList = new ArrayList();
        this.messagesRead = new ArrayList();
    }

    /**
     * Add id of a read message message read
     * 
     * @param msgID ID of a read message
     */          
    public void addReadMessage(String msgID){
        if (this.messagesRead.size() >= 255){
            this.messagesRead.clear();
        }
        messagesRead.add(msgID);
    }    
    
    /**
     * Verifies if is a new message
     * 
     * @param msgID ID of a read message
     * @return true if is a new message
     */          
    public boolean isNewMessage(String msgID){
        boolean isNew = true;
        for (int i=0; i<this.messagesRead.size(); i++){
            String msgread = (String)this.messagesRead.get(i);
            if (msgread.equals(msgID)){
                isNew = false;
            }
        }
        return isNew;
    }
    
    /**
     * add if is new message ID
     * 
     * @param msgID ID of a read message
     * @return true if is a new message
     */          
    public boolean addIfIsNewMessage(String msgID){
        boolean isNew = this.isNewMessage(msgID);
        if (isNew){
            this.addReadMessage(msgID);
        }
        return isNew;
    }    
    
    
    
    /**
     * Return the list of known peer list
     * 
     * @return known peers list
     */          
    public ArrayList getKnownPeersList(){
        return this.knownPeersList;
    }
    
    /**
     * add the peer address that are not mapped yet
     * 
     * @param obj EndpointAddress for the new known peer
     */       
    public synchronized void addKnownPeersList(Object obj){
        boolean newPeer = true;
//        System.out.println("=============================================================================="); 
        if (obj.getClass() == EndpointAddress.class){
            String newAddress = ((EndpointAddress) obj).getProtocolAddress();
            for (int i=0; i<this.getKnownPeersList().size(); i++){
                String address = ((EndpointAddress) this.getKnownPeersList().get(i)).getProtocolAddress();
                if (newAddress.equals(address)){
                    newPeer = false;
                    break;
                }
            }
            if (newPeer){
                this.getKnownPeersList().add(obj);
                StDData.instanceOfStDData().addNewPeerConnected(((EndpointAddress) obj).getProtocolAddress());
            }
        }
    }
    
/**
     * remove the peer address
     * 
     * @param peerToRemoveadress address of the peer to remove
     */       
    public void removePeersFromList(String peerToRemove){
        peerToRemove = peerToRemove.replace("urn:jxta:", "");
        for (int i=0; i<this.getKnownPeersList().size(); i++){
            String address = ((EndpointAddress) this.getKnownPeersList().get(i)).getProtocolAddress();
            if (peerToRemove.equals(address)){
                this.getKnownPeersList().remove(i);
                StDData.instanceOfStDData().removePeerConnected(address);
                return;
            }
        }
    }    
    
    
    /**
     * Return buffer list of messages that must be sent
     * 
     * @return buffer list of messages that must be sent
     */ 
    public ArrayList getMessageBufferList(){
        return this.messageSenderBufferList;
    }

    /**
     * Return buffer messages received
     * 
     * @return buffer list of messages received
     */     
    public ArrayList getMessageReceiverBufferList(){
        return this.messageReceiverBufferList;
    }    
   
    /**
     * Return the message with a specific ID
     * 
     * @param msgID ID of a message
     * 
     * @return message with a specific ID
     */         
    public Object searchForResponseById(String msgID){  
        Object response = null;
        synchronized(this.messageReceiverBufferList){
           for (int i=0; i<this.messageReceiverBufferList.size();i++){
                LOP2PMessage msg = (LOP2PMessage) this.messageReceiverBufferList.get(i);
                if(msg.getItem("IDMSG").equals(msgID)){
                    this.messageReceiverBufferList.remove(i);
                    i--;
                    return msg;
                }
            }
        }
        return response;
    }
}
