/*
 * Copyright (c) 2002 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *       Sun Microsystems, Inc. for Project JXTA."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact Project JXTA at http://www.jxta.org.
 *
 * 5. Products derived from this software may not be called "JXTA",
 *    nor may "JXTA" appear in their name, without prior written
 *    permission of Sun.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL SUN MICROSYSTEMS OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 *====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Project JXTA.  For more
 * information on Project JXTA, please see <http://www.jxta.org/>.
 *
 * This license is based on the BSD license adopted by the Apache Foundation.
 *
 * $Id: JxtaCast.java,v 1.8 2005/04/26 19:06:51 hamada Exp $
 *
 */

/*****************************************************************************
*
* JxtaCast release history
*
* Version numbers below correspond to the JxtaCast.version string, not to the
* CVS check-in ID.
*
* 1.00  03/18/02  Beta release.  The class was named "FileCast", and was a
*                 part of the PicShare demo.  There was an unversioned alpha
*                 on 03/09/02.
* 1.01  03/20/02  Don't show "duplicate block" message if we were the
*                 ones that sent the block.  Create pipes AFTER advs have
*                 been published.
* 1.02  03/23/02  Use "average time between blocks" calculation to determine
*                 wait time before requesting missing blocks.
*                 Shorten inactive lifetime of input wranglers to 5 minutes.
*                 Put sending peer name in more log messages.
* 1.03  04/07/02  Change name to JxtaCast, change package location.
* 1.04  10/11/02  Migrate from deprecated Message and PipeService methods.
* 2.00  04/07/03  Tested with JXTA 2.0 platform: JXTA_2_0_Stable_20030301.
*                 Bumped default block size to 12kb.  Tightened time between
*                 BossCheck checks, from 2 seconds to 1.
*
*****************************************************************************/

package utils;

import java.io.*;
import java.util.*;

import net.jxta.discovery.*;
import net.jxta.document.*;
import net.jxta.endpoint.*;
import net.jxta.id.*;
import net.jxta.peergroup.*;
import net.jxta.pipe.*;
import net.jxta.protocol.*;
import net.jxta.rendezvous.*;


/*
 * JxtaCast: Sends data files to all peers in a peer group (those that are
 *           listening for them with JxtaCast).  Receives data files sent
 *           by other JxtaCast users.
 *
 *           Large files are broken up and sent in blocks.  Since blocks may
 *           arrive out of order, the receivers re-assemble all the blocks in
 *           memory before writing the file. (JxtaCast is therefore a memory hog
 *           if used with very large files...  Should change it to read/write
 *           blocks directly from disk files.)
 *
 *           The default block size is set in the public outBlockSize variable.
 *           Client applications can change this size if they wish.  The default
 *           size is 12kb.  The maximum message size that can be sent using IP
 *           multicasting is 16kb, so we want to stay under that.  You can use
 *           bigger blocks if you are always using a rendezvous.
 *
 *           This class was originally named "FileCast".  After finding that the
 *           name had already been used (oops), we changed it to JxtaCast.
 *           The string "FileCast" is still used in some places, to maintain
 *           backwards compatibility with earlier versions.
 */
public class JxtaCast implements PipeMsgListener, Runnable {

    // JxtaCast version number.  The version number is placed in the messages that
    // JxtaCast sends.  Hopefully this will help us orchestrate communication
    // between newer and older versions of JxtaCast.
    //
    public static String version = "2.00";


    // JxtaCast supports two types of messages: FILE and CHAT.  File messages are
    // used to broadcast files to peers through the wire protocol.  Chat messages
    // can be sent through the same pipes.  They allow the peers to carry on a side
    // chat while sending and receiving files.
    //
    // Most of the following message elements are used for file messages.  Chat
    // messages require only the MESSAGETYPE, SENDERNAME, and CAPTION elements.
    // The chat text is contained in the CAPTION element.
    //
    // All of the element's data values are stored in the message as strings, except
    // for the DATABLOCK element, containing binary image file data.  Numeric values
    // such as FILESIZE are converted to strings for storage.

    // Message element names.
    final static String MESSAGETYPE = "MessageType";         // See list of types below.
    final static String SENDERNAME  = "JxtaTalkSenderName";  // The sending peer name.
    final static String SENDERID    = "SenderID";            // Peer ID of the sender.
    final static String VERSION     = "FileCastVersion";     // JxtaCast version number.
    final static String CAPTION     = "Caption";             // Description of the file.

    final static String FILEKEY     = "FileKey";      // Unique key for this file transaction.
    final static String FILENAME    = "FileName";     // File name (no path).
    final static String FILESIZE    = "FileSize";     // File size.
    final static String BLOCKNUM    = "BlockNum";     // Large files are sent in blocks.
    final static String TOTALBLOCKS = "TotalBlocks";  // Total number of blocks in the file.
    final static String BLOCKSIZE   = "BlockSize";    // The size of one block.
    final static String DATABLOCK   = "DataBlock";    // One block of file data.

    // REQTOPEER is a message element name, the value will usually be a peer ID.
    // REQ_ANYPEER is a value for the REQTOPEER element, requesting from any peer.
    //
    final static String REQTOPEER   = "ReqToPeer";    // Peer ID to which we're addressing a FILE_REQ message.
    final static String REQ_ANYPEER = "ReqAnyPeer";   // Addressing the FILE_REQ message to any peer.


    // MESSAGETYPE element data values.
    final static String MSG_FILE          = "FILE";          // File transfer message.
    final static String MSG_FILE_ACK      = "FILE_ACK";      // Block received acknowledgement.
    final static String MSG_FILE_REQ      = "FILE_REQ";      // Request a block from another peer.
    final static String MSG_FILE_REQ_RESP = "FILE_REQ_RESP"; // Respond to a block request.
    final static String MSG_CHAT          = "CHAT";          // Chat message.

    public final static String DELIM      = "]--,',--[";     // Delimiter for some pipe name sections.

    public static boolean logEnabled;        // Log debug messages if true.
    public int outBlockSize        =  12288; // Size of the data block to send with each message, in bytes.
    public int outWranglerLifetime = 600000; // 10 mins: time to store inactive output wranglers, in millis.
    public int inWranglerLifetime  = 300000; //  5 mins: time to store inactive input wranglers, in millis.
    public int timeTilReq          =  60000; // 60 secs: max time that we'll wait before requesting missing file blocks.
    public int trailBossPeriod     =   1000; //  1  sec: worker thread sleep time between wrangler checks.
    public String fileSaveLoc;               // Destination directory for saved files.


    // We may be receiving several files at once, from multiple peers.  Since
    // the files are sent in chunks, we need objects that can hold on to what
    // we've got so far, while we process a piece of a different file.  We'll
    // use a hash table of FileWrangler objects.  Each FileWrangler will handle
    // the incoming messages for one file.  The wrangler's composeKey() func will
    // supply us with a unique hash key for each file transfer.
    
    protected Hashtable wranglers = new Hashtable(40);

    // When sending a file, requests are temporarily queued here.  Another thread
    // reads the queue, loads the file, and starts the transmission out through
    // the pipes.  This helps keep the GUI thread cleared for action.  The vector
    // will contain OutputFileWrangler objects.
    
    protected Vector sendFileQueue = new Vector(10);

    protected DiscoveryService  disco;
    protected PeerAdvertisement myPeer;
    protected PeerGroup   group;
    protected String      castName;
    protected PipeService pipeServ;
    protected InputPipe   inputPipe;     // Public propagation pipe, the "broadcast channel".
    protected OutputPipe  outputPipe;    // Public propagation pipe, paired with the above.
    protected InputPipe   privInputPipe; // Private unicast pipe, the "back channel".
    protected Vector      jcListeners;   // Registered JxtaCastEventListener objects.


    /** Constructor
     *
     *  @param group - peergroup that we've joined.
     *  @param castName - name to use in the pipe advertisement ID , such as an
     *                    application name.  This permits the creation of
     *                    multiple JxtaCast channels within a single group.
     */
    public JxtaCast(PeerAdvertisement myPeer, PeerGroup group, String castName) {

        this.myPeer = myPeer;
        this.castName = new String(castName);
        setPeerGroup(group);

        // Default destination for saved files is the current directory.
        fileSaveLoc = "." + File.separator;

        // Create collection to hold JxtaCastEventListener objects.
        jcListeners = new Vector(10);

        // Create a worker thread to handle file loading and message output.
        // Also checks thru the list of FileWranglers to give any stalled
        // file transactions kick in the pants.
        //
        Thread trailBossThread = new Thread(this, "JxtaCast:TrailBoss");
        trailBossThread.start();
    }


    /** Return the currently joined peer group. */
    public PeerGroup getPeerGroup() {
        return group;
    }


    /** Change to a new peer group.
     *  @return true if we successfully created the pipes in the new group.
     */
    public boolean setPeerGroup(PeerGroup group) {

        boolean rc;

        // If the new group is the same group we already have, it's a no-op.
        if (this.group != null  &&
            group.getPeerGroupID().equals(this.group.getPeerGroupID()))
            return true;

        // By synchronizing on the wranglers object, we ensure that the
        // trailboss thread is not trying to use the current pipes while
        // we create new ones.
        //
        synchronized (wranglers) {
            this.group = group;
            disco = group.getDiscoveryService();
            pipeServ = group.getPipeService();
            rc = createPipes(castName);
        }

        return rc;
    }


    /** Log a debug message to the console.  Should maybe use Log4J?
     *  Have to figure out whether we can use Log4J to show our application
     *  debug messages, but suppress all the JXTA platform messages.
     */
    public static void logMsg(String msg) {
        if (logEnabled)
            System.out.println(msg);
    }


    /**
     *  Create an input and output pipe to handle the file transfers.
     *  Publish their advertisements so that other peers will find them.
     *
     *  @param castName - name to use in the pipe advertisement ID.
     *  @return true if successful.
     */
    protected boolean createPipes(String castName) {

        // Close any existing pipes.
        if (inputPipe != null)
            inputPipe.close();
        if (outputPipe != null)
            outputPipe.close();
        if (privInputPipe != null)
            privInputPipe.close();

        // Create the input and output pipes for the many-to-many "broadcast channel",
        // using propagation pipes.  The broadcast channel is used to send the
        // file data out to all listening peers.  First we cook up an
        // advertisement, and then create the pipes using the adv.
        //
        PipeAdvertisement pipeAdvt;
        pipeAdvt = (PipeAdvertisement)AdvertisementFactory.newAdvertisement(
            PipeAdvertisement.getAdvertisementType());

        // This is a pre-defined ID for the propagate pipes used for file transfers.
        // Using this known ID allows us to start using the pipes immediately,
        // without having to discover other peers pipe advertisements first.
        // There is, however, a potential for collision with another app using the
        // same ID.  We use a prefix given for this JxtaCast object (castName), and
        // then append a string and byte array that should be unique to JxtaCast.
        //
        byte jxtaCastID[] = {
            (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA,
            (byte) 0xBB, (byte) 0xBB, (byte) 0xBB, (byte) 0xBB,
            (byte) 0xBB, (byte) 0xBB, (byte) 0xBB, (byte) 0xBB,
            (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA };

        String idStr = castName + "-[FileCast Pipe ID]-" + new String(jxtaCastID);
        PipeID id = (PipeID)IDFactory.newPipeID(group.getPeerGroupID(), idStr.getBytes());
        pipeAdvt.setPipeID(id);
        pipeAdvt.setName("JxtaTalkSenderName." + castName);
        pipeAdvt.setType(PipeService.PropagateType);

        try {
            disco.publish(pipeAdvt);
            inputPipe = pipeServ.createInputPipe(pipeAdvt, this);
            outputPipe = pipeServ.createOutputPipe(pipeAdvt, 2000);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // Create the input pipe for the "back channel", using a unicast pipe.
        // Peers use this pipe for one-to-one communication, such as requesting
        // a file block from a specific peer.
        //
        // FIXME - The back channel concept isn't fully implemented yet.  We're
        // creating the pipe and adv here, but not using the pipe anywhere.
        // We want to leave this code active now, even though the pipes aren't
        // used, because the advs are useful.  JxtaCast apps can do a filtered
        // adv discovery to detect other peers running the same JxtaCast app.
        //
        // TODO - Include this adv in outgoing messages, so that receivers
        // can respond thru the back channel pipe.  
        //
        pipeAdvt = (PipeAdvertisement)AdvertisementFactory.newAdvertisement(
            PipeAdvertisement.getAdvertisementType());

        id = (PipeID)IDFactory.newPipeID(group.getPeerGroupID());
        pipeAdvt.setPipeID(id);
        pipeAdvt.setName(getBackChannelPipeName());
        pipeAdvt.setType(PipeService.UnicastType);

        try {
            disco.publish(pipeAdvt);
            privInputPipe = pipeServ.createInputPipe(pipeAdvt, this);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }


    /** Return the name used in advertisement for our "back channel" input pipe.
     *  The string contains a known prefix that can be used for discovery,
     *  plus our peer name and ID.
     */
    public String getBackChannelPipeName() {

        // Use a complex delimiter to mark off the peer name and ID.
        // We need to parse this string later, so we need something that's
        // unlikely to appear in a peer name.  (A simple period is too risky.)
        //
        String name = getBackChannelPipePrefix() + DELIM +
                      myPeer.getName()           + DELIM +
                      myPeer.getPeerID().toString();

        return name;
    }


    /** Return the prefix used in the name of our "back channel" input pipe.
     *  This prefix can be used with advertisement discovery to narrow the
     *  discovery results to peers using JxtaCast with your application.
     */
    public String getBackChannelPipePrefix() {

        return "FileCastBackChannel." + castName;
    }


    /** Extract the peer name from the given pipe advertisement name.
     */
    public static String getPeerNameFromBackChannelPipeName(String pipeName) {

        // The peer name is located between the first and second delimiters.
        int start = pipeName.indexOf(DELIM);
        if (start < 0)
            return null;

        int end = pipeName.indexOf(DELIM, start + 1);
        if (end < 0)
            return null;

        // Extract the peer name.
        start += DELIM.length();
        if (start > end)
            return null;
        return pipeName.substring(start, end);
    }


    /** Extract the peer ID from the given pipe advertisement name.
     */
    public static String getPeerIdFromBackChannelPipeName(String pipeName) {

        // The peer ID is located after the second delimiter.
        int pos = pipeName.indexOf(DELIM);
        if (pos < 0)
            return null;
        pos = pipeName.indexOf(DELIM, ++pos);
        if (pos < 0)
            return null;

        return pipeName.substring(pos + DELIM.length());
    }


    /**
     *  Send a Message down the output pipe.
     */
    public synchronized void sendMessage(Message msg) {

        try {
            outputPipe.send(msg);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Receive messages from the input pipe.
     *
     * @param event PipeMsgEvent the event that contains our message.
     */
    public synchronized void pipeMsgEvent(PipeMsgEvent event) {

        Message msg = event.getMessage();
        try {
            // logMsg("Message received: " + getMsgString(msg, MESSAGETYPE));

            // Determine the message type, and dispatch it.
            String msgType = getMsgString(msg, MESSAGETYPE);
            if (msgType == null) {
                logMsg("Error: message received with no MESSAGETYPE.");
                return;
            }
            if (msgType.equals(MSG_CHAT))
                receiveChatMsg(msg);
            else
                receiveFileMsg(msg);

        } catch (Exception e) {
            e.printStackTrace();   
        }
    }


    /**
     * Receive a file transfer message.
     *
     * @param msg a file transfer message.
     */
    public synchronized void receiveFileMsg(Message msg) {

        try {
            String msgType = getMsgString(msg, MESSAGETYPE);
            if (msgType == null)
                return;

            // Check for a wrangler in the hash table, to see if we've
            // already started processing this file.  If not, create a
            // new FileWrangler to handle it.  (But only if it's an a
            // MSG_FILE message from the original sender.)
            //
            FileWrangler wrangler = (FileWrangler)wranglers.get(getMsgString(msg, FILEKEY));
            if (wrangler == null  &&  msgType.equals(MSG_FILE)) {
                wrangler = new InputFileWrangler(this, msg);
                wranglers.put(wrangler.key, wrangler);
            }
            if (wrangler == null)
                logMsg("Unable to obtain wrangler for message.  Msg type: " +
                    msgType + "  key: " + getMsgString(msg, FILEKEY));
            else
                wrangler.processMsg(msg);
            

        } catch (Exception e) {
            e.printStackTrace();   
        }
    }


    /**
     * Receive a chat message.
     *
     * @param msg a chat message.
     */
    public synchronized void receiveChatMsg(Message msg) {

        try {
            String sender   = getMsgString(msg, SENDERNAME);
            String caption  = getMsgString(msg, CAPTION);

            logMsg(sender + " : " + caption);

            // FIXME - Send the chat message to any registered listeners.
        } catch (Exception e) {
            e.printStackTrace();   
        }
    }


    /**
     *  Send a chat message out to the group members.
     *
     *  @param  text   the message text.
     */
    public synchronized void sendChatMsg(String text) {
        try {
            // Create a message, fill it with our standard headers.
            Message msg = new Message();
            setMsgString(msg, MESSAGETYPE, MSG_CHAT);
            setMsgString(msg, SENDERNAME,  myPeer.getName());
            setMsgString(msg, SENDERID,    myPeer.getPeerID().toString());
            setMsgString(msg, VERSION,     version);
            setMsgString(msg, CAPTION,     text);

            sendMessage(msg);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     *  Send a file out to the group members.
     *
     *  @param  file       the file to send.
     *  @param  caption    description of the file (optional)
     */
    public synchronized void sendFile(File file, String caption) {

        // Create a wrangler to handle the file transfer, and then queue it
        // in our sendFileQueue Vector.  Another thread will retrieve it
        // from the queue and start the send process.  (We don't want to
        // hang up the GUI while the file is loading.)
        //
        OutputFileWrangler wrangler = new OutputFileWrangler(this, file, caption);
        sendFileQueue.add(wrangler);
    }


    /** Worker thread.  Call functions to perform time-intensive operations that
     *  would bog down the main thread.
     */
    public void run() {

        while (true) {
            try {Thread.sleep(trailBossPeriod);} catch (InterruptedException e) {}

            synchronized (wranglers) {

                checkFileWranglers();
                checkSendFileQueue();
            }
        }
    }


    /** Loop thru our current collection of FileWrangler objects, and call
     *  bossCheck() for each one.  This gives them a chance to perform any
     *  needed tasks.
     *
     *  We keep wranglers stored in our Hashtable for awhile after we've finished
     *  sending or receiving the file. They're available to respond to requests
     *  from other peers for file blocks that they are missing.
     *
     *  In response to the bossCheck() call, wranglers that have been inactive
     *  for a long time will remove themselves from the collection.  Input
     *  wranglers that are missing file blocks will request them.
     */
    protected void checkFileWranglers() {

        Enumeration elements = wranglers.elements();;
        FileWrangler wrangler;

        while (elements.hasMoreElements()) {
            wrangler = (FileWrangler)elements.nextElement();
            wrangler.bossCheck();
        }
    }


    /**
     * Outgoing file send operations are queued by the main thread.  This function
     * is called by the worker thread.  It reads them from the queue, and triggers
     * the file load and send process.
     */
    protected void checkSendFileQueue() {

        OutputFileWrangler wrangler = null;
      
        // Yank the first wrangler from the queue, if there is one.
        // Put it in our collection of active wranglers, and start the
        // file load and send process.
        //
        if (sendFileQueue.isEmpty())
            return;
        wrangler = (OutputFileWrangler)sendFileQueue.remove(0);
        if (wrangler != null) {
            wranglers.put(wrangler.key, wrangler);
            wrangler.sendFile();
            wrangler = null;
        }
    }


    /**
     * Register a JxtaCastEventListener.  Listeners are sent progress events while
     * sending and receiving files.
     */
    public synchronized void addJxtaCastEventListener(JxtaCastEventListener listener) {

        // Add the listener to our collection, unless we already have it.
        if (!jcListeners.contains(listener))
            jcListeners.addElement(listener);
    }


    /**
     * Un-register a JxtaCastEventListener.
     */
    public synchronized void removeJxtaCastEventListener(JxtaCastEventListener listener) {

        jcListeners.removeElement(listener);
    }


    /**
     * Send a JxtaCastEvent to all registered listeners.
     */
    protected void sendJxtaCastEvent(JxtaCastEvent e) {

        JxtaCastEventListener listener = null;

        Enumeration elements = jcListeners.elements();
        while (elements.hasMoreElements()) {
            listener = (JxtaCastEventListener)elements.nextElement();
            listener.jxtaCastProgress(e);
        }
    }


    public static void setMsgString(Message msg, String name, String str) {

        msg.replaceMessageElement(new StringMessageElement(name, str, null));
    }


    public static String getMsgString(Message msg, String name) {

        MessageElement elem = msg.getMessageElement(name);
        if (elem == null)
            return null;

        return elem.toString();
    }
}



/**
 * Base class for processing a file.
 *
 * Files are split up and sent in blocks of data.  This class gathers
 * the data blocks for a file as they come in, and writes the file
 * to disk when it is complete.
 *
 * It's ok for blocks to arrive out of order, and ok for duplicate blocks
 * to arrive.
 */
abstract class FileWrangler {

    public String key;    // Unique identifier for this file transfer.

    JxtaCast jc;          // The parent JxtaCast obj.

    String sender;        // Sender's peer name.
    String senderId;      // Sender's peer ID.
    String filename;
    String caption = "";

    byte fdata[];         // The file data.
    int  myBlockSize;     // This file's block size.
    int  totalBlocks;     // Number of blocks in the file.

    long lastActivity;    // Timestamp when the most recent message was processed.


    /** Process a file transfer message.
     */
    public abstract void processMsg(Message msg);


    /** Receive a regular 'maintainence' check-in from the TrailBoss thread.
     */
    public abstract void bossCheck();


    /** Create a unique key for this file transfer. */
    public static String composeKey(String senderId, String filename) {

        // The key is a combination of the sender's PeerId, the file name,
        // and a timestamp.
        String keyStr = senderId + "+" + filename + "+" + 
                        String.valueOf(System.currentTimeMillis());

        return keyStr;
    }


    /** Send the specified block of file data out over the wire.
     *
     *  @param  blockNum  The block to send.
     *  @param  msgType   Message type: MSG_FILE or MSG_FILE_REQ_RESP.
     */
    synchronized void sendBlock(int blockNum, String msgType) {

        // Make sure it's a valid block.
        if (blockNum < 0  ||  blockNum >= totalBlocks)
            return;

        try {
            lastActivity = System.currentTimeMillis();

            // Create a message, fill it with our standard headers.
            Message msg = new Message();
            jc.setMsgString(msg, jc.MESSAGETYPE, msgType);
            jc.setMsgString(msg, jc.SENDERNAME,  jc.myPeer.getName());
            jc.setMsgString(msg, jc.SENDERID,    jc.myPeer.getPeerID().toString());
            jc.setMsgString(msg, jc.VERSION,     jc.version);
            jc.setMsgString(msg, jc.FILEKEY,     key);
            jc.setMsgString(msg, jc.FILENAME,    filename);
            jc.setMsgString(msg, jc.FILESIZE,    String.valueOf(fdata.length));

            // If we've got a caption, store it in the first message.
            if (blockNum == 0  &&  caption != null)
                jc.setMsgString(msg, jc.CAPTION, caption);

            // Place the block info in the message.
            jc.setMsgString(msg, jc.BLOCKNUM,    String.valueOf(blockNum));
            jc.setMsgString(msg, jc.TOTALBLOCKS, String.valueOf(totalBlocks));
            jc.setMsgString(msg, jc.BLOCKSIZE,   String.valueOf(myBlockSize));

            // Place the block of file data in the message.
            // If this is the last block, it's probably smaller than a full block.
            //
            int bSize = myBlockSize;
            if (blockNum == totalBlocks - 1)
                bSize = fdata.length - (blockNum * myBlockSize);
            ByteArrayMessageElement elem = new ByteArrayMessageElement(
                jc.DATABLOCK, null, fdata, blockNum * myBlockSize, bSize, null);
            msg.replaceMessageElement(elem);

            // Send the message.
            jc.logMsg("Sending: " + filename + "  block: " + (blockNum+1) +
                               "  of: " + totalBlocks); 
            jc.sendMessage(msg);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



/**
 * Class for sending a file.
 *
 * Files are split up and sent in blocks of data.  This class loads the
 * file into memory, and sends out data blocks.  It re-send blocks in response
 * to requests from other peers. 
 *
 */
class OutputFileWrangler extends FileWrangler {

    File file;
    int  blocksSent;      // Number of outgoing blocks processed so far.


    /**
     * Constructor - Build a wrangler to process an outgoing file.
     *
     */
    public OutputFileWrangler(JxtaCast jc, File file, String caption) {

        this.jc = jc;
        this.file = file;
        lastActivity = System.currentTimeMillis();

        // Get some header data that we only need once.
        sender   = jc.myPeer.getName();
        senderId = jc.myPeer.getPeerID().toString();
        filename = file.getName();
        this.caption = caption;
        key = composeKey(senderId, filename);

        // Get info about the blocks.
        blocksSent = 0;
        myBlockSize = jc.outBlockSize;
        int lastBlockSize = (int)file.length() % myBlockSize;
        totalBlocks = (int)file.length() / myBlockSize;
        if (lastBlockSize != 0)
            totalBlocks++;
    }


    /** Process a file transfer message.
     */
    public void processMsg(Message msg) {

        lastActivity = System.currentTimeMillis();

        // Since this is an output wrangler, we ignore messages of MSG_FILE.
        // They came from us!  Respond to ACK and REQ messages from peers
        // that are receiving this file from us.
        //
        String msgType = jc.getMsgString(msg, jc.MESSAGETYPE);
        if (msgType.equals(jc.MSG_FILE_ACK))
            processMsgAck(msg);
        else if (msgType.equals(jc.MSG_FILE_REQ))
            processMsgReq(msg);
    }


    /** Receive a regular 'maintainence' check-in from the TrailBoss.
     */
    public void bossCheck() {

        // If there's been no activity since our last check-in, and we still have
        // blocks to send, send the next one now.  (But make sure we've sent at
        // least one block.  If we haven't, then the other thread hasn't gotten
        // thru the sendFile() function yet, and the wrangler is not initialized.)
        //
        if (blocksSent > 0            &&
            blocksSent < totalBlocks  &&
            System.currentTimeMillis() - lastActivity > jc.trailBossPeriod + 500) {
            jc.logMsg("bossCheck sending block.");
            sendBlock(blocksSent++, jc.MSG_FILE);
            updateProgress();
        }

        // If this wrangler has been inactive for a long time, remove it from
        // JxtaCast's collection.
        if (System.currentTimeMillis() - lastActivity > jc.outWranglerLifetime)
            jc.wranglers.remove(key);
    }


    /** Process a file transfer ACK message.
     *
     *  Peers will send us an ACK message when they've received a block.
     *  When we get one (from any peer), for the last block we've sent,
     *  then we can send the next block.
     */
    private void processMsgAck(Message msg) {

        int blockNum = Integer.parseInt(jc.getMsgString(msg, jc.BLOCKNUM));

        jc.logMsg("Received ACK: " + filename + "  block " + (blockNum+1) +
                  ", from " + jc.getMsgString(msg, jc.SENDERNAME));

        // If there are more blocks to send, send the next one now.
        int nextBlock = blockNum + 1;
        if (nextBlock == blocksSent  &&  nextBlock < totalBlocks) {
            blocksSent++;
            sendBlock(nextBlock, jc.MSG_FILE);
            updateProgress();
        }
    }


    /** Process a file transfer REQ message.
     *
     *  A peer has requested a block of this file.  Send it out as a
     *  REQ_RESP 'request response' message.
     */
    private void processMsgReq(Message msg) {

        int blockNum = Integer.parseInt(jc.getMsgString(msg, jc.BLOCKNUM));

        // If this is a request for a block we haven't sent yet, send the
        // next block as a normal MSG_FILE message, instead of as a REQ_RESP
        // (request response).  We want to keep to the "push" protocol of
        // MSG_FILE/MSG_FILE_ACK messages until we've sent all the blocks
        // one time.  The peers will use the "pull" REQ/REQ_RESP protocol
        // to fill in their missing blocks.
        //
        if (blockNum >= blocksSent) {
            jc.logMsg("Received " + jc.getMsgString(msg, jc.MESSAGETYPE) +
                      ": " + filename + "  block " + (blockNum+1) +
                      ", from " + jc.getMsgString(msg, jc.SENDERNAME));
            sendBlock(blocksSent++, jc.MSG_FILE);
            updateProgress();
            return;
        }

        // Send out the block, but only if the request was addressed to us or
        // to "any peer".
        String reqToPeer = jc.getMsgString(msg, jc.REQTOPEER);
        if (reqToPeer.equals(jc.myPeer.getPeerID().toString())  ||
            reqToPeer.equals(jc.REQ_ANYPEER)) {

            if (reqToPeer.equals(jc.REQ_ANYPEER))
                jc.logMsg("Received REQ_ANYPEER: " + filename + "  block " +
                          (blockNum+1) + ", from " + jc.getMsgString(msg, jc.SENDERNAME));
            else
                jc.logMsg("Received FILE_REQ: " + filename + "  block " +
                          (blockNum+1) + ", from " + jc.getMsgString(msg, jc.SENDERNAME));
            sendBlock(blockNum, jc.MSG_FILE_REQ_RESP);
        }
    }


    /** Start the file transfer process.
     *
     *  We read the file into memory here, instead of in the constructor,
     *  so that the operation will take place in the desired thread.  (See the
     *  JxtaCast.sendFile() method.)
     *
     *  We'll send out the file's first data block.  Additional blocks will be
     *  sent in response to acknowledgement messages from the peers, or in response
     *  to bossCheck() calls from the TrailBoss (whichever comes faster).
     *
     *  Why?  Because if we tried to send all the blocks at once, we'd overload
     *  the capabilities of the propagate pipes, and lots of messages would be
     *  dropped.  So we use the ACK in order to send blocks at a rate that can
     *  be managed.
     */
    public void sendFile() {

        blocksSent = 0;

        // Allocate space to store the file data.
        fdata = new byte[(int)file.length()];

        // Read the file into memory.
        try {
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(fdata, 0, fdata.length);
            fis.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Send out the first block;
        sendBlock(blocksSent++, jc.MSG_FILE);
        updateProgress();
    }


    /** Notify listeners of our transmission progress.
     */
    private void updateProgress() {

        // Notify listeners of file progress.
        JxtaCastEvent e = new JxtaCastEvent();
        e.transType   = e.SEND;
        e.filename    = new String(filename);
        e.filepath    = new String(jc.fileSaveLoc);
        e.sender      = new String(sender);
        e.senderId    = new String(senderId);

        if (caption != null)
            e.caption     = new String(caption);

        e.percentDone = ((float)blocksSent / totalBlocks) * 100;
        jc.sendJxtaCastEvent(e);
    }
}



/**
 * Class for receiving a file.
 *
 * Files are split up and sent in blocks of data.  This class gathers
 * the data blocks for a file as they come in, and writes the file
 * to disk when it is complete.
 *
 * It's ok for blocks to arrive out of order, and ok for duplicate blocks
 * to arrive.   The wrangler can send out requests for missing blocks, and
 * also provide blocks for other peers that are missing them.
 */
class InputFileWrangler extends FileWrangler {

    boolean blockIn[];    // Each slot is true when we've received the corresponding data block.
    String  lastAck[];    // Parallel to block array, ID of last peer to send an ACK for each block.
    boolean askedOrig[];  // Parallel to block array, true if we've asked the original sender for this block.
    int blocksReceived;   // Number of incoming blocks processed so far.

    String reqLevel;      // Method of requesting missing blocks: from original sender or from anyone.
    int  currReqBlock;    // Block to request if it's missing.
    long lastReqTime;     // Timestamp when we last requested a missing block.
    long firstBlockTime;  // Timestamp when we received the first block message.
    long latestBlockTime; // Timestamp when we received the most recent block.
    long minTimeToWait;   // Minimum time to wait with no activity before requesting a block.


    /**
     * Constructor - Build a wrangler to process an incoming file.
     *
     * The message used in the constructor doesn't have to be the first
     * message in the sequence.  Any will do.  The message is not
     * processed from the constructor, so be sure to call processMsg() as
     * well.
     */
    public InputFileWrangler(JxtaCast jc, Message msg) {

        this.jc = jc;
        lastActivity = System.currentTimeMillis();

        // Get some header data that we only need once.
        sender   = jc.getMsgString(msg, jc.SENDERNAME);
        senderId = jc.getMsgString(msg, jc.SENDERID);
        key      = jc.getMsgString(msg, jc.FILEKEY);
        filename = jc.getMsgString(msg, jc.FILENAME);

        // Get info about the blocks.
        blocksReceived = 0;
        totalBlocks = Integer.parseInt(jc.getMsgString(msg, jc.TOTALBLOCKS));
        myBlockSize = Integer.parseInt(jc.getMsgString(msg, jc.BLOCKSIZE));

        // Allocate space to store the file data.  We also create an array
        // to check off the blocks as we process them, and a couple parallel arrays
        // to track who to ask for missing blocks.
        //
        fdata = new byte[Integer.parseInt(jc.getMsgString(msg, jc.FILESIZE))];
        blockIn = new boolean[totalBlocks];
        lastAck = new String[totalBlocks];
        askedOrig = new boolean[totalBlocks];

        // Initialize tracking info for blocks to request.
        currReqBlock = 0;
        lastReqTime  = System.currentTimeMillis();
        minTimeToWait = 4000;
    }


    /** Process a file transfer message.
     */
    public void processMsg(Message msg) {

        String msgType = jc.getMsgString(msg, jc.MESSAGETYPE);
        if (msgType.equals(jc.MSG_FILE)  ||  msgType.equals(jc.MSG_FILE_REQ_RESP))
            processMsgFile(msg);
        else if (msgType.equals(jc.MSG_FILE_ACK))
            processMsgAck(msg);
        else if (msgType.equals(jc.MSG_FILE_REQ))
            processMsgReq(msg);
    }


    /** Receive a regular 'maintainence' check-in from the TrailBoss.
     */
    public void bossCheck() {

        // If this wrangler has been inactive for a long time, remove it from
        // JxtaCast's collection.
        if (System.currentTimeMillis() - lastActivity > jc.inWranglerLifetime)
            jc.wranglers.remove(key);


        // Calculate the time of inactivity that we'll endure before
        // requesting missing blocks.  First determine the average amount
        // of time between blocks for the blocks we've received so far.
        // We'll wait either thrice that amount, or the time contained in
        // JxtaCast's timeTilReq member, whichever is shorter.  Let's also
        // impose a minimum of a few seconds.
        //
        // This calculation should help us find an optimal time to wait,
        // based on current network conditions.  We want to give any missing
        // blocks an adequate amount of time to reach us before we give up and
        // start requesting them.  But this amount of time can be very different
        // depending on the network topology.  It is very short when the sending
        // and receiving peers are on the same subnet.  It can be long, over 30
        // seconds, if the peers are separated by an HTTP relay.
        //
        // The minimum time to wait starts out at a few seconds, and grows each
        // time we send a REQ, until a new file block comes in.  Then it is
        // reset.  This will keep a single peer from spewing out too many requests.
        //
        long timeToWait = jc.timeTilReq;
        long avgTimeTweenBlocks;
        if (blocksReceived > 1) {
            avgTimeTweenBlocks = (latestBlockTime - firstBlockTime) / blocksReceived;
            if ((avgTimeTweenBlocks * 3) < timeToWait)
                timeToWait = avgTimeTweenBlocks * 2;
            if (timeToWait < minTimeToWait)
                timeToWait = minTimeToWait;
        }

        // Are we missing any blocks?  We'll request missing blocks, but don't
        // want to do it too often, or we'll queue up a bunch requests and then
        // receive a bunch of duplicate blocks.
        //
        if (blocksReceived < totalBlocks  &&
            System.currentTimeMillis() - lastReqTime  > timeToWait  &&
            System.currentTimeMillis() - lastActivity > timeToWait) {
            requestNextMissingBlock();
        }
    }


    /** Process one incoming block of file data.
     */
    public void processMsgFile(Message msg) {

        lastActivity = System.currentTimeMillis();

        try {
            int blockNum = Integer.parseInt(jc.getMsgString(msg, jc.BLOCKNUM));
            String msgSender   = jc.getMsgString(msg, jc.SENDERNAME);
            String msgSenderId = jc.getMsgString(msg, jc.SENDERID);

            // Have we already processed this block?  If we've received a
            // duplicate message, ignore it.
            //
            if (blockIn[blockNum] == true) {
                // Log a msg, unless we were the sender.
                if (!msgSenderId.equals(jc.myPeer.getPeerID().toString()));
                    jc.logMsg("Duplicate block: " + filename + " block: " + (blockNum+1));
                return;
            }

            // Record some timestamps to be used later.  bossCheck() uses these
            // to calculate an average time between blocks.
            latestBlockTime = lastActivity;
            if (blocksReceived == 0)
                firstBlockTime = lastActivity;
            minTimeToWait = 4000;

            // The caption is stored with the first block.
            if (blockNum == 0)
                caption = jc.getMsgString(msg, jc.CAPTION);

            jc.logMsg("From " + sender + " - " + " < " + filename +
                      " > block: " + (blockNum+1) + " of " + totalBlocks);

            // Get the file data block, place it in our data array.
            MessageElement elem = msg.getMessageElement(jc.DATABLOCK);
            if (elem == null)
                return;
            byte dataBlock[] = elem.getBytes(false);
            System.arraycopy(dataBlock, 0, fdata, blockNum * myBlockSize, dataBlock.length);

            // Record that we've processed this block.
            blockIn[blockNum] = true;
            blocksReceived++;

            // Acknowledge receipt of the block, so the sender will send the next.
            // This also serves to notify other peers that we have received this block;
            // they may request it from us.
            //
            sendAck(msg);

            // Are we done?  Then write the file.  Otherwise, if this was a response to
            // a missing block request, ask for the next one.
            //
            if (blocksReceived == totalBlocks) {
                writeFile();
            }
            else if (jc.getMsgString(msg, jc.MESSAGETYPE).equals(jc.MSG_FILE_REQ_RESP)) {

                // The REQ_RESP may not have been in response to a request from this
                // peer.  But assume that if one peer is already requesting dropped
                // block messages, we should be too.
                //
                requestNextMissingBlock();
            }

            // Notify listeners of file progress.
            JxtaCastEvent e = new JxtaCastEvent();
            e.transType   = e.RECV;
            e.filename    = new String(filename);
            e.filepath    = new String(jc.fileSaveLoc);
            e.senderId    = new String(senderId);

            if (sender == null)
                sender = "<anonymous>";
            e.sender = new String(sender);

            if (caption != null)
                e.caption = new String(caption);

            e.percentDone = ((float)blocksReceived / totalBlocks) * 100;
            jc.sendJxtaCastEvent(e);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /** Send an acknowledgement that we've received a file data block.
     */
    private void sendAck(Message msg) {

        try {
            // Create and send and ACK message.
            Message ackMsg = new Message();
            jc.setMsgString(ackMsg, jc.MESSAGETYPE, jc.MSG_FILE_ACK);
            jc.setMsgString(ackMsg, jc.SENDERNAME,  jc.myPeer.getName());
            jc.setMsgString(ackMsg, jc.SENDERID,    jc.myPeer.getPeerID().toString());
            jc.setMsgString(ackMsg, jc.VERSION,     jc.version);
            jc.setMsgString(ackMsg, jc.FILEKEY,     jc.getMsgString(msg, jc.FILEKEY));
            jc.setMsgString(ackMsg, jc.FILENAME,    filename);
            jc.setMsgString(ackMsg, jc.BLOCKNUM,    jc.getMsgString(msg, jc.BLOCKNUM));

            // Send the ACK message.
            int blockNum = Integer.parseInt(jc.getMsgString(msg, jc.BLOCKNUM));
            jc.logMsg("Sending ACK: " + filename + "  block " + (blockNum+1));
            jc.sendMessage(ackMsg);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /** Send a request for specific file data block.
     *
     *  @param  blockNum  the block to request.
     */
    private void sendReq(int blockNum) {

        // Increase the wait time every time we send a request.  It's reset
        // when we get a response.  The longer we go without getting a response,
        // the less often we'll send requests.
        minTimeToWait += 4000;

        try {
            // Create a message, fill it with key info, and the block number.
            Message reqMsg = new Message();
            jc.setMsgString(reqMsg, jc.MESSAGETYPE, jc.MSG_FILE_REQ);
            jc.setMsgString(reqMsg, jc.SENDERNAME,  jc.myPeer.getName());
            jc.setMsgString(reqMsg, jc.SENDERID,    jc.myPeer.getPeerID().toString());
            jc.setMsgString(reqMsg, jc.VERSION,     jc.version);
            jc.setMsgString(reqMsg, jc.FILEKEY,     key);
            jc.setMsgString(reqMsg, jc.FILENAME,    filename);
            jc.setMsgString(reqMsg, jc.BLOCKNUM,    String.valueOf(blockNum));

            // Who are we requesting it from?
            // If we've gotten an ACK for this block, ask that peer.  Then clear
            // him from the lastAck array, so we don't ask the same peer again.
            //
            String reqTo = "last ACK";
            if (lastAck[blockNum] != null) {
                jc.setMsgString(reqMsg, jc.REQTOPEER, lastAck[blockNum]);
                lastAck[blockNum] = null;

            } else if (!askedOrig[blockNum]) {

                // We haven't asked the original sender for this block yet, so
                // ask him now.
                jc.setMsgString(reqMsg, jc.REQTOPEER, senderId);
                askedOrig[blockNum] = true;
                reqTo = "orig sender";

            } else {

                // Ask any peer to respond.
                jc.setMsgString(reqMsg, jc.REQTOPEER, jc.REQ_ANYPEER);
                reqTo = "ANYONE!";
            }

            // Send the REQ message.  It'd be nice to send this through a
            // "back channel" unicast pipe directly to the peer we're requesting
            // from.  For now we'll send it out over the wire.  Everyone else
            // can just ignore it.
            //
            jc.logMsg("Sending REQ to " + reqTo + ": " + filename +
                      "  block " + (blockNum+1)); 
            jc.sendMessage(reqMsg);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Request the next missing block of file data.  We request the block from
     * the latest peer known to have received that block.  If none are known, or
     * we've already requested from that peer and not gotten a response, we request
     * from the original sender of the file.  If we've already done that, then
     * we request from anyone.  (We hope not to have to do that, since it may
     * result in many peers responding at once with the same data block.)
     */
    private void requestNextMissingBlock() {

        // Find and request the next missing block.  We just request one block.
        // We'll request the next after it comes in, or when the TrailBoss triggers
        // the next bossCheck().
        //
        while (currReqBlock < blockIn.length) {

            if (blockIn[currReqBlock] == false) {
                sendReq(currReqBlock);
                currReqBlock++;
                lastReqTime = System.currentTimeMillis();
                break;
            }

            currReqBlock++;
        }

        // If we've reached the end of the array, start over.
        //
        if (currReqBlock == blockIn.length)
            currReqBlock = 0;
    }


    /** Process a file transfer ACK message.
     *
     *  Peers will send out an ACK message when they've received a block.
     *  We'll keep track of the latest peer that sent an ACK for each block.
     *  If we don't get that block ourselves, we can request it from a peer
     *  that has it.
     */
    private void processMsgAck(Message msg) {

        // Ignore the ACK if it's from us.
        String senderId = jc.getMsgString(msg, jc.SENDERID);
        if (senderId.equals(jc.myPeer.getPeerID().toString()))
            return;

        int blockNum = Integer.parseInt(jc.getMsgString(msg, jc.BLOCKNUM));
        lastAck[blockNum] = jc.getMsgString(msg, jc.SENDERID);

        jc.logMsg("Received ACK: " + filename + "  block " +
                  (blockNum+1) + ", from " + jc.getMsgString(msg, jc.SENDERNAME));
    }


    /** Process a file transfer REQ message.
     *
     *  A peer has requested a block of this file.  If the request is addressed
     *  to us, or to any peer, send the block out as a REQ_RESP 'request response'
     *  message.
     */
    private void processMsgReq(Message msg) {

        // If it's not addressed to us, or to "any peer", bail out.
        String reqToPeer = jc.getMsgString(msg, jc.REQTOPEER);
        if (reqToPeer == null)
            return;
        if (!reqToPeer.equals(jc.myPeer.getPeerID().toString())  &&
            !reqToPeer.equals(jc.REQ_ANYPEER))
            return;

        // If it's FROM us, bail out.  (It's an any peer req that we sent.)
        String reqSender = jc.getMsgString(msg, jc.SENDERID);
        if (reqSender == null)
            return;
        if (reqSender.equals(jc.myPeer.getPeerID().toString()))
            return;

        int blockNum = Integer.parseInt(jc.getMsgString(msg, jc.BLOCKNUM));

        if (reqToPeer.equals(jc.REQ_ANYPEER))
            jc.logMsg("Received REQ_ANYPEER: " + filename + "  block " +
                      (blockNum+1) + ", from " + jc.getMsgString(msg, jc.SENDERNAME));
        else
            jc.logMsg("Received FILE_REQ: " + filename + "  block " +
                      (blockNum+1) + ", from " + jc.getMsgString(msg, jc.SENDERNAME));

        // Send the block, if we actually do have it.
        if (blockNum > 0  &&  blockNum < blockIn.length  &&  blockIn[blockNum] == true)
            sendBlock(blockNum, jc.MSG_FILE_REQ_RESP);
    }


    /** Write the file data to a disk file.
     */
    private void writeFile() {

        jc.logMsg("*** WRITING FILE ***   " + jc.fileSaveLoc + filename);

        try {
            FileOutputStream fos = new FileOutputStream(jc.fileSaveLoc + filename);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bos.write(fdata, 0, fdata.length);
            bos.flush();
            fos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
