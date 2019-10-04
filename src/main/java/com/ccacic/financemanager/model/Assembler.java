package com.ccacic.financemanager.model;

/**
 * Directs the creation of instances of type T or subclasses of T
 * from a ParamMap. Useful when it is not known at compile time
 * exactly what subclass of T needs to be assembled from the ParamMap,
 * allowing subclasses of Assembler to handle the assembly of the
 * instance from the ParamMap
 * @author Cameron Cacic
 *
 * @param <T> the type to assemble
 */
public abstract class Assembler<T> {
	
	private final String assemblerName;
	
	/**
	 * Creates a new Assembler with the passed name
	 * @param assemblerName the name of the Assembler
	 */
	Assembler(String assemblerName) {
		this.assemblerName = assemblerName;
	}
	
	/**
	 * Returns the Assembler name
	 * @return the name
	 */
	public String getAssemblerName() {
		return assemblerName;
	}

	/**
	 * Returns an instance of type or subclass of T, assembled
	 * from the passed ParamMap. The ParamMap should follow the
	 * same format as a ParamMap created from the disassembleItem
	 * method of this Assembler. In other words, this method
	 * should map perfectly to the inverse of disassembleItem
	 * @param paramMap the ParamMap to assemble from
	 * @return the instance of type or subclass of T
	 */
	public abstract T assembleItem(ParamMap paramMap);
	
	/**
	 * Modifies the passed instance of type or subclass of T using
	 * the data present in the passed ParamMap
	 * @param item the instance to modify
	 * @param paramMap the ParamMap to get modification data from
	 * @param delta passthrough Delta to reflect the modifications made to the instance
	 */
	public abstract void modifyItem(T item, ParamMap paramMap, Delta delta);
	
	/**
	 * Disassembles the passed instance into a ParamMap. This ParamMap
	 * should follow a format such that passing the created ParamMap
	 * into assembleItem creates a deep copy of the passed instance
	 * @param item the instance to disassemble
	 * @return the ParamMap created from the disassembly
	 */
	public abstract ParamMap disassembleItem(T item);
	
}
