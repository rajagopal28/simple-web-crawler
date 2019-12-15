package com.rm.monzo.app.worker;

import com.rm.monzo.app.model.CrawlerResponseModel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.URL;
import java.util.Collections;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class, URL.class})
public class CrawlerCallableTest {

    @Test
    public void testWorkerBuilder() {
        CrawlerCallable callable = CrawlerCallable.builder()
                .crawledSites(new ConcurrentSkipListSet<>())
                .currentDepth(0)
                .currentURL("someURL")
                .depthLimit(10)
                .isExternalCrawlingAllowed(true)
                .executorService(Mockito.mock(ExecutorService.class))
                .build();

        Assert.assertNotNull(callable);
    }

    @Test
    public void testMultiCallableInvokingWhenDepthWithinThreshold() throws Exception {
        Document mockDoc = Mockito.mock(Document.class);

        Elements mockElements = Mockito.mock(Elements.class);

        Element mockElement = Mockito.mock(Element.class);

        String child = "http://www.example2.com/";
        Mockito.when(mockElement.attr(Mockito.anyString())).thenReturn(child);

        Mockito.when(mockElements.iterator()).thenReturn(Collections.singleton(mockElement).iterator());

        Mockito.when(mockDoc.select(Mockito.anyString())).thenReturn(mockElements);

        PowerMockito.mockStatic(Jsoup.class);
        ThreadPoolExecutor mockExecutor = Mockito.mock(ThreadPoolExecutor.class);

        Future mockFuture = Mockito.mock(Future.class);
        Mockito.when(mockExecutor.submit(Mockito.any(CrawlerCallable.class))).thenReturn(mockFuture);

        PowerMockito.when(Jsoup.parse(Mockito.any(URL.class), Mockito.anyInt())).thenReturn(mockDoc);

        ConcurrentSkipListSet<String> crawledSites = new ConcurrentSkipListSet<>();
        String currentURL = "http://www.example.com/";
        CrawlerCallable callable = CrawlerCallable.builder()
                .executorService(mockExecutor)
                .depthLimit(2)
                .currentDepth(1)
                .isExternalCrawlingAllowed(true)
                .crawledSites(crawledSites)
                .currentURL(currentURL)
                .build();
        CrawlerResponseModel response = callable.call();
        System.out.println(crawledSites);
        Assert.assertFalse(crawledSites.isEmpty());
        Assert.assertEquals(1, crawledSites.size());
        Assert.assertTrue(crawledSites.stream().noneMatch(s -> s.equals(currentURL)));
        Assert.assertTrue(crawledSites.stream().anyMatch(s -> s.equals(child)));
        Mockito.verify(mockExecutor).submit(Mockito.any(CrawlerCallable.class));

        Mockito.verify(mockDoc).select(Mockito.anyString());
        Mockito.verify(mockElements).iterator();
        Mockito.verify(mockElement).attr(Mockito.anyString());

        Assert.assertNotNull(response);
        Assert.assertEquals(currentURL, response.getCurrentURL());
        Assert.assertEquals(1, response.getChildrenFutures().size());
        Assert.assertEquals(mockFuture, response.getChildrenFutures().get(0));
    }

    @Test
    public void testMultiCallableShouldNotInvokeWhenDepthMoreThanThreshold() throws Exception {
        Document mockDoc = Mockito.mock(Document.class);

        Elements mockElements = Mockito.mock(Elements.class);

        Element mockElement = Mockito.mock(Element.class);

        String child = "http://www.example2.com/";
        Mockito.when(mockElement.attr(Mockito.anyString())).thenReturn(child);

        Mockito.when(mockElements.iterator()).thenReturn(Collections.singleton(mockElement).iterator());

        Mockito.when(mockDoc.select(Mockito.anyString())).thenReturn(mockElements);

        PowerMockito.mockStatic(Jsoup.class);
        ThreadPoolExecutor mockExecutor = Mockito.mock(ThreadPoolExecutor.class);

        Future mockFuture = Mockito.mock(Future.class);
        Mockito.when(mockExecutor.submit(Mockito.any(CrawlerCallable.class))).thenReturn(mockFuture);

        PowerMockito.when(Jsoup.parse(Mockito.any(URL.class), Mockito.anyInt())).thenReturn(mockDoc);

        ConcurrentSkipListSet<String> crawledSites = new ConcurrentSkipListSet<>();
        String currentURL = "http://www.example.com/";
        CrawlerCallable callable = CrawlerCallable.builder()
                .executorService(mockExecutor)
                .depthLimit(2)
                .currentDepth(3)
                .isExternalCrawlingAllowed(true)
                .crawledSites(crawledSites)
                .currentURL(currentURL)
                .build();
        CrawlerResponseModel response = callable.call();
        System.out.println(crawledSites);
        Assert.assertTrue(crawledSites.isEmpty());
        Mockito.verify(mockExecutor, Mockito.never()).submit(Mockito.any(CrawlerCallable.class));

        Mockito.verify(mockDoc).select(Mockito.anyString());

        Assert.assertNotNull(response);
        Assert.assertEquals(currentURL, response.getCurrentURL());
        Assert.assertEquals(0, response.getChildrenFutures().size());
    }

    @Test
    public void testMultiCallableInvokingWhenDepthWithinThresholdButExternalProhibited() throws Exception {
        Document mockDoc = Mockito.mock(Document.class);

        Elements mockElements = Mockito.mock(Elements.class);

        Element mockElement = Mockito.mock(Element.class);

        String child = "http://www.example2.com/";
        Mockito.when(mockElement.attr(Mockito.anyString())).thenReturn(child);

        Mockito.when(mockElements.iterator()).thenReturn(Collections.singleton(mockElement).iterator());

        Mockito.when(mockDoc.select(Mockito.anyString())).thenReturn(mockElements);

        PowerMockito.mockStatic(Jsoup.class);
        ThreadPoolExecutor mockExecutor = Mockito.mock(ThreadPoolExecutor.class);

        Future mockFuture = Mockito.mock(Future.class);
        Mockito.when(mockExecutor.submit(Mockito.any(CrawlerCallable.class))).thenReturn(mockFuture);

        PowerMockito.when(Jsoup.parse(Mockito.any(URL.class), Mockito.anyInt())).thenReturn(mockDoc);

        ConcurrentSkipListSet<String> crawledSites = new ConcurrentSkipListSet<>();
        String currentURL = "http://www.example.com/";
        CrawlerCallable callable = CrawlerCallable.builder()
                .executorService(mockExecutor)
                .depthLimit(2)
                .currentDepth(3)
                .isExternalCrawlingAllowed(false)
                .crawledSites(crawledSites)
                .currentURL(currentURL)
                .build();
        CrawlerResponseModel response = callable.call();
        System.out.println(crawledSites);
        Assert.assertTrue(crawledSites.isEmpty());
        Mockito.verify(mockExecutor, Mockito.never()).submit(Mockito.any(CrawlerCallable.class));

        Mockito.verify(mockDoc).select(Mockito.anyString());

        Assert.assertNotNull(response);
        Assert.assertEquals(currentURL, response.getCurrentURL());
        Assert.assertEquals(0, response.getChildrenFutures().size());
    }

    @Test
    public void testMultiCallableInvokingWhenDepthWithinThresholdButSameDomainInternalURLSProhibited() throws Exception {
        Document mockDoc = Mockito.mock(Document.class);

        Elements mockElements = Mockito.mock(Elements.class);

        Element mockElement = Mockito.mock(Element.class);

        String child = "http://www.example.com/page1#somelink";
        Mockito.when(mockElement.attr(Mockito.anyString())).thenReturn(child);

        Mockito.when(mockElements.iterator()).thenReturn(Collections.singleton(mockElement).iterator());

        Mockito.when(mockDoc.select(Mockito.anyString())).thenReturn(mockElements);

        PowerMockito.mockStatic(Jsoup.class);
        ThreadPoolExecutor mockExecutor = Mockito.mock(ThreadPoolExecutor.class);

        Future mockFuture = Mockito.mock(Future.class);
        Mockito.when(mockExecutor.submit(Mockito.any(CrawlerCallable.class))).thenReturn(mockFuture);

        PowerMockito.when(Jsoup.parse(Mockito.any(URL.class), Mockito.anyInt())).thenReturn(mockDoc);

        ConcurrentSkipListSet<String> crawledSites = new ConcurrentSkipListSet<>();
        String currentURL = "http://www.example.com/page1#somelink22";
        CrawlerCallable callable = CrawlerCallable.builder()
                .executorService(mockExecutor)
                .depthLimit(2)
                .currentDepth(3)
                .isExternalCrawlingAllowed(false)
                .crawledSites(crawledSites)
                .currentURL(currentURL)
                .build();
        CrawlerResponseModel response = callable.call();
        System.out.println(crawledSites);
        Assert.assertTrue(crawledSites.isEmpty());
        Mockito.verify(mockExecutor, Mockito.never()).submit(Mockito.any(CrawlerCallable.class));

        Mockito.verify(mockDoc).select(Mockito.anyString());

        Assert.assertNotNull(response);
        Assert.assertEquals(currentURL, response.getCurrentURL());
        Assert.assertEquals(0, response.getChildrenFutures().size());
    }

    @Test
    public void testMultiCallableInvokingWhenDepthWithinThresholdButAlreadyCrawledProhibited() throws Exception {
        Document mockDoc = Mockito.mock(Document.class);

        Elements mockElements = Mockito.mock(Elements.class);

        Element mockElement = Mockito.mock(Element.class);

        String child = "http://www.example2.com/";
        Mockito.when(mockElement.attr(Mockito.anyString())).thenReturn(child);

        Mockito.when(mockElements.iterator()).thenReturn(Collections.singleton(mockElement).iterator());

        Mockito.when(mockDoc.select(Mockito.anyString())).thenReturn(mockElements);

        PowerMockito.mockStatic(Jsoup.class);
        ThreadPoolExecutor mockExecutor = Mockito.mock(ThreadPoolExecutor.class);

        Future mockFuture = Mockito.mock(Future.class);
        Mockito.when(mockExecutor.submit(Mockito.any(CrawlerCallable.class))).thenReturn(mockFuture);

        PowerMockito.when(Jsoup.parse(Mockito.any(URL.class), Mockito.anyInt())).thenReturn(mockDoc);

        ConcurrentSkipListSet<String> crawledSites = new ConcurrentSkipListSet<>();
        crawledSites.add(child);
        String currentURL = "http://www.example.com/";
        CrawlerCallable callable = CrawlerCallable.builder()
                .executorService(mockExecutor)
                .depthLimit(2)
                .currentDepth(1)
                .isExternalCrawlingAllowed(true)
                .crawledSites(crawledSites)
                .currentURL(currentURL)
                .build();
        CrawlerResponseModel response = callable.call();
        System.out.println(crawledSites);
        Assert.assertFalse(crawledSites.isEmpty());
        Mockito.verify(mockExecutor, Mockito.never()).submit(Mockito.any(CrawlerCallable.class));

        Mockito.verify(mockDoc).select(Mockito.anyString());

        Assert.assertNotNull(response);
        Assert.assertEquals(currentURL, response.getCurrentURL());
        Assert.assertEquals(0, response.getChildrenFutures().size());
    }
}