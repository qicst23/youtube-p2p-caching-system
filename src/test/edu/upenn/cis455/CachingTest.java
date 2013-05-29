package test.edu.upenn.cis455;

import java.net.MalformedURLException;
import java.net.URL;

import edu.upenn.cis455.youtube.P2PCache;
import edu.upenn.cis455.youtube.YouTubeClient;
import edu.upenn.cis455.youtube.YouTubeSearch;
import junit.framework.TestCase;

public class CachingTest extends TestCase {
	
	/**
	 *  Test WSDL commnication 
	 */
	public void testWSDL() {
		// Change dbRoot as you like
		String dbRoot = "/Users/Alantyy/Documents/database";
		YouTubeClient client = new YouTubeClient(dbRoot);
		
		String result = client.searchVideo("apple");
		System.out.println(result);

	}

}
