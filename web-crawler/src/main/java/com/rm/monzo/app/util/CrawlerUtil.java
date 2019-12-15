package com.rm.monzo.app.util;

public interface CrawlerUtil {
    String PATTERN_CSS_QUERY_SELECTION = "a[href]";
    String ATTRIBUTE_KEY_URL_SELECTION = "abs:href";
    String URL_REMOVABLE_PROTOCOL_PATTERN = "http[s]?://";
    String URL_REMOVABLE_URL_PATH_DELIMITER = "/";
    String URL_REMOVABLE_URL_INTERNAL_LINK_DELIMITER = "#";
    String REPLACEABLE_EMPTY_STRING = "";
    int TEN_SECONDS_IN_MILLIS = 10000;

    static boolean isValidSameDomainURL(String currentURL, String nextURL) {
        String currentDomain = getAbsoluteURLWithoutInternalReference(currentURL)
                .replaceAll(URL_REMOVABLE_PROTOCOL_PATTERN,REPLACEABLE_EMPTY_STRING)
                .split(URL_REMOVABLE_URL_PATH_DELIMITER)[0];
        String nextDomain = getAbsoluteURLWithoutInternalReference(nextURL)
                .replaceAll(URL_REMOVABLE_PROTOCOL_PATTERN, REPLACEABLE_EMPTY_STRING)
                .split(URL_REMOVABLE_URL_PATH_DELIMITER)[0];
        return currentDomain.equalsIgnoreCase(nextDomain);
    }

    static String getAbsoluteURLWithoutInternalReference(String urlString) {
        return urlString.split(URL_REMOVABLE_URL_INTERNAL_LINK_DELIMITER)[0];

    }
}
