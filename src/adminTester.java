import java.util.*;
import java.io.*;
import java.sql.*;
import java.security.*;

public class adminTester{
    public static void main(String[] args){
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream("data.properties"));
            String host = prop.getProperty("host");
            String user = prop.getProperty("username");
            String pass = prop.getProperty("password");

            Class.forName("org.mariadb.jdbc.Driver");
            Connection con = DriverManager.getConnection(host,user,pass);

            PreparedStatement pStmt = con.prepareStatement(
                "INSERT INTO user (username, passwd, salt) VALUES (?, ?, ?)"
            );
            String salt = salt();
            System.err.println(salt);
            pStmt.setString(1, args[0]);
            pStmt.setString(2, sha(args[1],salt));
            pStmt.setString(3, salt);
            pStmt.executeQuery();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static String salt(){
        try{
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            byte[] salt = new byte[16];
            sr.nextBytes(salt);
            return new String(salt);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static String sha(String passwd, String salt){
        try{
            String passwd_plus_hash = passwd+salt;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(passwd_plus_hash.getBytes());
            return new String(bytes);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}