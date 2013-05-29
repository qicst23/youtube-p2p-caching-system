package test.edu.upenn.cis455;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gdata.client.youtube.YouTubeQuery;
import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.extensions.Rating;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.google.gdata.util.ServiceException;
import com.sun.tools.javac.util.Context;

import edu.upenn.cis455.youtube.P2PCache;
import edu.upenn.cis455.youtube.YouTubeSearch;

import junit.framework.TestCase;
public class WebServiceTest extends TestCase {
	/** 
	 * Test parsing JSON body 
	 */
	public void testJSONSplit(){
		System.out.println("[debug]");
		String json = "{title: \"Yayang won the Google award\"; uploader: \"Yayang\"}";
		String[] pieces = json.split("}");
		String jsonReg  = "([^{:,\" ]+)(:)([ ]+\\\")([^\\\"]+)";
		Pattern p = Pattern.compile(jsonReg);
		Matcher m = p.matcher(pieces[0]);
		while(m.find()){
			assertTrue(m.group(1) != null);
			assertTrue(m.group(4) != null);
		}
	}

	/** 
	 * test YouTube API 
	 */
	public void testYouTubeAPI() throws IOException, ServiceException{
		String keyword = "iphone";
		YouTubeService service = new YouTubeService("Yayang Tian", "AI39si4BMI10-cEQjlaTcmFxU-1AwJJW86R-Ocfra19KIbqnVG7K18vnN0_5XUrrWEObNa69LIqmkie5r4z4bo-XsfU-mfz3Kg");
		YouTubeQuery query = new YouTubeQuery(new URL("http://gdata.youtube.com/feeds/api/videos"));
		query.setFullTextQuery(keyword);
		query.setOrderBy(YouTubeQuery.OrderBy.VIEW_COUNT);
		query.setSafeSearch(YouTubeQuery.SafeSearch.NONE);
		query.setMaxResults(20);
		VideoFeed videoFeed = service.query(query, VideoFeed.class);

		for(VideoEntry ve : videoFeed.getEntries()){
			//title
			assertTrue(ve.getTitle().getPlainText() != null);
			// content
			YouTubeMediaGroup mg = ve.getMediaGroup();
			assertTrue(mg.getPlayer().getUrl() != null);
			assertTrue(mg.getUploader() != null);
			assertTrue(mg.getDescription().getPlainTextContent() != null);
			Rating rating = ve.getRating();
			if(rating != null){ String rate = rating.getAverage().toString();
			assertTrue(rate != null);
			}
		}
	}
	
	/**
	 *  Test PING commnication 
	 */
	public void testPing(){
		P2PCache cache = new P2PCache(9001, "158.130.213.1", 9001, 10001, "/Users/Alantyy/Documents/database", 30, 30);
	}
	/**
	 *  Test WSDL commnication 
	 */
	public void testWSDL() {
		// Three nodes
		P2PCache cache = new P2PCache(9001, "158.130.213.1", 9001, 10001, "/Users/Alantyy/Documents/database", 30, 30);
		
		YouTubeSearch servlet = new YouTubeSearch();
		URL serverURL = null;
		try {
			serverURL = new URL("http://158.130.213.1:10001");
		} catch (MalformedURLException e) {
		} 
		String keyword = "apple";
//		String WSDLResult = servlet.sendRESTMessage(keyword, serverURL);
//		String htmlResult = servlet.getHTMLFromWSDL(WSDLResult);
//		System.out.println(htmlResult);
//		assertTrue(htmlResult != null);
	}

}
