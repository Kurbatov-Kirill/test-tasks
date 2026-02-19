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

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public int getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(int viewsCount) {
        this.viewsCount = viewsCount;
    }

    public void setDownloadsCount(int downloadsCount) {
        this.downloadsCount = downloadsCount;
    }

    public void setDisplayingUploadTime(String displayingUploadTime) {
        this.displayingUploadTime = displayingUploadTime;
    }

    public void setDisplayingLastDownloadTime(String displayingLastDownloadTime) {
        this.displayingLastDownloadTime = displayingLastDownloadTime;
    }

    public void setDisplayingTerminationTime(String displayingTerminationTime) {
        this.displayingTerminationTime = displayingTerminationTime;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public void setLastDownloadTime(LocalDateTime lastDownloadTime) {
        this.lastDownloadTime = lastDownloadTime;
    }

    public void setTerminationTime(LocalDateTime terminationTime) {
        this.terminationTime = terminationTime;
    }
}
