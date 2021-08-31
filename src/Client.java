import java.io.File;
import java.security.KeyManagementException;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Client {

    public static void main(String[] args){
        try{
            int port = Integer.parseInt(args[0]);
            String host = args[1];
            DownloadKey(host);
            SSLSocketFactory factory = SSLFactoryBootstrapper();
            SSLSocket sslSocket = (SSLSocket) factory.createSocket(host,port);
            System.err.println("Connected to " + host + " on port" + port);
            new ReadWriteThread(System.in, sslSocket.getOutputStream(),"").start();
            new ReadWriteThread(sslSocket.getInputStream(), System.out,"--> ").start();
        } catch (Exception e){
            e.printStackTrace();
            System.err.println("Usage: java Client <port> <host>");
        }
    }
    public static void DownloadKey(String host) throws Exception{
        String hostUrl = "http://" + host + "/yourKEYSTORE";
        URL url = new URL(hostUrl);
        try(InputStream in = url.openStream()){
            Files.copy(in, Paths.get("yourKEYSTORE"));
        }
    }
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