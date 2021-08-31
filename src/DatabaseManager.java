import java.sql.*;
import java.io.*;
import java.util.*;

public class DatabaseManager {
    private Connection con;
    private String host;
    private String user;
    private String pass;

    public DatabaseManager(){
        try{
            Properties prop = new Properties();
            prop.load(new FileInputStream("data.properties"));
            host = prop.getProperty("host");
            user = prop.getProperty("username");
            pass = prop.getProperty("password");

            Class.forName("org.mariadb.jdbc.Driver");
        }catch(Exception e){
            e.printStackTrace();
        }
    } 

    //Search for a username
    public String findUser(String queriedUser){
        try{
            con=DriverManager.getConnection(host,user,pass);
            PreparedStatement pStmt = con.prepareStatement(
                "SELECT * from User WHERE username=?");
            pStmt.setString(1, queriedUser);

            ResultSet rs = pStmt.executeQuery();
            if (rs.next()){
                con.close();
                return queriedUser;
            } else{
                return null;
            }
        }catch(Exception e){
            e.printStackTrace();
            return null;
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