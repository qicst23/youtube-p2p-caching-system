package edu.upenn.cis455.youtube;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.w3c.dom.NodeList;

import rice.p2p.commonapi.Node;


/**
 * Servlet implementation class UserInterfaceServlet
 */
public class YouTubeSearch extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/*------------------------------doGet----------------------------------------*/

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		out.println("<html>");
		setStyle(out);

		/** Show Search Form */
		out.println("<body><form action='youtube' method = 'POST' >");
		out.println("<h1>Youtube Search and Caching</h1>");
		out.println("<p>cis555-hw3 | Yayang Tian | yaytian@cis.upenn.edu</p><br>"); 
		out.println("<label>Query <span class='hint'>youtube resources</span></label>");
		out.println("<input type='text' id='search' name='search' value='" + "' />");
		out.println("<br><input type='submit' name='login' class='btn' value='search'/>");
		out.println("<input type='hidden' name='page' class='btn' value='1'/>");
		out.println("</form></body></html>");
	}

	/*------------------------------doPost----------------------------------------*/

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// get query from input form
		String query = request.getParameter("search");
		if(query == null || query == "") 
			doGet(request, response);
		query = URLDecoder.decode(query, "UTF-8");
		// get P2P URL from xml params
		String server = getServletConfig().getServletContext().getInitParameter("cacheServer");
		String serverPort = getServletConfig().getServletContext().getInitParameter("cacheServerPort");
		URL serverURL = new URL("http://" + server + ":" + serverPort);
		
		System.out.println("url: " + "http://" + server + ":" + serverPort);
		//		URL serverURL = new URL("http://158.130.213.1:10001"); //####### Change back before submission!
		//		try {
		// create a SOAP msg, send to P2P ring, and get response
		//			SOAPMessage wsResponse = sendSOAPMessage(query, serverURL);

		/** Alternatively, create a REST msg, send to P2P ring, and get response */ 
		String WSDLResult = sendRESTMessage(query, serverURL);

		/** parse incoming SOAP msg into HTML result */
		String htmlResult = getHTMLFromWSDL(WSDLResult);

		/** print Search Result */
		PrintWriter out = response.getWriter();
		out.println("<html><body>");
		setStyle(out);
		out.println(htmlResult);
		out.println("<a href='youtube'>Query again</a>");
		out.println("</body></html>");

		//		} catch (SOAPException e) {
		//			e.printStackTrace();
		//		}

	}

	/*----------------------send & respond & parse SOAP message----------------*/

	/**
	 * @param query the input of the user
	 * @return SOAP message which is good for cross-platform web service 
	 */
	public SOAPMessage sendSOAPMessage(String query, URL serverURL) throws SOAPException{

		/** create a SOAP msg from query */
		SOAPMessage wsRequest = MessageFactory.newInstance().createMessage();

		// SOAP params
		SOAPPart part = wsRequest.getSOAPPart();
		SOAPEnvelope envelope = part.getEnvelope();
		SOAPBody body = envelope.getBody();

		//Name: a local name, a namespace prefix, and a namesapce URI.
		Name name = envelope.createName("query", "yyns", "http://cis555.co.nf/");
		SOAPElement elem = body.addChildElement(name);
		elem.addTextNode(query);
		//		body.addChildElement(envelope.createName("query", "yyns", "http://cis555.co.nf/")).addTextNode(query);  //###
		wsRequest.saveChanges();

		/** send SOAP to P2P ring*/
		// get response
		SOAPConnection conn = SOAPConnectionFactory.newInstance().createConnection();
		SOAPMessage wsResponse = conn.call(wsRequest, serverURL);
		conn.close();
		return wsResponse;
	}

	/**
	 * @param wsResponse the web service response from P2P ring 
	 * @return html format to show users
	 */
	String parseSOAPIntoHTML(SOAPMessage wsResponse) throws SOAPException{
		StringBuffer result = new StringBuffer();
		SOAPPart part = wsResponse.getSOAPPart();
		SOAPEnvelope envelope = part.getEnvelope();
		SOAPBody body = envelope.getBody();
		Iterator<Node> iter = body.getChildElements(envelope.createName("response", "yyns", "http://cis555.co.nf/"));

		NodeList childNodes = ((org.w3c.dom.Node) iter.next()).getChildNodes();
		for(int i = 0; i <  childNodes.getLength(); i ++){
			result.append(childNodes.item(i).getTextContent()).append("<br>");
		}
		return result.toString();
	}

	String getHTMLFromWSDL(String wsdl){
		StringBuffer html = new StringBuffer(); 
		html.append("<table><tr><th>title</th><th>url</th><th>uploader</th><th>description</th>" +
				"<th>rating</th></tr>");
		String[] pieces = wsdl.split("}");
		String jsonReg  = "([^{:,\" ]+)(:)([ ]+\\\")([^\\\"]+)";
		Pattern p = Pattern.compile(jsonReg);

		for(String piece : pieces){
			Matcher m = p.matcher(piece);
			while(m.find()){
				String key = m.group(1);
				String value = m.group(4);
				if(key.equals("title")) html.append("<tr><td>" + value + "</td>");
				else if(key.equals("rating"))html.append("<td>"+ value + "</td></tr>");
				else html.append("<td>"+ value + "</td>");
			}
		}
		html.append("</table>");
		return html.toString();
	}

	/*------------------------------send / respond REST message -----------------*/

	String sendRESTMessage(String query, URL serverURL){
		String response = "";
		try {
			HttpURLConnection conn = (HttpURLConnection) serverURL.openConnection();

			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);
			String json = "{\"query\":" + query + "}";

			OutputStream output =conn.getOutputStream();
			output.write(json.getBytes());
			output.flush();

			// Get response from P2P server
			if(conn.getResponseCode() != 200)
				throw new RuntimeException("Error: " + conn.getResponseCode());
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = "";
			while((line = reader.readLine()) != null){
				response = response + line;
			}
			conn.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
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
