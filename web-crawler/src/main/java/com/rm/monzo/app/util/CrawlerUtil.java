package com.rm.monzo.app.util;

public interface CrawlerUtil {
    String PATTERN_CSS_QUERY_SELECTION = "a[href]";
    String ATTRIBUTE_KEY_URL_SELECTION = "abs:href";
    String URL_REMOVABLE_PROTOCOL_PATTERN = "http[s]?://";
    String URL_REMOVABLE_URL_PATH_DELIMITER = "/";
    String URL_REMOVABLE_URL_INTERNAL_LINK_DELIMITER = "#";
    String URL_REMOVABLE_URL_PARAM_DELIMITER = "\\?";
    String REPLACEABLE_EMPTY_STRING = "";
    int TEN_SECONDS_IN_MILLIS = 10000;

    String UI_INTERFACE_TITLE = "Web Crawler Interface";
    String UI_ENTER_URL_LABEL = "Enter URL:";
    int UI_FRAME_WIDTH = 1000;
    int UI_FRAME_HEIGHT = 700;
    String UI_DEFAULT_CRAWL_URL_VALUE = "https://www.google.com/s";
    String UI_DEPTH_LABEL = "Depth:";
    String UI_CRAWL_BUTTON_LABEL = "Crawl";
    String UI_RADIO_BUTTON_LABEL = "Allow External link Crawling?";
    String UI_TREE_ROOT_NOTE_TEXT = "Crawled Sites";
    String UI_DEFAULT_DEPTH_VALUE = "5";
    int UI_URL_FIELD_MAX_LENGTH = 40;
    int UI_DEPTH_FIELD_MAX_LENGTH = 5;
    String UI_STATUS_VALUE_NONE_TEXT = "None";
    String UI_STATUS_VALUE_CRAWLING_TEXT = "Crawling!!";
    String UI_STATUS_ERROR_LABEL = "Error::";
    String UI_STATUS_SUCCESS_LABEL = "Success!!";
    String UI_STATUS_LABEL_TEXT = "Status:";

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
        return getAbsoluteURLWithoutQueryParams(urlString.split(URL_REMOVABLE_URL_INTERNAL_LINK_DELIMITER)[0]);

    }

    static String getAbsoluteURLWithoutQueryParams(String urlString) {
        return urlString.split(URL_REMOVABLE_URL_PARAM_DELIMITER)[0];

    }
}
