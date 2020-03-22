package ch.fork.adhocrailway.ui.utils;

import javax.swing.*;

public class CoalescedEventUpdater {
  private Timer timer;

  public CoalescedEventUpdater(int delay, Runnable callback) {
      timer = new Timer(delay, e -> {
          timer.stop();
          callback.run();
      });
  }

  public void update() {
      if (!SwingUtilities.isEventDispatchThread()) {
          SwingUtilities.invokeLater(() -> {timer.restart();});
      } else {
          timer.restart();
      }
  }
}
