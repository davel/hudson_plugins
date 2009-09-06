package org.jggug.hudson.plugins.gcrawler;

public class SVNFIleInfo {

    private String url;

    private String text;

    public SVNFIleInfo(String url, String text) {
        this.url = url;
        this.text = text;
    }

    public String getUrl() {
        return url;
    }

    public String getText() {
        return text;
    }
}
