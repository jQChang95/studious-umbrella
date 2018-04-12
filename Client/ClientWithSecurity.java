
import java.net.*;
import java.security.*;
import java.security.cert.*;
import java.security.spec.*;
import java.io.*;

public class ClientWithSecurity {
    private static final String caCert = "CA.crt";

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

            //Sending greetings and waiting for signed reply
            toServer.writeInt(3);
            toServer.flush();
            int messageCode = -1;
            //wait for reply
            while (messageCode != 3) {
                messageCode = fromServer.readInt();
            }
            int messageLen = fromServer.readInt();
            byte[] message = new byte[messageLen];
            fromServer.readFully(message, 0, messageLen);

            //Request for Cert sign by CA
            System.out.println("Requesting for certificate");
            toServer.writeInt(4);
            toServer.flush();
            //wait for reply
            while (messageCode != 4) {
                messageCode = fromServer.readInt();
            }
            int certLen = fromServer.readInt();
            byte[] serverCertInBytes = new byte[certLen];
            fromServer.readFully(serverCertInBytes, 0, certLen);
            //certificate received in bytes
            System.out.println("Certificate received");

            //generating X509Certs from CA's cert and Server's cert, then verify them
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream input = new ByteArrayInputStream(serverCertInBytes);
            InputStream fis = new FileInputStream(caCert);
            X509Certificate caCertificate = (X509Certificate) cf.generateCertificate(fis);
            PublicKey caKey = caCertificate.getPublicKey();
            try {
                X509Certificate serverCert = (X509Certificate) cf.generateCertificate(input);
                System.out.println("Checking validity");
                serverCert.checkValidity();
                serverCert.verify(caKey);
            } catch (Exception ex) {
                System.out.println("Certificate not yet valid/expire");
                toServer.writeInt(2);
                toServer.close();
                fromServer.close();
                clientSocket.close();
            }
            System.out.println("Certificate verified");


        } catch (Exception ex) {
            ex.printStackTrace();
        }
        long timeTaken = System.nanoTime() - timeStarted;
        System.out.println("Program took: " + timeTaken / 1000000.0 + "ms to run");
    }

}