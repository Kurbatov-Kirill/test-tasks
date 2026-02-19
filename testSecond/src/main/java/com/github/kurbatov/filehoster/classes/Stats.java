package com.github.kurbatov.filehoster.classes;

import java.time.LocalDateTime;

public class Stats {
    private String fileId;
    private int viewsCount;
    private int downloadsCount;
    private String filename;
    private LocalDateTime uploadTime;
    private LocalDateTime lastDownloadTime;
    private LocalDateTime terminationTime;
    private String displayingUploadTime;
    private String displayingLastDownloadTime;
    private String displayingTerminationTime;

    public Stats () {

    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public int getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(int viewsCount) {
        this.viewsCount = viewsCount;
    }

    public int getDownloadsCount() {
        return downloadsCount;
    }

    public void setDownloadsCount(int downloadsCount) {
        this.downloadsCount = downloadsCount;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public LocalDateTime getLastDownloadTime() {
        return lastDownloadTime;
    }

    public void setLastDownloadTime(LocalDateTime lastDownloadTime) {
        this.lastDownloadTime = lastDownloadTime;
    }

    public LocalDateTime getTerminationTime() {
        return terminationTime;
    }

    public String getDisplayingUploadTime() {
        return displayingUploadTime;
    }

    public void setDisplayingUploadTime(String displayingUploadTime) {
        this.displayingUploadTime = displayingUploadTime;
    }

    public String getDisplayingLastDownloadTime() {
        return displayingLastDownloadTime;
    }

    public void setDisplayingLastDownloadTime(String displayingLastDownloadTime) {
        this.displayingLastDownloadTime = displayingLastDownloadTime;
    }

    public String getDisplayingTerminationTime() {
        return displayingTerminationTime;
    }

    public void setDisplayingTerminationTime(String displayingTerminationTime) {
        this.displayingTerminationTime = displayingTerminationTime;
    }

    public void setTerminationTime(LocalDateTime terminationTime) {
        this.terminationTime = terminationTime;
    }
}
