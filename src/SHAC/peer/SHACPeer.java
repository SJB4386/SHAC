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
import java.util.Timer;
import java.util.TimerTask;

import SHAC.protocol.*;

public class SHACPeer extends Thread {

    private Timer timer;
    private Random rand;
    private DatagramSocket socket;
    public ArrayList<SHACNode> peerNodes;
    
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
        timer = new Timer();
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
    }

    public void run() {
        listenForUpdates();
    }    
    
    private void schedulePrune(SHACNode node) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                pruneNode(node);
            }
        }, secondsTilDeadNode * 1000);
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
        timer.purge();
    }

    private void startSendingUpdates() {
        // Send an update to all peers, then set a timer to do it again
        sendUpdates();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                startSendingUpdates();
            }
        }, rand.nextInt(30) * 1000);
        timer.purge();
    }

    private void sendUpdates() {
        SHACData update = null;
        for (SHACNode node : peerNodes) {
            // Send all known peers your full list of peers
            try {
                update = new SHACData(peerNodes.size(), NodeType.PEER);
                update.dataNodes = peerNodes;
                byte[] updateData = SHACProtocol.encodePacketData(update);
                DatagramPacket sendPacket = new DatagramPacket(updateData, updateData.length, node.ip, SHACProtocol.SHAC_SOCKET);
                socket.send(sendPacket);
                System.out.printf("Sending:\n%s", update.toString());
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
                    schedulePrune(receivedNode);
                }
                
                if (listChanged) {
                    sendUpdates();
                }
                System.out.println("Received availability update from server.");
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
        System.out.println("Available nodes:");
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
