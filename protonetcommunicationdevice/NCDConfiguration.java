/*
 * Copyright (c) Rafael de Santiago.  All rights reserved.
 *  
 * This software is under LGPL license
 *  
 *  ====================================================================
  */

package protonetcommunicationdevice;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jxta.platform.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.util.Date;
import java.util.Properties;
import net.jxta.impl.id.UUID.PeerGroupID;
import protoStatisticsDevice.StDData;

/**
 *
 * @author Rafael de Santiago
 */


/**
 *
 * Configuration of the LOP2P network  connection
 */
public class NCDConfiguration {
    
      
    public String NT_INSTANCE_NAME = "NCDLOP2P";
      
    private transient NetworkManager networkManager; // network manager of that peer
    
    private NCDData ncdData;
    private String receivedFilesPath = "./";
    private String messagesTempPath = "./";    
    private String messagesSerializatedMsgPath = "./serializated"; 
    
    private long threadSleepTime = 2 * 1000;
    private int standardTimeOut = 30 * 1000;
    public final String SOCKETIDSTR = "urn:jxta:uuid-59616261646162614E5047205032503393B5C2F6CA7A41FBB0F890173088E79404";
    
    private String messageNamespace = "LOP2PMessage";


    
    /**
     * Constructor for the NCDConfiguration. Objects from this class are 
     * responsible for maintain configuration data of the Network Communication 
     * Device.
     * 
     * @param ncdData data object of the Network Communication Device
     */    
    public NCDConfiguration(NCDData ncdData){
        try {
            this.ncdData = ncdData;
            
            //properties of this peer
            Properties properties = new Properties();
            String p_peer_name = "DEFAULT";
            String p_peer_type = "EDGE";
            String p_super_seeding[] = {"NONE", "NONE", "NONE", "NONE", "NONE"};

            
            try {
                properties.load(new FileInputStream("config.properties"));
                
                p_peer_name = properties.getProperty("peer_name");
                p_peer_type = properties.getProperty("peer_type");
                p_super_seeding[0] = properties.getProperty("super_seeding1");
                p_super_seeding[1] = properties.getProperty("super_seeding2");
                p_super_seeding[2] = properties.getProperty("super_seeding3");
                p_super_seeding[3] = properties.getProperty("super_seeding4");
                p_super_seeding[4] = properties.getProperty("super_seeding5");
                                    
            } catch (IOException e) {
                System.out.println("Error when trying to configurate peer with " +
                        "the file 'config.properties': "+e.toString());
            }
            
            //setting name of that peer
            
            if (p_peer_name.equals("DEFAULT")){
                InetAddress localHost = InetAddress.getLocalHost();
                NetworkInterface netInter = NetworkInterface.getByInetAddress(localHost);
                //##? verificar depois: em algumas interfaces naum consegue recuperar o MAC ADDRESS
              //  byte[] macAddressBytes = netInter.getHardwareAddress();-Dhttp.proxyHost=200.225.165.132 -Dhttp.proxyPort=6588
              //  String macAddress = String.format("%1$02x-%2$02x-%3$02x-%4$02x-%5$02x-%6$02x", macAddressBytes[0], macAddressBytes[1], macAddressBytes[2], macAddressBytes[3], macAddressBytes[4], macAddressBytes[5]).toUpperCase();
                long timestamp = new Date().getTime();
                String macAddress = netInter.getDisplayName().replace(' ', '-');
                this.NT_INSTANCE_NAME += localHost.getHostName()+timestamp+macAddress;
            }else{
                this.NT_INSTANCE_NAME = p_peer_name;
            }
            //this.NT_INSTANCE_NAME = "SLOP2P_UNIVALI";
            System.out.println(" - My name will be: "+this.NT_INSTANCE_NAME+" - peer said.");
            
            //setting configuration mode of the peer
            NetworkManager.ConfigMode peerMode;
            try{
                peerMode = NetworkManager.ConfigMode.valueOf(p_peer_type);
            }catch(Exception e){
                peerMode = NetworkManager.ConfigMode.EDGE;
            }
            
            //start the network
            try {
                networkManager = new NetworkManager(peerMode, this.NT_INSTANCE_NAME, new File(new File(".cache"), this.NT_INSTANCE_NAME).toURI());
//
                
File home = new File(".test");
NetworkConfigurator config = networkManager.getConfigurator();
config.setHome(home);
//config.addRdvSeedingURI(new URI("http://rdv.jxtahosts.net/cgi-bin/rendezvous.cgi?2"));
//config.addRelaySeedingURI(new URI("http://rdv.jxtahosts.net/cgi-bin/relays.cgi?2"));

for(String address: p_super_seeding){
    if (address.equals("NONE") == false){
        config.addRdvSeedingURI(new URI(address));
        config.addRelaySeedingURI(new URI(address));
    }
}





//config.addSeedRelay(new URI("http://iceheartpeer.no-ip.org:9700"));
//config.addSeedRendezvous(new URI("http://iceheartpeer.no-ip.org:9700"));
//...

//http://forums.java.net/jive/thread.jspa?messageID=339599
config.setTcpEnabled(true);
config.setTcpIncoming(true);
config.setTcpOutgoing(true);

config.setTcpStartPort(-1);
config.setTcpEndPort(-1);



config.setHttpEnabled(true);
config.setHttpIncoming(true);
config.setHttpOutgoing(true);

/*config.setUseMulticast(false);
config.setTcpInterfaceAddress("m.n.o.p");
config.setTcpPublicAddress("w.x.y.z", false);*/


config.save();            
//System.out.println("Configuracoes Salvas");
              
                
                networkManager.startNetwork();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Its not possible to create a NetworkManager");
            }

            this.setReceivedFilesPath("./receivedfiles");
            this.setMessagesTempPath("./messagesasserts");
            this.setMessagesSerializatedMsgPath("./messagesasserts/serializated");
            System.out.println("My JXTA Address is: "+this.networkManager.getPeerID().toString());
            
            //collect initial data
            StDData.instanceOfStDData().setPeername(this.NT_INSTANCE_NAME);
            StDData.instanceOfStDData().setPeerid(this.networkManager.getPeerID().toString());

            
        } catch (UnknownHostException ex) {
            Logger.getLogger(NCDConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SocketException ex) {
            Logger.getLogger(NCDConfiguration.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    /**
     * Return the peer ID
     * 
     * @return peer ID
     */        
    public String getPeerID(){
        return this.networkManager.getPeerID().toString();
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
     * Return the NetworkManager of the peer to peer network.
     * 
     * @return NetworkManager of the peer to peer network
     */         
    public NetworkManager getNetworkManager(){
        return this.networkManager;
    }
    

    /**
     * Return the data object of the Network Communication Device.
     * 
     * @return data object of the Network Communication Device
     */             
    public NCDData getNCDData(){
        return this.ncdData;
    }

    /**
     * Return the path for received files (from the messages).
     * 
     * @return path for received files (from the messages)
     */        
    public String getReceivedFilesPath() {
        return receivedFilesPath;
    }

    /**
     * Set the path for received files (from the messages).
     * 
     * @param receivedFilesPath path for received files (from the messages)
     */            
    public void setReceivedFilesPath(String receivedFilesPath) {
        this.createDiretory(receivedFilesPath);
        this.receivedFilesPath = receivedFilesPath;
    }

    /**
     * Return the standard timeout value.
     * 
     * @return standard timeout value
     */      
    public int getStandardTimeOut() {
        return standardTimeOut;
    }

    /**
     * Set the standard timeout value.
     * 
     * @param standardTimeOut standard timeout value
     */       
    public void setStandardTimeOut(int standardTimeOut) {
        this.standardTimeOut = standardTimeOut;
    }

    /**
     * Return the standard messages namespace.
     * 
     * @return standard messages namespace
     */           
    public String getMessageNamespace() {
        return messageNamespace;
    }

    /**
     * Set the standard messages namespace.
     * 
     * @param messageNamespace standard messages namespace
     */               
    public void setMessageNamespace(String messageNamespace) {
        this.messageNamespace = messageNamespace;
    }

    /**
     * Return the temporary path of messages store.
     * 
     * @return temporary path of messages store
     */       
    public String getMessagesTempPath() {
        return messagesTempPath;
    }

    /**
     * Set the temporary path of messages store.
     * 
     * @param messagesTempPath temporary path of messages store
     */           
    public void setMessagesTempPath(String messagesTempPath) {
        this.createDiretory(messagesTempPath);
        this.messagesTempPath = messagesTempPath;
    }

    /**
     * Return the standard threads sleep time.
     * 
     * @return standard threads sleep time
     */     
    public long getThreadSleepTime() {
        return threadSleepTime;
    }

    /**
     * Set the standard threads sleep time.
     * 
     * @param threadSleepTime standard threads sleep time
     */         
    public void setThreadSleepTime(long threadSleepTime) {
        this.threadSleepTime = threadSleepTime;
    }

    /**
     * Return path to store serialized messages.
     * 
     * @return path to store serialized messages
     */     
    public String getMessagesSerializatedMsgPath() {
        return messagesSerializatedMsgPath;
    }

    /**
     * Set path to store serialized messages.
     * 
     * @param messagesSerializatedMsgPath path to store serialized messages
     */         
    public void setMessagesSerializatedMsgPath(String messagesSerializatedMsgPath) {
        this.createDiretory(messagesSerializatedMsgPath);
        this.messagesSerializatedMsgPath = messagesSerializatedMsgPath;
    }


    
    

}
