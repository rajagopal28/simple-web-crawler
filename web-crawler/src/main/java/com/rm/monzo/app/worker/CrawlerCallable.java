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
import java.util.concurrent.*;

@Builder
public class CrawlerCallable implements Callable<CrawlerResponseModel> {

    private int currentDepth;
    private ExecutorService executorService;
    private int depthLimit;
    private String currentURL;
    private boolean isExternalCrawlingAllowed;
    private ConcurrentSkipListSet<String> crawledSites;


    public CrawlerResponseModel call() throws Exception {
        System.out.println(Thread.currentThread().getName());

        List<Future<CrawlerResponseModel>> childrenPromises = new ArrayList<>();

        Document document = Jsoup.parse(new URL(currentURL), CrawlerUtil.TEN_SECONDS_IN_MILLIS); // get document with timeouts
        Elements linksOnPage = document.select(CrawlerUtil.PATTERN_CSS_QUERY_SELECTION);

        int newDepth = currentDepth+1;

        System.out.println(String.format("Crawled current URL:: %s, currentDepth:: %d, newDepth:: %d", currentURL, currentDepth, newDepth));

        if(newDepth <= depthLimit) { // only go if the depth is not restricted
            for (Element page : linksOnPage) {
                String childURL = page.attr(CrawlerUtil.ATTRIBUTE_KEY_URL_SELECTION);
                String URLWithoutInternalReference = CrawlerUtil.getAbsoluteURLWithoutInternalReference(childURL);
                if(isValidURLToCrawl(URLWithoutInternalReference)) { // is external URL restricted
                    CrawlerCallable childCallable = CrawlerCallable.builder()
                            .currentURL(URLWithoutInternalReference)
                            .executorService(executorService)
                            .depthLimit(depthLimit)
                            .currentDepth(newDepth)
                            .crawledSites(crawledSites)
                            .build();
                    crawledSites.add(URLWithoutInternalReference);
                    childrenPromises.add(executorService.submit(childCallable));
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
