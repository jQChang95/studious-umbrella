
import java.net.*;
import java.security.*;
import java.security.cert.*;
import java.security.spec.*;
import java.io.*;

public class ClientWithSecurity{

    public static void main(String[] args){
        String filename = "rr.txt";

        int numBytes = 0;

        Socket clientSocket = null;

        DataOutputStream toServer = null;
        DataInputStream fromServer = null;

        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;

        long timeStarted = System.nanoTime();

        try{
            System.out.println("Establishing connection to server...");

            // Connect to server and get the input and output streams
            clientSocket = new Socket("localhost", 4321);
			toServer = new DataOutputStream(clientSocket.getOutputStream());
            fromServer = new DataInputStream(clientSocket.getInputStream());
            System.out.println("Establishing Handshake, Sending Request");
            toServer.writeInt(3);
            toServer.flush();
            
            while(!clientSocket.isClosed()){
                int messageCode = fromServer.readInt();

                //Transfer filename
                if (messageCode == 0){

                //transfer file
                } else if (messageCode == 1){

                //close connection
                }else if (messageCode == 2){
                    // System.out.println("Closing connection...");

					// if (bufferedFileOutputStream != null) bufferedFileOutputStream.close();
					// if (bufferedFileOutputStream != null) fileOutputStream.close();
					// fromClient.close();
					// toClient.close();
                    // connectionSocket.close();
                //handshake message
                }else if (messageCode == 3){
                    int messageLen = fromServer.readInt();
                    System.out.println("Recieved Message Length: " + messageLen);
                    byte[] message = new byte[messageLen];
                    fromServer.readFully(message, 0, messageLen);
                    //PublicKey pKey = getCAPKey("CA.crt");
                }
                
                
            }
            

            




        }catch (Exception ex){
            ex.printStackTrace();
        }
        long timeTaken = System.nanoTime() - timeStarted;
        System.out.println("Program took: " + timeTaken/1000000.0 + "ms to run");
    }

    private static PublicKey getCAPKey(String caCertPath) throws Exception{
        X509Certificate CAcert = loadCertificate(caCertPath);
        CAcert.checkValidity();
        PublicKey key = CAcert.getPublicKey();
        return key;
    }
    
    private static X509Certificate loadCertificate(String filename) throws Exception {
        FileInputStream fis = new FileInputStream(filename);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(fis);
    }
}