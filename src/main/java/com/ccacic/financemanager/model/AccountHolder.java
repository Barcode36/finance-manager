package com.ccacic.financemanager.model;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.model.account.Account;
import com.ccacic.financemanager.model.account.AccountAssembler;
import com.ccacic.financemanager.model.currency.Currency;

/**
 * The topmost layer of the Model. An AccountHolder is in charge
 * of holding various Accounts, knowing what currency to display
 * the total value of all the Accounts in, knowing its name, and
 * knowing what Category it's associated with
 * @author Cameron Cacic
 *
 */
public class AccountHolder extends Unique {
	
	protected static final Map<String, Category> categories = new HashMap<>();
	protected static final List<AccountHolder> accountHolders = new CopyOnWriteArrayList<>();
	
	public static final String NAME = "name";
	public static final String CATEGORY = "category";
	public static final String MAIN_CURRENCY_CODE = "main_curr_code";
	public static final String ACCOUNTS = "accounts";
	
	/**
	 * Listens for changes to the AccountHolder model to update the
	 * internal inventory
	 */
	static {
		EventManager.addListener(null, e -> {
			accountHolders.add((AccountHolder) e.getData());
		}, Event.NEW_ACCT_HOLDER);
		EventManager.addListener(null, e -> {
			accountHolders.remove((AccountHolder) e.getData());
		}, Event.DELETE_ACCT_HOLDER);
	}
	
	/**
	 * Adds a Category to AccountHolder
	 * @param cat the Category to add
	 */
	public static void addCategory(Category cat) {
		categories.put(cat.getName(), cat);
	}
	
	/**
	 * Converts a String to a Category
	 * @param cat the String to convert
	 * @return the converted Category
	 */
	public static Category getCategory(String cat) {
		return categories.get(cat);
	}
	
	/**
	 * Returns all the Categories AccountHolder is aware of
	 * @return a List of Categories
	 */
	public static List<Category> getAllCategories() {
		List<Category> list = new ArrayList<>(categories.values());
		list.sort(null);
		return list;
	}
	
	/**
	 * Returns all the AccountHolders in the AccountHolder
	 * inventory. Unmodifiable, use EventManager to make
	 * changes to the model
	 * @return an unmodifiable List of AccountHolders
	 */
	public static ReadOnlyList<AccountHolder> getAccountHolders() {
		return new ReadOnlyList<>(accountHolders);
	}
	
	/**
	 * Removes all AccountHolders from the AccountHolder inventory
	 */
	public static void clear() {
		accountHolders.clear();
	}
	
	private String name;
	private String category;
	private Currency mainCurr;
	private List<Account> accounts;
	
	/**
	 * Creates a new AccountHolder
	 * @param identifier the ID
	 * @param name the name
	 * @param category the Category
	 * @param mainCurr the main Currency to display the AccountHolder total in
	 * @param accounts the Accounts the AccountHolder is responsible for
	 */
	public AccountHolder(String identifier, String name, String category, Currency mainCurr, List<Account> accounts) {
		this.name = name;
		this.category = category;
		this.mainCurr = mainCurr;
		this.accounts = accounts;
		
		setIdentifier(identifier);
		
		final String id = EventManager.getUniqueID(this);
		
		for (Account acct: accounts) {
			String acctId = EventManager.getUniqueID(acct);
			EventManager.addListener(this, e -> {
				EventManager.fireEvent(new Event(Event.UPDATE, id));
			}, Event.UPDATE, acctId);
		}
		
		EventManager.addListener(this, e -> {
			Account newAccount = (Account) e.getData();
			accounts.add(newAccount);
			String acctId = EventManager.getUniqueID(newAccount);
			
			EventManager.addListener(this, e2 -> {
				EventManager.fireEvent(new Event(Event.UPDATE, id));
			}, Event.UPDATE, acctId);
			
			EventManager.fireEvent(new Event(Event.UPDATE, id));
		}, Event.NEW_ACCOUNT, id);
		
		EventManager.addListener(this, e -> {
			Account deleteAccount = (Account) e.getData();
			accounts.remove(deleteAccount);
			AccountAssembler.purgeEventListeners(deleteAccount);
			EventManager.fireEvent(new Event(Event.UPDATE, id));
			EventManager.removeThisListener();
		}, Event.DELETE_ACCOUNT, id);
		
	}
	
	/**
	 * Removes all EventListeners from this AccountHolder
	 * and its held Accounts
	 */
	public void purgeEventListeners() {
		final String id = EventManager.getUniqueID(this);
		EventManager.removeListenersByIdentifiers(id);
		for (Account account: accounts) {
			AccountAssembler.purgeEventListeners(account);
		}
	}
	
	/**
	 * Returns an unmodifiable List of the Accounts held by this
	 * AccountHolder. Use EventManager to modify the model
	 * @return
	 */
	public ReadOnlyList<Account> getAccounts() {
		return new ReadOnlyList<>(accounts);
	}
	
	/**
	 * Returns the summed total of all the Accounts in this AccountHolder,
	 * converted to the main Currency of this AccountHolder
	 * @return the total
	 */
	public double getTotal() {
		return getTotal(mainCurr);
	}
	
	/**
	 * Returns the summed total of all the Accounts in this AccountHolder,
	 * converted to the passed Currency
	 * @param curr the Currency to convert to
	 * @return the total
	 */
	public double getTotal(Currency curr) {
		double total = 0;
		for (Account account: accounts) {
			total += account.getTotal(curr);
		}
		return total;
	}
	
	/**
	 * Returns the Category of this AccountHolder
	 * @return the Category
	 */
	public String getCategory() {
		return category;
	}
	
	/**
	 * Returns the main Currency of this AccountHolder
	 * @return the main Currency
	 */
	public Currency getMainCurr() {
		return mainCurr;
	}
	
	/**
	 * Returns the name of this AccountHolder
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the name to display of the Category of this AccountHolder
	 * @return the Category name to display
	 */
	public String getCatDisplayName() {
		return categories.get(category).getDisplayName();
	}
	
	/**
	 * Returns the primary color use for the Category of this AccountHolder
	 * @return the primary color
	 */
	public String getPrimaryColor() {
		return categories.get(category).getPrimaryColor();
	}
	
	/**
	 * Returns the secondary color use for the Category of this AccountHolder
	 * @return the secondary color
	 */
	public String getSecondaryColor() {
		return categories.get(category).getSecondaryColor();
	}
	
	/**
	 * Returns a Set of class names of the Accounts that the Category
	 * of this AccountHolder can hold
	 * @return a Set of class names
	 */
	public Set<String> getHoldableAccounts() {
		return categories.get(category).getHoldableAccounts();
	}
	
}
