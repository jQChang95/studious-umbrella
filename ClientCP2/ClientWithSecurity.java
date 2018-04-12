
import java.net.*;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.*;
import java.security.spec.*;
import java.nio.file.Paths;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.util.Scanner;

import java.io.*;

public class ClientWithSecurity {
    private static final String caCert = "CA.crt";

    public static void main(String[] args) {
        String filename = "Week4Psych.pdf";
        String encryptedName = "encoded" + filename;
        Scanner sc = new Scanner(System.in);
        boolean flag = true;
        while(flag){
            System.out.println("Enter file to be send to server (include ext): ");
            filename = sc.next();
            try{
                File file = new File(filename);
                flag = false;
            }catch(Exception ex){
                System.out.println("Invalid file name");
            }
        }
        

        int numBytes = 0;

        Socket clientSocket = null;

        DataOutputStream toServer = null;
        DataInputStream fromServer = null;

        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;

        SecureRandom srandom = new SecureRandom();

        long timeStarted = System.nanoTime();

        try {
            System.out.println("Establishing connection to server...");

            // Connect to server and get the input and output streams
            clientSocket = new Socket("localhost", 4321);
            toServer = new DataOutputStream(clientSocket.getOutputStream());
            fromServer = new DataInputStream(clientSocket.getInputStream());
            System.out.println("Establishing contact, Sending Request");

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
            X509Certificate serverCert = null;
            try {
                serverCert = (X509Certificate) cf.generateCertificate(input);
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

            //decrypt message send by server and checking if it matches the agreed message
            System.out.println("Checking Message");
            PublicKey serverPublicKey = serverCert.getPublicKey();
            String hsMessage = decryptMessage(message, serverPublicKey);
            if (!hsMessage.equals("Hello this is SecStore")) {
                System.out.println("Message does not match, closing connection");
                toServer.writeInt(2);
                toServer.close();
                fromServer.close();
                clientSocket.close();
            }

            //encrypt file with AES key
            File file = new File(filename);
            byte[] unencryptedFile = Files.readAllBytes(file.toPath());
            SecretKey symKey = generateAESKey();            
            byte[] encryptedFile = encryptFile(unencryptedFile, symKey);

            //encrypt key using public key
            byte[] encryptedKey = encryptKey(symKey, serverPublicKey);
            toServer.writeInt(5);
            toServer.writeInt(encryptedKey.length);
            toServer.write(encryptedKey);
            toServer.flush();

            // Send the filename
			toServer.writeInt(0);
			toServer.writeInt(filename.getBytes().length);
			toServer.write(filename.getBytes());
            toServer.flush();

            // Send the file
            toServer.writeInt(1);
			toServer.writeInt(encryptedFile.length);
			toServer.write(encryptedFile);
            toServer.flush();
            
            System.out.println("Transfer completed");
            toServer.writeInt(2);
            toServer.close();
            fromServer.close();
            clientSocket.close();
            

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        long timeTaken = System.nanoTime() - timeStarted;
        System.out.println("Program took: " + timeTaken / 1000000.0 + "ms to run");
    }

    public static String decryptMessage(byte[] encrypted, PublicKey key) {
        String output = null;
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypted = cipher.doFinal(encrypted);

            output = new String(decrypted);
        } catch (Exception ex) {
            System.out.println("Error in decrypting");
        }
        return output;
    }

    public static byte[] encryptFile(byte[] fileInBytes, SecretKey key) {
        byte[] encryptedFile = null;
        
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            encryptedFile = cipher.doFinal(fileInBytes);
        } catch (Exception ex) {
            System.out.println("Error in encrypting");
            ex.printStackTrace();
        }
        return encryptedFile;
    }

    public static byte[] encryptKey(SecretKey sKey, PublicKey pKey){
        byte[] encryptedKey = null;
        try{
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, pKey);
            encryptedKey = cipher.doFinal(sKey.getEncoded());
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return encryptedKey;
    }

    public static SecretKey generateAESKey() throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance( "AES" );
        generator.init(128);
		SecretKey key = generator.generateKey();
		return key;
    }
    
}