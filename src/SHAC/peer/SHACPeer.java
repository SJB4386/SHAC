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


    public SHACPeer() {
        initializePeer();
    }

    public SHACPeer(String[] firstPeers) {
        initializePeer();
        for (String peer : firstPeers) {
            try {
                SHACNode firstNode = new SHACNode(InetAddress.getByName(peer), new Date());
                peerNodes.add(firstNode);
                schedulePrune(firstNode);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

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

    private void runPeer() {
        //Launch update listening thread
        start();
        startSendingUpdates();
        //Periodically print list
        schedulePrint();
    }

    public void run() {
        listenForUpdates();
    }    
    
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

    private void sendUpdates() {
        lastSent = new Date();
        SHACData update = null;
        for (SHACNode node : peerNodes) {
            // Send all known peers your full list of peers
            try {
                update = new SHACData(peerNodes.size(), NodeType.PEER);
                update.dataNodes = peerNodes;
                byte[] updateData = SHACProtocol.encodePacketData(update);
                DatagramPacket sendPacket = new DatagramPacket(updateData, updateData.length, node.ip, SHACProtocol.SHAC_SOCKET);
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
                        peerNodes.set(peerNodes.indexOf(oldNode), receivedNode);
                    } else {
                        this.peerNodes.add(receivedNode);
                        listChanged = true;
                    }
                    if (receivedNode.isAvailable) {
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

    public void printAvailableNodes() {
        // Return status of each node. Change return type to what's appropriate
        System.out.println("Last sent a packet at " + lastSent.toString() +".");
        System.out.println("Available nodes as of " + lastReceived.toString() +":");
        for (SHACNode n : peerNodes) {
            System.out.println(n.toString());
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
