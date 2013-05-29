package edu.upenn.cis455.youtube;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.soap.SOAPMessage;

import com.google.gdata.util.ServiceException;


import edu.upenn.cis455.storage.DatabaseUtil;


/*
 * Every node has its own distinct daemon listening on a daemon port
 * for incoming queries
 */
public class P2PDaemonServer extends Thread{

	private Thread daemonThread;

	int daemonPort;
	YouTubeClient client;

	/**
	 * The server on top of each cache node
	 * @param port daemon port to communicate with servlet(user)
	 * @param client receive QUERY from daemonserver, whose request is from servlet(user)
	 */
	public P2PDaemonServer(int port, YouTubeClient client){
		this.daemonPort = port;
		this.client = client;

	}


	// nested Runnable class for usage of daemon thread

	public void run(){
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(daemonPort);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// constantly read request
		while(!serverSocket.isClosed()){
			try {
				System.out.println("\nDaemon thread waiting for WSDL query from servlet ...");

				Socket daemonSocket = serverSocket.accept();

				BufferedReader input = new BufferedReader(
						new InputStreamReader(daemonSocket.getInputStream()));

				// get query string from wrapped SOAP
				String query = getWSDLQuery(input);

				//query YouTube
				String WSDLResponse = client.searchVideo(query);

				// create a SOAP and respond
				respondRESTResult(WSDLResponse, daemonSocket);

				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	public String getWSDLQuery(BufferedReader input) throws IOException{
		// skip header
		int contentLength = 200;
		String line = "";
		while((line = input.readLine()) != null){
			if(!line.contains("POST") && !line.contains(":"))break;
			if(line.toLowerCase().startsWith("content-length")){
				contentLength = Integer.parseInt(line.split(":")[1].trim());
			}
		}

		// retrive query from SOAP/REST message
		StringBuffer serviceBody = new StringBuffer();
		int count = 0;
		while(input.ready()){
			serviceBody.append((char)input.read());
			count ++;
			if(count >= contentLength) break;
		}
		String query = serviceBody.toString().split(":")[1];
		query = query.substring(0, query.length() - 1);
		return query;
		//json retrival

		// soap retriveal
		//			String queryReg = "(Body.+\\>)(.+)(\\<\\/.+ns)";
		//			Pattern queryPattern = Pattern.compile(queryReg);
		//			Matcher queryMatcher = queryPattern.matcher(soapBody.toString());
		//			while(queryMatcher.find()){
		//				query = queryMatcher.group(2);
		//				System.out.println("[query]\t" + query);
		//			}
		//			return query;


	}


	void respondRESTResult(String WSDLResponse, Socket daemonSocket){
		try {
			String contentLength = "Content-Length" + WSDLResponse.length() + "\r\n"; 
			String date = "Date: " + new Date() + "\r\n";
			OutputStream output = daemonSocket.getOutputStream();
			output.write("HTTP/1.1 200 OK\r\n".getBytes());
			output.write(date.getBytes());
			output.write("Host: 158.130.213.1:10001\r\n".getBytes());
			output.write("Content-Type: application/json\r\n".getBytes());
			output.write(contentLength.getBytes());
			output.write("Connection: close\r\n\r\n".getBytes());
			output.write(WSDLResponse.getBytes());
			output.flush();
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}



