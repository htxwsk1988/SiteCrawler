package demo.multithread.sitecrawler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CrawlerWorker implements Runnable {
    private ArrayBlockingQueue<String> unvisited;
    private Set<String> visited;
    
    private final int CONNECT_TIMEOUT = 60 * 1000; // 60 Seconds
    private final String COLON_DOUBLE_SLASH = "://";
    
    public CrawlerWorker(ArrayBlockingQueue<String> unvisited, Set<String> visited) {
    	this.unvisited = unvisited;
    	this.visited = visited;
    }

    @Override
    public void run() {
    	if (this.visited.size() < SiteCrawler.RESULT_SIZE && !this.unvisited.isEmpty()) {
            try {
            	String currUrl = this.unvisited.poll();
            	System.out.println("[" + Thread.currentThread().getName() + "] " + currUrl);
            	visitPageBFS(currUrl, SiteCrawler.KEYWORD);
            } catch(Exception e) {
                 e.printStackTrace();
            }
    	}
    }

    private void visitPageBFS(String url, String keyword) throws IOException, InterruptedException, URISyntaxException {
    	if (this.visited.contains(url)) return;
    	
        Document doc = Jsoup.connect(url) 
        		.timeout(CONNECT_TIMEOUT)
        		.get();
        
        // Get all the link on the page
        Elements linkTags = doc.select("a[href]");
        String link;
        for(Element e : linkTags) {
        	link = e.attr("href");
            if (this.visited.contains(link) || link.equals(url)) continue; // Site has been visited.
            if (!findMatch(link, SiteCrawler.CATEGORY)) continue;
            
            if (link.startsWith("http") || link.startsWith("https")) {
            	if(!this.unvisited.contains(link)) this.unvisited.offer(link);
            }
            
            if (link.startsWith("www")) {
            	URI uri = new URI(url);
            	link = uri.getScheme() + COLON_DOUBLE_SLASH + link;
            	if(!this.unvisited.contains(link)) this.unvisited.offer(link);
            }
            
            if (link.startsWith("/")) {
            	URI uri = new URI(url);
            	link = uri.getScheme() + COLON_DOUBLE_SLASH + uri.getHost() + link;
            	if(!this.unvisited.contains(link)) this.unvisited.offer(link);
            }
        }
        // Index the page
        Elements h1Tags = doc.select("h1");
        String heading;
        for(Element e : h1Tags) {
        	heading = e.text();
        	if(findMatch(heading, keyword)) {
        		this.visited.add(url);
        	}
        }
    }
    
    private boolean findMatch(String content, String keyword) {
    	if (content == null || keyword == null) return false;
    	return content.toLowerCase().indexOf(keyword.toLowerCase()) != -1;
    }
}