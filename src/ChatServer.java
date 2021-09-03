import java.io.*;
import java.util.*;
import javax.net.ssl.*;
import java.sql.*;

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
        private String id;
        private static int count = 0;
        public DatabaseManager dbm;

        public ClientHandler(SSLSocket socket) throws Exception{
            try{
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(),true);
                id = "Client_" + ++count;
                dbm = new DatabaseManager();
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
            return id;
        }

        public void run(){
            try{
                System.err.println("Accepted connection on port " + this);
                authenticate();
                send("Welcome ! you are " + this);
                String line;
                while((line = input.readLine()) != null){
                    sendAll(line,this);
                }
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                synchronized(clients){
                    clients.remove(this);
                }
                System.err.println(this + " closed connection");
            }
        }
        public void authenticate() throws Exception{
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
            send("welcome " + dbm.findUser(user));
        }
    }
}