
package protonetcommunicationdevice;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.PipeAdvertisement;

import java.util.Enumeration;
/**
 *
 * @author Rafael de Santiago
 */

/**
 * class which is responsable for the advertisements of the peer
 */
public class NCDAdvertisements extends Thread implements DiscoveryListener {

    private static NCDConfiguration netConfig;
    private transient DiscoveryService discovery;

    /**
     * Constructor for the NCDAdvertisements. This is the class to make 
     * advertisements of the peer, to be known by others.
     * 
     * @param pNetConfig network configuration object
     */
    public NCDAdvertisements(NCDConfiguration pNetConfig) {
        super("NCDAdvertisements");

        netConfig = pNetConfig;
        PeerGroup netPeerGroup = netConfig.getNetworkManager().getNetPeerGroup();

        // get the discovery service
        discovery = netPeerGroup.getDiscoveryService();
        
        this.setPriority(MIN_PRIORITY);
    }

    /**
     * Execution method of the thread NCDAdvertisements.
     * 
     */    
    public void run(){
        long lifetime = 60 * 2 * 1000L;//60 * 2 * 1000L;
        long expiration = 60 * 2 * 1000L;
        long waittime = 60 * 3 * 1000L;//60 * 3 * 1000L;

        try {
            while (true) {
                PipeAdvertisement pipeAdv = getPipeAdvertisement();

                // publish the advertisement with a lifetime of 2 mintutes
                //System.out.println(
                  //      "Publishing the following advertisement with lifetime :" + lifetime + " expiration :" + expiration);
                //System.out.println(pipeAdv.toString());
                discovery.publish(pipeAdv, lifetime, expiration);
                discovery.remotePublish(pipeAdv, expiration);
                try {
                    //System.out.println("Sleeping for :" + waittime);
                    Thread.sleep(waittime);
                } catch (Exception e) {// ignored
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    


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
            while (en.hasMoreElements()) {
                adv = (Advertisement) en.nextElement();
                //System.out.println(adv);
            }
        }
    }

    /**
     * Creates a pipe advertisement
     *
     * @return a Pipe Advertisement
     */
    public static PipeAdvertisement getPipeAdvertisement() {
        PipeAdvertisement advertisement = (PipeAdvertisement)
                AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());

        advertisement.setPipeID(IDFactory.newPipeID(PeerGroupID.defaultNetPeerGroupID));
        advertisement.setType(PipeService.UnicastType);
        advertisement.setName("PROTO_LOP2P_ADV_PIPE_PEER");
        advertisement.setDescription(netConfig.getNetworkManager().getPeerID().toString());
        return advertisement;
    }

    /**
     * Stops the platform
     */
    public void stopa() {
        // Stop JXTA
        netConfig.getNetworkManager().stopNetwork();
    }
}

