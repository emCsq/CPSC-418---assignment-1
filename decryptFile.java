/***********************************************************
* Name: Emily Chow
* Course: CPSC 418					Term: Fall 2014
* Assignment: 1
*
* Class name: decryptFile.java
*
* Brief Description of File: The purpose of this program is to take in a user-designated
* input and decrypt it such that it can be read. The user must provide the correct 'seed'
* such that the file may be decrypted correctly. After successful decryption, the original file
* is separated from the saved message digest and a new message digest is created from the original file
* that was decrypted. Should they match, the original files' contents will be written into a file
* and a message will inform the user that the decryption was successful.
*
************************************************************/
import javax.crypto.*;
import javax.crypto.spec.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.security.SecureRandom;
import java.io.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.security.MessageDigest;

public class decryptFile {
	private static KeyGenerator keyGenerate = null;
	private static KeyGenerator newKeyGenerate = null;
	private static SecretKey realSecretKey = null;
	private static SecretKeySpec secretKeySpec = null;
	private static Cipher secretCipher = null;
	private static SecureRandom secRan = null;
	private static FileInputStream inFile = null;
	private static FileOutputStream outFile = null;

	public static void main (String[] args) throws Exception{
		try {
			if (args.length != 3) {
			System.out.println("incorrect input.\n Please type 'java secureFile [name_of_encrypt_file] [name_of_decrypt_file] [seed]");
			System.exit(0);
		}
			//open files + turn seed into an array of bytes
			inFile = new FileInputStream(args[0]);
			outFile = new FileOutputStream(args[1]);
			byte[] seed = args[2].getBytes();
			//input file becomes a byte array
			byte[] cipherToCrack = new byte[inFile.available()];
			int readBytes = inFile.read(cipherToCrack);
			
			//generate the 128 bit key
			keyGenerate = KeyGenerator.getInstance("AES");
			secRan = SecureRandom.getInstance("SHA1PRNG");
			secRan.setSeed(seed);
			keyGenerate.init(128, secRan);
			realSecretKey = keyGenerate.generateKey();
			
			//builds the initialization vector specs
			byte[] iv = new byte[16];
			
			//get key material
			byte[] temKey = realSecretKey.getEncoded();
			secretKeySpec = new SecretKeySpec(temKey, "AES");
			
			//create cipher object that'll implement AES-CBC algorithm
			secretCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			
			//do decryption on the cipherfile and store it into decryptedText
			byte[] decryptedText = null;
			secretCipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
			decryptedText = secretCipher.doFinal(cipherToCrack);
			
			//split original message and the message digest
			int mdPosition = decryptedText.length - 20;
			byte[] originalFile = new byte[mdPosition];
			byte[] savedMessageDigest = new byte[20];
			System.arraycopy(decryptedText, 0, originalFile, 0, mdPosition);
			System.arraycopy(decryptedText, mdPosition, savedMessageDigest, 0, 20);

			
			//create message digest from the resulting decrypted file
			//such that the message digest from the file will be compared with this
			// if they match, confirm that no alterations were made. Else error it.
			byte[] md = null; 
			MessageDigest sha = MessageDigest.getInstance("SHA1");
			md = sha.digest(originalFile);
			
			//will compare both message digests to ensure that nothing has been altered
			String savedMD = new String (savedMessageDigest, "UTF-8");
			String madeMD = new String (md, "UTF-8");
			
			

			if ((madeMD.equals(savedMD)) == true) {
				//Write the original message into the output file
				outFile.write(originalFile);
				System.out.println("\n===DECRYPTION WAS SUCCESSFUL===");
			} else {
				System.out.println("\nFile has been modified. Will not write to file.");
				System.exit(0);
			}
			
			
		} catch (Exception e) {
			System.out.println("\n===FAILED TO DECRYPT FILE. INCORRECT SEED.===");
		} finally {
			if (inFile != null) {
				inFile.close();
			}
			if (outFile != null) {
				outFile.close();
			}
		}
	}

}
