/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
// singleton!!!
package protoStatisticsDevice;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author Rafael de Santiago
 */
public class StDData {
    
    private static StDData instance;
    
    private String peername;
    private String peerid;
    private ArrayList<StDDataTransmission> downloads;
    private ArrayList<StDDataTransmission> uploads;
    private ArrayList<String> peersConnected;
    
    //log files
    private String downloadPathLOG = "./logs/downloads.log";
    private String peersPathLOG = "./logs/peers.log";
    
    private StDData(){
        this.downloads = new ArrayList();
        this.uploads = new ArrayList();
        this.peersConnected = new ArrayList();
        
        //crete log directory
        File dir = new File("./logs");
        if (dir.exists() == false){
            dir.mkdir();
        }
            
        
    }
    public static StDData instanceOfStDData(){
        if (instance == null){
            instance = new StDData();
        }        
        return instance;
    }
    
    public void addNewPeerConnected(String peerid){
        //logging action
        DateFormat formatter = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
        String dateTime = formatter.format(new Date());
        String logData = dateTime 
                + ";" + "ADD" 
                + ";" + peerid;
        this.appendLogFile(this.peersPathLOG, logData);                
        
        synchronized(this.getPeersConnected()){           
            this.getPeersConnected().add(peerid);    
        } 
    }
    
    public void removePeerConnected(String peerid){
        //logging action
        DateFormat formatter = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
        String dateTime = formatter.format(new Date());
        String logData = dateTime 
                + ";" + "REMOVE" 
                + ";" + peerid;
        this.appendLogFile(this.peersPathLOG, logData);                
        
        for (String peerNow: this.getPeersConnected()){
            if (peerNow.equals(peerid)){
                this.getPeersConnected().remove(peerNow);
                break;
            }
        }
    }    
    
    public void addNewDownload(String msgID, String peerID){        
        peerID = peerID.substring(peerID.indexOf("uuid"));
        StDDataTransmission stddt = new StDDataTransmission(msgID, peerID);
        synchronized(this.getDownloads()){
            this.getDownloads().add(stddt);    
        }
    }


    
    public void addNewUpload(String msgID, String peerID){
        peerID = peerID.substring(peerID.indexOf("uuid"));
        StDDataTransmission stddt = new StDDataTransmission(msgID, peerID);
        synchronized(this.getUploads()){
            this.getUploads().add(stddt);    
        }
    }    
    
    public void updateDownload(String msgID, String peerID, String title, Double progress, 
            Long time, Double rate, Double totalsize, Double downloaded){
        //search the right message
        peerID = peerID.substring(peerID.indexOf("uuid"));
        for (StDDataTransmission stddt: this.getDownloads()){
            if ((stddt.getMsgID().equals(msgID)) && (stddt.getPeerID().equals(peerID))){
                stddt.setTitle(title);
                stddt.setProgress(progress);
                stddt.setTimeElapsed(time);
                stddt.setRate(rate);
                stddt.setDownloaded(downloaded);
                stddt.setTotalSize(totalsize);
                break;
            }
        }
    }
    
    public void updateUpload(String msgID, String peerID, Double progress, 
            Long time, Double rate, Double totalsize, Double downloaded){
        //search the right message
        peerID = peerID.substring(peerID.indexOf("uuid"));
        for (StDDataTransmission stddt: this.getUploads()){
            if ((stddt.getMsgID().equals(msgID)) && (stddt.getPeerID().equals(peerID))){
                stddt.setProgress(progress);
                stddt.setTimeElapsed(time);
                stddt.setRate(rate);
                stddt.setTotalSize(totalsize);
                stddt.setDownloaded(downloaded);
                break;
            }
        }
    }   

    public void finishDownload(String msgID, String peerID){

        //search the right message
        peerID = peerID.substring(peerID.indexOf("uuid"));
        for (StDDataTransmission stddt: this.getDownloads()){
            if ((stddt.getMsgID().equals(msgID)) && (stddt.getPeerID().equals(peerID))){
                
                //logging action
                DateFormat formatter = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
                String dateTime = formatter.format(new Date());
                String logData = dateTime 
                        + ";" + stddt.getPeerID() 
                        + ";" + stddt.getTitle()
                        + ";" + stddt.getDownloaded().toString()
                        + ";" + stddt.getTotalSize().toString()
                        + ";" + stddt.getProgress().toString()
                        + ";" + stddt.getRate().toString()
                        + ";" + stddt.getTimeElapsed().toString();
                this.appendLogFile(this.downloadPathLOG, logData);                
                
                //remove
                //this.getDownloads().remove(stddt);
                break;
            }
        }
        
        
    }    

    
    public StDDataTransmission getDownloadByIDs(String msgID, String peerID){
        //search the right message
        peerID = peerID.substring(peerID.indexOf("uuid"));
        for (StDDataTransmission stddt: this.getDownloads()){
            if ((stddt.getMsgID().equals(msgID)) && (stddt.getPeerID().equals(peerID))){
                return stddt;
            }
        }
        return null;
    }    
    
    
    public void finishUpload(String msgID, String peerID){
        //search the right message
        peerID = peerID.substring(peerID.indexOf("uuid"));
        for (StDDataTransmission stddt: this.getUploads()){
            if ((stddt.getMsgID().equals(msgID)) && (stddt.getPeerID().equals(peerID))){
                //this.getUploads().remove(stddt);
                break;
            }
        }
    }

    public ArrayList<StDDataTransmission> getDownloads() {
        return downloads;
    }

    public void setDownloads(ArrayList<StDDataTransmission> downloads) {
        this.downloads = downloads;
    }


    public ArrayList<StDDataTransmission> getUploads() {
        return uploads;
    }

    public void setUploads(ArrayList<StDDataTransmission> uploads) {
        this.uploads = uploads;
    }

    public ArrayList<String> getPeersConnected() {
        return peersConnected;
    }

    public String getPeername() {
        return peername;
    }

    public void setPeername(String peername) {
        this.peername = peername;
    }

    public String getPeerid() {
        return peerid;
    }

    public void setPeerid(String peerid) {
        this.peerid = peerid;
    }
    
    private synchronized void appendLogFile(String path, String data){
       try{
            // Create file 
            FileWriter fstream = new FileWriter(path,true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(data+"\r\n");
            //Close the output stream
            out.close();

       }catch (Exception e){//Catch exception if any
          System.err.println("Error when storing a log: " + e.toString());
       }        
    }
    
}
