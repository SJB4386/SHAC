package SHAC.client;

import java.io.IOException;
import java.net.*;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class SHACClient extends Thread {
	
	Timer timer;
	Random rand;
	DatagramSocket socket;
	
	
	public SHACClient() {
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
	
	private void runClient() {
		start();
		startSendingUpdates();
	}

	public void run() {
		// TODO listen for availability updates
		
		
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
		// TODO Send a packet containing availability
    	try 
        {
            InetAddress IPAddress = InetAddress.getByName("localhost");
            String sentence = "Hello";
            byte[] data = sentence.getBytes();
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
    
    public void createAndListenSocket() 
    {
        try 
        {
            byte[] incomingData = new byte[1024];
            System.out.println("Message sent from client");
            DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
            socket.receive(incomingPacket);
            String response = new String(incomingPacket.getData());
            System.out.println("Response from server:" + response);
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
    
    public void getAvailableNodes() {
    	// TODO Return status of each node. Change return type to what's appropriate
    	
    }

	public static void main(String[] args) {
    	SHACClient s = new SHACClient();
    	s.runClient();
    }

}
