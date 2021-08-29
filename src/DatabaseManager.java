import java.sql.*;
import java.io.*;
import java.util.*;

public class DatabaseManager {
    public DatabaseManager(){
        try{
            Properties prop = new Properties();
            prop.load(new FileInputStream("data.properties"));
            String host = prop.getProperty("host");
            String user = prop.getProperty("username");
            String pass = prop.getProperty("password");
            Class.forName("org.mariadb.jdbc.Driver");

            Connection con=DriverManager.getConnection(host,user,pass);
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("Select * from User");
            while(rs.next()){
                System.err.println(rs.getInt(1)+" "+rs.getString(2));
                con.close();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    } 
}
