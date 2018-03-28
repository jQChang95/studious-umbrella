import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.*;
import java.security.spec.*;

public class ServerWithSecurity{

    public static void main(String[] args){
        ServerSocket welcomeSocket = null;
		Socket connectionSocket = null;
		DataOutputStream toClient = null;
        DataInputStream fromClient = null;

		FileOutputStream fileOutputStream = null;
        BufferedOutputStream bufferedFileOutputStream = null;
        
        try{
            System.out.println("Initialising...");
            welcomeSocket = new ServerSocket(4321);
            System.out.println("Waiting for Connection...");
            connectionSocket = welcomeSocket.accept();
            System.out.println("Connected");
            BufferedReader in = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            fromClient = new DataInputStream(connectionSocket.getInputStream());
            String inputLine;
            while(!(inputLine = in.readLine()).equals("")){
                System.out.println("Received from client: " + inputLine);
                if(inputLine.equals("Hello SecStore, please prove your identity!")){
                    break;
                }
            }

            String hsMessage = "Hello this is SecStore";

            byte[] signature = generateSignatureForMessage("privateServer.der", hsMessage);
            

            while(!connectionSocket.isClosed()){
                fromClient.close();
                toClient.close();
                connectionSocket.close();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static byte[] generateSignatureForMessage(String privateKeyPath, String message) throws Exception{
        PrivateKey privKey = loadPrivateKey(privateKeyPath);
        Signature s = Signature.getInstance("SHA256withRSA");
        s.initSign(privKey);
        s.update(ByteBuffer.wrap(message.getBytes()));
        byte[] signature = s.sign();
        return signature;
    }

    private static PrivateKey loadPrivateKey(String keyPath) throws Exception{
        KeyFactory kFactory = KeyFactory.getInstance("RSA");
        File privateKey = new File(keyPath);
        KeySpec ks = new PKCS8EncodedKeySpec(Files.readAllBytes(privateKey.toPath()));
        return kFactory.generatePrivate(ks);
    }


}