package ch.fork.AdHocRailway.domain.locomotives;

import java.util.SortedSet;

import com.jgoodies.binding.list.ArrayListModel;

public interface LocomotivePersistenceIface {
	public abstract ArrayListModel<Locomotive> getAllLocomotives();

	public abstract Locomotive getLocomotiveByBusAddress(int bus, int address);

	public abstract void addLocomotive(Locomotive locomotive);

	public abstract void deleteLocomotive(Locomotive locomotive);

	public abstract void updateLocomotive(Locomotive locomotive);

	public abstract ArrayListModel<LocomotiveGroup> getAllLocomotiveGroups();

	public abstract void addLocomotiveGroup(LocomotiveGroup group);

	public abstract void deleteLocomotiveGroup(LocomotiveGroup group)
			throws LocomotivePersistenceException;

	public abstract void updateLocomotiveGroup(LocomotiveGroup group);

	public abstract SortedSet<LocomotiveType> getAllLocomotiveTypes();

	public abstract LocomotiveType getLocomotiveTypeByName(String typeName);

	public abstract void addLocomotiveType(LocomotiveType defaultType);

	public abstract void deleteLocomotiveType(LocomotiveType type)
			throws LocomotivePersistenceException;

	public abstract void clear() throws LocomotivePersistenceException;

}