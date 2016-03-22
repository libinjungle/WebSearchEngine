package WebCrawler.src;

import java.awt.image.ColorConvertOp;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcesspageTest {
  public static void processpage(String page) {
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
           System.out.println(newUrlString);
           iLinkEnd = lcPage.indexOf(">", iEnd+2) + 1;
           String linkStr = page.substring(index, iLinkEnd);
           System.out.println(linkStr);

         } 
        } 
      }
      index = iEndAngle;
    }
  }
  
  public static String getLinkAnchor(String link) {
    int index = 0;
    int start = link.indexOf(">", index) + 1;
    int end;
    if (start != -1) {
      end = link.indexOf("<", start);
      if (end != -1) {
        return link.substring(start, end);
      }
    }
    return null; 
  }
  
  public static String getLinkURL(String link) {
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
  
  public static List<String> getFiveWords(String link, String page) {
    List<String> result = new ArrayList<>();
    Pattern pattern = Pattern.compile("([^\\s]+\\s+[^\\s]+\\s+[^\\s]+\\s+[^\\s]+\\s+[^\\s]+)\\s+"+link+
        "\\s+([^\\s]+\\s+[^\\s]+\\s+[^\\s]+\\s+[^\\s]+\\s+[^\\s]+)\\s+");
//    Pattern pattern = Pattern.compile("([[^\\s]+\\s+]{5}?)"+link+"\\s+([^\\s]+\\s+{5}?)");
    Matcher matcher = pattern.matcher(page);

    while (matcher.find()) {

      String[] group1 = matcher.group(1).trim().split("\\s+");
      String[] group2 = matcher.group(2).trim().split("\\s+");
      for (String word : group1) {
        if (word.charAt(0) == '(' 
            || word.charAt(0) == '{'
              || word.charAt(0) == '[') {
          result.add(word.substring(1).toLowerCase());
        } else if (word.charAt(word.length()-1) == ')' 
            || word.charAt(word.length()-1) == '}'
            || word.charAt(word.length()-1) == ']') {
          result.add(word.substring(0, word.length()-1).toLowerCase());
        } else {
          result.add(word.toLowerCase());
        }
      }
      for (String word : group2) {
        if (word.charAt(0) == '(' 
            || word.charAt(0) == '{'
              || word.charAt(0) == '[') {
          result.add(word.substring(1).toLowerCase());
        } else if (word.charAt(word.length()-1) == ')' 
            || word.charAt(word.length()-1) == '}'
            || word.charAt(word.length()-1) == ']') {
          result.add(word.substring(0, word.length()-1).toLowerCase());
        } else {
          result.add(word.toLowerCase());
        }
      }
    }
    return result;
  }
  
  /**
   * Convert linkStr with "-" replacing "<" and ">" in linkStr. Removes all tags, then 
   * converts linkStr back with "<" and ">" in page.
   * @param page
   * @param linkStr
   * @return
   */
  public static String convertPageWithDash(String page, String linkStr) {
    String lcpage = page.toLowerCase();
    String lcLink = linkStr.toLowerCase();
    
    String lcLinkDash = lcLink.replaceAll("[<>]", "-");
    String lcpageWithdash = lcpage.replace(lcLink, lcLinkDash);
    String convertedPage = lcpageWithdash.replaceAll("\\<.*?>","");
    
    return convertedPage;
  }
  
  public static String convertPage(String page, String linkStr) {
    String result = "";
    String lcPage = page.toLowerCase(); // Page in lower case
    String lclinkStr = linkStr.toLowerCase();
    // index of the given linkStr in page.
   
    int index = 0; // position in page
    int iEndAngle = 0, ihref, iURL, iCloseQuote, iHatchMark, iEnd, iLinkEnd;
    while ((index = lcPage.indexOf("<a",index)) != -1) {
      int givenLinkindex = lcPage.indexOf(lclinkStr);
      if (index != givenLinkindex) {
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
             String mylink = lcPage.substring(index, iLinkEnd);
             String anchor = getLinkAnchor(mylink);
             result = lcPage.replace(mylink, anchor);
             lcPage = result;
           } 
          } 
        }
        index = iEndAngle;
      } else {
        index += lclinkStr.length();
      }
    }
    return lcPage;  
  }
  
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
  
  public int scoreOutLink(List<String> leftWords, List<String> rightWords, String query) {    
    int nearbyScore = 0;
    int score = 0;
    // Store query words that are in the fivewords around link.
    Set<String> queryWordsNearby = new HashSet<>();
    // Store query words appear all over the page.
    Set<String> queryWords = new HashSet<>();

    String[] qwords = query.trim().toLowerCase().split("\\s+");
    System.out.println(Arrays.asList(qwords));
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
        System.out.println(qwords[i]);
        queryWords.add(qwords[i]);
      }
    }
    for (int i=0; i<qwords.length; i++) {
      if (leftfive.contains(qwords[i]) || rightfive.contains(qwords[i])) {
          System.out.println("leftfive or rightfive contains: " + qwords[i]);
          queryWordsNearby.add(qwords[i]);
      }
    }
    System.out.println("QUERY WORDS SIZE : " + queryWords.size());
    System.out.println("QUERY WORDS nearby SIZE : " + queryWordsNearby.size());

    nearbyScore = 4 * queryWordsNearby.size();
    score = nearbyScore + Math.abs(queryWords.size() - queryWordsNearby.size());
    return score;
    
  }

  public static void main(String[] args) {
    ProcesspageTest test = new ProcesspageTest();
    String link = "<A href=\"Dolphin.html\">dolphins</A>";
    String page = "<TITLE> Whale </TITLE> <H2> Whale </H2> (from Wikipedia) <p> Whale is the common name for a widely distributed and diverse group of  fully aquatic placental <A href=\"MarineMammal.html\">marine mammals.</A>. They are an informal grouping within the infraorder <A href=\"Cetacean.html\">Cetacea,</A> usually excluding <A href=\"Dolphin.html\">dolphins</A> and <A href=\"Porpoise.html\">porpoises.</A> Whales, dolphins and porpoises belong to the order Cetartiodactyla with even-toed. <A href=\"Ungulate.html\">ungulates</A> and their closest living relatives are the <A href=\"Hippopotamus.html\">hippopotamuses,</A> having diverged about 40 million years ago.";
    String query = "species whale whales";
    
    String lcpage = page.toLowerCase();
    String lclink = link.toLowerCase();
    
    int lStart = 0, lEnd = lcpage.indexOf(lclink);
    int rStart = lcpage.indexOf(lclink) + lclink.length();
    int rEnd = lcpage.length();
    System.out.println(lEnd);
    List<String> leftwords = test.getWords(lcpage, lclink, lStart, lEnd);
    System.out.println(leftwords);
    List<String> rightwords = test.getWords(lcpage, lclink, rStart, rEnd);
    System.out.println(test.getWords(lcpage, lclink, rStart, rEnd));
    for (int i=leftwords.size()-1; i>leftwords.size()-6; i--) {
      System.out.println(leftwords.get(i));
    }
    System.out.println();
    for (int i=0; i<5; i++) {
      System.out.println(rightwords.get(i));
    }
    
    System.out.println(test.scoreOutLink(leftwords, rightwords, query));
    
    
    
    
    //    System.out.println(convertPageWithDash(page, link));
  }
}
