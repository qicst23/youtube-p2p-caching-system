package edu.upenn.cis455.youtube;

import edu.upenn.cis455.storage.DatabaseUtil;
import rice.p2p.commonapi.Application;
import rice.p2p.commonapi.Endpoint;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.Node;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.RouteMessage;

public class P2PApplication implements Application{

	/** to disable PING PONG messages, set logOn to false */
	public static boolean logPingPongOn = true;
	public static boolean logOn = true;
	
	NodeFactory nodeFactory;
	Node node;
	Endpoint endPoint;

	/**
	 * Take Node responsible for the app, register itself.
	 * @param nodeFactory
	 */
	P2PApplication(NodeFactory nodeFactory){
		this.nodeFactory = nodeFactory;
		this.node = nodeFactory.getNode();
		this.endPoint = node.buildEndpoint(this, "YouTubeApp");
		this.endPoint.register();  //endpoint registered
		
		// send Ping every 3 seconds. the first delay is 1 second
		endPoint.scheduleMessage(new P2PMessage("START_PING"), 3000, 3000);

	}
	void cacheNodeInfo(int maxSize, int maxTime){
		DatabaseUtil.addNode(node.getId().toString(), 0, maxSize * 1000, maxTime);
		DatabaseUtil.close();
	}
	
	void sendQuery(String keyword){
		Id destId = nodeFactory.getIdFromString(keyword);
		sendMessage("QUERY", keyword, destId, node.getLocalNodeHandle());
	}
	void respondResult(String result){
		sendMessage("RESULT", result, null, node.getLocalNodeHandle());
	}

	/** send msg back or to another endPoint */
	void sendMessage(String msgType, String msgToSend, Id idToSendTo, NodeHandle from){
		P2PMessage msg =  new P2PMessage(node.getLocalNodeHandle(), msgToSend);
		msg.type = msgType;
		// send back directly
		if(idToSendTo == null && from != null)
			endPoint.route(null, msg, from);
		// send to another node
		else{
			endPoint.route(idToSendTo, msg, null);
		}
	}

	/** process/forward the received msg */
	public void deliver(Id id, Message message){
		P2PMessage msg = (P2PMessage)message;

		String msgType = msg.type;
//		if(!msgType.equals("START_PING"))
//			log("\nReceived message " + msg.content + " from" + msg.from);
		
		if(msgType.equals("START_PING")){
			Id destRandId = nodeFactory.nidFactory.generateNodeId();
			logPingPong("Sending PING to " + destRandId.toString());
			sendMessage("PING", "Sending PING!", destRandId, node.getLocalNodeHandle());
		}
		if(msgType.equals("PING")){
			logPingPong("Received PING to " + id.toString() + "from node " + msg.from.getId().toString() +
					"[port: " + msg.from.toString().split(":")[2] + " Returning PONG.");
			sendMessage("PONG", "Recieved PING! PONG back!", null, msg.from);
		}
		if(msgType.equals("PONG")){
			logPingPong("Received PONG from node " + msg.from.getId().toString() + 
					"[port: " + msg.from.toString().split(":")[2] + "\n");
		}
		if(msgType.equals("QUERY")){
			String query = msg.content;
//			log("\n\n --- [QUERY in application]");
		}
		
		if(msgType.equals("RESULT")){
			String query = msg.content;
//			log("\n\n --- [QUERY result]");
		}
	}

	public void update(NodeHandle handle, boolean joined){}

	public boolean forward(RouteMessage routeMessage){
		return true;
	}
	
	/**  System.out.println */
	private static void log(Object value){
		if(logOn)
			System.out.println(String.valueOf(value));
	}
	private static void logPingPong(Object value){
		if(logPingPongOn)
			System.out.println(String.valueOf(value));
	}

}
