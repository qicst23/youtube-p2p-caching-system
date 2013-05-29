package edu.upenn.cis455.youtube;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import edu.upenn.cis455.storage.DatabaseUtil;
import edu.upenn.cis455.storage.P2PNode;
import edu.upenn.cis455.storage.YouTubeResult;
import rice.p2p.commonapi.Id;


/**
 * Servlet implementation class ManagementServlet
 */
public class ManagementServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/*------------------------------doGet----------------------------------------*/

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		StringBuffer info = new StringBuffer();
		out.println("<html>");
		setStyle(out);

//		String dbRoot = request.getServletContext().getInitParameter("BDBstore");
		String dbRoot = "/Users/Alantyy/Documents/database";
		DatabaseUtil.setupContext(dbRoot);
		Id id;
		
		
		ArrayList<String> ids = DatabaseUtil.getNodeIds();
		int size = ids.size();
		System.out.println("[size of nodes]\t" + size );
		// print nodes information
		out.println("<body><form action='youtube' method = 'POST' >");
		out.println("<h1>Youtube P2PNodes Management</h1>");
		out.println("<p>cis555-hw3-extra 6.3 | Yayang Tian | yaytian@cis.upenn.edu</p><br>"); 
		if(size == 0) {
			info.append("There is no ring yet.");
		}
		if(size == 1){ 
			info.append(getEntry(ids.get(0)));
		}
		if(size == 2){
			info.append(getEntry(ids.get(0)));
			info.append(getEntry(ids.get(1)));
		}
		if(size >= 3){
			info.append(getEntry(ids.get(1)));
			info.append(getEntry(ids.get(2)));
			info.append(getEntry(ids.get(3)));
		}
		out.println(info.toString());
		out.println("</form></body></html>");
	}
	
		
	public String getEntry(String id){
		Date today = new Date(System.currentTimeMillis());
		P2PNode node = DatabaseUtil.getNode(id.toString());
		int maxTime = node.getMaxTime();
		StringBuffer info = new StringBuffer();
		ArrayList<YouTubeResult> results = DatabaseUtil.getResultsFromIdString(id);
		System.out.println("[size of result]\t" + results.size());
		info.append("<br><br><tr><td>nodeId</td><td>" + id + "</td></tr>");
		for(YouTubeResult result : results){
			info.append("<tr><td>keyword (filename)</td><td>" + result.getQuery() + "</td></tr>");
			info.append("<tr><td>result size</td><td>" + result.getQuery() + "</td></tr>");
			info.append("<tr><td>query lastCached</td><td>" + result.getLastCached().toString() + "</td></tr>");
			long expireTime =maxTime - (today.getTime() - result.getLastCached().getTime())/1000;
			info.append("<tr><td>query expires in(seconds)</td><td>" + expireTime + "</td></tr>");
		}
		return info.toString();
	}
	/*------------------------------doPost----------------------------------------*/

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// get query from input form
		String query = request.getParameter("search");
		if(query == null || query == "") 
			doGet(request, response);
		query = URLDecoder.decode(query, "UTF-8");
		// get P2P URL from xml params
		//		String server = request.getServletContext().getInitParameter("cacheServer");
		//		String serverPort = request.getServletContext().getInitParameter("cacheServerPort");
		//		System.out.pr	intln("[server url]\t" + "http://" + server + ":" + serverPort);
		//		URL serverURL = new URL("http://" + server + ":" + serverPort);

		URL serverURL = new URL("http://158.130.213.1:10001"); //####### Change back before submission!
		//		try {
		// create a SOAP msg, send to P2P ring, and get response
		//			SOAPMessage wsResponse = sendSOAPMessage(query, serverURL);

		/** print Search Result */
		PrintWriter out = response.getWriter();
		out.println("<html><body>");
		setStyle(out);
		out.println("<a href='youtube'>Back</a>");
		out.println("</body></html>");

		//		} catch (SOAPException e) {
		//			e.printStackTrace();
		//		}

	}



	/*------------------------------Others----------------------------------------*/

	public void destroy() {
		System.out.println("XPathServlet has destroyed!");
	}

	public void setStyle(PrintWriter out){
		out.println("<head><style>");

		// Default font & container
		out.println("body {font-family: 'Droid Sans', 'Trebuchet Ms', verdana; font-size: 16px;}");
		out.println("p,h1,form,button {border: 0; margin: 0; padding: 0;}");

		// label text
		out.println("form {background-color: #E7F3FF; width: 600px; height: 150px; padding: 30px; margin-left:auto; margin-right:auto;text-align:left;}");
		out.println("form label {display: block; font-weight: bold; text-align: right; width: 120px; float: left;}");
		out.println("form .hint {color: #666666; display: block; font-size: 12px; font-weight: normal; " +
				"text-align: right; width: 120px;}");

		//table
		out.println("table{border-collapse: collapse; margin-left: auto; margin-right: auto; width=100%;} ");
		out.println("th{padding: 3 15 3 15px; border-bottom: 2px solid #b7ddf3; color: #666666}");
		out.println("td{padding: 3 15 3 15px; border-bottom: 2px solid #b7ddf3; }");

		// input box & submit button
		out.println("input {float: left; padding: 4px 2px; font-size: 16px; " +
				"width: 250px; height: 30px; margin: 2px 0 20px 10px; overflow: auto;}");
		out.println("input .xpath{height: 300px;}");
		out.println("input.btn {margin-left: 20px;width: 160px; height: 30px; " + 
				"background: #666666; text-align: center; color: #FFFFFF; font-size: 16px;");

		out.println("</style></head>");
	}

	/**  System.out.println */
	private static void log(Object value){
		System.out.println(String.valueOf(value));
	}

}
