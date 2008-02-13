package ch.fork.AdHocRailway.domain.locomotives;

import java.util.SortedSet;

import com.jgoodies.binding.list.ArrayListModel;

public interface LocomotivePersistenceIface {
	public abstract ArrayListModel<Locomotive> getAllLocomotives()
			throws LocomotivePersistenceException;

	public abstract Locomotive getLocomotiveByBusAddress(int bus, int address)
			throws LocomotivePersistenceException;

	public abstract void addLocomotive(Locomotive locomotive)
			throws LocomotivePersistenceException;

	public abstract void deleteLocomotive(Locomotive locomotive)
			throws LocomotivePersistenceException;

	public abstract void updateLocomotive(Locomotive locomotive)
			throws LocomotivePersistenceException;

	public abstract ArrayListModel<LocomotiveGroup> getAllLocomotiveGroups()
			throws LocomotivePersistenceException;

	public abstract void addLocomotiveGroup(LocomotiveGroup group)
			throws LocomotivePersistenceException;

	public abstract void deleteLocomotiveGroup(LocomotiveGroup group)
			throws LocomotivePersistenceException;

	public abstract void updateLocomotiveGroup(LocomotiveGroup group)
			throws LocomotivePersistenceException;

	public abstract SortedSet<LocomotiveType> getAllLocomotiveTypes()
			throws LocomotivePersistenceException;

	public abstract LocomotiveType getLocomotiveTypeByName(String typeName)
			throws LocomotivePersistenceException;

	public abstract void addLocomotiveType(LocomotiveType defaultType)
			throws LocomotivePersistenceException;

	public abstract void deleteLocomotiveType(LocomotiveType type)
			throws LocomotivePersistenceException;

	public void flush() throws LocomotivePersistenceException;

	public abstract void clear() throws LocomotivePersistenceException;

}