package ch.fork.AdHocRailway.domain.turnouts;

import java.util.Map;
import java.util.SortedSet;

import ch.fork.AdHocRailway.domain.turnouts.TurnoutType.TurnoutTypes;

public interface TurnoutPersistenceIface {

	public abstract void preload();

	@SuppressWarnings("unchecked")
	public abstract SortedSet<Turnout> getAllTurnouts();

	public abstract Turnout getTurnoutByNumber(int number);

	public abstract Turnout getTurnoutByAddressBus(int bus, int address);

	public abstract void addTurnout(Turnout turnout);

	public abstract void deleteTurnout(Turnout turnout);

	public abstract void refreshTurnout(Turnout turnout);

	public abstract void updateTurnout(Turnout turnout);

	public abstract Map<Integer, Turnout> getNumberToTurnout();

	@SuppressWarnings("unchecked")
	public abstract SortedSet<TurnoutGroup> getAllTurnoutGroups();

	public abstract TurnoutGroup getTurnoutGroupByName(String name);

	public abstract void addTurnoutGroup(TurnoutGroup group);

	public abstract void deleteTurnoutGroup(TurnoutGroup group);

	public abstract void refreshTurnoutGroup(TurnoutGroup group);

	public abstract void updateTurnoutGroup(TurnoutGroup group);

	@SuppressWarnings("unchecked")
	public abstract SortedSet<TurnoutType> getAllTurnoutTypes();

	public abstract TurnoutType getTurnoutType(TurnoutTypes typeName);

	public int getNextFreeTurnoutNumber();

}