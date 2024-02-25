import java.io.*;
import java.net.*;
import java.sql.ResultSet;
import java.util.*;

/**
 * server
 */
public class Client {
    public static void main(String[] args) {
        Socket cs;
        DataInputStream sin;
        DataOutputStream sout;
        Scanner s;
        String cUName;
        String cPass,cRPass;
        try {
            String msg,access;
            int ch;
            cs= new Socket("localhost",1235);
            sin = new DataInputStream(cs.getInputStream());
            sout= new DataOutputStream(cs.getOutputStream());
            s = new Scanner(System.in);
            System.out.println("\nClient\n");
            System.out.println(sin.readUTF());
            System.out.print("your choise :");
            ch=s.nextInt();s.nextLine();
            sout.writeInt(ch);  // send choise to server

            if (ch == 1) {
                // Receives the appropriate choices from the server
                register(sout, sin, s);
            } else if (ch == 2) {
                login(sout, sin, s);
            } else {
                System.exit(0);
            }
            

        } catch (Exception e) {
           System.out.println(e);
        }
    }

    public static void register(DataOutputStream sout,DataInputStream sin,Scanner s){
        String cUName;
        String cPass,cRPass,access;
        try{
                        System.out.print(sin.readUTF());
                        cUName=s.nextLine();
                        System.out.print(sin.readUTF());
                        cPass=s.nextLine();
                        System.out.print(sin.readUTF());
                        cRPass=s.nextLine();
                        while(!cRPass.equals(cPass)){
                            System.out.println("Miss match password and retype password so re enter");
                            System.out.println("enter the password:");
                            cPass=s.nextLine();
                            System.out.println("Reenter the password:");
                            cRPass=s.nextLine();
                        }
                        //pass cusename and password to server
                        sout.writeUTF(cUName);  
                        sout.writeUTF(cPass);
                        access=sin.readUTF();
                        if (!access.equals("rsucess")) {
                            System.out.println("Register Sucessfully");
                        } else {
                            System.out.println("Register UnSucessfull");
                            System.exit(0);
                        }
            }
            catch(Exception e){System.out.println(e);}
    }

    public static void login(DataOutputStream sout,DataInputStream sin,Scanner s){
        String cUName;
        int ch;
        String cPass,access;
        try{
                        System.out.print(sin.readUTF());
                        cUName = s.nextLine();
                        System.out.print(sin.readUTF());
                        cPass = s.nextLine();
                        // pass username and password to server
                        sout.writeUTF(cUName);
                        sout.writeUTF(cPass);
                        access = sin.readUTF();
                        if (access.equals("success")) {
                            System.out.println("Login Successful\nWelcome "+sin.readUTF());
                            System.out.println(sin.readUTF());
                            System.out.println("your choise :");
                            ch=s.nextInt();s.nextLine();
                            sout.writeInt(ch);
                            switch (ch) {
                                case 1:
                                    createTask(sout, sin);
                                    break;
                                case 2:
                                    displayTasks(sout, sin);
                                    break;
                                case 3:
                                    displayPendingTasks(sout, sin);
                                    break;
                                case 4:
                                    updateTaskStatus(sout, sin, s);
                                default:
                                    break;
                            }
                        } else {
                            System.out.println("Login Unsuccessful");
                            System.exit(0);
                        }
        }
        catch(Exception e){
            System.out.println(e);
        }
    }
    

    public static void createTask(DataOutputStream sout, DataInputStream sin) {
        try {
            // Prompt to select the user
            System.out.println(sin.readUTF());
    
            // Display the list of usernames to the client
            System.out.println("List of Users:");
            String userNames = sin.readUTF();
            String[] userArray = userNames.split(",");
    
            // Display the list of usernames to the user
            for (int i = 0; i < userArray.length; i++) {
                System.out.println((i + 1) + ". " + userArray[i]);
            }
    
            // Prompt the user to select the user
            System.out.print("Enter the username: ");
            String selectedUsername = new Scanner(System.in).nextLine();
    
            // Send the selected username to the server
            sout.writeUTF(selectedUsername);
    
            // Receive the task details input from the client
            System.out.print("Enter the title: ");
            String title = new Scanner(System.in).nextLine();
            sout.writeUTF(title);
    
            System.out.print("Enter the description: ");
            String description = new Scanner(System.in).nextLine();
            sout.writeUTF(description);
    
            // Server responds with success or failure
            String response = sin.readUTF();
            System.out.println(response);
        } catch (IOException e) {
            System.out.println("Error in createTask: " + e);
        }
    }
    
    

    
    
    public static void displayTasks(DataOutputStream sout, DataInputStream sin) {
        try {
            // Receive the task details from the server
            String taskDetails = sin.readUTF();
    
            // Check if there are any tasks
            if (taskDetails.equals("No tasks assigned to you.")) {
                System.out.println("No tasks assigned to you.");
            } else if (taskDetails.equals("Tasks assigned to you:")) {
                System.out.println("Tasks assigned to you:");
    
                // Continue receiving and displaying tasks until "end_of_task_list" is encountered
                while (true) {
                    // Receive the next line of task details from the server
                    String line = sin.readUTF();
    
                    // Check if it's the end of the task list
                    if (line.equals("end_of_task_list")) {
                        break;
                    }
    
                    // Split each line to extract information
                    String[] parts = line.split(": ", 2);
    
                    // Ensure that the line has at least two parts
                    if (parts.length >= 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
    
                        // Extracted information based on the key
                        switch (key) {
                            case "Task ID":
                                int taskId = Integer.parseInt(value);
                                System.out.println("Task ID: " + taskId);
                                break;
                            case "Title":
                                System.out.println("Title: " + value);
                                break;
                            // case "Description":
                            //     System.out.println("Description: " + value);
                            //     break;
                            case "Status":
                                System.out.println("Status: " + value);
                                break;
                            default:
                                // Handle unknown key or add more cases as needed
                                break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error displaying tasks: " + e);
        }
    }
    
    

    //
    
    public static void displayPendingTasks(DataOutputStream sout, DataInputStream sin) {
        try {
            // Receive the task details from the server
            String taskDetails = sin.readUTF();
    
            // Check if there are any tasks
            if (taskDetails.equals("No tasks assigned to you.")) {
                System.out.println("No tasks assigned to you.");
            } else if (taskDetails.equals("Tasks assigned to you:")) {
                System.out.println("Tasks assigned to you:");
    
                // Continue receiving and displaying tasks until "end_of_task_list" is encountered
                while (true) {
                    // Receive the next line of task details from the server
                    String line = sin.readUTF();
    
                    // Check if it's the end of the task list
                    if (line.equals("end_of_task_list")) {
                        break;
                    }
    
                    // Print the received line for debugging
                    // System.out.println("Received line: " + line);
    
                    // Split each line to extract information
                    String[] parts = line.split(": ", 2);
    
                    // Ensure that the line has at least two parts
                    if (parts.length >= 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
    
                        // Extracted information based on the key
                        switch (key) {
                            case "Task ID":
                                int taskId = Integer.parseInt(value);
                                System.out.println("Task ID: " + taskId);
                                break;
                            case "Title":
                                System.out.println("Title: " + value);
                                break;
                            case "Description":
                                System.out.println("Description: " + value);
                                break;
                            case "Status":
                                System.out.println("Status: " + value);
                                break;
                            case "Creator ID":
                                int creatorID = Integer.parseInt(value);
                                System.out.println("Creator ID: " + creatorID);
                                break;
                            
                            default:
                                // Handle unknown key or add more cases as needed
                                break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error displaying tasks: " + e);
        }
    }
    
    public static void updateTaskStatus(DataOutputStream sout, DataInputStream sin, Scanner scanner) {
        try {
            // Receive the prompt from the server
            String prompt = sin.readUTF();
            System.out.print(prompt);
    
            // Read the Task ID from the user
            int taskId = scanner.nextInt();
            scanner.nextLine();
            sout.writeInt(taskId);
    
            // Receive the response from the server
            String response = sin.readUTF();
            System.out.println(response);
    
            // Check if the user has access to the task details
            if (response.equals("Current task details:")) {
                // Receive and display the current task details from the server
                System.out.println( sin.readUTF());
                System.out.println(sin.readUTF());
                System.out.println( sin.readUTF());
                System.out.println(sin.readUTF());
    
                // Prompt the user for the new status
                System.out.print("Enter the new status for the task: ");
                // String newStatus = scanner.nextLine();
    
                System.out.println("\n1. Pending\n2. Accepted\n3. Compleated\nyour choise :");
                    int newSid = scanner.nextInt();scanner.nextLine();

                    if (newSid == 1) {
                        sout.writeUTF("Pending");;
                    } 
                    else if( newSid == 2){
                        sout.writeUTF("Accepted");
                    }
                    else if( newSid == 3){
                        sout.writeUTF("Compleated");
                    }
                    else{
                        System.out.println("wrong choise");;
                    }
    
                // Receive and display the final status update response from the server
                String updateResponse = sin.readUTF();
                System.out.println(updateResponse);
            }
        } catch (IOException e) {
            System.out.println("Error updating task status: " + e);
        }
    }
    
    
    
}