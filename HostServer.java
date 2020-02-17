package version_0;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HostServer {

public static int NextPort = 3000;	// initial port number is 3000, the first port will be 3001. As a static variable of this class, it will plus one each time a new HostServer started
	
	public static void main(String[] a) throws IOException {
		int q_len = 6;
		int port = 1565;	// the server port number
		Socket sock;
		
		ServerSocket servsock = new ServerSocket(port, q_len);
		System.out.println("John Reagan's DIA Master receiver started at port 1565.");
		System.out.println("Connect from 1 to 3 browsers using \"http:\\\\localhost:1565\"\n");

		while(true) {
			NextPort = NextPort + 1;	// each time a new HostServer started port number plus one
			sock = servsock.accept();	// listening for a new connection request
			System.out.println("Starting AgentListener at port " + NextPort);	
			new AgentListener(sock, NextPort).start();	// start the listener
		}
		
	}
}
