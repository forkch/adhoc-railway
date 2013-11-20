package ch.fork.AdHocRailway.ui.widgets;


import java.awt.*;

public class BetterFlowLayout extends FlowLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3488417762706903536L;

	public BetterFlowLayout() {

		super();

	}

	public BetterFlowLayout(int align) {

		super(align);

	}

	public BetterFlowLayout(int align, int hgap, int vgap) {

		super(align, hgap, vgap);

	}

	@Override
	public Dimension preferredLayoutSize(Container target) {

		return betterPreferredSize(target);

	}

	@Override
	public Dimension minimumLayoutSize(Container target) {

		return betterPreferredSize(target);

	}

	public Dimension betterPreferredSize(Container target) {

		synchronized (target.getTreeLock()) {

			Insets insets = target.getInsets();

			int maxwidth = target.getWidth()
					- (insets.left + insets.right + getHgap() * 2);

			int nmembers = target.getComponentCount();

			int x = 0, y = insets.top + getVgap();

			int rowh = 0;

			for (int i = 0; i < nmembers; i++) {

				Component m = target.getComponent(i);

				if (m.isVisible()) {

					Dimension d = m.getPreferredSize();

					m.setSize(d.width, d.height);

					if ((x == 0) || ((x + d.width) <= maxwidth)) {

						if (x > 0) {

							x += getHgap();

						}

						x += d.width;

						rowh = Math.max(rowh, d.height);

					} else {

						x = d.width;

						y += getVgap() + rowh;

						rowh = d.height;

					}

				}

			}

			return new Dimension(maxwidth, y + rowh + getVgap());

		}

	}

}