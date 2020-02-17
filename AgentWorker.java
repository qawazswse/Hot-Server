package version_0;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

class AgentWorker extends Thread {	// the agent worker started by Agent listener thread
	
	Socket sock;
	AgentHolder parentAgentHolder;
	int localPort;
	
	AgentWorker (Socket s, int prt, AgentHolder ah) {
		sock = s;
		localPort = prt;
		parentAgentHolder = ah;
	}
	public void run() {

		PrintStream out = null;
		BufferedReader in = null;
		String NewHost = "localhost";
		int NewHostMainPort = 1565;		
		String buf = "";
		int newPort;
		Socket clientSock;
		BufferedReader fromHostServer;
		PrintStream toHostServer;
		
		try {
			out = new PrintStream(sock.getOutputStream());
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			
			String inLine = in.readLine();
			StringBuilder htmlString = new StringBuilder();
			
			System.out.println();
			System.out.println("Request line: " + inLine);
			
			if(inLine.indexOf("migrate") > -1) {	// if the user put "migrate" into the bar and submitted it
				
				clientSock = new Socket(NewHost, NewHostMainPort);
				fromHostServer = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
				toHostServer = new PrintStream(clientSock.getOutputStream());
				toHostServer.println("Please host me. Send my port! [State=" + parentAgentHolder.agentState + "]");
				toHostServer.flush();
				
				for(;;) {	// a for ever loop until HostServer send a message which contain Port
					buf = fromHostServer.readLine();
					if(buf.indexOf("[Port=") > -1) {
						break;
					}
				}
				
				String tempbuf = buf.substring( buf.indexOf("[Port=")+6, buf.indexOf("]", buf.indexOf("[Port=")) );
				newPort = Integer.parseInt(tempbuf);
				System.out.println("newPort is: " + newPort);
				
				htmlString.append(AgentListener.sendHTMLheader(newPort, NewHost, inLine));
				htmlString.append("<h3>We are migrating to host " + newPort + "</h3> \n");
				htmlString.append("<h3>View the source of this page to see how the client is informed of the new location.</h3> \n");
				htmlString.append(AgentListener.sendHTMLsubmit());

				System.out.println("Killing parent listening loop.");
				ServerSocket ss = parentAgentHolder.sock;	// for closing the ServerSocket of the Parent agent holder
				
				ss.close();	//close the port
				
				
			} else if(inLine.indexOf("person") > -1) {	// if the request contains "person" then add words include agent state of a parent agent holder
				parentAgentHolder.agentState++;		// agent state of parent agent holder plus one each time
				htmlString.append(AgentListener.sendHTMLheader(localPort, NewHost, inLine));
				htmlString.append("<h3>We are having a conversation with state   " + parentAgentHolder.agentState + "</h3>\n");
				htmlString.append(AgentListener.sendHTMLsubmit());

			} else {	// the request is invalid
				htmlString.append(AgentListener.sendHTMLheader(localPort, NewHost, inLine));
				htmlString.append("You have not entered a valid request!\n");
				htmlString.append(AgentListener.sendHTMLsubmit());		
				
		
			}
			AgentListener.sendHTMLtoStream(htmlString.toString(), out);
			sock.close();
			
			
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
	
}
