package SHAC.server;

import java.io.IOException;
import java.util.Date;
import java.util.ArrayList;
import java.net.*;
import java.time.*;
import SHAC.protocol.*;

public class SHACServer extends Thread {

	private String threadName;
	private DatagramSocket socket;
	public ArrayList<SHACNode> nodes;

	SHACServer(String command) {
		threadName = command;
	}

	public void run() {

		System.out.println("Running " + threadName);

		if (threadName.equals("ServerStart")) {
			createAndListenSocket();
		}

		if (threadName.equals("TimerChecker")) {
			createAndRunTimerChecker();
		}
	}

	public void createAndListenSocket() {
		while (true) {

			try {
				System.out.println("Server");
				DatagramSocket socket = new DatagramSocket(SHACProtocol.SHAC_SOCKET);
				byte[] incomingData = new byte[1024];
				DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
				socket.receive(incomingPacket);
				
				System.out.println("Data Recieved");
				SHACData data = SHACProtocol.decodePacketData(incomingPacket.getData());
				SHACNode sender = new SHACNode(incomingPacket.getAddress(), new Date());

				data.dataNodes.add(sender);

				boolean listChanged = false;
				if (data.nodeTypeFlag == NodeType.CLIENT) {
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
					}
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

	private void sendUpdates() {
		for (SHACNode node : (SHACNode[]) nodes.toArray()) {
			// Send all updates to all clients your full list of peers
			try {
				SHACData update = new SHACData(nodes.size(), NodeType.CLIENT);
				update.dataNodes = nodes;
				byte[] data = SHACProtocol.encodePacketData(update);
				DatagramPacket sendPacket = new DatagramPacket(data, data.length, node.ip, SHACProtocol.SHAC_SOCKET);
				socket.send(sendPacket);
				System.out.println("Message sent to client");
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void createAndRunTimerChecker(){
    		while(true) {
    			try{
    				if(nodes.size() > 0) {
    					for (int i = 0; i < nodes.size(); i++) {
    						Date then = nodes.get(i).timestamp;
    						long oldTime = then.getTime();

    						Date now = new Date();
    						long newTime = now.getTime();
    						if (newTime - oldTime > 3000) {
    							nodes.get(i).isAvailable = false;
    							sendUpdates();
    						}
    					}
    				}
    			}
    			catch(NullPointerException e){
    			}
    			
    		}
    }

	public static void main(String[] args) {
		
		SHACServer server = new SHACServer("ServerStart");
		server.start();
		SHACServer timerChecker = new SHACServer("TimerChecker");
		timerChecker.start();
	}
}