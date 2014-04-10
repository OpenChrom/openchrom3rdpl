/*******************************************************************************
 * Copyright (c) 2014 Dr. Philip Wenig.
 * 
 * All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package net.chemclipse.thirdpartylibraries.swtchart.ext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.Range;
import org.swtchart.IAxis.Direction;
import org.swtchart.ext.internal.SelectionRectangle;

public class InteractiveChartExtended extends Chart implements PaintListener, KeyListener, MouseListener, MouseMoveListener, MouseWheelListener {

	protected SelectionRectangle selectionRectangle;
	private long clickedTimeInMilliseconds;
	private int xStart;
	//
	public static final String ADJUST_AXIS_RANGE_GROUP = "Unzoom";
	public static final String ADJUST_AXIS_RANGE = "Reset 1:1";
	public static final String ADJUST_X_AXIS_RANGE = "Unzoom X-Axis";
	public static final String ADJUST_Y_AXIS_RANGE = "Unzoom Y-Axis";
	public static final String LEGEND = "Legend";
	public static final String LEGEND_SHOW = "Show Legend";
	public static final String LEGEND_HIDE = "Hide Legend";

	public InteractiveChartExtended(Composite parent, int style) {

		super(parent, style);
		init();
	}

	@Override
	public void handleEvent(Event event) {

		super.handleEvent(event);
		switch(event.type) {
			case SWT.Selection:
				widgetSelected(event);
				break;
		}
	}

	@Override
	public void paintControl(PaintEvent e) {

		selectionRectangle.draw(e.gc);
	}

	@Override
	public void dispose() {

		super.dispose();
	}

	@Override
	public void mouseDoubleClick(MouseEvent e) {

	}

	@Override
	public void mouseDown(MouseEvent e) {

		if(e.button == 1) {
			xStart = e.x;
			selectionRectangle.setStartPoint(e.x, e.y);
			clickedTimeInMilliseconds = System.currentTimeMillis();
		}
	}

	@Override
	public void mouseUp(MouseEvent e) {

		if(e.button == 1 && System.currentTimeMillis() - clickedTimeInMilliseconds > 100) {
			/*
			 * If the selection is too narrow, skip it.
			 * That prevents unwanted zooming.
			 */
			Composite plotArea = getPlotArea();
			int minSelectedWidth = plotArea.getBounds().width / 30;
			int deltaWidth = Math.abs(xStart - e.x);
			if(deltaWidth >= minSelectedWidth) {
				/*
				 * Calculate the range for each axis.
				 */
				for(IAxis axis : getAxisSet().getAxes()) {
					/*
					 * Get the range.
					 */
					Point range = null;
					if((getOrientation() == SWT.HORIZONTAL && axis.getDirection() == Direction.X) || (getOrientation() == SWT.VERTICAL && axis.getDirection() == Direction.Y)) {
						range = selectionRectangle.getHorizontalRange();
					} else {
						range = selectionRectangle.getVerticalRange();
					}
					/*
					 * Set the range.
					 */
					if(range != null && range.x != range.y) {
						setRange(range, axis);
					}
				}
			}
		}
		selectionRectangle.dispose();
		redraw();
	}

	@Override
	public void mouseMove(MouseEvent e) {

		if(!selectionRectangle.isDisposed()) {
			selectionRectangle.setEndPoint(e.x, e.y);
			redraw();
		}
	}

	@Override
	public void mouseScrolled(MouseEvent e) {

		for(IAxis axis : getAxes(SWT.HORIZONTAL)) {
			double coordinate = axis.getDataCoordinate(e.x);
			if(e.count > 0) {
				axis.zoomIn(coordinate);
			} else {
				axis.zoomOut(coordinate);
			}
		}
		for(IAxis axis : getAxes(SWT.VERTICAL)) {
			double coordinate = axis.getDataCoordinate(e.y);
			if(e.count > 0) {
				axis.zoomIn(coordinate);
			} else {
				axis.zoomOut(coordinate);
			}
		}
		redraw();
	}

	@Override
	public void keyPressed(KeyEvent e) {

		if(e.keyCode == SWT.ARROW_DOWN) {
			if(e.stateMask == SWT.CTRL) {
				getAxisSet().zoomOut();
			} else {
				for(IAxis axis : getAxes(SWT.VERTICAL)) {
					axis.scrollDown();
				}
			}
			redraw();
		} else if(e.keyCode == SWT.ARROW_UP) {
			if(e.stateMask == SWT.CTRL) {
				getAxisSet().zoomIn();
			} else {
				for(IAxis axis : getAxes(SWT.VERTICAL)) {
					axis.scrollUp();
				}
			}
			redraw();
		} else if(e.keyCode == SWT.ARROW_LEFT) {
			for(IAxis axis : getAxes(SWT.HORIZONTAL)) {
				axis.scrollDown();
			}
			redraw();
		} else if(e.keyCode == SWT.ARROW_RIGHT) {
			for(IAxis axis : getAxes(SWT.HORIZONTAL)) {
				axis.scrollUp();
			}
			redraw();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

	}

	public void widgetSelected(Event e) {

		if(!(e.widget instanceof MenuItem)) {
			return;
		}
		MenuItem menuItem = (MenuItem)e.widget;
		if(menuItem.getText().equals(ADJUST_AXIS_RANGE)) {
			getAxisSet().adjustRange();
		} else if(menuItem.getText().equals(ADJUST_X_AXIS_RANGE)) {
			for(IAxis axis : getAxisSet().getXAxes()) {
				axis.adjustRange();
			}
		} else if(menuItem.getText().equals(ADJUST_Y_AXIS_RANGE)) {
			for(IAxis axis : getAxisSet().getYAxes()) {
				axis.adjustRange();
			}
		} else if(menuItem.getText().equals(LEGEND_SHOW)) {
			getLegend().setVisible(true);
		} else if(menuItem.getText().equals(LEGEND_HIDE)) {
			getLegend().setVisible(false);
		}
		redraw();
	}

	private IAxis[] getAxes(int orientation) {

		IAxis[] axes;
		if(getOrientation() == orientation) {
			axes = getAxisSet().getXAxes();
		} else {
			axes = getAxisSet().getYAxes();
		}
		return axes;
	}

	private void setRange(Point range, IAxis axis) {

		if(range == null) {
			return;
		}
		double min = axis.getDataCoordinate(range.x);
		double max = axis.getDataCoordinate(range.y);
		axis.setRange(new Range(min, max));
	}

	private void init() {

		selectionRectangle = new SelectionRectangle();
		//
		Composite plotArea = getPlotArea();
		plotArea.addPaintListener(this);
		plotArea.addKeyListener(this);
		plotArea.addMouseListener(this);
		plotArea.addMouseMoveListener(this);
		plotArea.addMouseWheelListener(this);
		//
		createMenuItems();
	}

	private void createMenuItems() {

		Menu menu = new Menu(getPlotArea());
		getPlotArea().setMenu(menu);
		//
		MenuItem menuItem = new MenuItem(menu, SWT.CASCADE);
		menuItem.setText(ADJUST_AXIS_RANGE_GROUP);
		Menu adjustAxisRangeMenu = new Menu(menuItem);
		menuItem.setMenu(adjustAxisRangeMenu);
		//
		menuItem = new MenuItem(adjustAxisRangeMenu, SWT.PUSH);
		menuItem.setText(ADJUST_AXIS_RANGE);
		menuItem.addListener(SWT.Selection, this);
		//
		menuItem = new MenuItem(adjustAxisRangeMenu, SWT.PUSH);
		menuItem.setText(ADJUST_X_AXIS_RANGE);
		menuItem.addListener(SWT.Selection, this);
		//
		menuItem = new MenuItem(adjustAxisRangeMenu, SWT.PUSH);
		menuItem.setText(ADJUST_Y_AXIS_RANGE);
		menuItem.addListener(SWT.Selection, this);
		//
		menuItem = new MenuItem(menu, SWT.SEPARATOR);
		/*
		 * Properties
		 */
		menuItem = new MenuItem(menu, SWT.CASCADE);
		menuItem.setText(LEGEND);
		Menu legendMenu = new Menu(menuItem);
		menuItem.setMenu(legendMenu);
		//
		menuItem = new MenuItem(legendMenu, SWT.PUSH);
		menuItem.setText(LEGEND_SHOW);
		menuItem.addListener(SWT.Selection, this);
		//
		menuItem = new MenuItem(legendMenu, SWT.PUSH);
		menuItem.setText(LEGEND_HIDE);
		menuItem.addListener(SWT.Selection, this);
	}
}
