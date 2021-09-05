import java.io.*;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.net.ssl.*;

public class ChatServer {
    private static List<ClientHandler> clients = new LinkedList<ClientHandler>();

    public static void main(String[] args){
        try{
            new ChatServer().startServer(Integer.parseInt(args[0]));
        } catch (Exception e){
            e.printStackTrace();
            System.err.println("Usage java ChatServer <port>");
        }
    }

    private void startServer(int port) throws Exception{
        SSLServerSocketFactory factory = 
            (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
        SSLServerSocket sslServersocket = 
            (SSLServerSocket)factory.createServerSocket(port);
        System.err.println("ChatServer started");
        while(true){
            ClientHandler ch = new ClientHandler((SSLSocket)sslServersocket.accept());
            synchronized(clients){
                clients.add(ch);
            }
            ch.start();
        }
    }

    private static void sendAll(String line, ClientHandler sender){
        System.err.println("Sending '" + line + "' to : " + clients);
        synchronized(clients){
            for(ClientHandler cl : clients){
                //Dont send the message to the client that sent the message
                if(sender == cl){
                    continue;
                }
                cl.send(sender + ": " + line);
            }
        }
    }

    private static class ClientHandler extends Thread{

        private BufferedReader input;
        private PrintWriter output;
        private String username; 
        private String password;
        private DatabaseManager dbm;
        private DateTimeFormatter dtf = DateTimeFormatter.ofPattern(
            "dd/MM/yy HH:mm:ss");
        private LocalDateTime now;

        public ClientHandler(SSLSocket socket) throws Exception{
            try{
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(),true);
                dbm = new DatabaseManager();
                if(dbm.getConSuccess() == false){
                    send("Server cant connect to database");
                }
                now = LocalDateTime.now();
            }catch (Exception e){
                if(e instanceof java.sql.SQLNonTransientConnectionException){
                    send("Server Error: Could not connect to database");
                    e.printStackTrace();
                }
            }

        }

        public void send(String line){
            output.println(line);
        }

        public String toString(){
            return username;
        }

        public void run(){
            try{
                username = input.readLine();
                password = input.readLine();
                System.err.println("Accepted connection from " + this);
                authenticate(username, hasher(password,dbm.getSalt(username)));
                send("Welcome ! you are " + this);
                sendAll("User \'" + username + "\' joined server",this);
                missedMessages();
                String line;
                while((line = input.readLine()) != null){
                    dbm.storeMessage(line, this.toString());
                    line = dtf.format(now) + "] " + line ;
                    sendAll(line,this);
                }
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                synchronized(clients){
                    clients.remove(this);
                }
                sendAll("User \'" + username +"\' has left the server.", this);
                System.err.println(this + " closed connection");
            }
        }

        public void authenticate(String username, String password) throws Exception{
            if(dbm.findUser(username) == null ||
                dbm.checkPasswd(username, password) == false){
                    dbm.close();
                    send("Invalid username and password");
                    throw new InvalidCredentials("Invalid credentials");
                }
        }

        public void missedMessages(){
            try{
                ResultSet rs = dbm.lastFifty(); 
                while(rs.next()){
                    send(rs.getString(3) + ": " + rs.getDate(2) + " " + 
                    rs.getTime(2) + "] " + rs.getString(4));
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        public String hasher(String passwd, String salt){
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
}