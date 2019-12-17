package com.rm.monzo.app;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.rm.monzo.app.util.CrawlerUtil;

import com.rm.monzo.app.service.CrawlerService;

public class CrawlerApplication {
    public static void main(String... args) {
        //Creating the Frame

        JFrame mainFrame = new JFrame(CrawlerUtil.UI_INTERFACE_TITLE);
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setSize(CrawlerUtil.UI_FRAME_WIDTH, CrawlerUtil.UI_FRAME_HEIGHT);

        //Creating the inputPanel at bottom and adding components
        JPanel inputPanel = new JPanel(); // the inputPanel is not visible in output
        JLabel urlLabel = new JLabel(CrawlerUtil.UI_ENTER_URL_LABEL);


        JTextField urlTextField = new JTextField(CrawlerUtil.UI_DEFAULT_CRAWL_URL_VALUE, CrawlerUtil.UI_URL_FIELD_MAX_LENGTH); // accepts upto 10 characters
        JLabel depthLabel = new JLabel(CrawlerUtil.UI_DEPTH_LABEL);
        JTextField depthText = new JTextField(CrawlerUtil.UI_DEFAULT_DEPTH_VALUE, CrawlerUtil.UI_DEPTH_FIELD_MAX_LENGTH); // accepts upto 10 characters
        JRadioButton externalCrawlFlag = new JRadioButton(CrawlerUtil.UI_RADIO_BUTTON_LABEL);
        JButton crawlButton = new JButton(CrawlerUtil.UI_CRAWL_BUTTON_LABEL);

        inputPanel.add(urlLabel); // Components Added using Flow Layout
        inputPanel.add(urlTextField);

        inputPanel.add(depthLabel); // Components Added using Flow Layout
        inputPanel.add(depthText);
        inputPanel.add(externalCrawlFlag);
        inputPanel.add(crawlButton);

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(CrawlerUtil.UI_TREE_ROOT_NOTE_TEXT);

        JTree siteMapTree = new JTree(rootNode);
        JScrollPane treeView = new JScrollPane(siteMapTree);

        JLabel statusValueLabel = new JLabel(CrawlerUtil.UI_STATUS_VALUE_NONE_TEXT, SwingConstants.CENTER);
        crawlButton.addActionListener((l) -> {
            statusValueLabel.setText(CrawlerUtil.UI_STATUS_VALUE_CRAWLING_TEXT);
            boolean isExternalCrawlingAllowed = externalCrawlFlag.isSelected();
            siteMapTree.clearSelection();
            crawl(urlTextField.getText(), Integer.valueOf(depthText.getText()), isExternalCrawlingAllowed, (r, e) -> {
                if(e.isPresent()) {
                    statusValueLabel.setText(CrawlerUtil.UI_STATUS_ERROR_LABEL + e.get());
                } else {
                    statusValueLabel.setText(CrawlerUtil.UI_STATUS_SUCCESS_LABEL);
                    recursiveAddTreeStructure(Collections.singletonList(r), rootNode);
                }
            });
        });

        JPanel statusPanel = new JPanel();

        JLabel statusLabel = new JLabel(CrawlerUtil.UI_STATUS_LABEL_TEXT, SwingConstants.LEFT);
        statusPanel.setBackground(Color.ORANGE);
        statusPanel.add(statusLabel);
        statusPanel.add(statusValueLabel);

        //Adding Components to the mainFrame.
        mainFrame.getContentPane().add(BorderLayout.NORTH, inputPanel);
        mainFrame.getContentPane().add(BorderLayout.SOUTH, statusPanel);
        mainFrame.getContentPane().add(BorderLayout.CENTER, treeView);
        mainFrame.setVisible(true);
    }

    static void crawl(String s, int depth, boolean isExternalCrawlingAllowed, BiConsumer<Map, Optional<String>> consumer){
        // "https://docs.oracle.com/javase/tutorial/uiswing/components/tree.html";
        CrawlerService service = new CrawlerService(depth, 100, isExternalCrawlingAllowed);
        service.crawlSite(s, consumer);
    }

    private static void recursiveAddTreeStructure(List<Map> input, DefaultMutableTreeNode parent) {
        for(Map<String, List<Map>> item : input) {
            for(String key: item.keySet()) {
                DefaultMutableTreeNode child =
                        new DefaultMutableTreeNode(key);
                recursiveAddTreeStructure(item.get(key), child);
                parent.add(child);
            }
        }

    }
}
