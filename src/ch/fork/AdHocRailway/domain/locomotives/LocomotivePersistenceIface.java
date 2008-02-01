package ch.fork.AdHocRailway.domain.locomotives;

import java.util.SortedSet;

import com.jgoodies.binding.list.ArrayListModel;

public interface LocomotivePersistenceIface {

	public abstract void preload();

	/**
	 * Get a SortedSet of Locomotives.
	 * 
	 * @return locomotives
	 */
	@SuppressWarnings("unchecked")
	public abstract ArrayListModel<Locomotive> getAllLocomotives();

	/**
	 * Get a SortedSet of Locomotives.
	 * 
	 * @return locomotives
	 */
	@SuppressWarnings("unchecked")
	public abstract Locomotive getLocomotiveByNumber(int number);

	/**
	 * Get a SortedSet of Locomotives.
	 * 
	 * @return locomotives
	 */
	@SuppressWarnings("unchecked")
	public abstract Locomotive getLocomotiveByBusAddress(int bus, int address);

	public abstract void addLocomotive(Locomotive locomotive);

	public abstract void deleteLocomotive(Locomotive locomotive);

	public abstract void updateLocomotive(Locomotive locomotive);


	@SuppressWarnings("unchecked")
	public abstract ArrayListModel<LocomotiveGroup> getAllLocomotiveGroups();

	public abstract void addLocomotiveGroup(LocomotiveGroup group);

	public abstract void deleteLocomotiveGroup(LocomotiveGroup group);

	public abstract void updateLocomotiveGroup(LocomotiveGroup group);


	@SuppressWarnings("unchecked")
	public abstract SortedSet<LocomotiveType> getAllLocomotiveTypes();

	public abstract LocomotiveType getLocomotiveTypeByName(String typeName);

}