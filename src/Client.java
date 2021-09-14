import java.io.*;
import java.security.*;
import javax.net.ssl.*;
import java.net.*;

import java.nio.file.*;

public class Client {
    /**
     * Connects to ChatServer.
     * @param args port host username password.
     */
    public static void main(String[] args){
        try{
            int port = Integer.parseInt(args[0]);
            String host = args[1];
            DownloadKey(host);
            SSLSocketFactory factory = SSLFactoryBootstrapper();
            SSLSocket sslSocket = (SSLSocket) factory.createSocket(host,port);
            PrintWriter sender = new PrintWriter(sslSocket.getOutputStream(),true);
            //Send username and password
            sender.println(args[2]);
            sender.println(args[3]);
            System.err.println("Connected to " + host + " on port" + port);
            new ReadWriteThread(System.in, sslSocket.getOutputStream(),"").start();
            new ReadWriteThread(sslSocket.getInputStream(), System.out,"--> ").start();
        } catch (Exception e){
            if(e instanceof java.net.ConnectException ){
                System.err.println("Cant connect to address");
            }else if(e instanceof java.lang.NumberFormatException
            || e instanceof java.lang.ArrayIndexOutOfBoundsException ){
                System.err.println(
                "Usage: java -jar Client.jar <port> <host> <user> <password>");
            }else{
                e.printStackTrace();
            }
                System.exit(1); 
        }
    }
    /**
     * Downloads the SSL certificate from the web-server.
     * @param host String host of server.
     * @throws Exception java.net.ConnectException
     */
    public static void DownloadKey(String host) throws Exception{
        try{
            File file = new File("./yourKEYSTORE");
            if(file.exists()){
               file.delete(); 
            }
            String hostUrl = "http://" + host + "/yourKEYSTORE";
            URL url = new URL(hostUrl);
            InputStream in = url.openStream();
            Files.copy(in, Paths.get("yourKEYSTORE"));
        }catch(Exception e){
            if(e instanceof java.net.ConnectException){
                System.err.println("Cant download key from server");
                System.exit(1);
            }
           e.printStackTrace(); 
        }
    }
    /**
     * Creates the SSLSocketFactory using the SSL certificate.
     * @return SSLSocketFactory
     */
    public static SSLSocketFactory SSLFactoryBootstrapper(){
        try{
            final char[] password = "quack1nce4^".toCharArray();

            final KeyStore keyStore =
                 KeyStore.getInstance(new File("./yourKEYSTORE"), password);

            final TrustManagerFactory trustManagerFactory = 
                TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            final KeyManagerFactory keyManagerFactory = 
                KeyManagerFactory.getInstance("NewSunX509");
            keyManagerFactory.init(keyStore, password);

            final SSLContext context = SSLContext.getInstance("TLS");
            context.init(keyManagerFactory.getKeyManagers(), 
                trustManagerFactory.getTrustManagers(),null);
            return context.getSocketFactory();
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}