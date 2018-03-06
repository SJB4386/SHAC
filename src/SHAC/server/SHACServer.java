package SHAC.server;
import java.io.IOException;
import java.util.Date;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import SHAC.protocol.*;

public class SHACServer extends Thread{
	

	public void run() {

	}
	
	public void createAndListenSocket() 
    {
		 DatagramSocket socket = new DatagramSocket();
		
		while (true) {
			try {
				byte[] incomingData = new byte[1024];
				DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
				
				
				socket.receive(incomingPacket);
				SHACData data = SHACProtocol.decodePacketData(incomingPacket.getData());

				SHACNode sender = new SHACNode(incomingPacket.getAddress(), new Date());
				data.nodes.add(sender);

				boolean listChanged = false;
				if (data.nodeTypeFlag == NodeType.PEER) {
					for (SHACNode newNode : (SHACNode[]) data.nodes.toArray()) {
						if (!nodes.contains(newNode)) {
							this.nodes.add(newNode);
							listChanged = true;
						} else {
							SHACNode oldNode = this.nodes.get(this.nodes.indexOf(newNode));
							if (oldNode.isAvailable != newNode.isAvailable) {
								listChanged = true;
							}
							this.nodes.set(this.nodes.indexOf(oldNode), newNode);
						}
					}
				}
				if (!listChanged) {
					sendUpdates();
				}
				System.out.println("Received availability update from server.");
				socket.close();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
    
    public static void main(String[] args) {  
            SHACServer server = new SHACServer();
            server.createAndListenSocket();
        

    }
    

}
