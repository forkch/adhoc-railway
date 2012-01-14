/**
 * 
 */
package ch.fork.AdHocRailway.technical.configuration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.InputMap;
import javax.swing.KeyStroke;

/**
 * @author mnl
 *
 */
public class KeyBoardLayout {

    private String name;
    private KeyBoardLayout base = null;
    private Map<String, Set<KeyStroke>> keysByAction;
    
    public KeyBoardLayout (String name) {
        this.name = name;
        keysByAction = new HashMap<String, Set<KeyStroke>>(); 
    }

    /**
     * @return the name
     */
    protected String getName() {
        return name;
    }
    
    /**
     * @return the base
     */
    public KeyBoardLayout getBase() {
        return base;
    }

    /**
     * @param base the base to set
     */
    void setBase(KeyBoardLayout base) {
        this.base = base;
    }

    void addEntry(KeyStroke keyStroke, String action) {
        Set<KeyStroke> keyStrokes = keysByAction.get(action);
        if (keyStrokes == null) {
            keyStrokes = new HashSet<KeyStroke>();
            keysByAction.put(action, keyStrokes);
        }
        keyStrokes.add(keyStroke);
    }
    
    public Set<KeyStroke> getKeys (String action) {
        return keysByAction.get(action);
    }
    
    public void assignKeys (InputMap inputMap, String action) {
        Set<KeyStroke> keyStrokes = getKeys(action);
        if (keyStrokes == null) {
            return;
        }
        for (KeyStroke keyStroke : keyStrokes) {
            inputMap.put(keyStroke, action);
        }
    }
}
