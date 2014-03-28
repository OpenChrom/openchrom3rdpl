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
import org.swtchart.ext.internal.properties.PropertiesResources;

public class InteractiveChartExtended extends Chart implements PaintListener {

	/** the selection rectangle for zoom in/out */
	protected SelectionRectangle selection;
	/** the clicked time in milliseconds */
	private long clickedTime;
	/** the resources created with properties dialog */
	private PropertiesResources resources;
	//
	private static final String ADJUST_AXIS_RANGE_GROUP = "Unzoom";
	private static final String ADJUST_AXIS_RANGE = "Reset 1:1";
	private static final String ADJUST_X_AXIS_RANGE = "Unzoom X-Axis";
	private static final String ADJUST_Y_AXIS_RANGE = "Unzoom Y-Axis";
	private static final String PROPERTIES = "Properties";
	private static final String PROPERTIES_SHOW_LEGEND = "Show Legend";
	private static final String PROPERTIES_HIDE_LEGEND = "Hide Legend";

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            the parent composite
	 * @param style
	 *            the style
	 */
	public InteractiveChartExtended(Composite parent, int style) {

		super(parent, style);
		init();
	}

	/**
	 * Initializes.
	 */
	private void init() {

		selection = new SelectionRectangle();
		resources = new PropertiesResources();
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

	/**
	 * Creates menu items.
	 */
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
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText(PROPERTIES_SHOW_LEGEND);
		menuItem.addListener(SWT.Selection, this);
		//
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText(PROPERTIES_HIDE_LEGEND);
		menuItem.addListener(SWT.Selection, this);
		//
		menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText(PROPERTIES);
		menuItem.addListener(SWT.Selection, this);
	}

	/*
	 * @see PaintListener#paintControl(PaintEvent)
	 */
	public void paintControl(PaintEvent e) {

		selection.draw(e.gc);
	}

	/*
	 * @see Listener#handleEvent(Event)
	 */
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

	/*
	 * @see Chart#dispose()
	 */
	@Override
	public void dispose() {

		super.dispose();
		resources.dispose();
	}

	/**
	 * Handles mouse move event.
	 * 
	 * @param event
	 *            the mouse move event
	 */
	private void handleMouseMoveEvent(Event event) {

		if(!selection.isDisposed()) {
			selection.setEndPoint(event.x, event.y);
			redraw();
		}
	}

	/**
	 * Handles the mouse down event.
	 * 
	 * @param event
	 *            the mouse down event
	 */
	private void handleMouseDownEvent(Event event) {

		if(event.button == 1) {
			selection.setStartPoint(event.x, event.y);
			clickedTime = System.currentTimeMillis();
		}
	}

	/**
	 * Handles the mouse up event.
	 * 
	 * @param event
	 *            the mouse up event
	 */
	private void handleMouseUpEvent(Event event) {

		if(event.button == 1 && System.currentTimeMillis() - clickedTime > 100) {
			for(IAxis axis : getAxisSet().getAxes()) {
				Point range = null;
				if((getOrientation() == SWT.HORIZONTAL && axis.getDirection() == Direction.X) || (getOrientation() == SWT.VERTICAL && axis.getDirection() == Direction.Y)) {
					range = selection.getHorizontalRange();
				} else {
					range = selection.getVerticalRange();
				}
				if(range != null && range.x != range.y) {
					setRange(range, axis);
				}
			}
		}
		selection.dispose();
		redraw();
	}

	/**
	 * Handles mouse wheel event.
	 * 
	 * @param event
	 *            the mouse wheel event
	 */
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

	/**
	 * Handles the key down event.
	 * 
	 * @param event
	 *            the key down event
	 */
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

	/**
	 * Gets the axes for given orientation.
	 * 
	 * @param orientation
	 *            the orientation
	 * @return the axes
	 */
	private IAxis[] getAxes(int orientation) {

		IAxis[] axes;
		if(getOrientation() == orientation) {
			axes = getAxisSet().getXAxes();
		} else {
			axes = getAxisSet().getYAxes();
		}
		return axes;
	}

	/**
	 * Handles the selection event.
	 * 
	 * @param event
	 *            the event
	 */
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
		} else if(menuItem.getText().equals(PROPERTIES_SHOW_LEGEND)) {
			getLegend().setVisible(true);
		} else if(menuItem.getText().equals(PROPERTIES_HIDE_LEGEND)) {
			getLegend().setVisible(false);
		} else if(menuItem.getText().equals(PROPERTIES)) {
		}
		redraw();
	}

	/**
	 * Sets the axis range.
	 * 
	 * @param range
	 *            the axis range in pixels
	 * @param axis
	 *            the axis to set range
	 */
	private void setRange(Point range, IAxis axis) {

		if(range == null) {
			return;
		}
		double min = axis.getDataCoordinate(range.x);
		double max = axis.getDataCoordinate(range.y);
		axis.setRange(new Range(min, max));
	}
}
