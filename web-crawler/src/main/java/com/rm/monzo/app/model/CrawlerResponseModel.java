package com.rm.monzo.app.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.Future;

@Builder
@Getter
public class CrawlerResponseModel {
    private String currentURL;
    private List<Future<CrawlerResponseModel>> childrenFutures;
    private int currentDepth;
}
