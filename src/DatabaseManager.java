import java.sql.*;
import java.io.*;
import java.util.*;


public class DatabaseManager {
    private Connection con;
    private String host;
    private String user;
    private String pass;
    private Boolean conSuccess;

    /**
     * Connects to database using data.properties file.
     */
    public DatabaseManager(){
        try{
            Properties prop = new Properties();
            prop.load(new FileInputStream("data.properties"));
            host = prop.getProperty("host");
            user = prop.getProperty("username");
            pass = prop.getProperty("password");

            Class.forName("org.mariadb.jdbc.Driver");
            if(open() != 0){
                conSuccess = false;
            }else {
                conSuccess = true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    } 

    /**
     * Checks a user actually exists in the database. Returns the username
     * @param queriedUser User to search for
     * @return String username, null if not found.
     */
    public String findUser(String queriedUser){
        try{
            PreparedStatement pStmt = con.prepareStatement(
                "SELECT * from user WHERE username=?");
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

    /**
     * Checks the password against stored hash
     * @param user username
     * @param attmPasswd attempted password
     * @return returns a boolean true or false if a match.
     */
    public boolean checkPasswd(String user, String attmPasswd){
        try{
            PreparedStatement pStmt = con.prepareStatement(
            "Select passwd from user WHERE username=?"); 
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
    /**
     * Store a message in the database.
     * @param message String message being sent.
     * @param sender String username of sender.
     */
    public void storeMessage(String message, String sender){
        try{
            PreparedStatement pStmt = con.prepareStatement(
                "INSERT INTO message (message_user_id, message_content)" +
                "VALUES (?, ?)"
            );
            pStmt.setString(1, sender);
            pStmt.setString(2, message);
            pStmt.executeQuery();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Open a connection to the database
     * @return int 0 if successful, anything else is an error.
     */
    public int open(){
        try{
            con=DriverManager.getConnection(host,user,pass);
            return 0;
        }catch(Exception e){
            if(e instanceof java.sql.SQLNonTransientConnectionException ){
                System.err.println("Database connection failure");
                e.printStackTrace();
            }else{
                e.printStackTrace();
            }
            return 1;
        }
    }
    /**
     * Close the database connection
     */
    public void close(){
        try{
            con.close(); 
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    /**
     * Test connection
     */
    public Boolean getConSuccess(){
        return conSuccess;
    }
    /**
     * Returns the last fifty messages stored in the database, in a result set.
     * @return Result set.
     */
    public ResultSet lastFifty(){
        try{
            PreparedStatement pStmt = con.prepareStatement(
                "SELECT * FROM (SELECT * FROM message ORDER BY message_id DESC"+
                " LIMIT 50) sub ORDER BY message_id ASC"
            );
            return pStmt.executeQuery();
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    /**
     * Get the salt of a user
     * @param user Username
     * @return String salt.
     */
    public String getSalt(String user){
        try{
            PreparedStatement pStmt = con.prepareStatement(
                "SELECT salt FROM user WHERE username=?"
            );
            pStmt.setString(1,user);
            ResultSet rs = pStmt.executeQuery();
            if(rs.next()){
                return rs.getString(1);
            }else{
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}