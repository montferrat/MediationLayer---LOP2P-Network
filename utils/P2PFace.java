/*
 * Copyright (c) 2001 Sun Microsystems, Inc.  All rights reserved.
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
 * $Id: P2PFace.java,v 1.4 2005/04/26 19:06:50 hamada Exp $
 *
 */

package utils;


import java.util.*;

import net.jxta.discovery.*;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.peergroup.PeerGroup;


/*
 * P2PFace: Peer-to-Peer protocol interface.
 *
 *          Provides a generic interface to a set of (very JXTA-like)
 *          p2p protocols.  Implementations are used to transparently access 
 *          either JXTA itself or a simulation of JXTA.
 */
public interface P2PFace {

    /** Return my own peer name. */
    public String getMyPeerName();


    /** Return advertisement for the default (initial) peer group. */
    public PeerGroupAdvertisement getDefaultAdv();


    /** Return advertisement for my peer. */
    public PeerAdvertisement getMyPeerAdv();


    /** Return the default (initial) peer group. */
    public PeerGroup getDefaultGroup();


    /** Launch peer group discovery.
     *  @param targetPeerId  - limit to responses from this peer, or null for no limit.
     *  @param discoListener - listener for discovery events.  May be null,
     *                         if you don't want the notification.
     */
    public void discoverGroups(String targetPeerId, DiscoveryListener discoListener);


    /** Launch peer discovery, for the specified group.
     *  @param targetPeerId  - limit to responses from this peer, or null for no limit.
     *  @param group         - peer group for which to find peers.
     *  @param discoListener - listener for discovery events.  May be null,
     *                         if you don't want the notification.
     */
    public void discoverPeers(String targetPeerId,
                              PeerGroupAdvertisement group,
                              DiscoveryListener discoListener);


    /** Launch advertisement discovery, for the specified group.
     *  @param targetPeerId  - limit to responses from this peer, or null for no limit.
     *  @param group         - peer group for which to find advertisements.
     *  @param discoListener - listener for discovery events.  May be null,
     *                         if you don't want the notification.
     *  @param attribute     - Limit responses to advertisements with this attribute/value pair.
     *                         Set to null to place no limit.
     *  @param value         - See 'attribute', above.
     */
    public void discoverAdvertisements(String targetPeerId,
                                       PeerGroupAdvertisement group,
                                       DiscoveryListener discoListener,
                                       String attribute,
                                       String value);


    /** Return an enerator to an array of PeerGroupAdvertisement objects
     *  representing the groups known so far.
     */
    public Enumeration getKnownGroups();


    /** Return an enerator to an array of PeerAdvertisement objects
     *  representing the peers known so far, that are members of the specified
     *  peer group.
     */
    public Enumeration getKnownPeers(PeerGroupAdvertisement group);


    /** Return an enerator to an array of Advertisement objects
     *  representing the advs known so far, that were created within the
     *  specified peer group.  The list can be narrowed to advs matching
     *  an attribute/value pair.
     *
     *  @param group     - peer group for which to retrieve advertisements.
     *  @param attribute - Limit responses to advertisements with this attribute/value pair.
     *                     Set to null to place no limit.
     *  @param value     - See 'attribute', above.
     */
    public Enumeration getKnownAdvertisements(PeerGroupAdvertisement group,
                                              String attribute,
                                              String value);

    /** Join the specified PeerGroup.
     *  @param  groupAdv      Advertisement of the group to join.
     *  @param  beRendezvous  If true, act as a rendezvous for this group.
     *  @return PeerGroup if we were successfully able to join the group, or
     *          if we had already joined it.
     *          null if we were unable to join the group.
     */
    public PeerGroup joinPeerGroup(PeerGroupAdvertisement groupAdv, boolean beRendezvous);


    /** Create and join a new PeerGroup.  Also publishes the group
     *  advertisement.
     *
     *  @param  groupName     Name for the new group.
     *  @param  description   Group description.
     *  @param  beRendezvous  If true, act as a rendezvous for this group.
     *  @return The new peer group if successful, otherwise null.
     */
    public PeerGroup createNewGroup(String groupName, String description, boolean beRendezvous);


    /** Is the indicated peer a rendezvous? */
    public boolean isRendezvous(PeerAdvertisement peerAdv);
}
