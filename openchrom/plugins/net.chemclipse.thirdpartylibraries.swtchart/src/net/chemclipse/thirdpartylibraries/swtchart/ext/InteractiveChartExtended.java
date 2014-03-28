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

public class InteractiveChartExtended extends Chart implements PaintListener {

	protected SelectionRectangle selectionRectangle;
	private long clickedTimeInMilliseconds;
	//
	private static final String ADJUST_AXIS_RANGE_GROUP = "Unzoom";
	private static final String ADJUST_AXIS_RANGE = "Reset 1:1";
	private static final String ADJUST_X_AXIS_RANGE = "Unzoom X-Axis";
	private static final String ADJUST_Y_AXIS_RANGE = "Unzoom Y-Axis";
	private static final String LEGEND = "Legend";
	private static final String LEGEND_SHOW = "Show Legend";
	private static final String LEGEND_HIDE = "Hide Legend";

	public InteractiveChartExtended(Composite parent, int style) {

		super(parent, style);
		init();
	}

	private void init() {

		selectionRectangle = new SelectionRectangle();
		Composite plot = getPlotArea();
		plot.addListener(SWT.Resize, this);
		plot.addListener(SWT.MouseMove, this);
		plot.addListener(SWT.MouseDown, this);
		plot.addListener(SWT.MouseUp, this);
		plot.addListener(SWT.MouseWheel, this);
		plot.addListener(SWT.KeyDown, this);
		plot.addPaintListener(this);
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

	@Override
	public void paintControl(PaintEvent e) {

		selectionRectangle.draw(e.gc);
	}

	@Override
	public void handleEvent(Event event) {

		super.handleEvent(event);
		switch(event.type) {
			case SWT.MouseMove:
				handleMouseMoveEvent(event);
				break;
			case SWT.MouseDown:
				handleMouseDownEvent(event);
				break;
			case SWT.MouseUp:
				handleMouseUpEvent(event);
				break;
			case SWT.MouseWheel:
				handleMouseWheel(event);
				break;
			case SWT.KeyDown:
				handleKeyDownEvent(event);
				break;
			case SWT.Selection:
				handleSelectionEvent(event);
				break;
			default:
				break;
		}
	}

	@Override
	public void dispose() {

		super.dispose();
	}

	private void handleMouseMoveEvent(Event event) {

		if(!selectionRectangle.isDisposed()) {
			selectionRectangle.setEndPoint(event.x, event.y);
			redraw();
		}
	}

	private void handleMouseDownEvent(Event event) {

		if(event.button == 1) {
			selectionRectangle.setStartPoint(event.x, event.y);
			clickedTimeInMilliseconds = System.currentTimeMillis();
		}
	}

	private void handleMouseUpEvent(Event event) {

		if(event.button == 1 && System.currentTimeMillis() - clickedTimeInMilliseconds > 100) {
			for(IAxis axis : getAxisSet().getAxes()) {
				Point range = null;
				if((getOrientation() == SWT.HORIZONTAL && axis.getDirection() == Direction.X) || (getOrientation() == SWT.VERTICAL && axis.getDirection() == Direction.Y)) {
					range = selectionRectangle.getHorizontalRange();
				} else {
					range = selectionRectangle.getVerticalRange();
				}
				if(range != null && range.x != range.y) {
					setRange(range, axis);
				}
			}
		}
		selectionRectangle.dispose();
		redraw();
	}

	private void handleMouseWheel(Event event) {

		for(IAxis axis : getAxes(SWT.HORIZONTAL)) {
			double coordinate = axis.getDataCoordinate(event.x);
			if(event.count > 0) {
				axis.zoomIn(coordinate);
			} else {
				axis.zoomOut(coordinate);
			}
		}
		for(IAxis axis : getAxes(SWT.VERTICAL)) {
			double coordinate = axis.getDataCoordinate(event.y);
			if(event.count > 0) {
				axis.zoomIn(coordinate);
			} else {
				axis.zoomOut(coordinate);
			}
		}
		redraw();
	}

	private void handleKeyDownEvent(Event event) {

		if(event.keyCode == SWT.ARROW_DOWN) {
			if(event.stateMask == SWT.CTRL) {
				getAxisSet().zoomOut();
			} else {
				for(IAxis axis : getAxes(SWT.VERTICAL)) {
					axis.scrollDown();
				}
			}
			redraw();
		} else if(event.keyCode == SWT.ARROW_UP) {
			if(event.stateMask == SWT.CTRL) {
				getAxisSet().zoomIn();
			} else {
				for(IAxis axis : getAxes(SWT.VERTICAL)) {
					axis.scrollUp();
				}
			}
			redraw();
		} else if(event.keyCode == SWT.ARROW_LEFT) {
			for(IAxis axis : getAxes(SWT.HORIZONTAL)) {
				axis.scrollDown();
			}
			redraw();
		} else if(event.keyCode == SWT.ARROW_RIGHT) {
			for(IAxis axis : getAxes(SWT.HORIZONTAL)) {
				axis.scrollUp();
			}
			redraw();
		}
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

	private void handleSelectionEvent(Event event) {

		if(!(event.widget instanceof MenuItem)) {
			return;
		}
		MenuItem menuItem = (MenuItem)event.widget;
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

	private void setRange(Point range, IAxis axis) {

		if(range == null) {
			return;
		}
		double min = axis.getDataCoordinate(range.x);
		double max = axis.getDataCoordinate(range.y);
		axis.setRange(new Range(min, max));
	}
}
