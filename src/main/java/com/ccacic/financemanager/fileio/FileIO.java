package com.ccacic.financemanager.fileio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import com.ccacic.financemanager.exception.MismatchedHashException;
import com.ccacic.financemanager.logger.Logger;
import com.ccacic.financemanager.model.config.GeneralConfig;

/**
 * Handles File I/O for reading Files into Strings
 * and writing String to Files, with support for
 * using hashing to confirm the validity of Files
 * and reading/writing encrypted Files
 * @author Cameron Cacic
 *
 */
public class FileIO {
	
	/**
	 * Loads the passed File and returns its contents as a String.
	 * Uses the current User's password for handling encryption, and
	 * expects the hash of the File to match the expectedHash. Does
	 * not check the hash if the expectedHash is null
	 * @param sourceFile the File to load
	 * @param expectedHash the expected hash of the File
	 * @return the File's contents as a String
	 * @throws IOException
	 */
	public String loadFile(File sourceFile, String expectedHash) throws IOException {
		return loadFile(sourceFile, expectedHash, User.getCurrentUser().getPassword());
	}
	
	/**
	 * Loads the passed File and returns its contents as a String.
	 * Uses the passed password for encryption, assumes the File
	 * is unencrypted if a null password is provided. Expects the 
	 * hash of the File to match the expectedHash. Does not check 
	 * the hash if the expectedHash is null
	 * @param sourceFile the File to load
	 * @param expectedHash the expected hash of the File
	 * @param password the password to perform decryption with
	 * @return the File's contents as a String
	 * @throws IOException
	 */
	public String loadFile(File sourceFile, String expectedHash, String password) throws IOException {
		
		Hashing hashing = new Hashing();
		InputStream stream = new FileInputStream(sourceFile);
		InputStream wrappedStream = hashing.wrapStream(stream);
		String readFile = "";
		
		if (password != null) {
			
			Encryption encryption = new Encryption(wrappedStream, password);
			byte[] bytes = encryption.getDataDecrypted();
			if (bytes == null) {
				Logger.getInstance().logError("Decryption failed on file " + sourceFile);
				return null;
			}
			wrappedStream.close();
			readFile = new String(bytes, StandardCharsets.UTF_8);
			
		} else {
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(wrappedStream, StandardCharsets.UTF_8));
			while (reader.ready()) {
				readFile += reader.readLine();
			}
			reader.close();
			
		}
		
		String hash = hashing.getHash();
		if (expectedHash != null && !expectedHash.equals(hash)) {
			throw new MismatchedHashException("Expected hash for " + sourceFile.getName() + " did not match its actual hash",
					expectedHash, hash);
		}
		return readFile;
		
	}
	
	/**
	 * Writes the passed data to the passed File using the
	 * current User's password to perform encryption
	 * @param file the File to write to
	 * @param data the data to write
	 * @return the hash of the written File
	 * @throws IOException
	 */
	public String writeToFile(File file, String data) throws IOException {
		return writeToFile(file, data, User.getCurrentUser().getPassword());
	}
	
	/**
	 * Writes the passed data to the passed File using the
	 * passed password to perform encryption. If the password
	 * is null then no encryption is performed
	 * @param file the File to write to
	 * @param data the data to write
	 * @param password the password to perform encryption with
	 * @return the hash of the written File
	 * @throws IOException
	 */
	public String writeToFile(File file, String data, String password) throws IOException {
		ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(byteOutStream, StandardCharsets.UTF_8));
		writer.write(data);
		writer.close();
		byte[] bytes = byteOutStream.toByteArray();
		return writeToFile(file, bytes, password);
	}
	
	/**
	 * Writes the passed bytes to the passed File using the
	 * current User's password to perform encryption
	 * @param file the File to write to
	 * @param bytes the bytes to write
	 * @return the hash of the written File
	 * @throws IOException
	 */
	public String writeToFile(File file, byte[] bytes) throws IOException {
		return writeToFile(file, bytes, User.getCurrentUser().getPassword());
	}
	
	/**
	 * Writes the passed bytes to the passed File using the
	 * passed password to perform encryption. If the password
	 * is null then no encryption is performed
	 * @param file the File to write to
	 * @param bytes the bytes to write
	 * @param password the password to perform encryption with
	 * @return the hash of the written File
	 * @throws IOException
	 */
	public String writeToFile(File file, byte[] bytes, String password) throws IOException {
		
		file.getParentFile().mkdirs();
		file.createNewFile();
		
		Hashing hashing = new Hashing();
		if (password != null) {
			
			Encryption encryption = new Encryption(new ByteArrayInputStream(bytes), password);
			byte[] encData = encryption.getDataEncrypted();
			if (encData == null) {
				
				GeneralConfig.getInstance().setEncrypted(false);
				OutputStream outputStream = hashing.wrapStream(new FileOutputStream(file));
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
				writer.write(new String(bytes, StandardCharsets.UTF_8));
				writer.close();
				
			} else {
				
				OutputStream byteWriter = hashing.wrapStream(new FileOutputStream(file));
				byteWriter.write(encData);
				byteWriter.close();
				
			}
			
		} else {
		
			OutputStream outputStream = hashing.wrapStream(new FileOutputStream(file));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
			writer.write(new String(bytes, StandardCharsets.UTF_8));
			writer.close();
		}
		
		return hashing.getHash();
		
	}

}
