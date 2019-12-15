package com.rm.monzo.app.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

public class CrawlerResponseModelTest {

    @Test
    public void testModelCreation(){
        List<Future<CrawlerResponseModel>> actual = Collections.emptyList();
        int currentDepth = 10;
        String actualURL = "someURL";
        CrawlerResponseModel model = CrawlerResponseModel.builder()
                .currentURL(actualURL)
                .currentDepth(currentDepth)
                .childrenFutures(actual)
                .build();
        Assert.assertNotNull(model);
        Assert.assertEquals(actualURL, model.getCurrentURL());
        Assert.assertEquals(currentDepth, model.getCurrentDepth());
        Assert.assertEquals(actual, model.getChildrenFutures());
    }
}