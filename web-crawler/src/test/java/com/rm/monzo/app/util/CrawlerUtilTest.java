package com.rm.monzo.app.util;


import org.junit.Assert;
import org.junit.Test;

public class CrawlerUtilTest {

    @Test
    public void testSameDomainURL() {
        String url1 = "http://www.site1.com/page1?param1=value1";
        String url2 = "https://www.site1.com/page2?param2=value2";
        String url3 = "http://blog.site1.com/page3";
        String url4 = "https://blog.site2.com/page4";
        Assert.assertTrue(CrawlerUtil.isValidSameDomainURL(url1, url2));
        Assert.assertFalse(CrawlerUtil.isValidSameDomainURL(url3, url4));
        Assert.assertFalse(CrawlerUtil.isValidSameDomainURL(url2, url3));
        Assert.assertFalse(CrawlerUtil.isValidSameDomainURL(url4, url1));
    }

    @Test
    public void testSameURLInternalReference() {
        String url1 = "http://www.site1.com/page1#link1";
        String url2 = "https://www.site1.com/page1#link2";
        String url3 = "https://blog.site1.com/page1#link1";
        Assert.assertTrue(CrawlerUtil.isValidSameDomainURL(url1, url2));
        Assert.assertFalse(CrawlerUtil.isValidSameDomainURL(url2, url3));
        Assert.assertFalse(CrawlerUtil.isValidSameDomainURL(url3, url1));
    }

    @Test
    public void testTruncateQueryParam() {
        String url1 = "http://www.site1.com/page1?param1=value1";
        String url2 = "https://www.site1.com/page2?param2=value2";
        Assert.assertEquals("http://www.site1.com/page1",CrawlerUtil.getAbsoluteURLWithoutQueryParams(url1));
        Assert.assertEquals("https://www.site1.com/page2",CrawlerUtil.getAbsoluteURLWithoutQueryParams(url2));

    }

    @Test
    public void testTruncateInternalReference() {
        String url1 = "http://www.site1.com/page1#link1";
        String url2 = "https://www.site1.com/page2#link2s";
        Assert.assertEquals("http://www.site1.com/page1",CrawlerUtil.getAbsoluteURLWithoutInternalReference(url1));
        Assert.assertEquals("https://www.site1.com/page2",CrawlerUtil.getAbsoluteURLWithoutInternalReference(url2));

    }


}