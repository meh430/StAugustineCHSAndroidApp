package ca.staugustinechs.staugustineapp.Objects;

public class Note {
    private String title = "", contents = "", dueDate = "";
    private boolean finished = false;

    //For firebase
    public Note() {

    }

    public Note(String title, String contents, String dueDate) {
        this.title = title;
        this.contents = contents;
        this.dueDate = dueDate;
    }

    public String getTitle() {
        return title;
    }

    public String getContents() {
        return contents;
    }

    public String getDueDate() {
        return dueDate;
    }

    public boolean isDone() {
        return finished;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public void setState(boolean f) {
        finished = f;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("");
        ret.append("Title: " + title + "\n");
        ret.append("contents: " + contents + "\n");
        ret.append("dueDate: " + dueDate + "\n");
        ret.append("finished: " + finished + "\n");

        return ret.toString();
    }
}
