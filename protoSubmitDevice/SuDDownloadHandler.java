/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package protoSubmitDevice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import protoSearchDevice.SDConfiguration;
import protoSearchDevice.SDTorrentRequest;
import protoStandardAbstractData.LOP2PMetadata;
import protoStatisticsDevice.StDData;
import protoTranslatorDevice.TrDTranslator;

/**
 *
 * @author Administrador
 */
public class SuDDownloadHandler extends Thread{
    private String loid = "";

    //configuration
    private SuDConfiguration sdcfg;

    //translator
    TrDTranslator translator;
    
    private SDConfiguration searchCfg;
    private StDData statisticData;
    private String peerid;
    private boolean[] blocks;

    public SuDDownloadHandler(String loid, SuDConfiguration sdcfg, StDData sdata, TrDTranslator trl, SDConfiguration searchCfg){
        this.setPriority(MIN_PRIORITY);
        this.loid = loid;
        this.sdcfg = sdcfg;
        this.translator = trl;
        this.statisticData = sdata;
        this.searchCfg = searchCfg;
    }

    @Override
    public void run(){
        Calendar targetCal = new GregorianCalendar();
        targetCal.add(Calendar.SECOND, this.sdcfg.getExpirationTimeTorrent());
        Date target = targetCal.getTime();
        boolean entrou = false;
        //File dirBlocks = new File(this.gdcfg.getGdData().getBlocksLOs());
        //dirBlocks.mkdir();
        
        while(true){
            //verifies if its time to search new seeds!
            Date now = new Date();
            if (target.getTime() > now.getTime() || entrou == false){
                target = targetCal.getTime();
                System.out.println("Tempo (Target):" + target);
                entrou=true;
                SDTorrentRequest sdtr = new SDTorrentRequest(searchCfg, this.translator.getTrdcfg());
                boolean[] totalBlocksDownloaded = searchCfg.getGDCfg().getGdData().getMyMetadataByID(loid).getBlocks();

                //Verifies if we have any blocks from the LOID we're downloading
                for (int y = 0; y < totalBlocksDownloaded.length; y++){
                    File bloco = new File(sdcfg.getGDData().getBlocksLOs()+searchCfg.getGDCfg().getGdData().getMyMetadataByID(loid).getIdentifier()+"/quadro."+((y+1)*512));
                    if (bloco.exists() == true)
                        totalBlocksDownloaded[y] = true;
                    else
                        totalBlocksDownloaded[y] = false;
                }

                File dirFile = new File(this.sdcfg.getGDData().getBlocksLOs()+loid);
                dirFile.mkdir();
                SuDBitHandler sudbt = new SuDBitHandler(loid, sdcfg, statisticData, translator, searchCfg, totalBlocksDownloaded, dirFile);

                try {
                    System.out.println("Starting the search for blocks ...");
                    sdtr.search(this.loid);
                    System.out.println("Search has ended.");

                    // Gets the blocks to download, based on the TORRENT_REQUESTs already received
                    ArrayList blocksToDownload = new ArrayList();
                    blocksToDownload = sdcfg.getNCDCfg().getNCDData().getPeerBlocksOfFile(loid);


                    // Calls the BitHandlers to download the blocks we want, 10 at a time,
                    int j = 0;
                    int y = 0;
                    
                    for (int i = 0 ; i <= blocksToDownload.size(); i = i + 2 ) {
                        if ((String)blocksToDownload.get(i) != null) {
                            peerid = (String)blocksToDownload.get(i);
                            blocks = (boolean[])blocksToDownload.get(i+1);
                            if (sudbt.getDownloadedBlocks(y) == false && blocks[y] == true && SuDBitHandler.getInstances() < 10) {
                                System.out.println("Starting to download block " + (j+512) + ", from " + peerid);
                                sudbt.run(peerid, j+512);
                                System.out.println("Block " + (j+512) + " from peer " + peerid + " was downloaded sucessfully");
                            }
                        }
                        if ((i + 2) >= blocksToDownload.size())
                            break;
                        j = j + 512;
                        y++;
                    }

                    // If we still don't have all the blocks, search gradually for peers that have the missing blocks and
                    // download them.
                    
                    j = 0;
                    for (int i = 0 ; i <= totalBlocksDownloaded.length; i = i + 2 ) {
                        if ((String)blocksToDownload.get(i) != null) {
                            peerid = (String)blocksToDownload.get(i);
                            blocks = (boolean[])blocksToDownload.get(i+1);
                        }
                        for (int x = 0; x < blocks.length; x++) {
                            if (sudbt.getDownloadedBlocks(x) == false && blocks[x] == true && SuDBitHandler.getInstances() < 10) {
                                System.out.println("Starting to download block " + (j+512) + ", from " + peerid);
                                sudbt.run(peerid, j+512);
                                System.out.println("Block " + (j+512) + " from peer " + peerid + " was downloaded sucessfully");
                            }
                            j = j + 512;
                        }
                        if (i + 2 < totalBlocksDownloaded.length)
                            break;
                        
                    }

                    // Check if we already have all the blocks we need, if we do, join back the blocks to form the original file
                    boolean complete = true;
                    
                    for (int i = 0; i < totalBlocksDownloaded.length; i++) {
                        if (sudbt.getDownloadedBlocks(i) == false) { // If haven't downloaded all blocks, get out and continue
                            complete = false;
                        }
                    }
                    if (complete == true) { // If all blocks have been downloaded, join back the file
                        this.sdcfg.getGDData().saveMyMetadata();
                        File fq = new File(sdcfg.getGDData().getBlocksLOs()+searchCfg.getGDCfg().getGdData().getMyMetadataByID(loid).getIdentifier()+searchCfg.getGDCfg().getGdData().getMyMetadataByID(loid).getIdentifier()+".zip");
                        System.out.println("All blocks have been downloaded.");
                        System.out.println("The resulting file will be named: " + fq.toString());
                        fq.createNewFile();
                        FileOutputStream escrita = new FileOutputStream(fq);
                        int inicio = 0;
                        int id = 512;
                        for (int b=0; b< totalBlocksDownloaded.length; b+=1) {
                            if (b + 1 < totalBlocksDownloaded.length) {
                                byte buffer[] = new byte[512*1024];
                                FileInputStream leitor = new FileInputStream(sdcfg.getGDData().getBlocksLOs()+searchCfg.getGDCfg().getGdData().getMyMetadataByID(loid).getIdentifier()+"/quadro."+id);
                                leitor.read(buffer, 0, 512*1024);
                                escrita.write(buffer);
                            } else {
                                byte buffer[] = new byte[(int)searchCfg.getGDCfg().getGdData().getMyMetadataByID(loid).getSizeOA() - inicio*1024];
                                FileInputStream leitor = new FileInputStream(sdcfg.getGDData().getBlocksLOs()+searchCfg.getGDCfg().getGdData().getMyMetadataByID(loid).getIdentifier()+"/quadro."+id);
                                leitor.read(buffer, 0, (int)searchCfg.getGDCfg().getGdData().getMyMetadataByID(loid).getSizeOA() - inicio*1024);
                                escrita.write(buffer);
                                System.out.println("Joining of files has ended.");
                        }
                        id = id + 512;
                        inicio = inicio + 512;
                        }
                    }

                } catch (FileNotFoundException ex) {
                    Logger.getLogger(SuDDownloadHandler.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(SuDDownloadHandler.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(SuDDownloadHandler.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    Logger.getLogger(SuDDownloadHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }



}
