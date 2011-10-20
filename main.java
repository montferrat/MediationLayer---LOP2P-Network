
import java.io.IOException;
import protoGUIDevice.GUIStatistics;
import protoLowareInterfaceDevice.LIDConfiguration;
import protoLowareInterfaceDevice.LIDReceiveConnection;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import protoStatisticsDevice.StDData;
import protonetcommunicationdevice.NCDevice;

/**
 *
 * @author Rafael de Santiago
 */
public class main {
  public static void main(String[] args) throws IOException, ClassNotFoundException {
        
         NCDevice myNCD = new NCDevice();
         
         LIDConfiguration nicfg = new LIDConfiguration(myNCD.getNCDCfg());
         myNCD.setLIDCfg(nicfg);
         LIDReceiveConnection lirc = new LIDReceiveConnection(nicfg);
         
         myNCD.start();
         lirc.start();
         
         
         GUIStatistics gsWin;
         if (args.length > 0){
             if(args[0].equals("--gui")){
                 gsWin = new GUIStatistics(StDData.instanceOfStDData());
                 gsWin.setVisible(true);
             }
         }
  }
}
