package SHAC.client;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import SHAC.protocol.*;

public class SHACClient extends Thread {
    private Timer timer;
    private Random rand;
    private DatagramSocket socket;
    public ArrayList<SHACNode> clientNodes;
    public String serverIP;
    public Date lastReceived, lastSent;


    public SHACClient() {
        serverIP = "localhost";
        initializeClient();
    }

    public SHACClient(String serverIP) {
        this.serverIP = serverIP;
        initializeClient();
    }

    private void initializeClient() {
        lastReceived = lastSent = new Date();
        timer = new Timer();
        rand = new Random();
        clientNodes = new ArrayList<SHACNode>();
        try {
            socket = new DatagramSocket(SHACProtocol.SHAC_SOCKET);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void runClient() {
        start();
        startSendingUpdates();
        //Periodically print list
        schedulePrint();
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

    public void run() {
        listenForUpdates();
    }

    private void startSendingUpdates() {
        // Send an update, then set a timer to do it again
        sendUpdate();
        timer.schedule(new TimerTask() {
            public void run() {
                startSendingUpdates();
            }
        }, rand.nextInt(30) * 1000);
    }

    private void sendUpdate() {
        // Send a packet to greet server
        try {
            InetAddress IPAddress = InetAddress.getByName(serverIP);
            SHACData update = new SHACData(0, NodeType.CLIENT);
            byte[] updateData = SHACProtocol.encodePacketData(update);
            DatagramPacket sendPacket = new DatagramPacket(updateData, updateData.length, IPAddress, SHACProtocol.SHAC_SOCKET);
            socket.send(sendPacket);
            lastSent = new Date();
            System.out.println("Message sent from client");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listenForUpdates() {
        while (true) {
            try {
                byte[] incomingData = new byte[1024];
                DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                socket.receive(incomingPacket);
                SHACData receivedData = SHACProtocol.decodePacketData(incomingPacket.getData());
                if (receivedData.nodeTypeFlag == NodeType.SERVER || receivedData.nodeTypeFlag == NodeType.PEER) {
                    clientNodes = receivedData.dataNodes;
                }
                lastReceived = new Date();
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
        System.out.println("Last sent a packet at " + lastSent.toString() +".");
        System.out.println("Available nodes as of " + lastReceived.toString() +":");
        try {
            for (SHACNode n : clientNodes) {
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
        SHACClient s;
        if (args.length == 0) {
            s = new SHACClient();
        } else {
            s = new SHACClient(args[0]);
        }
        s.runClient();
    }

}
