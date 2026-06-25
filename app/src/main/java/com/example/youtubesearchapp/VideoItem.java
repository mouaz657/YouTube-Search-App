package com.example.youtubesearchapp;

public class VideoItem {
    private String title;
    private String description;
    private String publishTime;
    private String channelTitle;
    private String thumbnailUrl;

    public VideoItem(String title, String description, String publishTime, String channelTitle, String thumbnailUrl) {
        this.title = title;
        this.description = description;
        this.publishTime = publishTime;
        this.channelTitle = channelTitle;
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPublishTime() { return publishTime; }
    public String getChannelTitle() { return channelTitle; }
    public String getThumbnailUrl() { return thumbnailUrl; }
}
