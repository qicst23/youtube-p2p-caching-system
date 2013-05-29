package edu.upenn.cis455.youtube;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

import com.google.gdata.client.youtube.YouTubeQuery;
import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.extensions.Rating;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.google.gdata.util.ServiceException;

import edu.upenn.cis455.storage.DatabaseUtil;
import edu.upenn.cis455.storage.YouTubeResult;

public class YouTubeClient {
	
	private String dbRoot = null;    // Application layer: database root
	private P2PApplication router;   // IP layer:  underlying router
	
	/**
	 * Must has one param for db Root
	 * @param dbRoot
	 */
	public YouTubeClient(String dbRoot){
		this.dbRoot = dbRoot;
	}
	/**
	 * Must has one for param for keyword
	 * @param keyword
	 * @return json WSDL result
	 */
	public String searchVideo(String keyword){
		DatabaseUtil.setupContext(dbRoot);
		String jsonResult = ""; 
		
		// maxSize & maxTime is implemented within database
		boolean hit = DatabaseUtil.containsResult(keyword);
		
		
		// If in cache
		if(hit){
			System.err.println("Query for "+ keyword + " result in cache hit.");
			jsonResult = DatabaseUtil.getYouTube(keyword).getResult();
		}
		// If not, query using YouTube API!!!
		else{
			System.err.println("Query for "+ keyword + " result in cache miss.");
			//URL, Title, Uploaded person
			try {
				jsonResult = APISearch(keyword);
			} catch (IOException e) {
				System.out.println(e.getMessage());
			} catch (ServiceException e) {
				System.out.println(e.getMessage());
			}
			Date today = new Date();
			String nodeId = router.node.getId().toString();
			YouTubeResult newResult = new YouTubeResult(keyword, nodeId, today, jsonResult);
			DatabaseUtil.cacheYouTube(newResult);
			
			// send back to cache node directly, then back to user interface
			router.respondResult(jsonResult);
		}
		return jsonResult;
	}
	
	/**
	 * Have a reference of underlying router of this node 
	 */
	public void specifyRouter(P2PApplication router){
		this.router = router;
	}
	
	/**
	 * Query on-line using YouTube API
	 * @return json result of youtube
	 */
	public String APISearch(String keyword) throws IOException, ServiceException{
		StringBuffer apiResult = new StringBuffer();
		
		String title = "";
		String url = "";
		String uploader = "";
		String description = "";
		String rating = "";
		
		YouTubeService service = new YouTubeService("Yayang Tian", "AI39si4BMI10-cEQjlaTcmFxU-1AwJJW86R-Ocfra19KIbqnVG7K18vnN0_5XUrrWEObNa69LIqmkie5r4z4bo-XsfU-mfz3Kg");
		YouTubeQuery query = new YouTubeQuery(new URL("http://gdata.youtube.com/feeds/api/videos"));
		query.setFullTextQuery(keyword);
		query.setOrderBy(YouTubeQuery.OrderBy.VIEW_COUNT);
		query.setSafeSearch(YouTubeQuery.SafeSearch.NONE);
		query.setMaxResults(20);
		VideoFeed videoFeed = service.query(query, VideoFeed.class);
		for(VideoEntry ve : videoFeed.getEntries()){
			//title
			title = ve.getTitle().getPlainText();
			
			// content
			YouTubeMediaGroup mg = ve.getMediaGroup();
			url = mg.getPlayer().getUrl();
			uploader = mg.getUploader();
			description = mg.getDescription().getPlainTextContent();
			Rating rateObject = ve.getRating();
			if(rateObject != null){
				rating = rateObject.getAverage().toString();
			}
			
			// construct JSON
			apiResult.append("{title: \"" + title + "\";" + 
					"url: \"" + url + "\";" +
					"uploader: \"" + uploader + "\";" + 
					"description: \"" + description + "\";" + 
					"rating: \"" + rating + "\"}");
		}
		return apiResult.toString();
	}
	
}
