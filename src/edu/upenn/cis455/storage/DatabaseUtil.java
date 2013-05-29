package edu.upenn.cis455.storage;

import java.io.File;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import rice.p2p.commonapi.Id;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;




public class DatabaseUtil {

	private static Environment environment = null;
	private static EntityStore store = null;

	private static PrimaryIndex<String, P2PNode> P2PNodeIndex;
	private static PrimaryIndex<String, YouTubeResult> YouTubeResultIndex;

	public static String dbRoot = null;
	private static File dir;

	/*------------------------------- Setup ----------------------------------*/

	public static void setupContext(String root){

		/** if same set up, return */
		if(environment != null && environment.isValid() && root.equals(dbRoot)) {
			return;
		}

		/**  find dir to store databse */
		dbRoot = root;

		if(dbRoot == null)
			dbRoot = System.getProperty("user.dir") + "/database";

		dir = new File(dbRoot);
		if(!dir.exists()){
			dir.mkdir();
			System.out.print("[new db path]\t");
		}
		System.out.print("  [db] " + dbRoot);

		/** config and setup enviroment */  
		EnvironmentConfig envConf = new EnvironmentConfig();
		StoreConfig storeConf = new StoreConfig();

		envConf.setAllowCreate(true);
		storeConf.setAllowCreate(true);

		/* OFFICALLY launch environment */
		environment = new Environment(dir, envConf);
		store = new EntityStore(environment, "EntityStore", storeConf);

		YouTubeResultIndex = store.getPrimaryIndex(String.class, YouTubeResult.class);
		P2PNodeIndex = store.getPrimaryIndex(String.class, P2PNode.class);
	}

	public static void close(){
		try{
			if(store != null)
				store.close();
			if(environment != null)
				environment.close();
		}catch(DatabaseException e){
			System.out.println("Cannot close database");
		}
	}

	/*---------------------------Extra: P2PNode --------------------------------*/


	public static void addNode(String nodeId, int size, int maxSize, int maxTime){
		P2PNode node = new P2PNode(nodeId, size, maxSize, maxTime);
		P2PNodeIndex.put(node);

	}
	public static void updateNodeSize(String nodeId, int addSize){
		P2PNode node = P2PNodeIndex.get(nodeId);
		int newSize = node.getStoredSize() + addSize;
		int maxSize = node.getMaxSize();
		int maxTime = node.getMaxTime();
		addNode(nodeId, newSize, maxSize, maxTime);  // overlap the original one
	}

	public static P2PNode getNode(String nodeId){
		return P2PNodeIndex.get(nodeId);
	}
	public static int getMaxSize(String nodeId){
		return P2PNodeIndex.get(nodeId).getMaxSize();
	}

	public static int getMaxTime(String nodeId){
		return P2PNodeIndex.get(nodeId).getMaxTime();
	}


	public static ArrayList<String> getNodeIds(){
		ArrayList<String> ids = new ArrayList<String>();

		EntityCursor<P2PNode> cursor = P2PNodeIndex.entities();
		try{
			Iterator<P2PNode> iter = cursor.iterator();
			while(iter.hasNext()){
				ids.add(iter.next().getNodeId());
			}
		}finally{
			cursor.close();
		}
		return ids;
	}

	/*------------------------------- YouTubeResult --------------------------------*/

	public static ArrayList<YouTubeResult> getResultsFromIdString(String idString){
		ArrayList<YouTubeResult> results = new ArrayList<YouTubeResult>();
		EntityCursor<YouTubeResult> cursor = YouTubeResultIndex.entities();
		try{
			Iterator<YouTubeResult> iter = cursor.iterator();
			while(iter.hasNext()){
				YouTubeResult one = iter.next();
				if(one.getNodeId().equals(idString))
					results.add(one);
			}
		}finally{
			cursor.close();
		}
		return results;
		
		
	}
	public static void cacheYouTube(YouTubeResult video){

		// if only one
		// about this result
		int aSize = video.getResult().length();

		// about the capacity of this node
		String nodeId = video.getNodeId();
		P2PNode node = getNode(nodeId);

		// Check size rule
		int storedSize = node.getStoredSize();
		if(storedSize != 0){
			int available = node.getMaxSize() - storedSize - aSize;
			System.out.println("[available size]\t" + available);
			if(available < 0){
				System.out.println("[status] The oldest one is deleted because storage exceeds " + 
						(-available) + "megabytes.");
				deleteOldestQuery();
				YouTubeResultIndex.put(video);
				updateNodeSize(nodeId, aSize);
			}
		}
		// if no result stored, don't check size constraint
		else{
			YouTubeResultIndex.put(video);
			updateNodeSize(nodeId, aSize);
		}
		store.sync();
	}


	public static void deleteOldestQuery(){

		String oldestQuery = "";
		Date oldestDate = new Date(System.currentTimeMillis());

		EntityCursor<YouTubeResult> cursor = YouTubeResultIndex.entities();
		try{
			Iterator<YouTubeResult> iter = cursor.iterator();
			while(iter.hasNext()){
				YouTubeResult result = iter.next();
				Date date = result.getLastCached();
				// find oldest query
				if(date.before(oldestDate)){
					oldestQuery = result.getQuery();
				}

			}
		}finally{
			cursor.close();
		}
		System.out.println("[oldest query deleted]\t" + oldestQuery);
		YouTubeResultIndex.delete(oldestQuery);
	}


	public static boolean containsResult(String keyword){
		if(!YouTubeResultIndex.contains(keyword)) return false;

		Date last = YouTubeResultIndex.get(keyword).getLastCached();
		Date today = new Date(System.currentTimeMillis());
		long diff = (today.getTime() - last.getTime())/1000;
		int maxTime = getNodeFromKeyword(keyword).getMaxTime();

		// if too old, discard it
		if(diff > maxTime){
			System.out.println("[status] Original one is deleted because it's too old. " +
					"time diff = " + diff + "seconds.");
			deleteYouTube(keyword);
			return false;
		}
		return true;
	}

	public static P2PNode getNodeFromKeyword(String keyword){
		String nodeId = getYouTube(keyword).getNodeId();
		return getNode(nodeId);
	}


	// return a json result of REST communication
	public static YouTubeResult getYouTube(String keyword){
		return YouTubeResultIndex.get(keyword);
	}

	public static void deleteYouTube(String keyword){
		YouTubeResultIndex.delete(keyword);
	}





	/*------------------------------- Util -----------------------------------*/
	public static Date getDateFromString(String str){
		Date date = null;
		String[] format = {"EEEEE, dd-MMM-yy HH:mm:ss zzz",
				"EEE MMM dd HH:mm:ss yyyy", 
				"EEE, dd MMM yyyy HH:mm:ss zzz",
		"EEE, dd MMM yyyy HH"};
		SimpleDateFormat parser = new SimpleDateFormat(format[0]);
		try {
			date = parser.parse(str);
		} catch (ParseException e) {
			parser = new SimpleDateFormat(format[1]);
			try {
				date = parser.parse(str);
			} catch (ParseException e1) {
				parser = new SimpleDateFormat(format[2]);
				try {
					date = parser.parse(str);
				} catch (ParseException e2) {
					parser = new SimpleDateFormat(format[3]);
					try {
						date = parser.parse(str);
					} catch (ParseException e3) {
						System.out.println("Bad date format.");
						return null;
					}
				}
			}
		}
		return date;
	}

}

