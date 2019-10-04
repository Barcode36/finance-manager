package com.ccacic.financemanager.controller.control;

import com.ccacic.financemanager.event.Event;
import com.ccacic.financemanager.event.EventListener;
import com.ccacic.financemanager.event.EventManager;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.scene.control.TableView;

import java.util.HashMap;
import java.util.Map; 


public class UpdatableTableView<T> extends TableView<T> {
    
    private final Map<T, EventListener> currUpdateListeners;
    
	protected UpdatableTableView() {
	    super();

		currUpdateListeners = new HashMap<>();
		
		itemsProperty().addListener((obvVal, oldVal, newVal) -> {
			for (T item: currUpdateListeners.keySet()) {
				String id = EventManager.getUniqueID(item);
				EventManager.removeListener(currUpdateListeners.get(item), Event.UPDATE, id);
			}
			currUpdateListeners.clear();
			if (newVal != null) {
				newVal.addListener((ListChangeListener<T>) c -> {
					
					boolean refreshRequired = false;
					while (c.next()) {
						
						if (c.wasAdded()) {
							for (T item: c.getAddedSubList()) {
								String id = EventManager.getUniqueID(item);
								EventListener refreshListener = e -> refresh();
								EventManager.addListener(item, refreshListener, Event.UPDATE, id);
								currUpdateListeners.put(item, refreshListener);
							}
							refreshRequired = true;
						} else if (c.wasRemoved()) {
							for (T item: c.getRemoved()) {
								String id = EventManager.getUniqueID(item);
								EventManager.removeListener(currUpdateListeners.get(item), Event.UPDATE, id);
								currUpdateListeners.remove(item);
							}
							refreshRequired = true;
						}
							
					}

					if (refreshRequired) {
						refresh();
					}
					
				});
			}
		});

		prefHeightProperty().bind(Bindings.createDoubleBinding(() ->
				getItems().size() + getFixedCellSize() + 30.0, getItems(), fixedCellSizeProperty()));

	}

	/**
	 * Registers the passed value as a possible cell size. If the value is larger than the current alue, it will
	 * become the current value
	 * @param fixedCellSize the fixed cell size to register
	 */
	public void registerFixedCellSize(double fixedCellSize) {
		if (getFixedCellSize() < fixedCellSize) {
			setFixedCellSize(fixedCellSize);
		}
	}

}