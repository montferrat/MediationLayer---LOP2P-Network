/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package protoSubmitDevice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.ElementIterator;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.StringMessageElement;
import protoSearchDevice.SDConfiguration;
import protoStatisticsDevice.StDData;
import protoTranslatorDevice.TrDTranslator;
import protonetcommunicationdevice.LOP2PMessage;

/**
 *
 * @author Administrador
 */
public class SuDBitHandler extends Thread{
    private static Integer instances=0;
    private String loid = "";

    //configuration
    private SuDConfiguration sdcfg;

    //translator
    TrDTranslator translator;

    private SDConfiguration searchCfg;
    private StDData statisticData;
    private ArrayList blocksToDownload;
    private int block;
    String peerid;
    boolean[] totalBlocksDownloaded;
    private File dirFile;

    private File src;
    private File dst;

    public SuDBitHandler(String loid, SuDConfiguration sdcfg, StDData sdata, TrDTranslator trl, SDConfiguration searchCfg, boolean[] totalBlocksDownloaded, File dirFile){
        this.setPriority(MIN_PRIORITY);
        this.loid = loid;
        this.sdcfg = sdcfg;
        this.translator = trl;
        this.statisticData = sdata;
        this.searchCfg = searchCfg;
        this.blocksToDownload = blocksToDownload;
        this.totalBlocksDownloaded = totalBlocksDownloaded;
        this.dirFile = dirFile;


        //number of instances increment
        synchronized(SuDBitHandler.instances){
            SuDBitHandler.instances++;
        }
        this.setPriority(MIN_PRIORITY);
    }

    /**
     * @return the instances
     */
    public synchronized static Integer getInstances() {
        return instances;
    }

        /**
     * @return the instances
     */
    public synchronized boolean getDownloadedBlocks(int i) {
        return totalBlocksDownloaded[i];
    }

   public void run(String peerid, int block){
        //eternal loop
        this.block = block;
        this.peerid = peerid;
            //tratar download de cada bit
            String translatedMessage = "";
            //if the peer is that, send the object, if not, search!
            //send message
            ArrayList msgPool = this.sdcfg.getNCDCfg().getNCDData().getMessageBufferList();
            Date date = new Date();
            String msgID = "msgSentStore" + this.sdcfg.getNCDCfg().NT_INSTANCE_NAME + date.getTime();
            String peerTarget = peerid; // Add the PeerID, based on the ArrayList received
            peerTarget = peerTarget.substring(peerTarget.indexOf("uuid"));
            String thisPeer = this.sdcfg.getNCDCfg().getNetworkManager().getPeerID().toURI().toString();
            thisPeer = thisPeer.substring(thisPeer.indexOf("uuid"));
            LOP2PMessage msgToSend = new LOP2PMessage(thisPeer, peerTarget, this.sdcfg.getNCDCfg().getMessageNamespace());
            msgToSend.addItem("IDMSG", msgID);
            msgToSend.addItem("TYPE", LOP2PMessage.MESSAGE_TYPE.TORRENT_DOWNLOAD.toString());
            msgToSend.addItem("LOID", loid);
            msgToSend.addItem("BLOCK", ""+this.block);
            msgPool.add(msgToSend);

            //insert new mapping for download
            this.statisticData = StDData.instanceOfStDData();
            this.statisticData.addNewDownload(msgID, peerTarget);

            //wait response for some seconds
            Calendar targetCal = new GregorianCalendar();
            targetCal.add(Calendar.SECOND, sdcfg.getExpirationTimeTorrent());
            Date target = targetCal.getTime();
            Date now = new Date();
            translatedMessage = "";
            Double downloadPerc = 0.0;
            boolean aptoAParar = false; //stop only if the message was received
            while (aptoAParar != true){//(target.getTime() > now.getTime())){
                LOP2PMessage msg = (LOP2PMessage)this.sdcfg.getNCDCfg().getNCDData().searchForResponseById(msgID);
                if (msg != null){
                    aptoAParar = true;
                    //getting metadatas
                    Message.ElementIterator itMsg =  msg.getMessageElements();

                    while(itMsg.hasNext()){
                        MessageElement el = itMsg.next();
                        if (el.getElementName().equals("fileRefence")){
                            //recover the element
                            StringMessageElement sme =
                                                            (StringMessageElement) el;
                            String filePath = new String(sme.getBytes(true));

                            //copy object to repository peer
                            File loFile = new File(filePath);
                            //String newName = "LOP2P" + now.getTime()+loFile.getName();
                            String newPath = dirFile+"/quadro."+block;
                            File newLO = new File (newPath);
                            try {
                                copyFile(loFile, newLO);
                                totalBlocksDownloaded[((int)block / 512) - 1] = true;
                                this.sdcfg.getGDData().getMyMetadataByID(loid).setBlocks(totalBlocksDownloaded);
                                this.sdcfg.getGDData().saveMyMetadata();
                            } catch (IOException ex) {
                                Logger.getLogger(SuDBitHandler.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            //return the location in this computer
                            translatedMessage = newLO.getAbsolutePath();//(String) translator.translateBackStore(newLO.getAbsolutePath());
                            break;
                        }
                    }
                    //send statistics if necessary
                    Double downloadAux = this.statisticData.getDownloadByIDs(msgID, peerTarget).getProgress();
                    if (downloadPerc != downloadAux){
                        downloadPerc = downloadAux;
                        //send statistic
                        //synchronized(toCliente){
                        //    toCliente.write(downloadPerc.toString().getBytes());
                        //}
                    }

                }
                now = new Date();
            }
            //erase statistic
            this.statisticData.finishDownload(msgID, peerTarget);
        
        SuDBitHandler.instances--;
    }

    /**
     * @param aInstances the instances to set
     */
    public synchronized static void setInstances(Integer aInstances) {
        instances = aInstances;
    }

    void copyFile(File src, File dst) throws IOException {
        this.src = src;
        this.dst = dst;
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();

    }


}
