
import java.net.*;
import java.security.*;
import java.security.cert.*;
import java.security.spec.*;
import java.io.*;

public class ClientWithSecurity {

    public static void main(String[] args) {
        String filename = "rr.txt";

        int numBytes = 0;

        Socket clientSocket = null;

        DataOutputStream toServer = null;
        DataInputStream fromServer = null;

        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;

        long timeStarted = System.nanoTime();

        try {
            System.out.println("Establishing connection to server...");

            // Connect to server and get the input and output streams
            clientSocket = new Socket("localhost", 4321);
            toServer = new DataOutputStream(clientSocket.getOutputStream());
            fromServer = new DataInputStream(clientSocket.getInputStream());
            System.out.println("Establishing Handshake, Sending Request");
            toServer.writeInt(3);
            toServer.flush();
            int messageCode = -1;
            while (messageCode != 3) {
                messageCode = fromServer.readInt();
            }
            int messageLen = fromServer.readInt();
            System.out.println("Recieved Message Length: " + messageLen);
            byte[] message = new byte[messageLen];
            fromServer.readFully(message, 0, messageLen);

            System.out.println("Requesting for certificate");
            toServer.writeInt(4);
            toServer.flush();

            while (messageCode != 4) {
                messageCode = fromServer.readInt();
            }

            int certLen = fromServer.readInt();
            byte[] serverCertInBytes = new byte[certLen];
            fromServer.readFully(serverCertInBytes, 0, certLen);
            System.out.println("Certificate received");
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream input = new ByteArrayInputStream(serverCertInBytes);
            PublicKey publicKey;
            try {
                X509Certificate serverCert = (X509Certificate) cf.generateCertificate(input);
                System.out.println(serverCert);
                System.out.println("Checking validity");
                serverCert.checkValidity();
                publicKey = serverCert.getPublicKey();
            } catch (Exception ex) {
                System.out.println("Certificate not yet valid/expire");
                toServer.writeInt(2);
                toServer.close();
                fromServer.close();
                clientSocket.close();
            }

            //PublicKey pKey = getCAPKey("CA.crt");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        long timeTaken = System.nanoTime() - timeStarted;
        System.out.println("Program took: " + timeTaken / 1000000.0 + "ms to run");
    }

}