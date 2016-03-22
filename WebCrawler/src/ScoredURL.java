package WebCrawler.src;

import java.net.URL;

public class ScoredURL {
  private URL url;
  private int score;
  private int time;
  
  public ScoredURL(URL url, int score, int time) {
    this.url = url;
    this.score = score;
    this.time = time;
  }
  
  public int getTime() {
    return this.time;
  }
  public void setScore(int score) {
    this.score = score;
  }
  public URL getURL() {
    return this.url;
  }
  
  public int getScore() {
    return this.score;
  }
  
  public int getInserstionTime() {
    return this.time;
  }
}
