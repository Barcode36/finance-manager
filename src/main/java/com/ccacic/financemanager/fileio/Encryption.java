package com.ccacic.financemanager.fileio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.ccacic.financemanager.logger.Logger;

/**
 * Handles password-based encryption and decryption in a single, centralized
 * class. Uses PBKDF2WithHmacSHA256 to generate the secret key and AES 
 * as the algorithm
 * @author Cameron Cacic
 *
 */
public class Encryption {
	
	/**
	 * A byte sequence placed in the head of a byte sequence before encryption.
	 * Its presence in a decrypted byte sequence is the check for proper decryption,
	 * at which point it is removed from the String
	 */
	public static final byte[] DECRYP_CHECK = "decrypted_data".getBytes(StandardCharsets.UTF_8);
	
	private final String password;
	private InputStream dataStream;

	/**
	 * Creates a new Encryption object
	 * @param dataStream the InputStream to read data from
	 * @param password the password to work with
	 */
	public Encryption(InputStream dataStream, String password) {
		this.dataStream = dataStream;
		this.password = password;
	}
	
	/**
	 * Retrieves all data from the dataStream and returns it encrypted
	 * @return the encrypted data
	 */
	public byte[] getDataEncrypted() {
		
		try {
			
			byte[] salt = new byte[8];
			Random random = new Random();
			random.nextBytes(salt);
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 10000, 128);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKeySpec skey = new SecretKeySpec(tmp.getEncoded(), "AES");
			
			byte[] initVec = new byte[128 / 8];
			random.nextBytes(initVec);
			IvParameterSpec ivParameterSpec = new IvParameterSpec(initVec);
			
			ByteArrayOutputStream dataOutput = new ByteArrayOutputStream();
			dataOutput.write(salt);
			dataOutput.write(initVec);
			
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, skey, ivParameterSpec);
			
			dataStream = new SequenceInputStream(new ByteArrayInputStream(DECRYP_CHECK), dataStream);
			processData(cipher, dataStream, dataOutput);
			
			return dataOutput.toByteArray();
			
		} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException
				| IOException e) {
			Logger.getInstance().logError(e.getMessage());
		}
		
		return null;
		
	}
	
	/**
	 * Retrieves all data from the dataStream and returns it decrypted
	 * @return the decrypted data
	 */
	public byte[] getDataDecrypted() {
		
		try {
			
			byte[] salt = new byte[8];
			byte[] initVec = new byte[128 / 8];
			dataStream.read(salt);
			dataStream.read(initVec);
			
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 10000, 128);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKeySpec skey = new SecretKeySpec(tmp.getEncoded(), "AES");
			
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, skey, new IvParameterSpec(initVec));
			
			ByteArrayOutputStream dataOutput = new ByteArrayOutputStream();
			processData(cipher, dataStream, dataOutput);
			
			byte[] rawOutput = dataOutput.toByteArray();
			for (int i = 0; i < DECRYP_CHECK.length; i++) {
				if (DECRYP_CHECK[i] != rawOutput[i]) {
					throw new IOException("Decryption failed, missing decrypt check");
				}
			}
			
			byte[] output = new byte[rawOutput.length - DECRYP_CHECK.length];
			for (int i = DECRYP_CHECK.length; i < rawOutput.length; i++) {
				output[i - DECRYP_CHECK.length] = rawOutput[i];
			}
			
			return output;
			
		} catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException
				| IOException e) {
			Logger.getInstance().logError(e.getMessage());
		}
		
		return null;
		
	}
	
	/**
	 * Processes the data in the InputStream into the OutputStream,
	 * applying the given Cipher to it
	 * @param cipher the Cipher to apply
	 * @param in the InputStream to fetch data from
	 * @param out the OutputStream to put the data in
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IOException
	 */
	private void processData(Cipher cipher, InputStream in, OutputStream out) 
		throws IllegalBlockSizeException, BadPaddingException, IOException {
		
		byte[] inputBuffer = new byte[1024];
		int len;
		while ((len = in.read(inputBuffer)) != -1) {
			byte[] outputBuffer = cipher.update(inputBuffer, 0, len);
			if (outputBuffer != null) {
				out.write(outputBuffer);
			}
		}
		byte[] outputBuffer = cipher.doFinal();
		if (outputBuffer != null) {
			out.write(outputBuffer);
		}
		
		in.close();
		out.close();
		
	}
	
}
