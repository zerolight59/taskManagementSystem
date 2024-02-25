import java.util.Date;

public class Task {

    public int taskId;
    public String title;
    public String description;
    public String status;
    public int assigneeId;
    public int creatorId;
    public Date createdAt;
    public Date updatedAt;

    public Task(int taskId, String title, String description, String status, int assigneeId, int creatorId, Date createdAt, Date updatedAt) {
        this.taskId = taskId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.assigneeId = assigneeId;
        this.creatorId = creatorId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
