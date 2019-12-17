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
    private boolean isExternalCrawlingAllowed;
    private static final String KEY_ERROR = "error";

    public void crawlSite(String siteURL, BiConsumer<Map, Optional<String>> consumer) {
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolLimit);
        Map<String, List> result = new HashMap<>();
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
            result.put(siteURL, handleRecursiveCrawls(Collections.singletonList(executorService.submit(masterCallable))));
        } catch (Exception ie) {
            errorMsg = Optional.of(ie.getMessage());
            ie.printStackTrace();
            // only the top most one??
        } finally {
            executorService.shutdown();
            while (!executorService.isTerminated()) {
                // just wait in mainthread for executors to complete
            }
            if(result.containsKey(KEY_ERROR)) {
                errorMsg = Optional.of(result.get(KEY_ERROR).toString());
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
                // construct a nested error map
                List<Map> errorMap = parentResponse.getOrDefault(KEY_ERROR, new ArrayList<>());
                Map<String, String> error= new HashMap<>();
                error.put(KEY_ERROR, ie.getMessage());
                errorMap.add(error);
                parentResponse.put(KEY_ERROR, errorMap);
            }
        }
        System.out.println("Ending handleRecursiveCrawls.");
        return parents;
    }
}
