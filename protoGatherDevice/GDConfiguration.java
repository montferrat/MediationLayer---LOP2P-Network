/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package protoGatherDevice;

/**
 *
 * @author Rafael de Santiago
 */
public class GDConfiguration {
    private GDData gddata;
    
    /**
     * Creates a object GDData for Gather Device configuration.
     * 
     */    
    public GDConfiguration(){
        this.gddata = new GDData("./myMetadatas", "./othersMetadatas");
    }

    /**
     * Return the object GDData of the Gather Device configuration.
     * 
     * @return            Return the GDData
     */
    public GDData getGdData() {
        return gddata;
    }

    /**
     * Configurate a GDData object to Gather Device configuration.
     * 
     * @param   gddata GDData object
     */
    public void setGdData(GDData gddata) {
        this.gddata = gddata;
    }
}
