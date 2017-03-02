import org.h2.tools.Server;

import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    /**
     * Track to-do items.
     * Write a program that constantly loops and provides an option to list toggle and remove items.
     */

    public static void insertUser(Connection conn, String userName) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES (NULL, ?);");
        stmt.setString(1, userName);
        stmt.execute();
    }

    public static User selectUser(Connection conn, String name) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username=?;");
        stmt.setString(1,name);
        ResultSet result = stmt.executeQuery();
        if (result.next()) {
            int id = result.getInt("id");
            return new User(id, name);
        }
        return null;
    }

    public static void insertToDo(Connection conn, int ownerId, String text) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO todos VALUES (NULL, ?,?, false);");
        stmt.setInt(1, ownerId);
        stmt.setString(2, text);

        stmt.execute();
    }

    public static ArrayList<ToDoItem> selectToDos(Connection conn, int ownerId) throws SQLException {
        ArrayList<ToDoItem> items = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM todos JOIN users ON todos.owner_id = users.id WHERE owner_id = ?;");
        stmt.setInt(1, ownerId);
        ResultSet results = stmt.executeQuery();
        while(results.next()) {
            int id = results.getInt("id");
            String text = results.getString("text");
            boolean is_done  = results.getBoolean("is_done");
            items.add(new ToDoItem(id, ownerId, text, is_done));
        }
        return items;

    }

    public static ToDoItem selectToDoItem(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM todos JOIN users ON todos.owner_id = users.id WHERE todos.id = ?;");
        stmt.setInt(1, id);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            int owner = results.getInt("users.id");
            String text = results.getString("text");
            boolean isDone = results.getBoolean("is_done");
            return new ToDoItem(id, owner, text, isDone);
        }
        return null;
    }

    public static void toggleToDo(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE todos SET is_done = NOT is_done WHERE id = ?");
        stmt.setInt(1, id);
        stmt.execute();
    }


    public static void main(String[] args) throws SQLException {
        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS todos (id IDENTITY, owner_id INT, text VARCHAR, is_done BOOLEAN);");
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, username VARCHAR);");

        //ArrayList<ToDoItem> items = new ArrayList<>();      // now store in db.
        Scanner scanner = new Scanner(System.in);
        System.out.println("What is your name? ");
        Scanner reader = new Scanner(System.in);
        String name = reader.nextLine();
        User user = selectUser(conn, name);
        if(user == null) {
            insertUser(conn, name);
            user = selectUser(conn, name);
        }
        while (true) {
            System.out.println("1.) Create a To-Do item \n2.) Toggle a To-Do item \n3.) List To-Do items");
            String option = scanner.nextLine();
            if (option.equals("1")) {
                System.out.println("Enter your to-do item: ");
                String text = scanner.nextLine();
                insertToDo(conn,user.id, text);

            } else if (option.equals("2")) {
                System.out.println("Enter number of the item you want to toggle: ");
                int itemNum = Integer.parseInt(scanner.nextLine());
                toggleToDo(conn, itemNum);
            } else if (option.equals("3")) {
                //int i = 1;
                String status = "";
                ArrayList<ToDoItem> items = selectToDos(conn,user.id);
                for (ToDoItem item : items) {
                    if (item.isDone) {
                        status = " [X]";
                    } else {
                        status = " [ ]";
                    }
                    System.out.printf("%s %d. %s. \n", status, item.id, item.text);
                    //i++;
                }
                System.out.println("-----------------------");
            } else {
                System.out.println("Invalid option.  Try again.");
            }
        }
    }
}
