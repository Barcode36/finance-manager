package com.ccacic.financemanager.model.tag;

/**
 * Enum for tagging Objects with attributes outside their scope
 * @author Cameron Cacic
 *
 */
public enum Tag {
	
	NULL("NULL", "null"),
	CASH("Cash", "cash"),
	ASSET("Assets", "asset"),
	DEBT("Debt", "debt"),
	FIAT("Fiat", "fiat"),
	CRYPTO("Crypto", "crypto"),
	STOCK("Stock", "stock");

	/**
	 * Converts the passed ID to its corresponding Tag, or the NULL Tag
	 * if no Tag matches the ID
	 * @param id the ID to search with
	 * @return the corresponding Tag
	 */
	public static final Tag getTagByID(String id) {
		for (Tag tag: values()) {
			if (tag.getTagID().equals(id)) {
				return tag;
			}
		}
		return NULL;
	}
	
	private final String name;
	private final String tagID;
	
	/**
	 * Creates a new Tag
	 * @param name the name of the Tag, displayable to the user
	 * @param tagID the ID of the Tag
	 */
	private Tag(String name, String tagID) {
		this.name = name;
		this.tagID = tagID;
	}
	
	/**
	 * Returns the name of the Tag
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the ID of the Tag
	 * @return the ID
	 */
	public String getTagID() {
		return tagID;
	}
	
	@Override
	public String toString() {
		return tagID;
	}
	
}
