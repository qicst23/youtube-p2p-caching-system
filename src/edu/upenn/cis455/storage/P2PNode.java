package edu.upenn.cis455.storage;

import java.util.Date;

import rice.pastry.Id;



import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class P2PNode {
	
	public P2PNode(){}
	
	@PrimaryKey
	private String nodeId;
	
	private int storedSize;
	private int maxSize;
	private int maxTime;
	
	public P2PNode(String nodeId, int storedSize, int maxSize, int maxTime){
		this.nodeId = nodeId;
		this.maxSize = maxSize;
		this.storedSize = storedSize;
		this.maxTime = maxTime;
	}
	
	public String getNodeId(){return nodeId;}
	public int getStoredSize(){return storedSize;}
	public int getMaxSize(){return maxSize; }
	public int getMaxTime(){return maxTime;}
	
}
