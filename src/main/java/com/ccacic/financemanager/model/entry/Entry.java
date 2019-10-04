package com.ccacic.financemanager.model.entry;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import com.ccacic.financemanager.model.Unique;

/**
 * The lowest layer of the model. Entries record the date and time they
 * occured (in the real world), a description, an amount, and a List of
 * relevant Files. Comparisons between Entries are based on their amounts,
 * however actual equality is determined by the super class.
 * @author Cameron Cacic
 *
 */
public abstract class Entry extends Unique implements Comparable<Entry> {

	private LocalDateTime dateTime;
	private String descr;
	protected double amount;
	private List<File> files;
	
	/**
	 * Assembler method for setting the dateTime
	 * @param dateTime the LocalDateTime of the Entry
	 * @return this Entry, for chaining
	 */
	Entry dateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
		return this;
	}
	
	/**
	 * Assembler method for setting the description
	 * @param descr the description of the Entry
	 * @return this Entry, for chaining
	 */
	Entry description(String descr) {
		this.descr = descr;
		return this;
	}
	
	/**
	 * Assembler method for setting the amount
	 * @param amount the amount of the Entry
	 * @return this Entry, for chaining
	 */
	Entry amount(double amount) {
		this.amount = amount;
		return this;
	}
	
	/**
	 * Assembler method for setting the files
	 * @param files the Files relevant to the Entry
	 * @return this Entry, for chaining
	 */
	Entry files(List<File> files) {
		this.files = files;
		return this;
	}
	
	/**
	 * Returns the date and time the Entry occured
	 * @return the LocalDateTime of the Entry
	 */
	public LocalDateTime getDateTime() {
		return dateTime;
	}
	
	/**
	 * Returns all the Files relevant to the Entry
	 * @return a List of Files
	 */
	public List<File> getFiles() {
		return files;
	}
	
	/**
	 * Returns the amount of the Entry
	 * @return the amount
	 */
	public double getAmount() {
		return amount;
	}
	
	/**
	 * Returns the description of the Entry
	 * @return the description
	 */
	public String getDescription() {
		return descr;
	}
	
	@Override
	public String toString() {
		String descrDispStr = getDescription();
		if (descrDispStr.length() > 25) {
			descrDispStr = descrDispStr.substring(0, 23) + "...";
		}
		int filesLen = 0;
		if (getFiles() != null) {
			filesLen = getFiles().size();
		}
		return getDateTime() + " " + getAmount() + " " + descrDispStr + " Files: " + filesLen;
	}
	
	@Override
	public int compareTo(Entry e) {
		if (e == null) {
			return 1;
		}
		if (amount < e.getAmount()) {
			return -1;
		}
		if (e.equals(this)) {
			return 0;
		}
		return 1;
	}
}
