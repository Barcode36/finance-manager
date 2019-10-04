package com.ccacic.financemanager.fileio;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.ccacic.financemanager.launcher.Launcher;
import com.ccacic.financemanager.logger.Logger;

/**
 * Wraps input and output streams to produce a hash of the content that
 * flowed through them. Uses SHA-256 to create the hash
 * @author Cameron Cacic
 *
 */
class Hashing {
	
	private static final String algorithm = "SHA-256";
	
	private final MessageDigest messageDigest;
	private String hash;
	
	/**
	 * Creates a new, empty Hashing
	 */
	public Hashing() {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			md = null;
			Logger.getInstance().logError(e.getMessage());
			Launcher.exitImmediately();
		}
		messageDigest = md;
		hash = null;
	}
	
	/**
	 * Wraps the passed InputStream with the returned InputStream.
	 * Resets this Hashing
	 * @param stream the InputStream to wrap
	 * @return the wrapped InputStream
	 */
	public InputStream wrapStream(InputStream stream) {
		messageDigest.reset();
		hash = null;
		return new DigestInputStream(stream, messageDigest);
	}
	
	/**
	 * Wraps the passed OutputStream with the returned OutputStream.
	 * Resets this Hashing
	 * @param stream the OutputStream to wrap
	 * @return the wrapped OutputStream
	 */
	public OutputStream wrapStream(OutputStream stream) {
		messageDigest.reset();
		hash = null;
		return new DigestOutputStream(stream, messageDigest);
	}
	
	/**
	 * Returns the hash of the wrapped stream. This finalizes
	 * the hash; subsequent calls to this method will return
	 * the same hash value regardless of new data flowing
	 * through the wrapped stream. To reuse this Hashing
	 * object, a new stream should be wrapped
	 * @return the hash of the wrapped stream
	 */
	public String getHash() {
		if (hash == null) {
			byte[] bytes = messageDigest.digest();
			StringBuilder stringBuilder = new StringBuilder(bytes.length * 2);
			for (byte b: bytes) {
				stringBuilder.append(String.format("%02X", b));
			}
			hash = stringBuilder.toString();
		}
		return hash;
	}

}
