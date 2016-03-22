package WebCrawler.src;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class Crawler {
  @Option(name="-u", required=true, usage="print URL")
  private String urlStr;
  
  @Option(name="-q", required=true, usage="specify queries to Crawler.")
  private String queries;
  
  @Option(name="-docs", usage="speficy the output dir.")
  private String dir;
  
  @Option(name="-m", usage="the maximum number of pages to be downloaded.")
  private int max = 10;
  
  @Option(name="-t", usage="print trace")
  private static boolean debug = false;
  
  //A minimal Web Crawler written in Java
  //Usage: From command line 
  //  java WebCrawler <URL> [N]
  //where URL is the url to start the crawl, and N (optional)
  //is the maximum number of pages to download.

  public static final int  SEARCH_LIMIT = 20;  // Absolute max pages 
  public static final boolean DEBUG = true;
  public static final String DISALLOW = "Disallow:";
  public static final int MAXSIZE = 20000; // Max size of file 
  
  class URLComparator implements Comparator<ScoredURL> {
    @Override
    public int compare(ScoredURL url1, ScoredURL url2) {
      if (url1.getScore() > url2.getScore()) {
        return -1;
      } else if (url1.getScore() < url2.getScore()) {
        return 1;
      } else {
        if (url1.getInserstionTime() > url2.getInserstionTime()) {
          return 1;
        } else if (url1.getInserstionTime() < url2.getInserstionTime()) {
          return -1;
        }
        return 0;
      }
    }
  }
  /** Build a PriorityQueue based on the score of each URL. */
  public final Comparator<ScoredURL> comp = new URLComparator();
  public final PriorityQueue<ScoredURL> urlsQueue = new PriorityQueue<>(1000, comp);
  
  // List of newURLs
  List<URL> urlsList;
  // Known URLs
  Hashtable<URL,Integer> knownURLs;
  // max number of pages to download
  int maxPages; 
  // List of link string.
  List<String> linksStr;
  // Set of pages that have been downloaded.
  Set<URL> seen;
  
  

  int count = 0;
  
  public static void main(String[] argv) {
    new Crawler().run(argv);
  }
  
  // Only parsed -u and -m option.
  // initializes data structures.  argv is the command line arguments.
  public void initialize(String[] argv) {
    CmdLineParser parser = new CmdLineParser(this);
    try {
      parser.parseArgument(argv);
      parser.setUsageWidth(80);
    } catch (CmdLineException cle) {
      System.err.println(cle.getMessage());
    }
    
    URL url;
    knownURLs = new Hashtable<URL,Integer>();
    urlsList = new LinkedList<URL>();
    seen = new HashSet<URL>();
    linksStr = new ArrayList<String>();
    try { url = new URL(urlStr); }
    catch (MalformedURLException e) {
      System.out.println("Invalid starting URL " + argv[0]);
      return;
    }
    
    urlsQueue.add(new ScoredURL(url, 0, count++));
    //seen.add(url);
    knownURLs.put(url,new Integer(1));

//    System.out.println("Starting search: Initial URL " + url.toString());
    maxPages = SEARCH_LIMIT;
    if (argv.length > 1) {
      // max is the specified max pages in cli.
      int iPages = max;
      if (iPages < maxPages) maxPages = iPages; }
    System.out.println("Maximum number of pages:" + maxPages);

    /*Behind a firewall set your proxy and port here!
     */
    Properties props= new Properties(System.getProperties());
    props.put("http.proxySet", "true");
    props.put("http.proxyHost", "webcache-cup");
    props.put("http.proxyPort", "8080");
    Properties newprops = new Properties(props);
    System.setProperties(newprops);
    /**/
  }
  
  public void run(String[] argv) {
   initialize(argv);
   System.out.println("Crawling for " + maxPages + " pages relevant to "+  queries + " starting from " + urlStr + "\n");
   int fileCount = 0;
   while (!urlsQueue.isEmpty() && seen.size() <= maxPages) {
    
    ScoredURL scoredURL = urlsQueue.poll();
    URL url = scoredURL.getURL();
    // print "Downloading..." instead.
    
    if (!seen.contains(url)) {
      if (DEBUG) System.out.println("Downloading " + url.toString() + " Score: " + scoredURL.getScore());
      if (robotSafe(url)) {
        String page = getpage(url);
        try {
          BufferedWriter out = new BufferedWriter(new FileWriter(dir+"/sample"+fileCount+".txt"));
          fileCount++;
          out.write(page);
          out.close();
        }
        catch (IOException e)
        {
          System.out.println("Exception ");       
        }
        if (DEBUG) System.out.println("Received: " + url.toString() + ".");
        seen.add(url);
//        System.out.println("seen size is : "+seen.size());
        if (seen.size() == maxPages) {
          break;
        }
        // All links have been added to link list. 
        // urlString :  "file.html"
        if (page.length() != 0) processpage(url, page, queries);
        // Waiting for processing the urlList to add url to urlsQueue in the order of score.
        // If use HashMap to store url and linkStr, it will not process in the order as url appeears in page.
        // Suppose you have
//        System.out.println("\nTHE QUERIES ARE: \n" + queries);
//        System.out.println("urlslist size is : " + urlsList.size());
        
        for(URL key : urlsList) {
          //System.out.println("\nkey's url string representation is "+key.toString());
          // This page and linksStr arguments are not low case.
          int score = calcScore(linksStr.get(0), page, queries);
//          System.out.println("linkStr is: "+linksStr.get(0));
          linksStr.remove(0);
          ScoredURL sURL = new ScoredURL(key, score, count++);
          urlsQueue.add(sURL);
          
          if (DEBUG) System.out.println("Adding to queue: " + key.toString() + ". Score: " + score);
        }
        urlsList.clear();
        linksStr.clear();
      }
    }
    System.out.println();
  }
  System.out.println("Search complete.");
  } 
  
  public void updateURLsQueue(URL url, String link, String page, String query) {
    for (ScoredURL surl : urlsQueue) {
      //System.out.println("surl's url string representation is " + surl.getURL().toString());
      if (surl.getURL().toString().equals(url.toString())) {
//        System.out.println("\nURL EQUALS\n");
        int score = calcScore(link, page, query);
        int finalscore = surl.getScore()+score;
        int urlCount = surl.getTime();
        ScoredURL sURL = new ScoredURL(url, finalscore, urlCount);
        urlsQueue.add(sURL);
        System.out.println("Adding score " + score + " to " + surl.getURL().toString());
        break;
      }
    }
  }
  //adds new URL to the queue. Accept only new URL's that end in
  //htm or html. oldURL is the context, newURLString is the link
  //(either an absolute or a relative URL).
  // linkString : <a href .....> </a>
  public void addnewurl(URL oldURL, String newUrlString, String linkString, String query, String page) {
    URL url; 
//    if (DEBUG) System.out.println("URL String " + newUrlString);
    try { url = new URL(oldURL,newUrlString);
      if (!knownURLs.containsKey(url)) {
        String filename =  url.getFile();
        int iSuffix = filename.lastIndexOf("htm");
        if ((iSuffix == filename.length() - 3) ||
            (iSuffix == filename.length() - 4)) {
          knownURLs.put(url,new Integer(1));
          urlsList.add(url);
          linksStr.add(linkString);
//          System.out.println("Found new URL " + url.toString());
        } 
      } else {
        updateURLsQueue(url, linkString, page, query);
      }
    }
    catch (MalformedURLException e) 
    { return; }
  }

  //Go through page finding links to URLs. Add link url to urlList and linkStr to
  // LinkStr list. A link is signalled
  //by <a href=" ...   It ends with a close angle bracket, preceded
  //by a close quote, possibly preceded by a hatch mark (marking a
  //fragment, an internal page marker)

  public void processpage(URL url, String page, String query) {
    String lcPage = page.toLowerCase(); // Page in lower case
    int index = 0; // position in page
    int iEndAngle, ihref, iURL, iCloseQuote, iHatchMark, iEnd, iLinkEnd;
    while ((index = lcPage.indexOf("<a",index)) != -1) {
      iEndAngle = lcPage.indexOf(">",index);
      ihref = lcPage.indexOf("href",index);
      if (ihref != -1) {
        iURL = lcPage.indexOf("\"", ihref) + 1; 
        if ((iURL != -1) && (iEndAngle != -1) && (iURL < iEndAngle)) {
         iCloseQuote = lcPage.indexOf("\"",iURL);
         iHatchMark = lcPage.indexOf("#", iURL);
         if ((iCloseQuote != -1) && (iCloseQuote < iEndAngle)) {
           iEnd = iCloseQuote;
           if ((iHatchMark != -1) && (iHatchMark < iCloseQuote))
             iEnd = iHatchMark;
           String newUrlString = page.substring(iURL,iEnd);
           iLinkEnd = lcPage.indexOf(">", iEnd+2) + 1;         
           // Add link string.
           String linkStr = page.substring(index, iLinkEnd);
           addnewurl(url, newUrlString, linkStr, query, page); 
         } 
        } 
      }
      index = iEndAngle;
    }
  }

  //Download contents of URL
  public String getpage(URL url) {
 
    try { 
      // try opening the URL
      URLConnection urlConnection = url.openConnection();
//    System.out.println("Downloading " + url.toString());

      urlConnection.setAllowUserInteraction(false);

      InputStream urlStream = url.openStream();
      // search the input stream for links
      // first, read in the entire URL
      byte b[] = new byte[1000];
      int numRead = urlStream.read(b);
      String content = new String(b, 0, numRead);
      while ((numRead != -1) && (content.length() < MAXSIZE)) {
        numRead = urlStream.read(b);
        if (numRead != -1) {
          String newContent = new String(b, 0, numRead);
          content += newContent;
        }
      }
      return content;
    } catch (IOException e) {
      System.out.println("ERROR: couldn't open URL ");
      return "";
    }  
  }

  //Top-level procedure. Keep popping a url off newURLs, download
  //it, and accumulate new URLs

  //Check that the robot exclusion protocol does not disallow
  //downloading url.

  public boolean robotSafe(URL url) {
    String strHost = url.getHost();
    if (strHost.length() == 0) {
      return false;
    }
    // form URL of the robots.txt file
    String strRobot = "http://" + strHost + "/robots.txt";
    URL urlRobot;
    try { urlRobot = new URL(strRobot);
    } catch (MalformedURLException e) {
      // something weird is happening, so don't trust it
      return false;
    }

//    if (DEBUG) System.out.println("Checking robot protocol " + 
//        urlRobot.toString());
    String strCommands;
    try {
      InputStream urlRobotStream = urlRobot.openStream();
      // read in entire file
      byte b[] = new byte[1000];
      int numRead = urlRobotStream.read(b);
      strCommands = new String(b, 0, numRead);
      while (numRead != -1) {
        numRead = urlRobotStream.read(b);
        if (numRead != -1) {
          String newCommands = new String(b, 0, numRead);
          strCommands += newCommands;
        }
      }
      urlRobotStream.close();
    } catch (IOException e) {
      // if there is no robots.txt file, it is OK to search
      return true;
    }
//    if (DEBUG) System.out.println(strCommands);

    // assume that this robots.txt refers to us and 
    // search for "Disallow:" commands.
    String strURL = url.getFile();
    int index = 0;
    while ((index = strCommands.indexOf(DISALLOW, index)) != -1) {
      index += DISALLOW.length();
      String strPath = strCommands.substring(index);
      StringTokenizer st = new StringTokenizer(strPath);

      if (!st.hasMoreTokens())
        break;

      String strBadPath = st.nextToken();

      // if the URL starts with a disallowed path, it is not safe
      if (strURL.indexOf(strBadPath) == 0)
        return false;
    }

    return true;
  }

//What is the best way instead of returning null?
 public String getLinkAnchor(String link) {
   int index = 0;
   int start = link.indexOf(">", index) + 1;
   int end;
   if (start != -1) {
     end = link.indexOf("<", start);
     if (end != -1) {
       return link.substring(start, end).trim();
     }
   }
   return null; 
 }
 
 public String getLinkURL(String link) {
   String result;
   int index = 0;
   int start = link.indexOf("\"", index);
   int end;
   if (start != -1) {
     end = link.indexOf("\"", start+1);
     if (end != -1) {
       result = link.substring(start+1, end).trim();
       return result;
     }
   }
   return null;
 }
 
 
 /**
  * Calculate the link score based on the page words except the link.
  * Support case insensitive search.
  * @page This is the converted page.
  */
 public int scoreOutLink(List<String> leftWords, List<String> rightWords, String query) {    
   int nearbyScore = 0;
   int score = 0;
   // Store query words that are in the fivewords around link.
   Set<String> queryWordsNearby = new HashSet<>();
   // Store query words appear all over the page.
   Set<String> queryWords = new HashSet<>();

   String[] qwords = query.trim().toLowerCase().split("\\s+");
//   System.out.println(Arrays.asList(qwords));
   List<String> leftfive = new ArrayList<>();
   List<String> rightfive = new ArrayList<>();

   if (leftWords.size() <= 5) {
     leftfive = new ArrayList<>(leftWords);
   } else {
     for (int i=leftWords.size()-1; i>leftWords.size()-6; i--) {
       leftfive.add(leftWords.get(i));
     }
   }
   if (rightWords.size() <= 5) {
     rightfive = new ArrayList<>(rightWords);
   } else {
     for (int i=0; i<5; i++) {
       rightfive.add(rightWords.get(i));
     }
   }
   // Add query word to sets if matches.
   for (int i=0; i<qwords.length; i++) {
     if (leftWords.contains(qwords[i]) || rightWords.contains(qwords[i])) {
//       System.out.println(qwords[i]);
       queryWords.add(qwords[i]);
     }
   }
   for (int i=0; i<qwords.length; i++) {
     if (leftfive.contains(qwords[i]) || rightfive.contains(qwords[i])) {
//         System.out.println("leftfive or rightfive contains: " + qwords[i]);
         queryWordsNearby.add(qwords[i]);
     }
   }
//   System.out.println("QUERY WORDS SIZE : " + queryWords.size());
//   System.out.println("QUERY WORDS nearby SIZE : " + queryWordsNearby.size());

   nearbyScore = 4 * queryWordsNearby.size();
   score = nearbyScore + Math.abs(queryWords.size() - queryWordsNearby.size());
   return score;
   
 }
 
 // Support case-insensitive search.
 public int calcScore(String link, String page, String query) {
   if (query == null || query.length() == 0) {
     return 0;
   }
   String anchor = getLinkAnchor(link).toLowerCase();
   String url = getLinkURL(link).toLowerCase();
   String[] queries = query.toLowerCase().split("\\s+");
   int score = 0;
   for (String unit : queries) {
     if (anchor.indexOf(unit) != -1) {
       score += 50;
     }
   }
   if (score != 0) {
     return score;
   }
   for (String unit: queries) {
     if (url.indexOf(unit) != -1) {
       return 40;
     }
   }
   String lcpage = page.toLowerCase();
   String lclink = link.toLowerCase();
   
   int lStart = 0, lEnd = lcpage.indexOf(lclink);
   int rStart = lcpage.indexOf(lclink) + lclink.length();
   int rEnd = lcpage.length();
   List<String> leftWords = getWords(lcpage, lclink, lStart, lEnd);
   List<String> rightWords = getWords(lcpage, lclink, rStart, rEnd);
   
   int outLinkScore = scoreOutLink(leftWords, rightWords, query);
   return outLinkScore;

 }
 
 // all lowcase argument.
 public List<String> getWords(String page, String link, int start, int end) {
   List<String> result = new ArrayList<>();
   if (start >= end) {
     return result;
   }
   Deque<Character> stack = new LinkedList<>();
   for (int i=start; i<end; i++) {
     if (page.charAt(i) == '<') {
       stack.push(page.charAt(i));
       i++;
       while (!stack.isEmpty()) {
         if (page.charAt(i) == '>') {
           stack.pop();
         }
         i++;
       }
     }
     if (Character.toString(page.charAt(i)).matches("[^A-Za-z0-9\\-]")) {
       continue;
     } else {
       int sIndex = i;
       i++;
       while (Character.toString(page.charAt(i)).matches("[A-Za-z0-9\\-]")) {
         i++;
       }
       int eIndex = i;
       if (sIndex < eIndex) {
         result.add(page.substring(sIndex, eIndex));
       }
       // Need to check this eIndex character.
       i--;
     }
   }
   return result;
 }
}
