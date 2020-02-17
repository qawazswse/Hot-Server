package version_0;

import java.net.ServerSocket;

class AgentHolder {	// a class for initialization of an agentWorker thread. it holds a ServerSocket and an agentState number.
	ServerSocket sock;
	int agentState;
	AgentHolder(ServerSocket s) { sock = s;}
}
