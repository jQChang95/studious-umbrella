
import java.net.*;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.*;
import java.security.spec.*;
import java.nio.file.Paths;
import javax.crypto.*;

import java.io.*;

public class ClientWithSecurity {
    private static final String caCert = "CA.crt";

    public static void main(String[] args) {
        String filename = "rr.txt";
        String encryptedName = "err.txt";

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

            //encrypt file
            File file = new File(filename);
            byte[] unencryptedFile = Files.readAllBytes(file.toPath());

            //TODO: FIX THIS. I CANT GET THE 117 KEY THING DONE. basically. need to generate a symmetric key, encrypt with that key, encrypt that key with rsa, and send both
            // Key symKey = generateSymmetricKey();
            // byte[] encryptedFile = encryptFile(unencryptedFile, symKey);
            // Files.write(Paths.get(encryptedName), encryptedFile);

            fileInputStream = new FileInputStream(encryptedName);
            bufferedInputStream = new BufferedInputStream(fileInputStream);

            byte[] fromFileBuffer = new byte[117];
            
            // Send the filename
			toServer.writeInt(0);
			toServer.writeInt(encryptedName.getBytes().length);
			toServer.write(encryptedName.getBytes());
            toServer.flush();
            

            // Send the file
	        for (boolean fileEnded = false; !fileEnded;) {
				numBytes = bufferedInputStream.read(fromFileBuffer);
				fileEnded = numBytes < fromFileBuffer.length;

				toServer.writeInt(1);
				toServer.writeInt(numBytes);
				toServer.write(fromFileBuffer,0,numBytes);
				toServer.flush();
			}

	        bufferedInputStream.close();
	        fileInputStream.close();

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

    public static byte[] encryptFile(byte[] fileInBytes, Key key) {
        byte[] encryptedFile = null;
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            encryptedFile = cipher.doFinal(fileInBytes);
        } catch (Exception ex) {
            System.out.println("Error in encrypting");
            ex.printStackTrace();
        }

        return encryptedFile;
    }

    public static PublicKey generateSymmetricKey() throws Exception {
        KeyGenerator generator = KeyGenerator.getInstance( "RSA" );
        generator.init(128);
		Key key = generator.generateKeyPair();
		return key;
	}
}