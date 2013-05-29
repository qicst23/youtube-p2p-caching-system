package edu.upenn.cis455.youtube;

import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.NodeHandle;

public class P2PMessage implements Message{
	NodeHandle from;
	String content;
	String type = "PING";  //default msg type
	
	
	/**
	 * 
	 * @param from  nodeHandle: server-port pair; to respond directly 
	 * @param content content msg
	 */
	public P2PMessage(NodeHandle from, String content){
		this.from = from;
		this.content = content;
	}
	
	public P2PMessage(String msgType) {
		type = msgType;
	}

	public int getPriority(){
		return 0;
	}
}
