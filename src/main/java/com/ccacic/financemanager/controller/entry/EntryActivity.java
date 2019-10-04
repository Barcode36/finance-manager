package com.ccacic.financemanager.controller.entry;

import com.ccacic.financemanager.controller.FXPopupProgActivity;
import com.ccacic.financemanager.event.ChangeEvent;
import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventManager;
import com.ccacic.financemanager.model.Delta;
import com.ccacic.financemanager.model.ParamMap;
import com.ccacic.financemanager.model.currency.Currency;
import com.ccacic.financemanager.model.entry.Entry;
import com.ccacic.financemanager.model.entry.EntryFactory;

/**
 * A progression activity for building a new Entry or editing an exisiting one
 * @author Cameron Cacic
 *
 */
public class EntryActivity extends FXPopupProgActivity<Entry> {
	
	private Entry toEdit;
	private final String accountIdentifier;

	/**
	 * Creates a new EntryActivity
	 * @param key the entry type key
	 * @param intendedCurr the Currency intended for the Entry
	 * @param toEdit the Entry to edit, or null to create a new one
	 * @param accountIdentifier the unique event ID for the Account the
	 * Entry is intended for
	 */
	public EntryActivity(String key, Currency intendedCurr, Entry toEdit, String accountIdentifier) {
		this.toEdit = toEdit;
		this.accountIdentifier = accountIdentifier;
		
		EntryFrame entryFrame = new EntryFrame(key, intendedCurr, toEdit);
		frames.add(entryFrame);
		FXEntryFrameContainer fxEntryFrameContainer = FXEntryFrameContainer.getInstance();
		frames.addAll(fxEntryFrameContainer.getFrameList(key, toEdit));
	}
	
	@Override
	protected void callPopupLoader() {
		popupStage.setTitle(toEdit != null ? "Edit Entry" : "Create New Entry");
	}
	
	@Override
	protected void initializeActivity() {
		finishButton.setText(toEdit != null ? "Save Entry Changes" : "Create Entry");
		super.initializeActivity();
	}

	@Override
	protected Entry createResult(ParamMap frameValues) {
		EntryFactory factory = EntryFactory.getInstance();
		if (toEdit != null) {
			Delta delta = factory.modifyItem(toEdit, frameValues);
			String entryId = EventManager.getUniqueID(toEdit);
			EventManager.fireEvent(new ChangeEvent(delta, entryId));
		} else {
			toEdit = factory.requestItem(frameValues);
			if (accountIdentifier != null) {
				EventManager.fireEvent(new Event(Event.NEW_ENTRY, toEdit, accountIdentifier));
			}
		}
		return toEdit;
	}
}
