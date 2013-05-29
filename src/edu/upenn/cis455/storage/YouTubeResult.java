package edu.upenn.cis455.storage;

import java.util.Date;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity
public class YouTubeResult {
	
	public YouTubeResult(){}
	
	@PrimaryKey
	private String query;

	private Date cachedDate;
	private String nodeId;
	private String result;
	
	public YouTubeResult(String query, String nodeId, Date cachedDate, String result){
		this.query = query;
		this.nodeId = nodeId;
		this.cachedDate = cachedDate;
		this.result=  result;
	}
	
	public String getQuery(){return query;}
	public String getNodeId(){return nodeId; }
	public Date getLastCached(){return cachedDate;}
	public String getResult(){return result;}
}
