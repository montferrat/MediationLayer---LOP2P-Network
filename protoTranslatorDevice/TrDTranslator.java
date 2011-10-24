/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package protoTranslatorDevice;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import utils.*;
/**
 *
 * @author Rafael de Santiago
 */
import protoStandardAbstractData.*;
import org.jdom.*;
public class TrDTranslator {
    
    private TrDConfiguration trdcfg;
    
    
    /**
     * Constructor of the class TrDTranslator. This class have the purpose of
     * represent the Translator Device.
     * 
     * @param trdcfg configuration object of the Translator Device
     */        
    public TrDTranslator(TrDConfiguration trdcfg){
        //translator configurations
        this.trdcfg = trdcfg;
    }

    /**
     * Translate a loware message into a LOP2P message. Specific for 
     * Store/Submit requests.
     * 
     * @param source message that will be translated
     */             
    public ArrayList translateStoreRequest (String source){
        if (this.getTrdcfg().getLorMessagesType().equals("JSON")){
            try {
                JSONObject msg = new JSONObject(source);
                JSONArray ja = msg.optJSONArray("message");
                if (ja == null){
                    ja = new JSONArray();
                    ja.put(msg.get("message"));
                }
               
                ArrayList result = new ArrayList();
                for (int i = 0; i < ja.length(); i++) {
                    Object obj = ja.get(i);
                    if (obj.getClass() == String.class){
                        result.add((String) obj);
                    }
                    if (obj.getClass() == JSONObject.class){
                        result.add(((JSONObject) obj).getString("loid"));
                        result.add(((JSONObject) obj).getString("peerid"));                        
                    }                    
                }
                return result;
            } catch (JSONException ex) {
                Logger.getLogger(TrDTranslator.class.getName()).log(Level.SEVERE, null, ex);
            }        
        }
        return null;
    }    
    
    /**
     * Translate a loware message into a LOP2P message.
     * 
     * @param source message that will be translated
     */                
    public ArrayList translate (String source){
        if (this.getTrdcfg().getLorMessagesType().equals("JSON")){
            try {
                JSONObject msg = new JSONObject(source);
                JSONArray ja = msg.optJSONArray("message");
                if (ja == null){
                    ja = new JSONArray();
                    ja.put(msg.get("message"));
                }

                ArrayList result = new ArrayList();
                for (int i = 0; i < ja.length(); i++) {
                    Object obj = ja.get(i);
                    if (obj.getClass() == String.class){
                        result.add(new LOP2PMetadata((String) obj));
                    }
                    if (obj.getClass() == JSONObject.class){
                        result.add(this.translateJsonToLOP2PMetadata((JSONObject) obj));
                    }
                }
                return result;
            } catch (JSONException ex) {
                //Logger.getLogger(TrDTranslator.class.getName()).log(Level.SEVERE, null, ex);
                System.err.println("Problems when trying to translate one metadata. Source: "+source);
            }
        }
        return null;
    }
    
    /**
     * Translate a LOP2P message into a loware message. 
     * Specific for Submit/Store responses.
     * 
     * @param path path of the Learning Object
     */       
    public Object translateBackStore(String path){
        if (this.getTrdcfg().getLorMessagesType().equals("JSON")){
            try {
                JSONObject obj = new JSONObject();
                obj.append("lopath", path);
                return obj.toString();
            } catch (JSONException ex) {
                Logger.getLogger(TrDTranslator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }    
    
    /**
     * Translate a LOP2P message into a loware message. 
     * 
     * @param mtdt metadata to be attached
     * @param peersource peerid of the peer source
     */
    public Object translateBack(LOP2PMetadata mtdt, String peersource){
        if (this.getTrdcfg().getLorMessagesType().equals("JSON")){
            try {
                JSONObject obj = new JSONObject();
                obj.append("metadata", mtdt.getXMLString());
                obj.append("peerid", peersource);
                return obj.toString();
            } catch (JSONException ex) {
                Logger.getLogger(TrDTranslator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
        
      
    /**
     * Translate a JSON metadata into a LOM metadata. 
     * 
     * @param source JSON object to be converted
     * 
     * @return metadata converted
     */    
    public LOP2PMetadata translateJsonToLOP2PMetadata(JSONObject source){
        try {
            JSONObject jobj = source;
            
            
            
            LOP2PMetadata metadata = new LOP2PMetadata();

            //*** GENERAL
            
            Element general = null;
            if ((jobj.has("keyword")) || (jobj.has("title"))){
                general = new Element("general");
                metadata.setGeneral(general);
            }            
            
            if (jobj.has("keyword")){
                Element e2 = new Element("keyword");
                e2.setText(jobj.getString("keyword"));
                general.addContent(e2);
            }
            
            //*** TECHNICAL
            
            Element technical = null;
            if ((jobj.has("location"))){
                technical = new Element("technical");
                metadata.setTechnical(technical);
            }                        

            if ((jobj.has("location"))){
                Element e2 = new Element("location");
                e2.setText(jobj.getString("location"));
                technical.addContent(e2);
            }                       
            
              
            return metadata;
            
        } catch (JSONException ex) {
            Logger.getLogger(TrDTranslator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
        
    }

    /**
     * @return the trdcfg
     */
    public TrDConfiguration getTrdcfg() {
        return trdcfg;
    }
    
}
