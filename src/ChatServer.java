import java.io.*;
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
        //ServerSocket serverSocket = new ServerSocket(port);
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

        public ClientHandler(SSLSocket socket) throws Exception{
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(),true);
            id = "Client_" + ++count;
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
    }
}

