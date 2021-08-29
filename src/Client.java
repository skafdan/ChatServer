import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class Client {

    public static void main(String[] args){
        try{
            int port = Integer.parseInt(args[0]);
            String host = args[1];
            SSLSocketFactory factory = 
                (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket sslSocket = (SSLSocket) factory.createSocket(host,port);
            System.err.println("Connected to " + host + " on port" + port);
            new ReadWriteThread(System.in, sslSocket.getOutputStream(),"").start();
            new ReadWriteThread(sslSocket.getInputStream(), System.out,"--> ").start();
        } catch (Exception e){
            e.printStackTrace();
            System.err.println("Usage: java Client <port> <host>");
        }
    }
}