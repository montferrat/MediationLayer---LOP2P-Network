/*
 * Copyright (c) Rafael de Santiago.  All rights reserved.
 *  
 * This software is under LGPL license
 *  
 *  ====================================================================
  */

package protonetcommunicationdevice;
        
import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.peergroup.PeerGroup;
import net.jxta.protocol.DiscoveryResponseMsg;


import java.util.Enumeration;
import net.jxta.document.MimeMediaType;
import net.jxta.endpoint.EndpointAddress;
import net.jxta.impl.protocol.PipeAdv;
import net.jxta.protocol.PipeAdvertisement;
/**
 *
 * @author Rafael de Santiago
 * Discovery Service of LOP2P Specification
 */
public class NCDDiscovery extends Thread implements DiscoveryListener {

    private transient DiscoveryService discovery;

    private NCDConfiguration netConfig;
    
    /**
     * Constructor for the DiscoveryClient. This class has the purpose of 
     * discover other peers of the network
     * 
     * @param netConfig configuration of LOP2P network
     */
    public NCDDiscovery(NCDConfiguration netConfig) {
        super("NCDDiscovery");
        this.netConfig = netConfig;

        // Get the NetPeerGroup
        PeerGroup netPeerGroup = netConfig.getNetworkManager().getNetPeerGroup();

        // get the discovery service
        discovery = netPeerGroup.getDiscoveryService();
        
         this.setPriority(MIN_PRIORITY);
                
    }
    
    /**
     * Execution Method of the thread NCDDiscovery.
     */
    public void run(){
        long waittime = 5 * 1000L;//60 * 1000L;

        try {
            // Add ourselves as a DiscoveryListener for DiscoveryResponse events
            discovery.addDiscoveryListener(this);
            discovery.getRemoteAdvertisements(// no specific peer (propagate)
/*                    null, // Adv type
                    DiscoveryService.ADV, // Attribute = any
                    null, // Value = any
                    null, // one advertisement response is all we are looking for
                    255, // no query specific listener. we are using a global listener
                    null);
 */
                        // no specific peer (propagate)
                        null,
                        // Adv type
                        DiscoveryService.ADV,
                        // Attribute = name
                        "Name",
                        // Value = the tutorial
                        "PROTO_LOP2P_ADV_PIPE_PEER",
                        // one advertisement response is all we are looking for
                        255,
                        // no query specific listener. we are using a global listener
                        null);                    
            while (true) {
                // wait a bit before sending a discovery message
                try {
                    //System.out.println("Sleeping for :" + waittime);
                    this.sleep(waittime);
                } catch (Exception e) {
                    // ignored
                }
                //System.out.println("Sending a Discovery Message");
                // look for any peer
                discovery.getRemoteAdvertisements(
                        // no specific peer (propagate)
                        null,
                        // Adv type
                        DiscoveryService.ADV,
                        // Attribute = name
                        "Name",
                        // Value = the tutorial
                        "PROTO_LOP2P_ADV_PIPE_PEER",
                        // one advertisement response is all we are looking for
                        255,
                        // no query specific listener. we are using a global listener
                        null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * loop forever attempting to discover advertisements every minute
     */
   /* public void start() {
        long waittime = 60 * 1000L;

        try {
            // Add ourselves as a DiscoveryListener for DiscoveryResponse events
            discovery.addDiscoveryListener(this);
            discovery.getRemoteAdvertisements(// no specific peer (propagate)
                    null, // Adv type
                    DiscoveryService.ADV, // Attribute = any
                    null, // Value = any
                    null, // one advertisement response is all we are looking for
                    255, // no query specific listener. we are using a global listener
                    null);
            while (true) {
                // wait a bit before sending a discovery message
                try {
                    System.out.println("Sleeping for :" + waittime);
                    Thread.sleep(waittime);
                } catch (Exception e) {
                    // ignored
                }
                System.out.println("Sending a Discovery Message");
                // look for any peer
                discovery.getRemoteAdvertisements(
                        // no specific peer (propagate)
                        null,
                        // Adv type
                        DiscoveryService.ADV,
                        // Attribute = name
                        null,
                        // Value = the tutorial
                        null,
                        // one advertisement response is all we are looking for
                        255,
                        // no query specific listener. we are using a global listener
                        null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    /**
     * This method is called whenever a discovery response is received, which are
     * either in response to a query we sent, or a remote publish by another node
     *
     * @param ev the discovery event
     */
    public void discoveryEvent(DiscoveryEvent ev) {

        DiscoveryResponseMsg res = ev.getResponse();

        // let's get the responding peer's advertisement
        //System.out.println(" [  Got a Discovery Response [" + res.getResponseCount() + " elements]  from peer : " + ev.getSource() + "  ]");

        Advertisement adv;
        Enumeration en = res.getAdvertisements();

        if (en != null) {
            //store information captured
            
            while (en.hasMoreElements()) {
                adv = (Advertisement) en.nextElement();
                if (adv.getClass() ==  PipeAdv.class){
                    try{
                        if (((PipeAdv)adv).getDesc().getValue().toString().trim().equals("") == false){
                            PipeAdv pipeAdv = (PipeAdv)adv;
                            if (pipeAdv.getName().equals("PROTO_LOP2P_ADV_PIPE_PEER")){
                                String discoveredPeerID = pipeAdv.getDesc().getValue().toString();
                                EndpointAddress epa = new EndpointAddress(discoveredPeerID);
                                //verify if is my peer
                                if (this.netConfig.getPeerID().equals(discoveredPeerID) == false){
                                    this.netConfig.getNCDData().addKnownPeersList(epa);
                                }
                            }
                        }
                    }catch(Exception ex){             
                    
                    }
                }
                
                //System.out.println(adv);
            }
        }
    }

    /**
     * Stops the platform
     */
    public void stopa() {
        // Stop JXTA
        this.netConfig.getNetworkManager().stopNetwork();
    }
}

