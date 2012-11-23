/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package protoGatherDevice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import protoStandardAbstractData.LOP2PMetadata;
import protoTranslatorDevice.*;
import org.json.*;

/**
 *
 * @author Rafael de Santiago
 */
public class GDGather {
    
    private GDConfiguration gdcfg;
    
    private TrDTranslator translator;
    String peername;
    
    
    /**
     * Constructor of the class GDGather. This class have the purpose to 
     * represent the Gather Device. 
     * 
     * @param   peername    name of the the peer
     * @param   gdcfg       configuration object of the Gather Device
     * @param   trdcfg      configuration object of the Translator Device
     */        
    public GDGather(String peername, GDConfiguration gdcfg, TrDConfiguration trdcfg){
        this.peername = peername;
        this.gdcfg = gdcfg;
        this.translator = new TrDTranslator(trdcfg);
    }
    
    /**
     * Receive the message of the loware with the purpose to store the local 
     * metadatas. 
     * 
     * @param   message    message received from loware
     */      
    
    public void receiveMetadataFromLoware(JSONObject message){
        ArrayList metadatas = translator.translate(message);
        this.gdcfg.getGdData().getMyMetadatas().clear();
        for(int i=0; i<metadatas.size(); i++){
            //setNewId
            LOP2PMetadata mtdt = (LOP2PMetadata)metadatas.get(i);

            Date now = new Date();
            mtdt.setNewLOP2PID(this.peername, now.getTime(), i);

            //criando blocos
            String local = mtdt.getLocation();
            File dirFile = new File(this.gdcfg.getGdData().getBlocksLOs()+mtdt.getIdentifier());
            dirFile.mkdir();
            File oa = new File (local);

            //carregando informacoes
            mtdt.setSizeOA(oa.length());

            //grava no LOP2PMetadata os blocos (array de Boolean);
            long qtdBlocos = oa.length()/(512*1024);
            if ((oa.length() % 512) != 0) {
                qtdBlocos = qtdBlocos + 1;
            }
            boolean blocks[] = new boolean[(int)qtdBlocos];
            for (int b=0;b<blocks.length; b++){
                blocks[b] = true;
            }
            mtdt.setBlocks(blocks);
            try {
                FileInputStream fr = new FileInputStream(oa);
                int pos = 0;
                for (int j=0;j<oa.length(); j+=(512*1024)){
                    byte buffer[];
                    System.out.println(oa.length());
                    if (j + 512*1024 <= oa.length()){
                        buffer = new byte[512*1024];
                        fr.read(buffer, 0, (512*1024));
                    }else{
                        buffer = new byte[(int) oa.length() - (int)j];
                        fr.read(buffer, 0, ((int)oa.length() - (int)j));
                    }
                    pos = pos + 512;
                    long id = pos;
                    File fq = new File(this.gdcfg.getGdData().getBlocksLOs()+mtdt.getIdentifier()+"/quadro."+id);
                    fq.createNewFile();
                    FileOutputStream quadro = new FileOutputStream(fq);
                    quadro.write(buffer);
                    quadro.close();
                }
                fr.close();

                //include in myMetadata
                this.gdcfg.getGdData().getMyMetadatas().add(metadatas.get(i));


                // TESTE PARA AGRUPAMENTO DOS BLOCOS

                File fq = new File(this.gdcfg.getGdData().getBlocksLOs()+mtdt.getIdentifier()+mtdt.getIdentifier());
                fq.createNewFile();
                FileOutputStream escrita = new FileOutputStream(fq);
                int inicio = 0;
                int id = 512;
                for (int b=0; b< blocks.length; b+=1) {
                    if (b + 1 < blocks.length) {
                        byte buffer[] = new byte[512*1024];
                        FileInputStream leitor = new FileInputStream(this.gdcfg.getGdData().getBlocksLOs()+mtdt.getIdentifier()+"/quadro."+id);
                        leitor.read(buffer, 0, 512*1024);
                        escrita.write(buffer);
                    } else {
                        byte buffer[] = new byte[(int)mtdt.getSizeOA() - inicio*1024];
                        FileInputStream leitor = new FileInputStream(this.gdcfg.getGdData().getBlocksLOs()+mtdt.getIdentifier()+"/quadro."+id);
                        leitor.read(buffer, 0, (int)mtdt.getSizeOA() - inicio*1024);
                        escrita.write(buffer);
                    }
                    id = id + 512;
                    inicio = inicio + 512;
                }

                // FIM 

            } catch (Exception ex) {
                System.out.println("Erro ao dividir arquivo.");
                Logger.getLogger(GDGather.class.getName()).log(Level.SEVERE, null, ex);
            }


        }
        this.gdcfg.getGdData().saveAll();
    }
    

    
}
