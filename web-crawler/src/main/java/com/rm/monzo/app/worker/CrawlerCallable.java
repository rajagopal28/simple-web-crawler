package com.rm.monzo.app.worker;

import com.rm.monzo.app.model.CrawlerResponseModel;
import com.rm.monzo.app.util.CrawlerUtil;
import lombok.Builder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Logger;

@Builder
public class CrawlerCallable implements Callable<CrawlerResponseModel> {

    private int currentDepth;
    private ExecutorService executorService;
    private int depthLimit;
    private String currentURL;
    private boolean isExternalCrawlingAllowed;
    private ConcurrentSkipListSet<String> crawledSites;

    private static Logger logger = Logger.getLogger(CrawlerCallable.class.getCanonicalName());


    public CrawlerResponseModel call() throws Exception {
        logger.info(Thread.currentThread().getName());

        List<Future<CrawlerResponseModel>> childrenPromises = new ArrayList<>();

        String uniqueURL = CrawlerUtil.getAbsoluteURLWithoutInternalReference(currentURL);
        if(isValidURLToCrawl(uniqueURL)) { // is external URL restricted
            crawledSites.add(currentURL);
            Document document = Jsoup.parse(new URL(uniqueURL), CrawlerUtil.TEN_SECONDS_IN_MILLIS); // get document with timeouts
            Elements linksOnPage = document.select(CrawlerUtil.PATTERN_CSS_QUERY_SELECTION);
            int newDepth = currentDepth+1;

            logger.info(String.format("Crawled current URL:: %s, currentDepth:: %d, newDepth:: %d", currentURL, currentDepth, newDepth));
            if(newDepth <= depthLimit) { // only go if the depth is not restricted
                for (Element page : linksOnPage) {
                    String childURL = page.attr(CrawlerUtil.ATTRIBUTE_KEY_URL_SELECTION);
                    String URLWithoutInternalReference = CrawlerUtil.getAbsoluteURLWithoutInternalReference(childURL);
                    if (isValidURLToCrawl(URLWithoutInternalReference)
                            && checkForExternalURL(URLWithoutInternalReference)) { // is external URL restricted
                        CrawlerCallable childCallable = CrawlerCallable.builder()
                                .currentURL(URLWithoutInternalReference)
                                .executorService(executorService)
                                .depthLimit(depthLimit)
                                .currentDepth(newDepth)
                                .isExternalCrawlingAllowed(isExternalCrawlingAllowed)
                                .crawledSites(crawledSites)
                                .build();
                        childrenPromises.add(executorService.submit(childCallable));
                    }
                }
            }
        }
        return CrawlerResponseModel.builder()
                .childrenFutures(childrenPromises)
                .currentURL(currentURL)
                .currentDepth(currentDepth)
                .build();
    }

    private boolean isValidURLToCrawl(String nextURL) {
        return !nextURL.trim().isEmpty()
                && !crawledSites.contains(nextURL)
                && checkForExternalURL(nextURL);
    }

    private boolean checkForExternalURL(String nextURL) {
        return isExternalCrawlingAllowed || CrawlerUtil.isValidSameDomainURL(currentURL, nextURL);
    }

}
