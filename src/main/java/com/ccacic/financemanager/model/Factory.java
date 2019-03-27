package com.ccacic.financemanager.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ccacic.financemanager.logger.Logger;

/**
 * Represents an amalgamation of Assemblers that produce the same
 * type or various subclasses of T. Its power comes from directing
 * ParamMaps to the appropriate Assembler based on the reserved Type
 * field of ParamMap, allowing any subclass of T to be created from
 * the same method call based purely on the ParamMap being passed in.
 * The result of this is code that does not need to worry about what
 * kind of T is being made, as Factory and ParamMap obscure all the
 * pertinent details so long as a proper ParamMap has been created
 * and the Factory contains a relevant Assembler. Designed to work
 * as a singleton, though actual implementation of singleton properties
 * is to be handled in Factory subclasses
 * @author Cameron Cacic
 *
 * @param <T> the type of instances to produce
 */
public abstract class Factory<T> {
	
	/**
	 * Maps Assemblers to the class name they work with
	 */
	protected Map<String, Assembler<T>> assemblerMap;
	
	/**
	 * Creates a new, empty Factory
	 */
	protected Factory() {
		assemblerMap = new HashMap<>();
	}
	
	/**
	 * Adds the passed Assembler to the Factory
	 * @param assembler the Assembler to add
	 */
	public void addAssembler(Assembler<T> assembler) {
		assemblerMap.put(assembler.getAssemblerName(), assembler);
	}
	
	/**
	 * Finds the appropriate Assembler for the passed ParamMap
	 * using its reserved Type field, then uses that Assembler
	 * to assemble an instance of T or subclass of T from the
	 * ParamMap. Returns null if no Assembler can be found
	 * @param paramMap the ParamMap to assemble with
	 * @return the new instance
	 */
	public T requestItem(ParamMap paramMap) {
		Assembler<T> assembler = assemblerMap.get(paramMap.getType());
		if (assembler == null) {
			Logger.getInstance().logError("No assembler found for type " + paramMap.getType());
			return null;
		}
		T item = assembler.assembleItem(paramMap);
		
		return item;
	}
	
	/**
	 * Finds the appropriate Assemblers for the passed ParamMaps
	 * using their reserved Type fields, then uses those Assemblers
	 * to assemble their respective instances or subclasses of T from
	 * their respective ParamMaps. ParamMaps for whom an Assembler
	 * cannot be found are not assembled and thus nothing is added
	 * to the returned List
	 * @param paramMaps a List of ParamMaps
	 * @return a List of new instances, in the same order as their
	 * respective ParamMaps
	 */
	public List<T> requestItems(List<ParamMap> paramMaps) {
		if (paramMaps == null) {
			return new ArrayList<>();
		}
		List<T> list = new ArrayList<>();
		for (ParamMap paramMap: paramMaps) {
			T item = requestItem(paramMap);
			if (item != null) {
				list.add(item);
			}
		}
		return list;
	}
	
	/**
	 * Finds the appropriate Assembler for the passed ParamMap
	 * using its reserved Type field, then uses that Assembler
	 * to modify fields of the passed instance. Returns null
	 * and no modification occurs if no Assembler can be found
	 * @param item the instance to modify
	 * @param paramMap the ParamMap to modify with
	 * @return a Delta reflecting all the changes that occured
	 */
	public Delta modifyItem(T item, ParamMap paramMap) {
		Assembler<T> assembler = assemblerMap.get(paramMap.getType());
		if (assembler != null) {
			Delta delta = new Delta(item);
			assembler.modifyItem(item, paramMap, delta);
			return delta;
		}
		return null;
	}
	
	/**
	 * Finds the appropriate Assembler for the passed instance
	 * using its simple class name, then uses that Assembler
	 * to disassemble the passed instance into a new ParamMap.
	 * Returns null if no Assembler can be found
	 * @param item the instance to disassemble
	 * @return a new ParamMap representing the passed instance
	 */
	public ParamMap requestDisassembly(T item) {
		if (item == null) {
			return new ParamMap();
		}
		Assembler<T> assembler = assemblerMap.get(item.getClass().getSimpleName());
		if (assembler == null) {
			return null;
		}
		return assembler.disassembleItem(item);
	}
	
	/**
	 * Finds the appropriate Assemblers for the passed instances
	 * using their simple class name, then uses those Assemblers
	 * to disassemble their respective instances to a List of ParamMaps
	 * Instances for whom an Assembler cannot be found are not disassembled 
	 * and thus nothing is added to the returned List
	 * @param items instances to disassemble
	 * @return a List of new ParamMaps representing the passed instances
	 */
	public List<ParamMap> requestDisassembly(List<T> items) {
		if (items == null) {
			return new ArrayList<>();
		}
		List<ParamMap> list = new ArrayList<>();
		for (T item: items) {
			Assembler<T> assembler = assemblerMap.get(item.getClass().getSimpleName());
			if (assembler != null) {
				list.add(assembler.disassembleItem(item));
			}
		}
		return list;
	}
	
}
