
package protonetcommunicationdevice;

import net.jxta.credential.Credential;
import net.jxta.document.AdvertisementFactory;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.socket.JxtaServerSocket;
import net.jxta.socket.JxtaSocket;

import java.io.*;
import java.net.URI;
import java.security.MessageDigest;
import java.util.logging.Logger;
import protoStatisticsDevice.StDData;


/**
 *
 * @author Rafael de Santiago
 */
public class NCDMessageReceiver extends Thread {

    private transient PeerGroup netPeerGroup = null;
    //public final static String SOCKETIDSTR = "urn:jxta:uuid-59616261646162614E5047205032503393B5C2F6CA7A41FBB0F890173088E79404";
    private PipeID m_pipeID = null;
    private OutputStream m_outputStream =null;
    private boolean m_stopReceiver = false;
    private JxtaServerSocket m_serverSocket;
    
    private NCDConfiguration netcfg;

    private long m_thoughputKByte;

    public enum OVERRIDE_BEHAVIOR {
        OVERRIDE,  //override the existing file, transfer starts at offset 0
        RESUME,    //resume transfer (transfer only the missing part of the file and append them)
        APPEND     //append received data (transfer starts at offset 0, but the received data is append)
    }

    /**
     * Constructor for the NCDMessageReceiver. Objects from this class receive 
     * messages captured by objects of the class NCDMessagesReceiverVerifier and
     * give the expect treatment.
     * 
     * @param netcfg configuration object for Network Communicatio Device
     */
    public NCDMessageReceiver(NCDConfiguration netcfg) {
        System.setProperty("net.jxta.logging.Logging", "FINEST");
        System.setProperty("net.jxta.level", "FINEST");
        System.setProperty("java.util.logging.config.file", "logging.properties");
        
        this.netcfg = netcfg;
        
        try {
            this.netPeerGroup = netcfg.getNetworkManager().getNetPeerGroup();
            this.m_pipeID = (PipeID) IDFactory.fromURI(new URI(this.netcfg.SOCKETIDSTR));//.newPipeID(this.netPeerGroup.getPeerGroupID());
       
        } catch (Throwable e) {
            System.err.println("Failed : " + e);
            e.printStackTrace(System.err);
            System.exit(-1);
        }        
        
        
        m_outputStream = null;
    }


    /**
     * Creates a Unicast Pipe Advertisment with the given pipeID
     * 
     * @param pipeID ID for the pipe
     * 
     * @return pipe advertisement object
     */    
    public static PipeAdvertisement createSocketAdvertisement(PipeID pipeID) {
        try{
            PipeID socketID;
            socketID = pipeID;
            PipeAdvertisement advertisement = (PipeAdvertisement)
                    AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
            advertisement.setPipeID(socketID);
            advertisement.setType(PipeService.UnicastType);
            advertisement.setName("FileTransferSocket");
            return advertisement;
       }catch(Exception ex){
           System.err.println("NCDMessageReceiver error [createSocketAdvertisement]: "+ex.toString());
       }
       return null;
    }


    /**
     * shutdown the file-receiver, ongoing transports will be canceled
     * 
     */        
    public void shutdownListener() {
        m_stopReceiver = true;
        if (m_serverSocket != null)
            try {
                m_serverSocket.close();
            } catch (IOException e) {
                System.err.println("NCDMessageReceiver error [shutdownListener]: "+e.toString());
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

    }

    /**
     * Start the file receiver - an incoming JxtaServer Socket will be created.
     * This method blocks until shutdownListener is beeing called.
     */
    public void run() {

        System.out.println("Starting ServerSocket");

        m_serverSocket = null;

        try {
            PipeAdvertisement pipeAdvertisement = createSocketAdvertisement(m_pipeID);
            netPeerGroup.getDiscoveryService().publish(pipeAdvertisement);
            m_serverSocket = new JxtaServerSocket(netPeerGroup, pipeAdvertisement, 10000, 32000);
            m_serverSocket.setSoTimeout(0);
            
        } catch (IOException e) {
            System.err.println("NCDMessageReceiver error [createSocketAdvertisement]: "+e.toString());
            System.out.println("failed to create a server socket");
            e.printStackTrace();
            System.exit(-1);
        }


        while (!m_stopReceiver) {
            try {
                System.out.println("Waiting for connections");
                JxtaSocket socket = (JxtaSocket) m_serverSocket.accept();


                if (socket != null) {
                    System.out.println("New socket connection accepted");

                    Thread thread = 
                            new Thread(new ConnectionHandler(socket, this.netcfg), "Connection Handler Thread");
                    
                    thread.start();
                }
            } catch (Exception e) {
                System.err.println("NCDMessageReceiver error [run]: "+e.toString());
                if (!m_stopReceiver)
                    e.printStackTrace();
            }
        }
        System.out.println("File Transfer Socket Closed");
    }

    /**
     * handles one incoming message transfer.
     */
    private class ConnectionHandler implements Runnable {
        JxtaSocket socket = null;
        NCDConfiguration ncdcfg;
        
        ConnectionHandler(JxtaSocket socket, NCDConfiguration ncdcfg) {
            this.socket = socket;
            this.ncdcfg = ncdcfg;
        }

        /**
         * Sends data over socket
         *
         * @param socket the socket
         */
        private void sendAndReceiveData(JxtaSocket socket) {
            try {
                String requesterPeerid = "";
                try{
                    requesterPeerid = ((net.jxta.socket.JxtaSocketAddress) socket.getRemoteSocketAddress()).getPeerId().toString();    
                }catch(Exception ex){
                    System.err.println("Error [requesterPeerid]:"+ex.toString());
                }
                
            
                long start = System.currentTimeMillis();

                // get the socket output stream
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                // get the socket input stream
                InputStream in = socket.getInputStream();
                DataInput dis = new DataInputStream(in);

                //first read the fileName from the stream (sender will tell us whats the prefered filename)
                String fileName = dis.readUTF();

                //next read the fileSize (this value is only beeing used to inform the user what size he has to expect)
                long fileSize = dis.readLong();
                
                String msgID =  dis.readUTF();
                
                
               
               
                
                
                Credential credential = socket.getCredentialDoc();


                boolean append = false;
                File file;

                
                long wantedOffset;
                String sender = (credential != null ? credential.getPeerID().toString() : "unknown");
                do {
                    file = getTargetFile(fileName, fileSize, sender);
                    if (file == null) {
                        out.writeLong(-1); //request offset -1 --> transfer aborted
                        out.flush();
                        socket.close();
                        return;
                    }

                    if (file.exists()) {
                        switch (fileExistsHandling(file)) {
                            case RESUME:
                                wantedOffset = file.length();
                                append = true;
                                System.out.println("resuming file:" + file.getName());
                                break;
                            case APPEND:
                                wantedOffset = 0;
                                append = true;
                                System.out.println("appending to file:" + file.getName());
                                break;
                            case OVERRIDE:
                                wantedOffset = 0;
                                append = false;
                                System.out.println("overriding file:" + file.getName());
                                break;
                            default:
                                wantedOffset = -2; //unknown append type
                        }
                    } else { //new file
                        wantedOffset = 0;
                    }
                } while (wantedOffset == -2);

                out.writeLong(wantedOffset); //request a specific offset from the sender
                out.flush();

                m_outputStream = new FileOutputStream(file, append);


                MessageDigest md5 = MessageDigest.getInstance("MD5");
                long receivedOffset = dis.readLong();
                assert (receivedOffset == 0); // we have requestes 0

                
            
                
                int size = dis.readInt();

                
                long total = 0;
                while (size != 0) {
                    byte[] buf = new byte[size];
                    md5.reset();
                    System.out.println("waiting for offset:" + receivedOffset);
                    dis.readFully(buf);
                    md5.update(buf);
                    byte[] checkSum = md5.digest();
                    System.out.println("sending md5:" + wantedOffset);

                    out.write(checkSum);
                    out.flush();
                    wantedOffset = receivedOffset + size;
                    System.out.println("sending next offset request:" + wantedOffset);
                    out.writeLong(wantedOffset);
                    out.flush();
                    log("waiting for next offset:" + wantedOffset);
                    long nextOffset = dis.readLong();
                    log("got offset");
                    int nextSize = dis.readInt();
                    log("got size");
                    if (nextOffset == wantedOffset) { // it seems the our outgoing checksum was correct (the sender sends what we have requested, not a retransmit)
                        log("received and verified offset and size");
                        m_outputStream.write(buf); //write out the old buffer
                        receivedOffset = nextOffset;
                        size = nextSize;
                        total = total + buf.length;
                        
                    } else {
                        //offset wrong?
                        log("checksum wrong, re-receiving offset " + nextOffset);
                    }
                    
                    
                    
                    System.out.println("****************************************");
                    System.out.print("Total: ");
                    System.out.println(fileSize);
                    System.out.print("Bytes receiveds: ");
                    System.out.print( total);                    
                    System.out.println("bytes");
                    System.out.print("Progresso: ");
                    Long fsize = new Long(fileSize);
                    Long ttal = new Long(total);
                    System.out.print( ttal.doubleValue()/fsize.doubleValue() * 100.0);                    
                    System.out.println("%");                    
                    System.out.println("****************************************");                    
                    
                    //update statistic
                    Long timeElapsed = System.currentTimeMillis() - start;
                    double rate = ttal.doubleValue()/(timeElapsed.doubleValue()/1000.0);
                    StDData.instanceOfStDData().updateDownload(msgID, requesterPeerid, fileName, ttal.doubleValue()/fsize.doubleValue() * 100.0, System.currentTimeMillis() - start,  rate, fsize.doubleValue(), ttal.doubleValue());
                    
                }

                out.close();
                in.close();
                m_outputStream.flush();
                m_outputStream.close();

                long finish = System.currentTimeMillis();
                long elapsed = finish - start;
                m_thoughputKByte = (total / elapsed) * 1000 / 1024;
                info("transfer of" + file.getName() + "complete");
                socket.close();
                Double ttal = (new Long(total)).doubleValue();
                Double telapsed = (new Long(elapsed)).doubleValue();
                double rateFinal = ttal/(telapsed/1000.0);
                Long fsize = new Long(total);
                StDData.instanceOfStDData().updateDownload(msgID, requesterPeerid, fileName,100.0, elapsed,rateFinal,fsize.doubleValue(),fsize.doubleValue());
                
                /* 
                 * separating files from messages and putting the messages into 
                 * array of pending messages
                 */
                
                LOP2PMessage msg = new LOP2PMessage(file, this.ncdcfg.getReceivedFilesPath(), this.ncdcfg.getMessagesTempPath());
                synchronized(this.ncdcfg.getNCDData().getMessageReceiverBufferList()){
                    this.ncdcfg.getNCDData().getMessageReceiverBufferList().add(msg);
                }
                
                //log("Connection closed");
            } catch (Exception ie) {
                System.err.println("NCDMessageReceiver error [sendAndReceiveData]: "+ie.toString());
                log(ie.getMessage());
                ie.printStackTrace();
                
            }
        }

        public void run() {
            try{
                sendAndReceiveData(socket);
            }catch(Exception ex){
                System.err.println("NCDMessageReceiver error [run ConnectionHandler]: "+ex.toString());
            }
        }
    }

    protected void info(String message) {
        NCDMessageReceiver.log(message);
    }

    private static final Logger LOG = Logger.getLogger(NCDMessageReceiver.class.getName());

    private static void log(String s) {
        LOG.fine(s);
    }

    public long getThoughputKByte() {
        return m_thoughputKByte;
    }

    protected OVERRIDE_BEHAVIOR fileExistsHandling(File file) {
        return OVERRIDE_BEHAVIOR.RESUME;
    }

    /**
     * Ask where to store the file
     *
     * @param fileName
     * @param size
     * @param sender
     * @return the target position or null if the user has rejected the transfer
     */
    protected File getTargetFile(String fileName, long size, String sender) {
        try{
            //obviously this is only a sample implementatation...

            File incomingDir = new File(this.netcfg.getReceivedFilesPath());
            if (!incomingDir.exists())
                incomingDir.mkdir();

            return new File(incomingDir, fileName);
        }catch (Exception ex){
            System.err.println("NCDMessageReceiver error [getTargetFile]: "+ex.toString());
        }
        return null;
    }


}

