/*
   Scott Campbell
   CSE383-f15

   Program 1 client


   run this by java campbest.client localhost 4000 for example
 */
import java.net.*;
import java.io.*;

public class Client extends Thread {
	int port;
	String host;
	String message;
	DatagramSocket sock;
	InetSocketAddress serverAddress;
	java.util.Scanner input;
	static boolean run = true;

	//takes two agruments - host, port
	public static void main(String a[]) {
		if (a.length != 2) {
			System.err.println("host port message");
			System.exit(-1);
		}
		int port = 0;
		try {
			port = Integer.parseInt(a[1]);
		} catch (Exception err) {
			System.err.println("Could not parse arguemnt");
			System.exit(-1);
		}

		try {
			new Client(a[0],port).Main();
		} catch (IOException err) {
			System.err.println("Could not start client");
			System.exit(-1);
		}
	}

	public Client(String h, int port) throws IOException {
		this.port = port;
		host=h;

		sock = new DatagramSocket();
		input = new java.util.Scanner(System.in);

		serverAddress = new InetSocketAddress(host,port);
	}

	//constructor for thread
	public Client(DatagramSocket ds, InetSocketAddress sa) {
		sock = ds;
		serverAddress = sa;
	}

	public void Main() {
		//send message to server and wait for response
		try {
			//register
			sendMessage("HELLO","");

			//get response
			//set timeout for these
			sock.setSoTimeout(5000);
			String msg[] = getMessage();
			if (!"HELLO-RESPONSE".equals(msg[0])) {
				System.out.println("Invalid message from server - exiting");
				return;
			}
			sock.setSoTimeout(0);

			//create listener thread to receive messages from server
			new Client(sock,serverAddress).start();


			while(run) {
				//get input from keyboard
				System.out.println("Enter message to send:");
				String fromUser = input.nextLine();
				if ("QUIT".equals(fromUser)) {
					sendMessage("GOODBYE","");
					run=false;
					return;	//exit
				}
				//send to server
				sendMessage("MESSAGE",fromUser);

			}

		} catch (IOException err) {
			System.err.println("Error on communicating - exiting");
			System.exit(-1);
		}
	}

	//thread entry point
	public void run() {	
		while(run) {
			try {
				//get message
				String msg[] = getMessage();
				if ("GOODBYE-RESPONSE".equals(msg[0])) {
					run = false;
				}

				if ("MESSAGE".equals(msg[0])) {
					System.out.println("Got Message - " + msg[1]);
				}

			} catch (IOException err) {
				System.err.println("Error on receive - exiting");
				run= false;
			}
		}
	}

	public void sendMessage(String msg,String msg1 )  throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeUTF(msg);
		dos.writeUTF(msg1);
		byte byteArray[] = bos.toByteArray();
		DatagramPacket sendMsg = new DatagramPacket(byteArray,byteArray.length,serverAddress);
		sock.send(sendMsg);
	}


	public String[] getMessage() throws IOException {
		String msg[] = new String[2];
		byte b[] = new byte[1024];
		DatagramPacket pkt= new DatagramPacket(b,b.length);
		sock.receive(pkt);
		ByteArrayInputStream bis= new ByteArrayInputStream(b);
		DataInputStream dis = new DataInputStream(bis);
		msg[0] = dis.readUTF();
		msg[1] = dis.readUTF();
		return msg;
	}
}