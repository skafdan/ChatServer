import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.net.ssl.*;
import java.math.*;

public class ChatServer {
    private static List<ClientHandler> clients = new LinkedList<ClientHandler>();
    /**
     * Main ChatServer, stores connection to clients in a linked-list and 
     * creates a new thread for to handle each client.
     * @param args port
     */
    public static void main(String[] args){
        try{
            new ChatServer().startServer(Integer.parseInt(args[0]));
        } catch (Exception e){
            e.printStackTrace();
            System.err.println("Usage java ChatServer <port>");
        }
    }
    /**
     * Starts the chatServer on a specified port and creates SSL sockets
     * @param port int port to start server on 
     * @throws Exception
     */
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
    /**
     * Send a message to all clients in the list of clients.
     * @param line String message to send.
     * @param sender ClientHander which instance is sending the message.
     */
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

        private BufferedReader input; //Input stream
        private PrintWriter output; //Output stream
        private String username; 
        private String password;
        private DatabaseManager dbm; //Data base connection manager
        //Time stamp
        private DateTimeFormatter dtf = DateTimeFormatter.ofPattern( 
            "dd/MM/yy HH:mm:ss");
        private LocalDateTime now;

        public ClientHandler(SSLSocket socket) throws Exception{
            try{
                input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
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
                /** Authentication. Incase of invalid credentials throws 
                *   exception terminating connection.
                **/
                username = input.readLine();
                password = input.readLine();
                System.err.println("Accepted connection from " + this);
                authenticate(username, hasher(password,dbm.getSalt(username)));
                //Welcoming and sending missed messages
                send("Welcome ! you are " + this);
                sendAll("User \'" + username + "\' joined server",this);
                missedMessages();
                String line;
                //Loop output, appending timestamp to messages.
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
                //Notify all users of user disconnect
                sendAll("User \'" + username +"\' has left the server.", this);
                System.err.println(this + " closed connection");
            }
        }
        /**
         * Authenticate incoming connection
         * @param username String Username
         * @param password String Password
         * @throws Exception InvalidCredentials
         */
        public void authenticate(String username, String password) throws Exception{
            if(dbm.findUser(username) == null ||
                dbm.checkPasswd(username, password) == false){
                    dbm.close();
                    send("Invalid username and password");
                    throw new InvalidCredentials("Invalid credentials");
                }
        }
        /**
         * Retrieves last 50 messages from server and sends them to client
         */
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
        /**
         * Hashes the incoming password with salt from database.
         * @param passwd String password
         * @param salt String Salt
         * @return hashed password String.
         */
        public String hasher(String passwd, String salt){
            try{
                String saltedPassword = passwd+salt;
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hash = md.digest(saltedPassword.getBytes(
                     StandardCharsets.UTF_8));
                BigInteger number = new BigInteger(1, hash); 

                StringBuilder hexString = new StringBuilder(number.toString(16));
                while(hexString.length() < 32){
                    hexString.insert(0,'0');
                }

                return hexString.toString();
            }catch(Exception e){
                e.printStackTrace();
                return null;
           }
        }
    }
}