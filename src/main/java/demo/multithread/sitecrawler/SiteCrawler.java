package demo.multithread.sitecrawler;

import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SiteCrawler {
	protected static final int RESULT_SIZE = 2;
	protected static final String CATEGORY = "honda";
	protected static final String KEYWORD = "accord";
	
	// private static final String SEED_URL = "https://www.kijiji.ca/cars";
	private static final String SEED_URL = "https://www.kijiji.ca/b-cars-trucks/hamilton/honda/new/k0c174l80014a49?price=700__899&price-type=monthly";
	private static final int POOL_SIZE = 10;
	
    public static void main(String[] args) {
    	ExecutorService executorService = Executors.newFixedThreadPool(POOL_SIZE);
    	ArrayBlockingQueue<String> unvisited = new ArrayBlockingQueue<String>(20);
        Set<String> visited = new ConcurrentSkipListSet<String>();

        try {
        	unvisited.offer(SEED_URL);
        	while(visited.size() < RESULT_SIZE) {
        		executorService.execute(new CrawlerWorker(unvisited, visited));
        	}
        	
        	// Print indexed pages
        	for(String result : visited) {
        		System.out.println("RESULT: " + result);
        	}
        	
        	shutdownAndAwaitTermination(executorService);
        	unvisited = null;
        	visited = null;
        	
        } catch(Exception e) {
           e.printStackTrace();
        }
    }
    
    protected static void shutdownAndAwaitTermination(ExecutorService pool) {
	   pool.shutdown(); // Disable new tasks from being submitted
	   try {
	     // Wait a while for existing tasks to terminate
	     if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
	       pool.shutdownNow(); // Cancel currently executing tasks
	       if (!pool.awaitTermination(60, TimeUnit.SECONDS))
	           System.err.println("Pool did not terminate");
	     }
	   } catch (InterruptedException ie) {
	     pool.shutdownNow();
	     // Preserve interrupt status
	     Thread.currentThread().interrupt();
	   }
	 }
}
