# WebSearchEngine_CS2580

1. Indexer 

2. Web Crawler

  Given a source URL, find all URLs on this page. Add those URLs to queue based on Best-First algorithm (My implementation:   PriorityQueue based on score and insertion order). URL score is based on query correlation. Maintain a global HashMap which contains URLs that have already been processed. Then  URL with the highest score is poped out from queue and all inside URLs are processed, including checking with global processed URLs, updating score of URLs that are already inside queue and adding to queue if not exist. Do the whole procedure recursively.
  
  Pros: Crawled pages that are most relevant to query
  
  Cons: May miss a lot of pages, comparing to Bread-First and Depth-First.
  
  Chanlenges: 
    1) Needs to handle duplicate links and loop. 
    2) Handle ads
    
3. Retriever 

