package jp.app.ctrl;

public class UrlEntryBean {
    private String name;
    private String path;
    private String note = "何か説明.";

    public UrlEntryBean(String name, String path, String note) {
        this.name = name;
        this.path = path;
        this.note = note;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    ///////////
    public String getPathWithTrimg() {
        if (path.length() > 36) {
            return path.substring(0, Math.min(path.length(), 36)) + "...";
        } else {
            return path;
        }
    }
}