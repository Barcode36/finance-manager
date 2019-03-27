package com.ccacic.financemanager.model.currency.conversion;

import java.util.HashSet;
import java.util.Set;

import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventListener;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.logger.Logger;

/**
 * A looping Thread that periodicaly calls the refreshRates method in the
 * singleton instance of CurrencyExchangeFactory. Will also call the method
 * whenever a REFRESH_RATES Event is fired
 * @author Cameron Cacic
 *
 */
public class UpdateRatesThread extends Thread implements EventListener {
	
	private static final Set<UpdateRatesThread> runningThreads = new HashSet<>();
	
	/**
	 * Halts all running UpdateRatesThreads
	 */
	public static void stopAllThreads() {
		for (UpdateRatesThread updateThread: runningThreads) {
			updateThread.quitLooping();
		}
	}
	
	private static final long napTime = 1000; 
	
	private final long sleepTime;
	
	private final boolean[] quitLoop;
	private final boolean[] updateNow;
	
	/**
	 * Creates a new UpdateRatesThread with the passed long as
	 * the period between refreshes in millisconds
	 * @param sleepTime the period to sleep for before refreshing rates
	 */
	public UpdateRatesThread(long sleepTime) {
		super("UpdateRatesThread");
		this.sleepTime = sleepTime;
		
		this.updateNow = new boolean[1];
		updateNow[0] = false;
		this.quitLoop = new boolean[1];
		quitLoop[0] = false;
		
		EventManager.addListener(this, this, Event.REFRESH_RATES);
		
		runningThreads.add(this);
	}
	
	/**
	 * Signals the flag to quit looping
	 */
	public void quitLooping() {
		quitLoop[0] = true;
	}
	
	/**
	 * Signals the flag to call an update immediately
	 */
	public void updateNow() {
		updateNow[0] = true;
	}

	@Override
	public void run() {
		CurrencyExchangeFactory factory = CurrencyExchangeFactory.getInstance();
		int count = 0;
		boolean localQuitLoop;
		synchronized (quitLoop) {
			localQuitLoop = quitLoop[0];
		}
		while (!localQuitLoop) {
			
			boolean doRefresh = false;
			synchronized (updateNow) {
				if (count * napTime > sleepTime || updateNow[0]) {
					doRefresh = true;
					count = 0;
					updateNow[0] = false;
				}
			}
			
			if (doRefresh) {
				factory.refreshRates();
			}
			
			try {
				Thread.sleep(napTime);
				count++;
			} catch (InterruptedException e) {
				Logger.getInstance().logException(e);
			}
			
			synchronized (quitLoop) {
				localQuitLoop = quitLoop[0];
			}
			
		}
	}

	@Override
	public void onEvent(Event event) {
		updateNow();
	}
	
}
