package SHAC.peer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import SHAC.protocol.*;

public class SHACPeer extends Thread {

    private Random rand;
    private DatagramSocket socket;
    public ArrayList<SHACNode> peerNodes;
    public Date lastReceived, lastSent;
    
    private static final int secondsTilDeadNode = 30;
    
    /**
     * SHACPeer() is a default object that initializes a peer in a HAC. 
     */

    public SHACPeer() {
        initializePeer();
    }

    /**
     * SHACPeer(String[] firstPeers) initializes a SHACPeer
     * which adds the first peer to the local peer's list 
     * of peer nodes.
     */
    public SHACPeer(String[] firstPeers) {
        initializePeer();
        for (String peer : firstPeers) {
            try {
                SHACNode firstNode = new SHACNode(InetAddress.getByName(peer), new Date());
                firstNode.isAvailable = false;
                peerNodes.add(firstNode);
                schedulePrune(firstNode);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * initializePeer() creates the initializes all of the important fields for 
     * the peer class. Most notably this is where SHACPeer intitializes the 
     * DatagramSocket which is the UDP connection with the rest of the SHAC
     * Network. 
     */
    private void initializePeer() {
        lastReceived = lastSent = new Date();
        rand = new Random();
        peerNodes = new ArrayList<SHACNode>();
        try {
            socket = new DatagramSocket(SHACProtocol.SHAC_SOCKET);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * runPeer() Launches the listening thread. This allows peer to act as a server as
     * well as a client. runPeer() then begins sending updates to all of its peers and 
     * prints out its own internal list using the scedulePrint() method.
     */
    private void runPeer() {
        //Launch update listening thread
        start();
        startSendingUpdates();
        //Periodically print list
        schedulePrint();
    }

    /**
     * run() is called whenever a new SHACPeer thread is created. 
     * It runs the listenForUpdates() method. 
     */
    
    public void run() {
        listenForUpdates();
    }    
    
    /**
     * schedulePrint() prints the list of nodes whenever there is a change as well as periodically. 
     * The thread sleep in the try block is used to prevent a printing while the list of nodes is
     * updating.  
     */
    
    private void schedulePrint() {
        System.out.print("\n\n\n\n\n\n\n\n");
        System.out.flush();
        printAvailableNodes();

        Thread thread = new Thread(){
            public void run(){
                try {
                    sleep(5 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                schedulePrint();
            }
          };
        thread.start();
    }
    
    /**
     * shedulePrune(SHACNode node) is how the SHAC protocol determines when to declare a peer
     * unavailable. schedulePrune also uses the list of nodes so it uses a sleep to prevent 
     * race conditions. Note the actual pruning happens in pruneNode() not schedulePrune().  
     */
    
    private void schedulePrune(SHACNode node) {
        Thread thread = new Thread(){
            public void run(){
                try {
                    sleep(secondsTilDeadNode * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                pruneNode(node);
            }
          };
        thread.start();
    }
    
    /**
     * pruneNode(SHACNode node) changes a node from available to unavailable. Whether 
     * to prune or not is determined by the node's time stamp and the current time. If 
     * the current time - the node's time is >= to 30 seconds then the node is declared 
     * unavailable. 
     */
    
    private void pruneNode(SHACNode node) {
        boolean changed = false;
        if (new Date().getTime() - node.timestamp.getTime() >= (secondsTilDeadNode * 1000)) {
            if (node.isAvailable) {
                changed = true;
            }
            node.isAvailable = false;
        }
        if (changed) {
            sendUpdates();
        }
    }

    /**
     * startSendingUpdates() firsts calls sendUpdates to make sure all other peers are aware 
     * of the local peer's availability. Then it schedules when to send the next update, a
     * random interval between 0 and 30 seconds. 
     */
    
    private void startSendingUpdates() {
        // Send an update to all peers, then set a timer to do it again
        sendUpdates();
        Thread thread = new Thread(){
            public void run(){
                try {
                    sleep(rand.nextInt(30) * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                startSendingUpdates();
            }
          };
        thread.start();
    }
    
    /**
     * sendUpdates() is how SHACPeers communicate that they are still alive as well as sending
     * out lists of peers to other nodes. The list is then used to add new peers to the SHAC
     * network. 
     */
    
    private void sendUpdates() {
        lastSent = new Date();
        SHACData update = null;
        for (SHACNode nodeToSendTo : peerNodes) {
            // Send all known peers your full list of peers
            try {
                update = new SHACData(peerNodes.size() - 1, NodeType.PEER);
                
                for(int i = 0; i < peerNodes.size(); i++) {               		
                		if(!peerNodes.get(i).equals(nodeToSendTo))
                			update.dataNodes.add(peerNodes.get(i));
                }
                               
                byte[] updateData = SHACProtocol.encodePacketData(update);
                DatagramPacket sendPacket = new DatagramPacket(updateData, updateData.length, nodeToSendTo.ip, SHACProtocol.SHAC_SOCKET);
                socket.send(sendPacket);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * listenForUpdates() is how the peer works like a UDP server. listenForUpdates() begins a thread that readies for an incoming 
     * packet of SHACData and waits to receive it. After receiving the data, listenForUpdates then checks to see if an update has 
     * occurred. If a changes has occurred, then the list of peers is changed and the update is sent out to all other known peers. 
     * 
     */

    public void listenForUpdates() {
        while (true) {
            try {
                byte[] incomingData = new byte[1024];
                DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                socket.receive(incomingPacket);
                SHACData receivedData = SHACProtocol.decodePacketData(incomingPacket.getData());

                SHACNode sender = new SHACNode(incomingPacket.getAddress(), new Date());
                receivedData.dataNodes.add(sender);

                boolean listChanged = false;
                for (SHACNode receivedNode : receivedData.dataNodes) {
                    if (peerNodes.contains(receivedNode)) {
                        SHACNode oldNode = peerNodes.get(peerNodes.indexOf(receivedNode));
                        if (oldNode.isAvailable != receivedNode.isAvailable) {
                            listChanged = true;
                        }
                        if (receivedNode.isAvailable && receivedNode.timestamp.after(oldNode.timestamp)) {
                            peerNodes.set(peerNodes.indexOf(oldNode), receivedNode);
                            schedulePrune(receivedNode);
                        }
                    } else {
                        this.peerNodes.add(receivedNode);
                        listChanged = true;
                        schedulePrune(receivedNode);
                    }
                    lastReceived = new Date();
                }
                
                if (listChanged) {
                    sendUpdates();
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * printAvailableNodes() prints the list of nodes that is contained in peerNodes. 
     */
    
    public void printAvailableNodes() {
        // Return status of each node. Change return type to what's appropriate
        System.out.println("Last sent a packet at " + lastSent.toString() +".");
        System.out.println("Available nodes as of " + lastReceived.toString() +":");
        try {
            for (SHACNode n : peerNodes) {
                System.out.println(n.toString());
            }
        } catch (Exception e) {
            try {
                sleep(100);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            printAvailableNodes();
        }
    }

    public static void main(String[] args) {
        SHACPeer s;
        if (args.length == 0) {
            s = new SHACPeer();
        } else {
            s = new SHACPeer(args);
        }
        s.runPeer();

    }

}
