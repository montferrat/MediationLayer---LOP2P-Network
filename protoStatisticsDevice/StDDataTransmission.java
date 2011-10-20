/*
 * StDDataTransmission: class that stores information about downloads e uploads.
 */

package protoStatisticsDevice;

/**
 *
 * @author Rafael de Santiago
 */
public class StDDataTransmission {
    private String msgID;
    private String peerID;
    private String title;
    private Double progress;
    private Long timeElapsed;
    private Double totalSize;
    private Double rate;
    private Double downloaded;
    
    StDDataTransmission(String msgID, String peerID){
        this.msgID = msgID;
        this.peerID = peerID;
        this.title = "";
        this.progress = 0.0;
        this.timeElapsed = 0L;
        this.totalSize = 0.0;
        this.rate = 0.0;
        this.downloaded = 0.0;
        
    }
    
    public String getMsgID() {
        return msgID;
    }

    public void setMsgID(String msgID) {
        this.msgID = msgID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Double getProgress() {
        return progress;
    }

    public void setProgress(Double progress) {
        this.progress = progress;
    }

    public String getPeerID() {
        return peerID;
    }

    public void setPeerID(String peerID) {
        this.peerID = peerID;
    }

    public Long getTimeElapsed() {
        return timeElapsed;
    }

    public void setTimeElapsed(Long timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    public Double getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Double totalSize) {
        this.totalSize = totalSize;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public Double getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(Double downloaded) {
        this.downloaded = downloaded;
    }
    
}
