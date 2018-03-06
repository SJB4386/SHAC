package SHAC.server;
import java.io.IOException;
import java.util.Date;
import java.net.*;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import SHAC.protocol.*;

public class SHACServer extends Thread{
	
	private String threadName;
    private DatagramSocket socket;
    public ArrayList<SHACNode> nodes;
	
	public void run() {
		
		System.out.println("Running " + threadName);
		
		if (threadName.equals("Server")){
			createAndListenSocket();
		}
		
	}
	
	SHACServer(){ 
		initializeServer();
	}
	
	public void initializeServer(){
		nodes = new ArrayList<SHACNode>();
	}
	
	public void createAndListenSocket() 
    {	 	
		while (true) {
			try {
				DatagramSocket socket = new DatagramSocket(9649);
				byte[] incomingData = new byte[1024];
				DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
				
				socket.receive(incomingPacket);
				SHACData data = SHACProtocol.decodePacketData(incomingPacket.getData());

				SHACNode sender = new SHACNode(incomingPacket.getAddress(), new Date());				
				data.nodes.add(sender);

				boolean listChanged = false;
				if (data.nodeTypeFlag == NodeType.CLIENT) {
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
                update.nodes = nodes;
                byte[] data = SHACProtocol.encodePacketData(update);
                DatagramPacket sendPacket = new DatagramPacket(data, data.length, node.ip, 9876);
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
    
    
    
    public static void main(String[] args) {  
            SHACServer server = new SHACServer();
            server.createAndListenSocket();
    }    
}