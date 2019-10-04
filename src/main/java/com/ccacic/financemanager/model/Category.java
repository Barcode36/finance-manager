package com.ccacic.financemanager.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a category AccountHolders can be associated with. Controls
 * the display colors of these AccountHolder and what Accounts they are allowed
 * to hold. Has an internal name for programatic recognition and a name
 * appropriate for displaying to a user
 * @author Cameron Cacic
 *
 */
public class Category implements Comparable<Category> {
	
	private static final String CAT_NAME = "category_name";
	private static final String DISPLAY_NAME = "display_name";
	private static final String PRIMARY_COLOR = "primary_color";
	private static final String SECONDARY_COLOR = "secondary_color";
	private static final String HOLDABLE_ACCTS = "holdable_accounts";
	
	/**
	 * Creates a Category from the passed ParamMap
	 * @param paramMap the ParamMap to build the Category from
	 * @return the new Category instance
	 */
	public static Category buildCategory(ParamMap paramMap) {
		Set<String> holdAccts = new HashSet<>();
		String acctStr = paramMap.get(HOLDABLE_ACCTS);
		if (acctStr != null) {
			String[] acctSplit = acctStr.split(",");
			for (String acct: acctSplit) {
				holdAccts.add(acct.trim());
			}
		}
		return new Category(paramMap.get(CAT_NAME),
				paramMap.get(DISPLAY_NAME),
				paramMap.get(PRIMARY_COLOR),
				paramMap.get(SECONDARY_COLOR),
				holdAccts);
	}
	
	private final String catName;
	private final String displayName;
	private final String primaryColor;
	private final String secondarycolor;
	private final Set<String> holdableAccounts;
	
	/**
	 * Creates a new Category
	 * @param catName the internal name of the Category
	 * @param displayName the user appropriate name of the Category
	 * @param primaryColor the primary display color of the Category
	 * @param secondaryColor the secondary display color of the Category
	 * @param holdableAccounts the Accounts AccountHolders associated with this
	 * Category are allowed to hold
	 */
	private Category(String catName, String displayName, String primaryColor, String secondaryColor, Set<String> holdableAccounts) {
		this.catName = catName;
		this.displayName = displayName;
		this.primaryColor = primaryColor;
		this.secondarycolor = secondaryColor;
		this.holdableAccounts = holdableAccounts;
	}
	
	/**
	 * The internal name of the Category
	 * @return the name
	 */
	public String getName() {
		return catName;
	}
	
	/**
	 * The user appropriate name of the Cateogry
	 * @return the display name
	 */
	public String getDisplayName() {
		return displayName;
	}
	
	/**
	 * Returns the primary color of the Category
	 * @return the primary color
	 */
	public String getPrimaryColor() {
		return primaryColor;
	}
	
	/**
	 * Returns the secondary color of the Category
	 * @return the secondary color
	 */
	public String getSecondaryColor() {
		return secondarycolor;
	}
	
	/**
	 * Returns a Set of holdable Account class names for this Category
	 * @return a Set of holdable Account class names
	 */
	public Set<String> getHoldableAccounts() {
		return holdableAccounts;
	}
	
	/**
	 * Encodes this Category into a ParamMap representation
	 * @return a ParamMap representing this Category
	 */
	public ParamMap encode() {
		ParamMap paramMap = new ParamMap();
		paramMap.put(CAT_NAME, catName);
		paramMap.put(DISPLAY_NAME, displayName);
		paramMap.put(PRIMARY_COLOR, primaryColor);
		paramMap.put(SECONDARY_COLOR, secondarycolor);
		if (!holdableAccounts.isEmpty()) {
			String holdAcctsStr = holdableAccounts.toString();
			paramMap.put(HOLDABLE_ACCTS, "{" + holdAcctsStr.substring(1, holdAcctsStr.length() - 1) + "}");
		}
		return paramMap;
	}

	@Override
	public int compareTo(Category cat) {
		if (cat == null) {
			return 1;
		}
		return catName.compareTo(cat.catName);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !getClass().equals(obj.getClass())) {
			return false;
		}
		Category cat;
		try {
			cat = (Category) obj;
		} catch (ClassCastException e) {
			return false;
		}
		return catName.equals(cat.catName);
	}
}
