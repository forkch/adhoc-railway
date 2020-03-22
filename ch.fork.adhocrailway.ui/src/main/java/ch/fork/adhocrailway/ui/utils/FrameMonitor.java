package ch.fork.adhocrailway.ui.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.prefs.Preferences;

public class FrameMonitor {

  public static void registerFrame(JFrame frame, String frameUniqueId,
                                   int defaultX, int defaultY, int defaultW, int defaultH) {
      Preferences prefs = Preferences.userRoot()
                                     .node(FrameMonitor.class.getSimpleName() + "-" + frameUniqueId);
      frame.setLocation(getFrameLocation(prefs, defaultX, defaultY));
      frame.setSize(getFrameSize(prefs, defaultW, defaultH));

      CoalescedEventUpdater updater = new CoalescedEventUpdater(400,
              () -> updatePref(frame, prefs));

      frame.addComponentListener(new ComponentAdapter() {
          @Override
          public void componentResized(ComponentEvent e) {
              updater.update();
          }

          @Override
          public void componentMoved(ComponentEvent e) {
              updater.update();
          }
      });
  }

  private static void updatePref(JFrame frame, Preferences prefs) {
      System.out.println("Updating preferences");
      Point location = frame.getLocation();
      prefs.putInt("x", location.x);
      prefs.putInt("y", location.y);
      Dimension size = frame.getSize();
      prefs.putInt("w", size.width);
      prefs.putInt("h", size.height);
  }

  private static Dimension getFrameSize(Preferences pref, int defaultW, int defaultH) {
      int w = pref.getInt("w", defaultW);
      int h = pref.getInt("h", defaultH);
      return new Dimension(w, h);
  }

  private static Point getFrameLocation(Preferences pref, int defaultX, int defaultY) {
      int x = pref.getInt("x", defaultX);
      int y = pref.getInt("y", defaultY);
      return new Point(x, y);
  }
}
