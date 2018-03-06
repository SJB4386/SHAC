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
    public ArrayList<SHACNode> nodes;
    
    private static final int secondsTilDeadNode = 30;


    public SHACPeer() {
        initializePeer();
    }

    public SHACPeer(String[] firstPeers) {
        initializePeer();
        for (String peer : firstPeers) {
            try {
                nodes.add(new SHACNode(InetAddress.getByName(peer), new Date()));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    private void initializePeer() {
        timer = new Timer();
        rand = new Random();
        nodes = new ArrayList<SHACNode>();
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
    }

    private void startSendingUpdates() {
        // Send an update to all peers, then set a timer to do it again
        sendUpdates();
        timer.schedule(new TimerTask() {
            public void run() {
                startSendingUpdates();
            }
        }, rand.nextInt(30) * 1000);
    }

    private void sendUpdates() {
        for (SHACNode node : nodes) {
            // Send all known peers your full list of peers
            try {
                SHACData update = new SHACData(nodes.size(), NodeType.PEER);
                update.dataNodes = nodes;
                byte[] data = SHACProtocol.encodePacketData(update);
                DatagramPacket sendPacket = new DatagramPacket(data, data.length, node.ip, SHACProtocol.SHAC_SOCKET);
                socket.send(sendPacket);
                System.out.println("Message sent from client");
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
                SHACData data = SHACProtocol.decodePacketData(incomingPacket.getData());

                SHACNode sender = new SHACNode(incomingPacket.getAddress(), new Date());
                data.dataNodes.add(sender);

                boolean listChanged = false;
                if (data.nodeTypeFlag == NodeType.PEER) {
                    for (SHACNode receivedNode : data.dataNodes) {
                        if (nodes.contains(receivedNode)) {
                            SHACNode oldNode = this.nodes.get(this.nodes.indexOf(receivedNode));
                            if (oldNode.isAvailable != receivedNode.isAvailable) {
                                listChanged = true;
                            }
                            this.nodes.set(this.nodes.indexOf(oldNode), receivedNode);
                        } else {
                            this.nodes.add(receivedNode);
                            listChanged = true;
                        }
                        schedulePrune(receivedNode);
                    }
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
        for (SHACNode n : nodes) {
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
