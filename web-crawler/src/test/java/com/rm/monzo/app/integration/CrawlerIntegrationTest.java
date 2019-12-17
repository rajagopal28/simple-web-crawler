package com.rm.monzo.app.integration;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.rm.monzo.app.service.CrawlerService;
import com.rm.monzo.app.util.TestStubUtil;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class CrawlerIntegrationTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.wireMockConfig().port(8065));

    @Test
    public void crawlURL() throws Exception {
        TestStubUtil.stubURIWithFilename("/", "index.html");
        TestStubUtil.stubURIWithFilename("/page2", "page2.html");
        TestStubUtil.stubURIWithFilename("/page3", "page3.html");
        TestStubUtil.stubURIWithFilename("/page4", "page4.html");

        CrawlerService crawlerService = new CrawlerService(4, 10, false);
        crawlerService.crawlSite("http://127.0.0.1:8065/", (s, e) -> {
            Assert.assertFalse(e.isPresent());
            System.out.println(s);
            List<Map> firstChild = (List<Map>)s.get("http://127.0.0.1:8065/");
            Assert.assertNotNull(firstChild);
            Assert.assertFalse(firstChild.isEmpty());
            Assert.assertEquals(1, firstChild.size());
            Map secondChild = firstChild.get(0);
            Assert.assertNotNull(secondChild);

            List<Map> firstGChild = (List<Map>)secondChild.get("http://127.0.0.1:8065/");
            Assert.assertFalse(firstGChild.isEmpty());
            Assert.assertEquals(2, firstGChild.size());
            Map secondGChild = firstGChild.get(0);
            Map thirdGChild = firstGChild.get(1);
            Assert.assertFalse(secondGChild.isEmpty());
            Assert.assertFalse(thirdGChild.isEmpty());

            List<Map> firstGGChild = (List<Map>)secondGChild.get("http://127.0.0.1:8065/page3");
            List<Map> secondGGChild = (List<Map>)thirdGChild.get("http://127.0.0.1:8065/page4");
            Assert.assertTrue(firstGGChild.isEmpty());
            Assert.assertTrue(secondGGChild.isEmpty());
        });


    }}
