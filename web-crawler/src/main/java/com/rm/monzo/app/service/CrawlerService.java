package com.rm.monzo.app.service;

import com.rm.monzo.app.model.CrawlerResponseModel;
import com.rm.monzo.app.worker.CrawlerCallable;
import lombok.AllArgsConstructor;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

@AllArgsConstructor
public class CrawlerService {

    private int depthLimit;
    private int threadPoolLimit;
    private boolean isExternalCrawlingAllowed;
    private static Logger logger = Logger.getLogger(CrawlerService.class.getCanonicalName());

    public void crawlSite(String siteURL, BiConsumer<Map, Optional<String>> consumer) {
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolLimit);
        Map<String, List> result = new HashMap<>();
        List<String> errorList = new ArrayList<>();
        Optional<String> errorMsg = Optional.empty();
        try {
            CrawlerCallable masterCallable = CrawlerCallable.builder()
                    .currentDepth(1)
                    .depthLimit(depthLimit)
                    .executorService(executorService)
                    .crawledSites(new ConcurrentSkipListSet<>())
                    .currentURL(siteURL)
                    .isExternalCrawlingAllowed(isExternalCrawlingAllowed)
                    .build();
            List<Map> resultList = handleRecursiveCrawls(Collections.singletonList(executorService.submit(masterCallable)), errorList);
            result.put(siteURL, resultList);
        } catch (Exception ie) {
            errorMsg = Optional.of(ie.getMessage());
            logger.log(Level.SEVERE, ie.getMessage(), ie);
            // only the top most one??
        } finally {
            executorService.shutdown();
            while (!executorService.isTerminated()) {
                // just wait in mainthread for executors to complete
            }
            if(!errorList.isEmpty()) {
                errorMsg = Optional.of(errorList.toString());
                logger.severe(errorList.toString());
            }
            logger.info("Invoking consumer with data");
            consumer.accept(result, errorMsg);
        }
    }


    private List<Map> handleRecursiveCrawls(List<Future<CrawlerResponseModel>> futures, List<String> errorList) {
        logger.info("Starting handleRecursiveCrawls.");
        List<Map> parents = new ArrayList<>();
        for (Future<CrawlerResponseModel> future : futures) {
            Map<String, List<Map>> parentResponse = new HashMap<>();
            try {
                CrawlerResponseModel crawlerResponseModel = future.get();
                List<Map> children = new ArrayList<>();
                logger.info("crawlerResponseModel.getCurrentURL():::" + crawlerResponseModel.getCurrentURL());
                List<Future<CrawlerResponseModel>> childrenFutures = crawlerResponseModel.getChildrenFutures();
                if(!childrenFutures.isEmpty()) {
                    children.addAll(handleRecursiveCrawls(childrenFutures, errorList));
                }
                parentResponse.put(crawlerResponseModel.getCurrentURL(), children);
                parents.add(parentResponse);
            } catch (Exception ie){
                logger.log(Level.SEVERE, ie.getMessage(), ie);
                // construct a nested error map
                errorList.add(ie.getMessage());
            }
        }
        logger.info("Ending handleRecursiveCrawls.");
        return parents;
    }
}
