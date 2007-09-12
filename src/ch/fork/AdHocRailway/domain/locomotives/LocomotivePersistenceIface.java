package ch.fork.AdHocRailway.domain.locomotives;

import java.util.SortedSet;

public interface LocomotivePersistenceIface {

	public abstract void preload();

	/**
	 * Get a SortedSet of Locomotives.
	 * 
	 * @return locomotives
	 */
	@SuppressWarnings("unchecked")
	public abstract SortedSet<Locomotive> getAllLocomotives();

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
	public abstract Locomotive getLocomotiveByAddress(int address);

	public abstract void addLocomotive(Locomotive locomotive);

	public abstract void deleteLocomotive(Locomotive locomotive);

	public abstract void updateLocomotive(Locomotive locomotive);

	public abstract void refreshLocomotive(Locomotive locomotive);

	@SuppressWarnings("unchecked")
	public abstract SortedSet<LocomotiveGroup> getAllLocomotiveGroups();

	public abstract void addLocomotiveGroup(LocomotiveGroup group);

	public abstract void deleteLocomotiveGroup(LocomotiveGroup group);

	public abstract void updateLocomotiveGroup(LocomotiveGroup group);

	public abstract void refreshLocomotiveGroup(LocomotiveGroup group);

	@SuppressWarnings("unchecked")
	public abstract SortedSet<LocomotiveType> getAllLocomotiveTypes();

	public abstract LocomotiveType getLocomotiveTypeByName(String typeName);

}