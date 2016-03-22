package WebCrawler.src;

import java.io.IOException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class CommandLineParser {
  @Option(name="-u", required=true, usage="print URL")
  private String urlStr;
  
  @Option(name="-q", required=true, usage="specify queries to Crawler.")
  private String queries;
  
  @Option(name="-docs", required=true, usage="speficy the output dir.")
  private String dir;
  
  @Option(name="-m", usage="the maximum number of pages to be downloaded.")
  private int max;
  
  /** This is a MUST have even if there is no argument. */
//  @Argument 
//  private List<String> arguments = new ArrayList<>();
  
  public static void main(String[] args) throws IOException {
    try {
      new CommandLineParser().doMain(args);
    } catch (CmdLineException cle) {
      System.err.println(cle.getMessage());
    }
        
  }
  
  public void doMain(String[] args) throws IOException, CmdLineException{
    
    CmdLineParser cli = new CmdLineParser(this);
    cli.parseArgument(args);
    cli.setUsageWidth(80);
    System.out.println(urlStr);
    System.out.println(queries);
    System.out.println(dir);
    System.out.println(max);
  }
}
