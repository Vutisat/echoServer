/*
 * Framework of a server from Scott Campbell
   Edited and made serve the purpose by Pob Vutisalchavakul
   CSE383 B

   Homework 1

   Simple datagram server that says hello and goodbye to clients.
   The server also echo messages to all the connected client when received.
   
 */

import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.*;

/**
 * @author Pob
 * 
 */
public class Server {
	DatagramSocket sock;
	DatagramPacket pkt;
	int port;
	Log log;
	ArrayList<SocketAddress> handlers;
	String helloResponse = "HELLO-RESPONSE";
	String goodbyeResponse = "GOODBYE-RESPONSE";

	// start server
	public static void main(String a[]) {
		int port = 0;
		try {
			port = Integer.parseInt(a[0]);
		} catch (Exception err) {
			System.err.println("Could not parse arguemnt");
			System.exit(-1);
		}

		try {
			new Server(port).Main();
		} catch (IOException err) {
			System.err.println("Could not start server - probably port in use");
			System.exit(-1);
		}
	}

	// constructor - opens socket
	public Server(int port) throws IOException {
		this.port = port;
		sock = new DatagramSocket(port);
		log = new Log("UDP Server.log");
	}

	// main working code for server
	/*
	 * Listens for message from client and sends them the previous message
	 * received
	 */
	public void Main() {
		String msg = "first";
		System.out.println("Server Starting"); // only message I will send to
												// std out
		log.log("Server Starting");

		// This will keep track of clients connected.
		handlers = new ArrayList<SocketAddress>();

		// loop forever
		while (true) {
			try {

				// Following just take the packet in without any meaning.
				log.log("Waiting for packet");
				// get new message
				byte b[] = new byte[1024];
				DatagramPacket pkt = new DatagramPacket(b, b.length);
				sock.receive(pkt);

				log.log(pkt.getSocketAddress() + " Got Packet");
				System.out.println("Got a packet from "
						+ pkt.getSocketAddress());

				// See if there's a message that needs to be send to clients
				String[] messages = receiveMsg(b);

				// Dealing with the packets coming in
				if (messages[0].equals("HELLO")) {
					handlers.add(pkt.getSocketAddress());
					sendMsg(pkt.getSocketAddress(), helloResponse);
					log.log(pkt.getAddress().toString() + "has connected");
				} else if (messages[0].equals("MESSAGE")) {
					echoToAll(messages[1]);
				} else if (messages[0].equals("GOODBYE")) {
					sendMsg(pkt.getSocketAddress(), goodbyeResponse);
					diconnectAClient(pkt.getSocketAddress());
					log.log(pkt.getAddress().toString() + "has disconnected");
				} else{
					log.log("Invalid message command was received");
					sendMsg(pkt.getSocketAddress(), "Invalid command was received, please try again following the protocol");
				}

			} catch (IOException err) {
				log.log("Error " + err);
			}
		}
	}

	/**
	 * @param b
	 *            is the packet that is being received (as byte array)
	 * @return array of string/strings this method makes meaning of byte arrays
	 *         into strings
	 */
	public String[] receiveMsg(byte[] b) {
		String[] msgArr = new String[2];
		ByteArrayInputStream bis = new ByteArrayInputStream(b);
		DataInputStream dis = new DataInputStream(bis);
		try {
			msgArr[0] = dis.readUTF();
			msgArr[1] = dis.readUTF();
		} catch (IOException e) {
			log.log("Error receiving the message");
			e.printStackTrace();
			return null;
		}

		return msgArr;
	}

	/**
	 * @param sa
	 *            this is the place that the server is responding to.
	 * @param messageType
	 *            this int indicates what type of message will be sent. 0 means
	 *            a hello response, where 1 means a goodbye response.
	 */
	public void sendMsg(SocketAddress sa, String messageType) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		try {
			dos.writeUTF(messageType);
			// Making the packet to send
			byte bytesToSend[] = bos.toByteArray();
			DatagramPacket responsePacket = new DatagramPacket(bytesToSend,
					bytesToSend.length, sa);

			// Sending the packet
			sock.send(responsePacket);
		} catch (IOException e) {
			log.log("There was an error responding to client senging a message back to clients message: \n"
					+ e);
		}

	}
	
	public void echoToAll(String messageToEcho){
		for(int i = 0; i <= handlers.size(); i++){
			sendMsg(handlers.get(i), messageToEcho);
		}
	}
	
	public void diconnectAClient(SocketAddress sa){
		for(int i = 0; i <= handlers.size(); i++){
			if(handlers.get(i).equals(sa))
				handlers.remove(i);
		}
	}
}
