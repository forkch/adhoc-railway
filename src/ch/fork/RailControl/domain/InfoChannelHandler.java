package ch.fork.RailControl.domain;

import ch.fork.RailControl.domain.switches.SwitchControl;
import de.dermoba.srcp.client.InfoDataListener;

public class InfoChannelHandler implements InfoDataListener {

    private SwitchControl switchControl;

    public InfoChannelHandler(SwitchControl switchControl) {
        this.switchControl = switchControl;
    }

    public void infoDataReceived(String infoData) {

    }
}
