package com.rm.monzo.app.service;

import com.rm.monzo.app.model.CrawlerResponseModel;
import com.rm.monzo.app.worker.CrawlerCallable;
import lombok.AllArgsConstructor;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

@AllArgsConstructor
public class CrawlerService {

    private int depthLimit;
    private int threadPoolLimit;

    public void crawlSite(String siteURL, BiConsumer<Map, String> consumer) {
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolLimit);
        System.out.println(executorService);
        Map<String, List> result = new HashMap<>();
        String errorMsg = null;
        try {
            CrawlerCallable masterCallable = CrawlerCallable.builder()
                    .currentDepth(1)
                    .depthLimit(depthLimit)
                    .executorService(executorService)
                    .crawledSites(new ConcurrentSkipListSet<>())
                    .currentURL(siteURL)
                    .build();
            result.put(siteURL, handleRecursiveCrawls(Collections.singletonList(executorService.submit(masterCallable))));
            System.out.println("Crawly!!");
        } catch (Exception ie) {
            errorMsg = ie.getMessage();
            ie.printStackTrace();
            // only the top most one??
        } finally {
            executorService.shutdown();
            while (!executorService.isTerminated()) {
                // just wait in mainthread for executors to complete
            }
            consumer.accept(result, errorMsg);
        }
    }


    private List<Map> handleRecursiveCrawls(List<Future<CrawlerResponseModel>> futures) {
        System.out.println("Starting handleRecursiveCrawls.");
        List<Map> parents = new ArrayList<>();
        for (Future<CrawlerResponseModel> future : futures) {
            Map<String, List<Map>> parentResponse = new HashMap<>();
            try {
                CrawlerResponseModel crawlerResponseModel = future.get();
                List<Map> children = new ArrayList<>();
                System.out.println("crawlerResponseModel.getCurrentURL():::" + crawlerResponseModel.getCurrentURL());
                List<Future<CrawlerResponseModel>> childrenFutures = crawlerResponseModel.getChildrenFutures();
                if(!childrenFutures.isEmpty()) {
                    children.addAll(handleRecursiveCrawls(childrenFutures));
                }
                parentResponse.put(crawlerResponseModel.getCurrentURL(), children);
                parents.add(parentResponse);
            } catch (InterruptedException | ExecutionException ie){
                ie.printStackTrace();
                // just log??
            }
        }
        System.out.println("Ending handleRecursiveCrawls.");
        return parents;
    }
}
