package SHAC.client;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import SHAC.protocol.*;


public class SHACClient extends Thread
{
	private Timer timer;
	private Random rand;
	private DatagramSocket socket;
	public ArrayList<SHACNode> nodes;
	public String serverIP;
	
	
	public SHACClient()
	{
		serverIP = "localhost";
		initializeClient();
	}
	
	public SHACClient(String serverIP)
	{
		this.serverIP = serverIP;
		initializeClient();
	}
	
	private void initializeClient()
	{
		timer = new Timer();
		rand = new Random();
		try 
        {
            socket = new DatagramSocket();
        }
        catch (SocketException e) 
        {
            e.printStackTrace();
        }
	}
	
	private void runClient()
	{
		start();
		startSendingUpdates();
	}

	public void run()
	{
		listenForUpdates();
	}
	
	private void startSendingUpdates()
	{
		// Send an update, then set a timer to do it again
		sendUpdate();
		timer.schedule(new TimerTask()
			{
			  public void run()
			  {
			    startSendingUpdates();
			  }
			}, rand.nextInt(30) * 1000);
	}
	
    private void sendUpdate()
    {
		// TODO Send a packet to greet server
    	try 
        {
            InetAddress IPAddress = InetAddress.getByName(serverIP);
            SHACData update = new SHACData(0, NodeType.CLIENT);
            byte[] data = SHACProtocol.encodePacketData(update);
            DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, 9876);
            socket.send(sendPacket);
            System.out.println("Message sent from client");
        }
        catch (UnknownHostException e) 
        {
            e.printStackTrace();
        } 
        catch (SocketException e) 
        {
            e.printStackTrace();
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
	}
    
    public void listenForUpdates() 
    {
        while (true)
        {
			try
			{
				byte[] incomingData = new byte[1024];
				DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
				socket.receive(incomingPacket);
				SHACData data = SHACProtocol.decodePacketData(incomingPacket.getData());
				if (data.nodeTypeFlag == NodeType.SERVER)
				{
					nodes = data.nodes;
				}
				System.out.println("Received availability update from server.");
				socket.close();
			}
			catch (UnknownHostException e)
			{
				e.printStackTrace();
			}
			catch (SocketException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			} 
		}
    }
    
    public void printAvailableNodes()
    {
    	// Return status of each node. Change return type to what's appropriate
    	System.out.println("Available nodes:");
    	for (SHACNode n : (SHACNode[]) nodes.toArray())
    	{
    		System.out.println(n.ip.toString() + " last checked in " + n.timestamp.toString());
    		if (n.isAvailable)
    		{
        		System.out.println(n.ip.toString() + " is available.");
    		}
    		else
    		{
        		System.out.println(n.ip.toString() + " is unavailable.");
    		}
    	}
    }

	public static void main(String[] args)
	{
    	SHACClient s = new SHACClient();
    	s.runClient();
    }

}
