package com.ccacic.financemanager.event;

/**
 * Represents an event that has occured. Has a type field
 * for distinguishing different types of events. Some default
 * types are avalible as public static final fields. Contains
 * an Object field for carrying any type of data associated
 * with the event. Finally, has an id field for associating
 * this event with a specific structure. See EventManager
 * for more details.
 * @author Cameron Cacic
 *
 */
public class Event {
	
	public static final String UPDATE = "update";
	public static final String NEW_ACCT_HOLDER = "new_acct_holder";
	public static final String DELETE_ACCT_HOLDER = "delete_acct_holder";
	public static final String NEW_ACCOUNT = "new_account";
	public static final String DELETE_ACCOUNT = "delete_account";
	public static final String DELETE_ENTRY_CHUNK = "delete_entry_chunk";
	public static final String NEW_ENTRY = "new_entry";
	public static final String DELETE_ENTRY = "delete_entry";
	public static final String PASSWORD_REQUEST = "password_requst";
	public static final String PASSWORD_RECEIVED = "password_received";
	public static final String REFRESH_RATES = "refresh_rates";
	public static final String RATES_REFRESHED = "rates_refreshed";
	public static final String NEW_ENTRY_CHUNK = "new_entry_chunk";
	public static final String LOAD_ARCHIVE_REQUEST = "load_archive_request";
	public static final String SAVE_ARCHIVE_REQUEST = "save_archive_request";
	public static final String CONFIRMATION_REQUEST = "confirmation_request";
	public static final String CONFIRMATION_RECEIVED = "confirmation_received";
	public static final String BLOCKING_PROGRESS_REQUEST = "blocking_progress_request";
	public static final String BLOCKING_PROGRESS_END = "blocking_progress_end";
	public static final String BLOCKING_PROGRESS_CANCELED = "blocking_progress_canceled";
	
	private final String type;
	private final Object data;
	private final String identifier;
	
	/**
	 * Creates a new Event with the given type
	 * @param type the type of the Event
	 */
	public Event(String type) {
		this(type, null, null);
	}
	
	/**
	 * Creates a new Event with the given type and data
	 * @param type the type of the Event
	 * @param data the data associated with the Event
	 */
	public Event(String type, Object data) {
		this(type, data, null);
	}
	
	/**
	 * Creates a new Event with the given type and id
	 * WARNING: This constructor is called whenever the second
	 * argument is a String. If you want to pass a String as
	 * data, use the three argument constructor with null
	 * for the id argument
	 * @param type the type of the Event
	 * @param id the id for the Event
	 */
	public Event(String type, String id) {
		this(type, null, id);
	}
	
	/**
	 * Creates a new Event with the given type, data, and id
	 * @param type the type of the Event
	 * @param data the data associated with the Event
	 * @param id the id for the event
	 */
	public Event(String type, Object data, String id) {
		this.type = type;
		this.data = data;
		this.identifier = id;
	}
	
	/**
	 * Returns the type of this Event
	 * @return the type of this Event
	 */
	public String getEventType() {
		return type;
	}
	
	/**
	 * Returns the data associated with the Event
	 * @return the data associated with the Event
	 */
	public Object getData() {
		return data;
	}
	
	/**
	 * Returns the id for the Event
	 * @return the id for the Event
	 */
	public String getIdentifier() {
		return identifier;
	}

}
