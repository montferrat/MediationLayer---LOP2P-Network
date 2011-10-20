
package protonetcommunicationdevice;

import java.net.URISyntaxException;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeID;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.socket.JxtaSocket;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.attribute.standard.DateTimeAtCompleted;
import net.jxta.endpoint.*;
import protoStatisticsDevice.StDData;

/**
 *
 * @author Rafael de Santiago
 */
public final class NCDMessageSender /*extends Thread*/ {
    private int m_timeout = 5000;
    private StDData statisticData;

    public enum TRANSFER_STATUS_ENUM {
        CONNECTING, WAITING_FOR_RECEIVER, TRANSFERING, FINISHED
    }

    public static final String TRANSFER_PROGRESS_PROPERTY = "transferProgress";
    public static final String TRANSFER_STATUS_PROPERTY = "transferStatus";
    public static final String TRANSFER_THROUGHPUT_KBYTE_PROPERTY = "transferThroughputKByte";

    /**
     * payload size
     */
    private final static int PAYLOADSIZE = 64 * 1024;


    private String m_messageName = "";
    private String m_messagePath = "";
    private String m_fileName    = "";
    private File   m_file        = null;    
    
    private long m_bytesTransfered = 0;
    private LOP2PMessage m_message = null;
    private long m_estimatedFileSize = 0;

    private int m_progress = 0;

    private NCDConfiguration netcfg = null;
    private String peerIdDestination = "";
    
    
    private PeerGroup m_peergroup;
    private PipeAdvertisement m_pipeAdv;
    private InputStream m_inputStream;
    
    
    
    PropertyChangeSupport m_changeSupport = new PropertyChangeSupport(this);

    
    /**
     * Constructor for the NCDMessageSender. Objects from this class are 
     * responsible for send messages for a specific peer.
     * 
     * @param netcfg configuration object for Network Communicatio Device
     * @param msg message that will be sent
     * @param peerIdDestination ID of the desntination peer
     */    
    public NCDMessageSender(NCDConfiguration netcfg, LOP2PMessage msg, String peerIdDestination) throws FileNotFoundException {
        /*super("NCDMessageSender");*/
        //get the statistic storage instance
      //  this.statisticData = StDData.instanceOfStDData();
        
        PipeID socketID;
        try {
            this.peerIdDestination = peerIdDestination;
            
            
            
            this.netcfg = netcfg;           
            this.m_peergroup = netcfg.getNetworkManager().getNetPeerGroup();
            socketID = (PipeID) IDFactory.fromURI(new URI(this.netcfg.SOCKETIDSTR));//.newPipeID(this.m_peergroup.getPeerGroupID()) ;
            
            this.m_message = msg;
            
            //the bean communicates with us via a property change listener
            this.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                    System.out.println(propertyChangeEvent.getPropertyName() + ":" + propertyChangeEvent.getNewValue());
                }
            });
            this.setTimeout(this.netcfg.getStandardTimeOut());                    

            m_pipeAdv = NCDMessageReceiver.createSocketAdvertisement(socketID);
            
            m_messageName = m_message.getNamespace();
            
            /*File fil = new File(this.netcfg.getMessagesTempPath());
            if(fil.exists() == false){
                fil.mkdir();
            }*/
            Date dt = new Date();
            
            String fileName = new Integer(msg.getMessageNumber()).toString()+dt.getTime();
            this.m_messagePath = this.netcfg.getMessagesSerializatedMsgPath() +'/'+ fileName+".ser";           
            this.m_fileName = this.m_messagePath;
            ObjectOutputStream objstream = new ObjectOutputStream(new FileOutputStream(this.m_messagePath));
            objstream.writeObject(msg);
            objstream.close();
            
            
            this.m_file = new File(this.m_messagePath);
            m_estimatedFileSize = this.m_file.length();
            m_fileName = m_file.getName();
            if (m_file.exists() && m_file.canRead()) {
                m_inputStream = new FileInputStream(this.m_file);
            } else {
                m_inputStream = null;
            }                 
        } catch (Throwable e) {
            System.err.println("MessageSender error: " + e);
            e.printStackTrace(System.err);
            System.exit(-1);
        }
        


    }

    /**
     * Put a event listener.
     * 
     * @param listener event listener
     */        
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        m_changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Return the total bytes transfered.
     * 
     * @return total bytes transfered
     */           
    public long getTransferedBytes() {
        return m_bytesTransfered;
    }
    
    /**
     * Set timeout.
     * 
     * @param m_timeout timeout
     */  
    public void setTimeout(int m_timeout) {
        this.m_timeout = m_timeout;
    }

    /**
     * Send the message.
     * 
     */  
    public void send() {
        try {

            
            
            
            long start = System.currentTimeMillis();
            System.out.println("Connecting to the server");
            m_changeSupport.firePropertyChange(TRANSFER_STATUS_PROPERTY, null, TRANSFER_STATUS_ENUM.CONNECTING);
            JxtaSocket socket = new JxtaSocket(m_peergroup,
                    //no specific peerid
                    //null,
                    (net.jxta.peer.PeerID)IDFactory.fromURI(new URI(this.peerIdDestination)),
                    m_pipeAdv,
                    //connection timeout: 5 seconds
                    30000,
                    // reliable connection
                    true);
             
            // get the socket output stream
            OutputStream out = socket.getOutputStream();
            DataOutput dos = new DataOutputStream(out);

            // get the socket input stream
            InputStream in = socket.getInputStream();
            DataInput dis = new DataInputStream(in);

//            long bytesSend = ITERATIONS * (long) PAYLOADSIZE * 2;
//            System.out.println("Sending/Receiving " + bytesSend + " bytes.");
            m_changeSupport.firePropertyChange(TRANSFER_STATUS_PROPERTY, null, TRANSFER_STATUS_ENUM.WAITING_FOR_RECEIVER);
            dos.writeUTF(this.m_fileName);
            dos.writeLong(m_estimatedFileSize);
            //inicio
            dos.writeUTF(this.m_message.getItem("IDMSG"));
            //fim            
            out.flush();
            long requestedOffset = -1;
            try {
                requestedOffset = dis.readLong();
            } catch (IOException e) {
                //ignore
            }
            
            long bytesSend = 0;
            if (requestedOffset != -1) {
                byte[] out_buf = new byte[PAYLOADSIZE];
                byte[] in_buf = new byte[16];
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.reset();


                
                long offset = requestedOffset;
                int bytesRead = readFile(0, offset, out_buf);
                md5.update(out_buf, 0, bytesRead);
                while (bytesRead != -1) {
                    System.out.println("sending offset: " + offset);
                    dos.writeLong(offset);
                    System.out.println("sending size:" + bytesRead);
                    dos.writeInt(bytesRead);
                    out.flush();
                    log("sending data");
                    out.write(out_buf, 0, bytesRead);
                    bytesSend += bytesRead;
                    out.flush();
                    log("waiting for md5");
                    dis.readFully(in_buf);
                    log("waiting for next offset request");
                    requestedOffset = dis.readLong();
                    bytesSend += in_buf.length;
                    if (Arrays.equals(in_buf, md5.digest())) {
                        log("md5 checksum correct:" + offset);
                        md5.reset();
                        offset = offset + bytesRead;
                        updateProgress(offset);
                        if (requestedOffset != -1) {
                            bytesRead = readFile(offset, requestedOffset, out_buf);

                            if (bytesRead != -1) {
                                offset = requestedOffset;
                                md5.update(out_buf, 0, bytesRead);
                            }
                        }
                    } else {
                        log("md5 checksum wrong, resending offset " + offset);
                    }
                    System.out.println("****************************************");
                    System.out.print("Total: ");
                    System.out.println(this.m_estimatedFileSize);
                    System.out.print("Percentual: ");
                    System.out.print( ((double)bytesSend) / ((double)this.m_estimatedFileSize) * 100.0);                    
                    System.out.println("%");
                    System.out.print("Bytes lefting: ");
                    System.out.print( this.m_estimatedFileSize - bytesSend);                    
                    System.out.println("bytes");
                    System.out.println("****************************************");
                }
                dos.writeLong(offset); //should be the overall filesize
                dos.writeInt(0); //end of transfer
                

            } else {
                //other side rejected the transfer
            }


            out.close();
            in.close();

            long finish = System.currentTimeMillis();
            long elapsed = finish - start;

            int throughputKBytes = (int) (bytesSend / elapsed) * 1000 / 1024;
            m_changeSupport.firePropertyChange(NCDMessageSender.TRANSFER_THROUGHPUT_KBYTE_PROPERTY, 0, throughputKBytes);
            socket.close();
            log("Socket connection closed");
            m_changeSupport.firePropertyChange(TRANSFER_STATUS_PROPERTY, null, TRANSFER_STATUS_ENUM.FINISHED);
            finish();
            
            
        } catch (URISyntaxException ex) {

            System.err.println("NCDMessageSender error: "+ex.toString());
            Logger.getLogger(NCDMessageSender.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException io) {
            //remove peer trouble
            this.netcfg.getNCDData().removePeersFromList(this.peerIdDestination);            
            System.err.println("NCDMessageSender error: "+io.toString());
            //io.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            //remove peer trouble
            this.netcfg.getNCDData().removePeersFromList(this.peerIdDestination);
            
            System.err.println("NCDMessageSender error: "+e.toString());
            //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return;
        
    }
    
    
    private static final Logger LOG = Logger.getLogger(NCDMessageSender.class.getName());

    private static void log(String s) {
        LOG.fine(s);
    }


    private int readFile(long offset, long requestedOffset, byte[] out_buf) throws IOException {
        if (requestedOffset > offset) {
            long skipped = m_inputStream.skip(requestedOffset - offset);
            assert (skipped == requestedOffset - offset);
        } else if (requestedOffset < offset)
            return -1;   //not possible with input streams

        return m_inputStream.read(out_buf, 0, PAYLOADSIZE);
    }

    private void finish() {
        m_changeSupport = null;
    }
    /**
     * Update progress of transferation.
     * 
     */  
    private void updateProgress(long offset) {
        int newProgress = Math.round(100 * ((float) offset / (float) m_estimatedFileSize));
        if (newProgress != m_progress) {
            int oldProgress = m_progress;
            m_progress = newProgress;
            m_changeSupport.firePropertyChange(TRANSFER_PROGRESS_PROPERTY, oldProgress, m_progress);
        }
    }
}

