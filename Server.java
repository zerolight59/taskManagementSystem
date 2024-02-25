
import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;

class CDataBase{
    Connection con;
    Statement st;
    public CDataBase() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3308/db_tms?characterEncoding=utf8","root","");
            st = con.createStatement(); // Initialize statement only if connection is successful
        } catch(Exception e1) {
            System.out.println("CDataBase :"+e1);
        }
    }
    
    public boolean idu(String sql) {
        try {
            if (st != null) {
                int rowsAffected = st.executeUpdate(sql);
                return rowsAffected > 0;
            } else {
                System.out.println("Database connection not established!");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("server : idu \t" + e);
            return false;
        }
    }
    
    
    
    public ResultSet select(String sql){
        ResultSet rs = null;
        try {
            rs=st.executeQuery(sql);
        } catch (Exception e) {
            System.out.println("server : Select  \t"+e);
        }
        return rs;
    }
}


public class Server {
    
    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(1235);
        
        while (true) {
            Socket as = ss.accept();
            System.out.println("Client connected!"+as);
            // Create a new thread to handle the client
            ClientHandler handler = new ClientHandler(as);
            new Thread(handler).start();
        }
    }
}

class ClientHandler implements Runnable {

    private Socket clientSocket;
    User user;
    Task task;
    
    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        CDataBase db=new CDataBase(); 
        DataInputStream sin;
        DataOutputStream sout;
        Scanner s;
        String msg="";

        try {
            sin = new DataInputStream(clientSocket.getInputStream());
            sout = new DataOutputStream(clientSocket.getOutputStream());
            s = new Scanner(System.in);
            // String cAuth[]=new String[2];
            int ch;

            sout.writeUTF("1 : Register \n2 : login");
            ch=sin.readInt();  // resive the choise from the client
            if (ch == 1) {
                register(sout,sin,db);
            }
             else if (ch == 2) {
                login(sout,sin,db);
            } 
            else if (ch == 3) {
                System.out.println("Exiting...");
            }
            
        

           
            System.out.println("Client disconnected!");
        }
         catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void login(DataOutputStream sout, DataInputStream sin, CDataBase db) {
        int ch;
        
        String cUName, cPass, sql = "select * from users where username=";
        try {
            sout.writeUTF("Enter the username:");
            sout.writeUTF("Enter the password:");
            cUName = sin.readUTF();
            cPass = sin.readUTF();
            // Modify the SQL statement construction
            sql = sql + "\"" + cUName + "\" AND password=\"" + cPass + "\"";
            // System.out.println(sql);
            // Check if the login is successful
            ResultSet rs = db.select(sql);
            if (rs.next()) 
            {
                sout.writeUTF("success");
                user=new User(rs.getInt("userID"), rs.getString("username"), rs.getString("password"));
                sout.writeUTF(user.username);
                System.out.println("Client connected is "+user.username);

                sout.writeUTF("1. create task\n2. Display all the task\n3. Display pending task\n4. Update the progress");
                ch=sin.readInt();
                switch (ch) {
                    case 1:
                        createTask(sout, sin, db);
                        break;
                    case 2:
                        displayTasks(sout, sin, db);
                        break;
                    case 3:
                        displayPendingTasks(sout,sin, db);
                        break;
                    case 4:
                        updateTaskStatus(sout, sin, db);
                        break;
                    default:
                        break;
                }
            } else {
                sout.writeUTF("failure");
            }
        } catch (Exception e) {
            System.out.println("Server: login \t" + e);
        }
    }

    public void register(DataOutputStream sout,DataInputStream sin,CDataBase db){
        String cUName,cPass,sql="insert into users(username,password)values(";
        try {
            sout.writeUTF("enter the uname :");
            sout.writeUTF("enter the password :");
            sout.writeUTF("retype type the password");
            cUName=sin.readUTF();
            cPass=sin.readUTF();

            // Modify the SQL statement construction
            sql = sql + "\"" + cUName + "\",\"" + cPass + "\")";

            System.out.println(sql);

                // Check if the SQL statement is executed successfully
            if (db.idu(sql)) {
                sout.writeUTF("success");
            }
            else {
                sout.writeUTF("failure");
            }
        } catch (Exception e) {
            System.out.println("Serer :refisteration \t"+e);
        }
                    
    }
    
    public void createTask(DataOutputStream sout, DataInputStream sin, CDataBase db) {
        try {
            sout.writeUTF("select the User:");
    
            // Fetch the list of usernames from the database
            ResultSet usersResultSet = db.select("SELECT username FROM users");
    
            // Build a string containing all the usernames
            StringBuilder usernamesStringBuilder = new StringBuilder();
    
            while (usersResultSet.next()) {
                usernamesStringBuilder.append(usersResultSet.getString("username")).append(",");
            }
    
            // Send the list of usernames to the client
            sout.writeUTF(usernamesStringBuilder.toString());
    
            // Receive the selected username from the client
            String selectedUsername = sin.readUTF();
    
            // Fetch the user ID of the selected username from the database
            ResultSet userIdResultSet = db.select("SELECT userID FROM users WHERE username='" + selectedUsername + "'");
            int assigneeId = 0;  // Default value
    
            if (userIdResultSet.next()) {
                assigneeId = userIdResultSet.getInt("userID");
            }
    
            // Receive the task details from the client
            String title = sin.readUTF();
            String description = sin.readUTF();
    
            // Insert the task into the database
            String sql = "INSERT INTO tasks(title, description, status, assigneeId, creatorId) VALUES ("
                         + "'" + title + "', '" + description + "', 'Pending', " + assigneeId + ", " + user.userId + ")";
            
            // Check if the SQL statement is executed successfully
            if (db.idu(sql)) {
                sout.writeUTF("success");
            } else {
                sout.writeUTF("failure");
            }
        } catch (Exception e) {
            System.out.println("Server: createTask \t" + e);
        }
    }
    
    
    

    
    public void displayTasks(DataOutputStream sout, DataInputStream sin, CDataBase db) {
        try {
            // Fetch tasks assigned to the client from the database
            ResultSet tasksResultSet = db.select("SELECT * FROM tasks WHERE assigneeId=" + user.userId);
    
            // Check if there are any tasks
            if (!tasksResultSet.next()) {
                sout.writeUTF("No tasks assigned to you.");
            } else {
                sout.writeUTF("Tasks assigned to you:");
    
                // Process and concatenate all task details
                do {
                    int taskId = tasksResultSet.getInt("taskId");
                    String title = tasksResultSet.getString("title");
                    //String description = tasksResultSet.getString("description");
                    String status = tasksResultSet.getString("status");
    
                    // Customize the display format based on your requirements
                    sout.writeUTF("Task ID: " + taskId);
                    sout.writeUTF("Title: " + title);
                    // sout.writeUTF("Description: " + description);
                    sout.writeUTF("Status: " + status);
                    sout.writeUTF("--------------------");
    
                } while (tasksResultSet.next());
    
                // Signal the end of task details
                sout.writeUTF("end_of_task_list");
            }
        } catch (Exception e) {
            System.out.println("Server: displayTasks \t" + e);
        }
    }
    
    public void displayPendingTasks(DataOutputStream sout, DataInputStream sin, CDataBase db) {
        try {
            // Fetch tasks assigned to the client from the database
            String c = "Pending";
            ResultSet tasksResultSet = db.select("SELECT * FROM tasks WHERE assigneeId=" + user.userId + " AND status='" + c + "'");
    
            // Check if there are any tasks
            if (!tasksResultSet.next()) {
                sout.writeUTF("No tasks assigned to you.");
            } else {
                sout.writeUTF("Tasks assigned to you:");
    
                // Process and concatenate all task details
                do {
                    int taskId = tasksResultSet.getInt("taskId");
                    String title = tasksResultSet.getString("title");
                    String description = tasksResultSet.getString("description");
                    String status = tasksResultSet.getString("status");
                    int createrID = tasksResultSet.getInt("creatorID");
    
                    // Send task details to the client
                    sout.writeUTF("Task ID: " + taskId);
                    sout.writeUTF("Title: " + title);
                    sout.writeUTF("Description: " + description);
                    sout.writeUTF("Status: " + status);
                    sout.writeUTF("Creator ID: " + createrID);
                    sout.writeUTF("--------------------");
    
                } while (tasksResultSet.next());
    
                // Signal the end of task details
                sout.writeUTF("end_of_task_list");
            }
        } catch (Exception e) {
            System.out.println("Server: displayPendingTasks \t" + e);
        }
    }
    
    public void updateTaskStatus(DataOutputStream sout, DataInputStream sin, CDataBase db) {
        try {
            sout.writeUTF("Enter the Task ID you want to update:");
            int taskId = sin.readInt();
    
            // Check if the task with the given ID exists
            ResultSet taskResultSet = db.select("SELECT * FROM tasks WHERE taskId=" + taskId + " AND assigneeID =" + user.userId);
    
            if (!taskResultSet.next()) {
                sout.writeUTF("no access " + taskId);
                return;
            }
    
            // Display the current task details
            sout.writeUTF("Current task details:");
            sout.writeUTF("Task ID: " + taskResultSet.getInt("taskId"));
            sout.writeUTF("Title: " + taskResultSet.getString("title"));
            sout.writeUTF("Description: " + taskResultSet.getString("description"));
            sout.writeUTF("Current Status: " + taskResultSet.getString("status"));
    
            sout.writeUTF("Enter the new status for the task:");
            String newStatus = sin.readUTF();
    
            // Update the status of the task in the database
            String updateSql = "UPDATE tasks SET status='" + newStatus + "' WHERE taskId=" + taskId;
            if (db.idu(updateSql)) {
                sout.writeUTF("Task status updated successfully.");
            } else {
                sout.writeUTF("Failed to update task status.");
            }
        } catch (Exception e) {
            System.out.println("Server: updateTaskStatus \t" + e);
        }
    }
    
    
}
