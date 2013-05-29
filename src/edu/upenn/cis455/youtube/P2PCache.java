package edu.upenn.cis455.youtube;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import edu.upenn.cis455.storage.DatabaseUtil;

import rice.environment.Environment;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.PastryNodeFactory;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.standard.RandomNodeIdFactory;

public class P2PCache {

	/** From five input params */
	int localBindPort;
	String bootIP;
	int bootPort;
	int daemonPort;
	String dbRoot;

	/** Can produce node */
	NodeFactory nodeFactory;

	/** launch application on nodes */
	P2PApplication app;

	/** daemon server listening on incoming msgs */
	P2PDaemonServer server;

	/**  interact with database */
	YouTubeClient client;

	/**
	 * @param bindPort local bind port of node
	 * @param bootAddr Store (IP address, port) of boot node to identify ring
	 * @param daemonPort used to listen to incoming query
	 * @param dbRoot the location of databas
	 */
	public P2PCache(int bindPort, String bootIP, int bootPort, int dmPort, 
			String dbRoot, int maxStorage, int expireTime){
		this.localBindPort = bindPort;
		this.bootIP = bootIP;
		this.bootPort = bootPort;
		this.daemonPort = dmPort;
		this.dbRoot = dbRoot;

		// When creating new node, must provide bootAddress object as an entrance
		InetAddress inetAddr = null;
		try {
			inetAddr = InetAddress.getByName(bootIP);
		} catch (UnknownHostException e) {
			System.out.println(e.getMessage());
			System.exit(-1);
		}
		InetSocketAddress bootSocketAddr = new InetSocketAddress(inetAddr, bootPort);

		/** Network layer */

		// start a Pastry node
		DatabaseUtil.setupContext(dbRoot);
		nodeFactory = new NodeFactory(localBindPort, bootSocketAddr);
		P2PApplication app = new P2PApplication(nodeFactory);
		app.cacheNodeInfo(maxStorage, expireTime);

		/** Application layer */

		// init client class
		YouTubeClient client = new YouTubeClient(dbRoot);
		client.specifyRouter(app);
		
		// wait for QUERY message - talk to cli
		P2PDaemonServer server = new P2PDaemonServer(dmPort ,client);
		server.run();


	}

	public static void main(String[] args) throws Exception{
		String dbRoot = "";
		int maxStorage = 10000000;	 // MB
		int expireTime = 60000000;   // seconds 

		try{
			// Five input arguments
			int localBindPort = Integer.parseInt(args[0]);
			String bootIP = args[1];
			int bootPort = Integer.parseInt(args[2]);
			int daemonPort = Integer.parseInt(args[3]);

			//Extra credit 1 & 2
			if(args.length > 4) dbRoot = args[4];
			if(args.length > 5) maxStorage = Integer.parseInt(args[5]);
			if(args.length > 6) expireTime = Integer.parseInt(args[6]);

			System.out.println("[bindAddr] " + "http://" + "158.130.213.1" + ":" + localBindPort + 
					"  [daemonAddr] " + "http://" + "158.130.213.1" + ":" + daemonPort + 
					"  [maxStorage] "+ maxStorage + " MB  [maxExpireTime] " + expireTime + " seconds.");
			P2PCache p2pCache = new P2PCache(localBindPort, bootIP, bootPort, daemonPort, 
					dbRoot, maxStorage, expireTime);
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("[Usage] java -cp youtube.jar:target/WEB-INF/lib/* edu/upenn/cis455/youtube/P2PCache " +
					"<localBindPort> <bootIP> <bootPort> <daemonPort> <databasePath> <maxStorageSize(MB)>? <contentExpirationTime(seconds)>?");
			System.out.println("[Example] java -cp youtube.jar:target/WEB-INF/lib/* edu/upenn/cis455/youtube/P2PCache " +
					"9001 spec01 9001 10001 database 1 20");
		}


	}
}

