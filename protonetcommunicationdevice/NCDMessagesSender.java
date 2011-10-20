/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package protonetcommunicationdevice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.endpoint.StringMessageElement;

/**
 *
 * @author Rafael de Santiago
 */
public class NCDMessagesSender extends Thread{
    private NCDConfiguration ncdcfg;

    /**
     * Constructor for the NCDMessagesSender. Objects from this class 
     * verify if a new message must be sent and request treatment by the 
     * NCDMessageSender
     * 
     * @param ncdcfg configuration object for Network Communication Device
     */       
    public NCDMessagesSender(NCDConfiguration ncdcfg){
        this.setPriority(MIN_PRIORITY);  
        this.ncdcfg = ncdcfg;
    }
    
    /**
     * Execution method of the thread NCDMessagesSender.
     */   
    public void run(){
        while(true){
            try {
                ArrayList msgsReceiveds = this.ncdcfg.getNCDData().getMessageBufferList();
                
                if(msgsReceiveds.size()>0){
                    //capture one message and send
                    LOP2PMessage msg = (LOP2PMessage) msgsReceiveds.remove(0);
                    StringMessageElement el = (StringMessageElement)msg.getMessageElement("peerDestination");
                    
                    String peerDestination = new String(el.getChars(true));
                    peerDestination = "urn:jxta:"+peerDestination;
                    NCDMessageSender ms = new NCDMessageSender(this.ncdcfg,msg, peerDestination);
                    ms.send();
                }else{
                    sleep(this.ncdcfg.getThreadSleepTime());
                }
            } catch (IOException ex) {
                System.err.println("NCDMessagesSender error[0]: "+ex.toString());
                Logger.getLogger(NCDMessagesSender.class.getName()).log(Level.SEVERE, null, ex);               
            } catch (InterruptedException ex) {
                System.err.println("NCDMessagesSender error[2]: "+ex.toString());
                Logger.getLogger(NCDMessagesSender.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
