import java.io.*;
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
                System.err.println("Accepted connection on port " + this);
                username = authenticate();
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

        public String authenticate() throws Exception{
            String user = "";
            String password = "";
            int attempts = 0;
            while(attempts <= 4 ){
                send("Enter User:");
                user = input.readLine();
                send("Enter Password:");
                password = input.readLine();
                if(dbm.findUser(user) != null && 
                    dbm.checkPasswd(user,password)){
                        break;
                }else {
                    send("Username and password dont match");
                    attempts++;
                }
            }
            if(attempts >= 5){
                dbm.close();
                send("To many invalid attempts " +
                    "Connection closed, restart client to retry");
                throw new InvalidCredentials("Invalid credentials");
            }
            return user;
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
    }
}