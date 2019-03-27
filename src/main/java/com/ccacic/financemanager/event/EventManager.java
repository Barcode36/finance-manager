package com.ccacic.financemanager.event;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.ccacic.financemanager.logger.Logger;
import com.ccacic.financemanager.model.Unique;

/**
 * A utility class handling the distribution of Events throughout the system.
 * EventListeners can register themselves with EventManager to recieve the
 * appropriate Events passed to EventManager to be fired. See the method
 * descriptions for more details. Thread safe
 * @author Cameron Cacic
 *
 */
public final class EventManager {
	
	/**
	 * Blocks instantiation
	 */
	private EventManager() {
		// block instantiation
	}
	
	/**
	 * Maps Event types to a mapping of EventManager ID's to Lists of EventListeners.
	 * EventListeners are stored in WeakStrongReferences to help prevent listener memory bloat
	 */
	private static final Map<String, Map<String, List<WeakStrongReference<EventListener>>>> eventMapper = new ConcurrentHashMap<>();
	/**
	 * Holds Events with a null identifier for Event types that are yet unrecognized by EventManager
	 */
	private static final Map<String, List<Event>> droppedNullEvents = new ConcurrentHashMap<>();
	/**
	 * Stores a mapping for EventManager ID's to Objects of any kind. Backed by a WeakHashMap
	 * to allow mappings to Objects no longer in memory to be easily garbage collected
	 */
	private static final Map<Object, String> objectIdMap = Collections.synchronizedMap(new WeakHashMap<>());
	/**
	 * Allows a synchronized way to count removeThisListener occurences and handle them
	 */
	private static final List<Object> removeThisListenerStack = Collections.synchronizedList(new LinkedList<>());
	
	/**
	 * An Object for locking on the garbage collection process
	 */
	private static final Object garbageCollectionLock = new Object();
	/**
	 * Holds the references to the beans EventListeners are associated with when they are garbage collected
	 */
	private static final ReferenceQueue<Object> garbageQueue = new ReferenceQueue<>();
	/**
	 * Maps a Reference bean to an EventListener to hold a reference to them
	 */
	private static final Map<Reference<Object>, List<EventListener>> beanMap = new HashMap<>();
	
	/**
	 * A synchronized inventory of EventThreads for halting all threads at the end of the program's life
	 */
	private static final List<EventThread> eventThreadInventory = Collections.synchronizedList(new LinkedList<>());
	/**
	 * Holds EventThreads waiting to be reused, must be manually synchronized
	 */
	private static final Stack<EventThread> eventThreadStack = new Stack<>();
	/**
	 * A counter for generating EventThread ID's
	 */
	private static final int[] threadIdCounter = new int[] {0};
	
	/**
	 * Holds all Threads waiting on an Event to finish
	 */
	private static final List<Thread> eventFinishInventory = Collections.synchronizedList(new LinkedList<>());
	
	/**
	 * A Thread specifically tasked to pass an Event to a List
	 * of EventListeners before returning to a queue of threads
	 * to await a new Event
	 * @author Cameron Cacic
	 *
	 */
	private static final class EventThread extends Thread {
		
		private List<WeakStrongReference<EventListener>> pertinentListeners;
		private Event event;
		private boolean keepRunning;
		
		/**
		 * Creates a new EventThread
		 * @param pertinentListeners a List of EventListeners
		 * @param event the Event to pass
		 * @param name the name of this Thread
		 */
		public EventThread(List<WeakStrongReference<EventListener>> pertinentListeners, Event event, String name) {
			super(name);
			this.pertinentListeners = pertinentListeners;
			this.event = event;
			this.keepRunning = true;
		}
		
		/**
		 * Resets the EventThread to fire a new Event through a new List of
		 * EventListeners
		 * @param pertinentListeners
		 * @param event
		 */
		public void reset(List<WeakStrongReference<EventListener>> pertinentListeners, Event event) {
			if (this.pertinentListeners == null && this.event == null) {
				this.pertinentListeners = pertinentListeners;
				this.event = event;
			}
		}
		
		/**
		 * Returns the Event the EventThred is currently firing or finished
		 * firing
		 * @return the Event
		 */
		public Event getEvent() {
			return event;
		}
		
		/**
		 * Causes the EventThread to exit its run method at the next opportunity
		 */
		public void stopRunning() {
			keepRunning = false;
		}
		
		@Override
		public void run() {
			
			while (keepRunning) {
				
				if (pertinentListeners != null) {
					
					// loops through all listeners and fires the Event
					for (WeakStrongReference<EventListener> ref: pertinentListeners) {
						EventListener listener = ref.getRef();
						if (listener != null) {
							listener.onEvent(event);
							
							/* Expects to be interrupted by EventManager to signal
							 * removeThisListener() has been called. Checks with
							 * removeThisListenerStack to validate							 
							 */
							if (Thread.interrupted()) {
								if (!removeThisListenerStack.isEmpty()) {
									removeThisListenerStack.remove(0);
									removeListener(listener, event.getEventType(), event.getIdentifier());
								}
							}
						}
					}
					
					// sends out a notification to any threads waiting on the Event to end
					synchronized (event) {
						event.notifyAll();
					}
				}
				
				// puts the thread on the back burner to check for garbage
				Thread.currentThread().setPriority(MIN_PRIORITY);
				checkGarbage();
				Thread.currentThread().setPriority(NORM_PRIORITY);
				
				// prepares for a possible reset
				pertinentListeners = null;
				event = null;
					
				// registers itself to be run again
				synchronized (eventThreadStack) {
					eventThreadStack.push(this);
				}
				
				// waits to be run or removed if interrupted
				synchronized (this) {
					try {
						wait();
					} catch (InterruptedException e) {
						synchronized (eventThreadStack) {
							eventThreadStack.remove(this);
						}
						break;
					}
				}
				
			}
			
		}
		
	}
	
	/**
	 * A specialized Reference holder for holding either a
	 * strong reference to a T Object or a WeakReference
	 * to it in the same place
	 * @author Cameron Cacic
	 *
	 * @param <T> the type of Reference to hold
	 */
	private static class WeakStrongReference<T> {
		
		private final T strongRef;
		private final WeakReference<T> weakRef;
		private final boolean strong;
		
		/**
		 * Creates a new WeakStrongReference holding the
		 * passed Object as either a strong reference
		 * or a WeakReference, based on the strong parameter
		 * @param obj the Object to hold
		 * @param strong if the reference is to be stored as a strong
		 * reference or not
		 */
		public WeakStrongReference(T obj, boolean strong) {
			this.strong = strong;
			if (strong) {
				strongRef = obj;
				weakRef = null;
			} else {
				strongRef = null;
				weakRef = new WeakReference<>(obj);
			}
		}
		
		/**
		 * Returns the Object the Reference holds
		 * @return the Object held
		 */
		public T getRef() {
			if (strong) {
				return strongRef;
			} else {
				return weakRef.get();
			}
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			WeakStrongReference<T> ref = (WeakStrongReference<T>) obj;
			T refObj = ref.getRef();
			return strong ? refObj.equals(strongRef) : refObj.equals(weakRef.get());
		}
		
	}
	
	/**
	 * A WeakReference that passes its hashing and equality responsibilities
	 * to the reference it stores, or back to WeakReference if its reference
	 * has been garbage collected
	 * @author Cameron Cacic
	 *
	 * @param <T> the type of Object to reference
	 */
	private static class HashedWeakReference<T> extends WeakReference<T> {

		public HashedWeakReference(T referent, ReferenceQueue<? super T> q) {
			super(referent, q);
		}
		
		@Override
		public int hashCode() {
			if (get() == null) {
				return super.hashCode();
			}
			return get().hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (get() == null) {
				return super.equals(obj);
			}
			return get().equals(obj);
		}
		
	}
	
	//********************************************************************************//
	//	METHODS																		  //
	//********************************************************************************//
	
	/**
	 * Checks if the gargabe queue has an garbage collected beans,
	 * and if so removes their respective EventListeners
	 */
	private static void checkGarbage() {
		synchronized (garbageCollectionLock) {
			Reference<?> beanKey = garbageQueue.poll();
			while (beanKey != null) {
				List<EventListener> listeners = beanMap.get(beanKey);
				if (listeners != null) {
					for (EventListener listener : listeners) {
						removeListener(listener);
					}
				}
				beanKey = garbageQueue.poll();
			}
		}
	}

	/**
	 * Adds the passed EventListener to the EventManager. Associates the listener
	 * with the passed eventType and every identifier passed along with it. The
	 * listener will remain registered with EventManager until its bean expires
	 * or it is requested to be removed. The bean can be passed as null to prevent
	 * garbage collection from ever removing the listener. Null is a valid eventType.
	 * For identifiers, passing null will cause the listener to recieve all Events
	 * of eventType, regardless of their ID. Returns the listener to give a reference
	 * to it if instantiated as a laymda expression
	 * @param bean the bean to track the EventListener with
	 * @param listener the EventListener to register
	 * @param eventType the eventType to register it with
	 * @param identifiers the identifiers to associate with the listener
	 * @return the passed listener
	 */
	public static EventListener addListener(Object bean, EventListener listener, String eventType,
			String... identifiers) {

		boolean strong = bean == null || bean != listener;
		if (strong && bean != null) {
			// given a strong, nonnull bean, so add a reference to the beanMap
			HashedWeakReference<Object> reference = new HashedWeakReference<Object>(bean, garbageQueue);
			List<EventListener> listeners = beanMap.get(reference);
			if (listeners == null) {
				listeners = Collections.synchronizedList(new LinkedList<>());
				beanMap.put(reference, listeners);
			}
			listeners.add(listener);
		}
		
		// find the proper mapping for the passed eventType
		Map<String, List<WeakStrongReference<EventListener>>> idMap = eventMapper.get(eventType);
		if (idMap == null) {
			idMap = Collections.synchronizedMap(new WeakHashMap<>());
			eventMapper.put(eventType, idMap);
		}
		
		if (identifiers == null || identifiers.length == 0) {
			// no identifiers provided, so associate it with the null key
			List<WeakStrongReference<EventListener>> list = idMap.get(null);
			if (list == null) {
				list = Collections.synchronizedList(new LinkedList<>());
				idMap.put(null, list);
			}
			list.add(new WeakStrongReference<>(listener, strong));
			
		} else {
			// associate the listener with a reference and add it to the proper list
			for (String id: identifiers) {
				List<WeakStrongReference<EventListener>> list = idMap.get(id);
				if (list == null) {
					list = Collections.synchronizedList(new LinkedList<>());
					idMap.put(id, list);
				}
				WeakStrongReference<EventListener> weakStrongReference = new WeakStrongReference<>(listener, strong);
				list.add(weakStrongReference);
			}
			
		}
		
		if (droppedNullEvents.get(eventType) != null) {
			for (Event e: droppedNullEvents.get(eventType)) {
				fireEvent(e);
			}
		}
		
		return listener;
		
	}
	
	/**
	 * Logs the listener content of EventManager
	 */
	private static void logStatus() {
		if (Logger.getInstance() != null) {
			String log = "EventManager State\n";
			for (String eventType2: eventMapper.keySet()) {
				log += eventType2 + ":\n";
				for (String id: eventMapper.get(eventType2).keySet()) {
					log += "\t" + id + ": [";
					for (WeakStrongReference<EventListener> ref: eventMapper.get(eventType2).get(id)) {
						log += ref.getRef().hashCode() + ", ";
					}
					log += "]\n";
				}
			}
			Logger.getInstance().logDebug(log);
		}
	}
	
	/**
	 * Should only be called from within an EventListener's fire() method.
	 * After calling, the listener it is called within will be removed from
	 * EventManager permanently
	 */
	public static void removeThisListener() {
		Thread thread = Thread.currentThread();
		try {
			EventThread eventThread = (EventThread) thread;
			eventThread.interrupt();
		} catch (ClassCastException e) {
			throw new IllegalStateException("Attempted to remove the current EventListener while not on an Event thread");
		}
	}
	
	/**
	 * Searches for and removes all occurences of the passed listener from EventManager
	 * @param listener the listener to remove
	 */
	public static void removeListener(EventListener listener) {
		for (String key: eventMapper.keySet()) {
			removeListener(listener, key);
		}
		removeListener(listener, null);
	}
	
	/**
	 * Searches for and removes all occurences of the passed listener associated
	 * with the passed eventType
	 * @param listener the listener to remove
	 * @param eventType the eventType to search with
	 */
	public static void removeListener(EventListener listener, String eventType) {
		Map<String, List<WeakStrongReference<EventListener>>> idMap = eventMapper.get(eventType);
		for (String key: idMap.keySet()) {
			List<WeakStrongReference<EventListener>> listeners = idMap.get(key);
			listeners.remove(new WeakStrongReference<>(listener, true));
		}
		List<WeakStrongReference<EventListener>> listeners = idMap.get(null);
		listeners.remove(new WeakStrongReference<>(listener, true));
	}
	
	/**
	 * Searches for and removes all occurences of the passed listener associated
	 * with the passed eventType and ID's
	 * @param listener the listener to remove
	 * @param eventType the eventType to search with
	 * @param identifier the identifiers to search with
	 */
	public static void removeListener(EventListener listener, String eventType, String... identifier) {
		Map<String, List<WeakStrongReference<EventListener>>> idMap = eventMapper.get(eventType);
		for (String id: identifier) {
			List<WeakStrongReference<EventListener>> listeners = idMap.get(id);
			if (listeners != null) {
				listeners.remove(new WeakStrongReference<>(listener, true));
			}
		}
	}
	
	/**
	 * Removes all listeners associated with the passed ID's
	 * @param identifiers the identifiers to remove
	 */
	public static void removeListenersByIdentifiers(String... identifiers) {
		for (String key: eventMapper.keySet()) {
			Map<String, List<WeakStrongReference<EventListener>>> idMap = eventMapper.get(key);
			for (String id: identifiers) {
				idMap.remove(id);
			}
		}
	}
	
	/**
	 * Fires an Event to all relevant listeners. Wait on the Event
	 * to be notified when all listeners have been run
	 * @param event the Event to fire
	 * @return the fired Event
	 */
	public static Event fireEvent(Event event) {

		if (event == null) {
			Logger.getInstance().logInfo("Null event rejected");
			return null;
		}
		
		Map<String, List<WeakStrongReference<EventListener>>> pertinentIDMap = eventMapper.get(event.getEventType());
		
		if (pertinentIDMap == null) {
			Logger.getInstance().logInfo("Event type unregistered, storing: TYPE " + event.getEventType() + " ID " + event.getIdentifier());
			if (droppedNullEvents.get(event.getEventType()) == null) {
				droppedNullEvents.put(event.getEventType(), Collections.synchronizedList(new LinkedList<>()));
			}
			droppedNullEvents.get(event.getEventType()).add(event);
			return null;
		} else if (pertinentIDMap.get(event.getIdentifier()) == null) {
			Logger.getInstance().logInfo("Event identifier unregistered, rejected: TYPE " + event.getEventType() + " ID " + event.getIdentifier());
			return null;
		}
		
		List<WeakStrongReference<EventListener>> pertinentListeners = new LinkedList<>(pertinentIDMap.get(event.getIdentifier()));
		if (event.getIdentifier() != null && pertinentIDMap.get(null) != null) {
			pertinentListeners.addAll(pertinentIDMap.get(null));
		}
		
		if (pertinentListeners.isEmpty()) {
			Logger.getInstance().logInfo("No registered listeners, rejected: TYPE " + event.getEventType() + " ID " + event.getIdentifier());
			return null;
		}
		
		Logger.getInstance().logDebug("Event Fired: TYPE " + event.getEventType() + " ID " + event.getIdentifier());

		EventThread thread = startEventThread(pertinentListeners, event);
		
		return thread.getEvent();
		
	}
	
	/**
	 * Finds an EventThread to run and starts it
	 * @param pertinentListeners the listeners to run
	 * @param event the event to fire
	 * @return the running EventThread
	 */
	private static EventThread startEventThread(List<WeakStrongReference<EventListener>> pertinentListeners, Event event) {
		
		EventThread thread;
		
		synchronized (eventThreadStack) {
			if (eventThreadStack.isEmpty()) {
				String name = "EventThread-" + threadIdCounter[0];
				threadIdCounter[0]++;	// safe so long as it is only modified with the lock on eventThreadStack
				thread = new EventThread(pertinentListeners, event, name);
				eventThreadInventory.add(thread);
				thread.start();
			} else {
				thread = eventThreadStack.pop();
				synchronized (thread) {
					thread.reset(pertinentListeners, event);
					thread.notify();
				}
			}
		}
		
		return thread;
		
	}
	
	/**
	 * Registers a Runnable to be run when the passed Event
	 * is finished being fired. Does not check if the Event
	 * is currently being fired or ever will be fired
	 * @param event the Event to wait on
	 * @param runnable the Runnable to run
	 */
	public static void onEventFinish(Event event, Runnable runnable) {
		Thread eventFinalizeThread = new Thread(() -> {
			synchronized (event) {
				try {
					event.wait();
					runnable.run();
				} catch (InterruptedException e) {
					Logger.getInstance().logError("Event finalization thread interrupted: " + event.getEventType());
				}
			}
		});
		eventFinalizeThread.start();
		synchronized (eventFinishInventory) {
			for (int i = 0; i < eventFinishInventory.size(); i++) {
				if (!eventFinishInventory.get(i).isAlive()) {
					eventFinishInventory.remove(i);
					i--;
				}
			}
			eventFinishInventory.add(eventFinalizeThread);
		}
	}
	
	/**
	 * Creates an ID for the passed Object. The same Object passed multiple times
	 * will recieve the same ID each time. This method should be used to create ID's
	 * to associate with EventListeners in EventManager
	 * @param obj the Object to create an ID for
	 * @return the ID of the Object
	 */
	public static String getUniqueID(Object obj) {
		if (objectIdMap.containsKey(obj)) {
			return objectIdMap.get(obj);
		} else {
			String id = Unique.genUUID();
			objectIdMap.put(obj, id);
			return id;
		}
	}
	
	/**
	 * Leverages the already unique ID in Unique as the EventManager ID
	 * for the passed Unique
	 * @param unique the Unique to pull an ID from
	 * @return the ID of the Unique
	 */
	public static String getUniqueID(Unique unique) {
		return unique.getIdentifier();
	}

	/**
	 * Terminates all EventThreads. EventThreads already running
	 * through a firing will finish firing before terminating
	 */
	public static void haltEventThreads() {
		for (EventThread thread: eventThreadInventory) {
			thread.stopRunning();
			thread.interrupt();
		}
	}
	
}
