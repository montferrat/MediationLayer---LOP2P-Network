/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package protoStandardAbstractData;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.*;
import javax.xml.*;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.*;
import org.xml.sax.InputSource;
/**
 *http://www.cafeconleche.org/books/xmljava/chapters/
 * @author Rafael de Santiago
 */

/*
 * 
 *<?xml version="1.0"?>

<lom
  xmlns="http://www.imsglobal.org/xsd/imsmd_rootv1p2p1"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.imsglobal.org/xsd/imsmd_rootv1p2p1 imsmd_rootv1p2p1.xsd">

  <general>
    <title>
      <langstring>Cruzadas</langstring>
    </title>
    <catalogentry>
      <catalog>eXe Authored Course ID</catalog>
      <entry>
        <langstring>OAI-01</langstring>
      </entry>
    </catalogentry>
    <language>pt</language>
    <description>
      <langstring>Descrição breve sobre as cruzadas</langstring>
    </description>
    <aggregationlevel>
      <vocabulary>
        <source>
          <langstring xml:lang="x-none">LOMv1.0</langstring>
        </source>
        <value>
          <langstring xml:lang="x-none">3</langstring>
        </value>
      </vocabulary>
    </aggregationlevel>
  </general>

  <lifecycle>
    <contribute>
      <role>
        <source>
	  <langstring xml:lang="x-none">LOMv1.0</langstring>
	</source>
	<value>
	  <langstring xml:lang="x-none">Author</langstring>
	</value>
      </role>
      <centity>
        <vcard>BEGIN:vCard FN:Rafael de Santiago END:vCard</vcard>
      </centity>
      <date>
        <datetime>2008-08-28</datetime>
      </date>
    </contribute>
    <contribute>
      <role>
        <source>
	  <langstring xml:lang="x-none">LOMv1.0</langstring>
	</source>
	<value>
	  <langstring xml:lang="x-none">Publisher</langstring>
	</value>
      </role>
      <centity>
        <vcard>BEGIN:vCard FN:Publicar END:vCard</vcard>
      </centity>
      <date>
        <datetime>2008-08-28</datetime>
      </date>
    </contribute>
    <contribute>
      <role>
        <source>
	  <langstring xml:lang="x-none">LOMv1.0</langstring>
	</source>
	<value>
	  <langstring xml:lang="x-none">Unknown</langstring>
	</value>
      </role>
      <centity>
        <vcard>BEGIN:vCard FN:Wikipédia END:vCard</vcard>
      </centity>
      <date>
        <datetime>2008-08-28</datetime>
      </date>
    </contribute>
  </lifecycle>

  <metadata>
    <metadatascheme>ADL SCORM 1.2</metadatascheme>
  </metadata>

  <technical>
    <format></format>
  </technical>

  <relation>
    <resource>
      <description>
        <langstring>Relação</langstring>
      </description>
    </resource>
  </relation>

  <rights>
    <copyrightandotherrestrictions>
      <source>
        <langstring xml:lang="x-none">LOMv1.0</langstring>
      </source>
      <value>
        <langstring xml:lang="x-none">no</langstring>
      </value>
    </copyrightandotherrestrictions>
    <description>
      <langstring>Copyright</langstring>
    </description>
  </rights>
</lom>

 * 
 */



public class LOP2PMetadata extends Document{
    private boolean blocks[];
    private String fileName;
    private long sizeOA;
    private Element lom;
    private Element general;
    private Element lifeCycle;
    private Element metaMetadata;
    private Element technical;
    private Element educational;
    private Element rights;
    private Element relation;
    private Element annotation;
    private Element classification;
    
    
    /**
     * Constructor of the class LOP2PMetadata. This class have the purpose of
     * standard metadata object (IEEE LOM) of the Mediation Layer. This constructor create 
     * the root "lom" of the metadata
     * 
     */        
    public LOP2PMetadata(){
        lom = new Element("lom");//, Namespace.getNamespace("xmlns", "http://www.imsglobal.org/xsd/imsmd_rootv1p2p1"));
        this.addContent(lom);
    }

    /**
     * Construnctor of the class LOP2PMetadata. This class have the purpose of
     * standard metadata object (IEEE LOM) of the Mediation Layer. With a xml
     * string parameter, this contructor create the object
     * 
     * @param strXml xml string that will be converted in this object
     */            
    public LOP2PMetadata(String strXml){
    //    lom = new Element("lom");
    //    this.addContent(lom);
        try {

            SAXBuilder builder = new SAXBuilder();
            builder.setValidation(false);
            builder.setIgnoringElementContentWhitespace(true);
            

            
            Document myMetadata = builder.build(new InputSource(new StringReader(strXml)));
            Element myRoot = myMetadata.getRootElement();
            lom = myMetadata.getRootElement();
 
    	    Element general;
            general = (Element)myRoot.getChild("general", myRoot.getNamespace());
            
	    if (general != null){
                general.setNamespace(this.lom.getNamespace());
                //myRoot.removeContent(general);  
                //this.setGeneral(general);
                this.general = general;
            }        
            
            
            Element lifeCycle;
            lifeCycle = (Element)myRoot.getChild("lifecycle", myRoot.getNamespace());
            
	    if (lifeCycle != null){
                lifeCycle.setNamespace(this.lom.getNamespace());
               // myRoot.removeContent(lifeCycle);
                //this.setLifeCycle(lifeCycle);
                this.lifeCycle = lifeCycle;
            }            
            
            Element metaMetadata;
            metaMetadata = (Element)myRoot.getChild("metametadata", myRoot.getNamespace());
	    if (metaMetadata != null){
                metaMetadata.setNamespace(this.lom.getNamespace());
                //myRoot.removeContent(metaMetadata);
                //this.setMetaMetadata(metaMetadata);
                this.metaMetadata = metaMetadata;
            }            
            
            Element technical;
            technical = (Element)myRoot.getChild("technical", myRoot.getNamespace());
	    if (technical != null){
                technical.setNamespace(this.lom.getNamespace());
                //myRoot.removeContent(technical);
                //this.setTechnical(technical);
                this.technical = technical;
            }            
            
            Element educational;
            educational = (Element)myRoot.getChild("educational", myRoot.getNamespace());
	    if (educational != null){
                educational.setNamespace(this.lom.getNamespace());
                //myRoot.removeContent(educational);
                //this.setEducational(educational);
                this.educational = educational;
            }            
            
            Element rights;
            rights = (Element)myRoot.getChild("rights", myRoot.getNamespace());
	    if (rights != null){
                rights.setNamespace(this.lom.getNamespace());
                //myRoot.removeContent(rights);
                //this.setRights(rights);
                this.rights = rights;
            }            
            
            Element relation;
            relation = (Element)myRoot.getChild("relation", myRoot.getNamespace());
	    if (relation != null){
                relation.setNamespace(this.lom.getNamespace());
                //myRoot.removeContent(relation);
                //this.setRelation(relation);
                this.relation = relation;
            }            
            
            Element annotation;
            annotation = (Element)myRoot.getChild("annotation", myRoot.getNamespace());
	    if (annotation != null){
                annotation.setNamespace(this.lom.getNamespace());
                //myRoot.removeContent(annotation);
                //this.setAnnotation(annotation);
                this.annotation = annotation;
            }            
            
            Element classification;
            classification = (Element)myRoot.getChild("classification", myRoot.getNamespace());
	    if (classification != null){
                classification.setNamespace(this.lom.getNamespace());
                //myRoot.removeContent(classification);
                //this.setClassification(classification);
                this.classification = classification;
            }
            
        } catch (JDOMException ex) {
            Logger.getLogger(LOP2PMetadata.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LOP2PMetadata.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Return the General element.
     * 
     * @return General element of this metadata
     */       
    public Element getGeneral() {
        return general;
    }

    /**
     * Set the General element.
     * 
     * @param general General element to be used in this metadata
     */        
    public void setGeneral(Element general) {
        getLom().removeContent(this.general);
        this.general = general;
        getLom().addContent(this.general);
        this.general.setName("general");
    }

    /**
     * Return the Life Cycle element.
     * 
     * @return Life Cycle  element of this metadata
     */           
    public Element getLifeCycle() {
        return lifeCycle;
    }

    /**
     * Set the Life Cycle element.
     * 
     * @param lifeCycle Life Cycle element to be used in this metadata
     */            
    public void setLifeCycle(Element lifeCycle) {
        getLom().removeContent(this.lifeCycle);
        this.lifeCycle = lifeCycle;
        getLom().addContent(this.lifeCycle);
        this.lifeCycle.setName("lifecycle");
    }
    
    /**
     * Return the Meta Metadata element.
     * 
     * @return Meta Metadata element of this metadata
     */    
    public Element getMetaMetadata() {
        return metaMetadata;
    }
    
    /**
     * Set the Meta Metadata element.
     * 
     * @param metaMetadata Meta Metadata element to be used in this metadata
     */           
    public void setMetaMetadata(Element metaMetadata) {
        getLom().removeContent(this.metaMetadata);
        this.metaMetadata = metaMetadata;
        getLom().addContent(this.metaMetadata);
        this.metaMetadata.setName("metadata");
    }
    
    /**
     * Return the Technical element.
     * 
     * @return Technical element of this metadata
     */    
    public Element getTechnical() {
        return technical;
    }

    /**
     * Set the Technical element.
     * 
     * @param technical Technical element to be used in this metadata
     */            
    public void setTechnical(Element technical) {
        getLom().removeContent(this.technical);
        this.technical = technical;
        getLom().addContent(this.technical);
        this.technical.setName("technical");
    }

    /**
     * Return the Educational element.
     * 
     * @return Educational element of this metadata
     */        
    public Element getEducational() {
        return educational;
    }

    /**
     * Set the Educational element.
     * 
     * @param educational Educational element to be used in this metadata
     */            
    public void setEducational(Element educational) {
        getLom().removeContent(this.educational);
        this.educational = educational;
        getLom().addContent(this.educational);
        this.educational.setName("educational");
    }

    /**
     * Return the Rights element.
     * 
     * @return Rights element of this metadata
     */        
    public Element getRights() {
        return rights;
    }

    /**
     * Set the Rights element.
     * 
     * @param rights Rights element to be used in this metadata
     */            
    public void setRights(Element rights) {
        getLom().removeContent(this.rights);
        this.rights = rights;
        getLom().addContent(this.rights);
        this.rights.setName("rights");
    }

    /**
     * Return the Relations element.
     * 
     * @return Relations element of this metadata
     */        
    public Element getRelation() {
        return relation;
    }

    /**
     * Set the Relation element.
     * 
     * @param relation Relation element to be used in this metadata
     */            
    public void setRelation(Element relation) {
        getLom().removeContent(this.relation);
        this.relation = relation;
        getLom().addContent(this.relation);
        this.relation.setName("relation");
    }

    /**
     * Return the Annotation element.
     * 
     * @return Annotation element of this metadata
     */        
    public Element getAnnotation() {
        return annotation;
    }

    /**
     * Set the Annotation element.
     * 
     * @param annotation Annotation element to be used in this metadata
     */            
    public void setAnnotation(Element annotation) {
        getLom().removeContent(this.annotation);
        this.annotation = annotation;
        getLom().addContent(this.annotation);
         this.annotation.setName("annotation");
    }

    /**
     * Return the Classification element.
     * 
     * @return Classification element of this metadata
     */        
    public Element getClassification() {
        return classification;
    }

    /**
     * Set the Classification element.
     * 
     * @param classification Classification element to be used in this metadata
     */            
    public void setClassification(Element classification) {
        getLom().removeContent(this.classification);
        this.classification = classification;
        getLom().addContent(this.classification);
        this.classification.setName("classification");
    }
    
    /**
     * Compares a metadata with that object and return a similarity level.
     * 
     * @param mtdtToCompare metadata that will be compared with object
     * @return similarity level of this metadata compared to mtdtToCompare
     */        
    public Double compare(LOP2PMetadata mtdtToCompare){
        ArrayList myList = new ArrayList();
        this.listContents(this.getLom(), myList);
        
        ArrayList otherList = new ArrayList();
        listContents(mtdtToCompare.getLom(),otherList);
        
        for (int i=0; i<myList.size();i++){
            String myItem = (String)myList.get(i);
            for (int j=0; j<otherList.size();j++){
                String otherItem = (String)otherList.get(j);    
                if (myItem.endsWith(otherItem)){
                    return 100.0;
                }
            }           
        }
        
        return 0.0;
    }

    
    /**
     * Put all contents of the elements of this metadata in a ArrayList of 
     * contents (second parameter).
     * 
     * @param e element that the search will start
     * @param contents ArrayList that store all contents of the elements 
     */            
    private void listContents(Element e, ArrayList contents){
        if (e.getTextNormalize().equals("") == false){
            contents.add(e.getTextNormalize());
        }
        
        List children = e.getChildren();
        for (int i=0; i<children.size();i++){ 
            if ( children.get(i).getClass() == Element.class ){
                listContents((Element)children.get(i), contents);
            }
        } 
    }

    /**
     * Return the LOM element.
     * 
     * @return LOM element of this metadata
     */        
    public Element getLom() {
        return lom;
    }

    /**
     * Set the LOM element.
     * 
     * @param lom LOM element to be used in this metadata
     */            
    public void setLom(Element lom) {
        this.lom = lom;
    }
    
    /**
     * Return the xml string of this metadata.
     * 
     * @return xml string of this metadata
     */       
    public String getXMLString() {    
        XMLOutputter outp = new XMLOutputter();
        return outp.outputString(this.lom);      
    }
    
    
    /**
     * Set new location that correpond to the location of the Learning Object 
     * that this object represent.
     * 
     * @param location string of the location of the Learning Object that this object represent
     */           
    public void setLocation(String location){
        Element atrlocation = this.getTechnical().getChild("location");
        if (atrlocation == null){
            atrlocation = new Element("location");
        }else{
            this.getTechnical().removeChild("location");
        }
        atrlocation.setText(location);
        this.getTechnical().addContent(atrlocation);
                
    }
    
   
    /**
     * Set a unique ID in LOP2P network for this metadata
     * 
     * @param peername name of this peer
     * @param time atual time
     * @param id index of this metadata in the creation
     */         
   public void setNewLOP2PID(String peername, long time, int id){
        Element elID = this.getGeneral().getChild("identifier");
        if (elID != null){
            this.getTechnical().removeChild("identifier");
        }
        elID = new Element("identifier");
        
        
        Element catalog = new Element("catalog");
        catalog.setText("LOP2PID");
        Element entry = new Element("entry");
        entry.setText("LOP2PID"+peername+time+id);
        
        elID.addContent(catalog);
        elID.addContent(entry);
        
        this.getGeneral().addContent(elID);
    }

    /**
     * Return ID of this metadata
     * 
     * @return metadata ID
     */     
   public String getIdentifier(){
        Element elID = this.getGeneral().getChild("identifier");
        if (elID != null){
            return this.getGeneral().getChild("identifier").getChildTextTrim("entry");
        }
        return null;
    }
   

    /**
     * Return location of the Learning Object that this metadata represent
     * 
     * @return location of the Learning Object that this metadata represent
     */        
    public String getLocation(){
        return this.getTechnical().getChildTextTrim("location",  this.lom.getNamespace());
    }

    /**
     * @return the blocks
     */
    public boolean[] getBlocks() {
        return blocks;
    }

    /**
     * @param blocks the blocks to set
     */
    public void setBlocks(boolean[] blocks) {
        this.blocks = blocks;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getSizeOA() {
        return sizeOA;
    }

    public void setSizeOA(long sizeOA) {
        this.sizeOA = sizeOA;
    }

    public void setIdentifier(String loid) {

        Element ge = this.general;
        if (ge == null){
            this.general = new Element("general", this.lom.getNamespace());
            
        }


        Element elID = this.getGeneral().getChild("identifier");
        if (elID != null){
            this.getTechnical().removeChild("identifier");
        }
        elID = new Element("identifier");


        Element catalog = new Element("catalog");
        catalog.setText("LOP2PID");
        Element entry = new Element("entry");
        entry.setText(loid);

        elID.addContent(catalog);
        elID.addContent(entry);

        this.getGeneral().addContent(elID);
    }
   
}