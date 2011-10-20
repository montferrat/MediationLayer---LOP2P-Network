/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package protoLowareInterfaceDevice;

import java.io.IOException;
import java.net.*;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rafael de Santiago
 */
public class LIDReceiveConnection extends Thread{
    private LIDConfiguration lidcfg;
    private ServerSocket serverSocket;
    
    
    /**
     * Constructor of the class LIDReceiveConnection. This class have the 
     * purpose of receive the connections of the loware and to request for a 
     * handler treatment. 
     * 
     * @param   lidcfg      configuration object of the Loware Interface Device
     */           
    public LIDReceiveConnection (LIDConfiguration lidcfg){
        super("LIDReceiveConnection");
        
        //configuration
        this.lidcfg = lidcfg;

        //setting priority
        this.setPriority(MIN_PRIORITY);

       
        
        try {    
            this.serverSocket = new ServerSocket(this.getLidcfg().getPortServerSocket());
            
        } catch (IOException ex) {
            Logger.getLogger(LIDReceiveConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
             
    }
    
    /**
     * Execution method of the thread LIDReceiveConnection
     * 
     */       
    public void run(){
        //eternal loop
        while (true){
            try {
                System.out.println("Waiting for LOWARE connections...");
                //receives a connection and prepares a connection handler
                Socket clientSocket = this.serverSocket.accept();
                System.out.println("LOWARE connection received!");
                //prepares a request
                LIDRequestHandler captured = new LIDRequestHandler(getLidcfg(),clientSocket);
                captured.start();
                
            } catch (IOException ex) {
                Logger.getLogger(LIDReceiveConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Return the configuration object of the Loware Interface Device
     * 
     * @return configuration object of the Loware Interface Device
     */        
    public LIDConfiguration getLidcfg() {
        return lidcfg;
    }

    /**
     * Set the configuration object of the Loware Interface Device
     * 
     * @param lidcfg configuration object of the Loware Interface Device
     */            
    public void setLidcfg(LIDConfiguration lidcfg) {
        this.lidcfg = lidcfg;
    }
    
}
