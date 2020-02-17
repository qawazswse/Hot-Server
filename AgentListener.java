package version_0;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

class AgentListener extends Thread {
	Socket sock;
	int localPort;
	
	AgentListener(Socket As, int prt) {
		sock = As;
		localPort = prt;
	}
	int agentState = 0;	// the initial agent state number is 0
	
	public void run() {
		BufferedReader in = null;
		PrintStream out = null;
		String NewHost = "localhost";
		System.out.println("In AgentListener Thread");		
		try {
			String buf;
			out = new PrintStream(sock.getOutputStream());
			in =  new BufferedReader(new InputStreamReader(sock.getInputStream()));
			buf = in.readLine();
			
			if(buf != null && buf.indexOf("[State=") > -1) {	//see if the input stream has a state, if it has the parse and store it to tempbuf
				String tempbuf = buf.substring(buf.indexOf("[State=")+7, buf.indexOf("]", buf.indexOf("[State=")));
				agentState = Integer.parseInt(tempbuf);
				System.out.println("agentState is: " + agentState);
			}
			
			System.out.println(buf);
			StringBuilder htmlResponse = new StringBuilder();	//this string Builder is for the html response 
			htmlResponse.append(sendHTMLheader(localPort, NewHost, buf));	//the HTML header
			htmlResponse.append("Now in Agent Looper starting Agent Listening Loop\n<br />\n");
			htmlResponse.append("[Port="+localPort+"]<br/>\n");	// the port that this listener is listening
			htmlResponse.append(sendHTMLsubmit());
			sendHTMLtoStream(htmlResponse.toString(), out);	// show it on web browser
			ServerSocket servsock = new ServerSocket(localPort,2);	//requested maximum length of the queue of incoming connections is two.
			AgentHolder agenthold = new AgentHolder(servsock);
			agenthold.agentState = agentState;
			
			while(true) {	// run the AgentWorker with the corresponding agentState after the server socket accepted a connection request.
				sock = servsock.accept();
				System.out.println("Got a connection to agent at port " + localPort);
				new AgentWorker(sock, localPort, agenthold).start();
			}
		
		} catch(IOException ioe) {	// tell the user that either connection failed, or just killed listener loop for agent at this port
			System.out.println("Either connection failed, or just killed listener loop for agent at port " + localPort);
			System.out.println(ioe);
		}
	}
	
	static String sendHTMLheader(int localPort, String NewHost, String inLine) {	// put the header part of the corresponding HTML content in to a string and return it
		
		StringBuilder htmlString = new StringBuilder();

		htmlString.append("<html><head> </head><body>\n");
		htmlString.append("<h2>This is for submission to PORT " + localPort + " on " + NewHost + "</h2>\n");
		htmlString.append("<h3>You sent: "+ inLine + "</h3>");
		htmlString.append("\n<form method=\"GET\" action=\"http://" + NewHost +":" + localPort + "\">\n");
		htmlString.append("Enter text or <i>migrate</i>:");
		htmlString.append("\n<input type=\"text\" name=\"person\" size=\"20\" value=\"YourTextInput\" /> <p>\n");
		
		return htmlString.toString();
	}
	
	static String sendHTMLsubmit() {	// finish the tail of HTML string which sendHTMLheader() method returns
		return "<input type=\"submit\" value=\"Submit\"" + "</p>\n</form></body></html>\n";
	}
	
	static void sendHTMLtoStream(String html, PrintStream out) {	// send the request header and the HTML content to an out put stream
		
		out.println("HTTP/1.1 200 OK");
		out.println("Content-Length: " + html.length());
		out.println("Content-Type: text/html");
		out.println("");		
		out.println(html);
	}
	
}
