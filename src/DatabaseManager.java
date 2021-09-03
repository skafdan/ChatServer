import java.sql.*;
import java.io.*;
import java.util.*;

public class DatabaseManager {
    private Connection con;
    private String host;
    private String user;
    private String pass;

    //MariaDB
    //https://docs.cs.cf.ac.uk/notes/accessing-mysql-with-java/
    public DatabaseManager(){
        try{
            Properties prop = new Properties();
            prop.load(new FileInputStream("data.properties"));
            host = prop.getProperty("host");
            user = prop.getProperty("username");
            pass = prop.getProperty("password");

            Class.forName("org.mariadb.jdbc.Driver");
            //con=DriverManager.getConnection(host,user,pass);
            open();
        }catch(Exception e){
            System.err.println("here");
            e.printStackTrace();
        }
    } 

    //Search for a username
    public String findUser(String queriedUser){
        try{
            PreparedStatement pStmt = con.prepareStatement(
                "SELECT * from User WHERE username=?");
            pStmt.setString(1, queriedUser);

            ResultSet rs = pStmt.executeQuery();
            if (rs.next()){
                return queriedUser;
            } else{
                return null;
            }
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    //Check passed password equals stored password in database
    public boolean checkPasswd(String user, String attmPasswd){
        try{
            PreparedStatement pStmt = con.prepareStatement(
            "Select passwd from User WHERE username=?"); 
            pStmt.setString(1, user);
            ResultSet rs = pStmt.executeQuery();
            if(rs.next()){
                if(rs.getString(1).equals(attmPasswd)){
                    return true;
                }else {
                    return false;
                }
            }else {
                return false;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //Open connection to the database
    public void open(){
        try{
            con=DriverManager.getConnection(host,user,pass);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //Close the connection to the database
    public void close(){
        try{
            con.close(); 
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}