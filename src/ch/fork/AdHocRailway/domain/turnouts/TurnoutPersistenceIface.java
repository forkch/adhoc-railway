package ch.fork.AdHocRailway.domain.turnouts;

import java.util.Set;
import java.util.SortedSet;

import ch.fork.AdHocRailway.domain.turnouts.TurnoutType.TurnoutTypes;
import ch.fork.AdHocRailway.domain.turnouts.exception.TurnoutException;

import com.jgoodies.binding.list.ArrayListModel;

public interface TurnoutPersistenceIface {

	public abstract void preload() throws TurnoutException;

	@SuppressWarnings("unchecked")
	public abstract ArrayListModel<Turnout> getAllTurnouts();

	public abstract Turnout getTurnoutByNumber(int number) throws TurnoutException;

	public abstract Turnout getTurnoutByAddressBus(int bus, int address);

	public abstract void addTurnout(Turnout turnout) throws TurnoutPersistenceException;

	public abstract void deleteTurnout(Turnout turnout);

	public abstract void updateTurnout(Turnout turnout) throws TurnoutPersistenceException;

	public abstract ArrayListModel<TurnoutGroup> getAllTurnoutGroups();

	public abstract TurnoutGroup getTurnoutGroupByName(String name);

	public abstract void addTurnoutGroup(TurnoutGroup group);

	public abstract void deleteTurnoutGroup(TurnoutGroup group) throws TurnoutPersistenceException;

	public abstract void updateTurnoutGroup(TurnoutGroup group);

	public abstract SortedSet<TurnoutType> getAllTurnoutTypes();

	public abstract TurnoutType getTurnoutType(TurnoutTypes typeName);

	public int getNextFreeTurnoutNumber();

	public Set<Integer> getUsedTurnoutNumbers();
}