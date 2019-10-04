package com.ccacic.financemanager.model;

/**
 * Assembles Unique objects. See Assembler for more details
 * @author Cameron Cacic
 *
 * @param <T> the type to Assemble, extends Unique
 */
public abstract class UniqueAssembler<T extends Unique> extends Assembler<T> {
	
	public static final String ID = "id";
	
	/**
	 * Creates a new UniqueAssembler with the passed name
	 * @param assemblerName the UniqueAssembler name
	 */
    protected UniqueAssembler(String assemblerName) {
		super(assemblerName);
	}
	
	/**
	 * Begin the assembly of the Unique instance. UniqueAssembler will
	 * continue the assembly with Unique specifc fields on the returned
	 * instance. WARNING: if any assembly requires the Unique's ID and
	 * calls getIdentifier before the termination of thie method, a
	 * random ID will be returned instead of any ID contained within
	 * the passed ParamMap which should have been the Unique's ID. Instead,
	 * the method preloadIdentifier has been provided which properly
	 * instantiates the ID in the Unique instance upon completion. After
	 * calling it, getIdentifier will return the proper ID
	 * @param paramMap the ParamMap to assemble from
	 * @return the partially assembled Unique instance
	 */
	protected abstract T assembleUniqueItem(ParamMap paramMap);
	
	/**
	 * Begins the disassembly of the passed Unique instance.
	 * UniqueAssembler will continue the disassembly with Unique specific
	 * fields into the returned ParamMap
	 * @param item the instance to disassemble
	 * @return the partially filled ParamMap 
	 */
	protected abstract ParamMap disassembleUniqueItem(T item);
	
	@Override
	public T assembleItem(ParamMap paramMap) {
		T item = assembleUniqueItem(paramMap);
		item.setIdentifier(paramMap.get(ID));
		return item;
	}
	
	@Override
	public ParamMap disassembleItem(T item) {
		ParamMap paramMap = disassembleUniqueItem(item);
		paramMap.put(ID, item.getIdentifier());
		return paramMap;
	}
	
	/**
	 * Loads the ID into the passed Unique instance, sourced
	 * from the passed ParamMap. This should be called for cases
	 * where the assembly of a Unique requires its ID before the
	 * ID has been properly assembled by UniqueAssembler, as is
	 * the case within the assembleUniqueItem method
	 * @param paramMap the ParamMap to source the ID from
	 * @param item the instance to load the ID into
	 */
	protected void preloadIdentifier(ParamMap paramMap, T item) {
		item.setIdentifier(paramMap.get(ID));
	}

}
