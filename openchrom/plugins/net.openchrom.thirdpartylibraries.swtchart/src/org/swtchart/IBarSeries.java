/*******************************************************************************
 * Copyright (c) 2008-2011 SWTChart project. All rights reserved.
 * 
 * This code is distributed under the terms of the Eclipse Public License v1.0
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * @author 2013 Dr. Philip Wenig - I've added the bar width functionality.
 *******************************************************************************/
package org.swtchart;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Bar series.
 */
public interface IBarSeries extends ISeries {

	/**
	 * Gets the bar padding in percentage.
	 * 
	 * @return the bar padding in percentage
	 */
	int getBarPadding();

	/**
	 * Sets the bar padding in percentage.
	 * 
	 * @param padding
	 *            the bar padding in percentage
	 */
	void setBarPadding(int padding);

	/**
	 * Gets the bar width in pixel.
	 * 
	 * @return int
	 */
	int getBarWidth();

	/**
	 * Sets the bar width in pixel.
	 * If the value > 0, the bar padding will be discarded.
	 * Allowed values are 0 - 10, otherwise the bar width will be set to 0.
	 * 
	 * @param barWidth
	 */
	void setBarWidth(int barWidth);

	/**
	 * Gets the bar color.
	 * 
	 * @return the bar color
	 */
	Color getBarColor();

	/**
	 * Sets the bar color. If null is given, default color will be set.
	 * 
	 * @param color
	 *            the bar color
	 */
	void setBarColor(Color color);

	/**
	 * Gets the array of bar rectangles. This method is typically used for mouse
	 * listener to check whether mouse cursor is on bar.
	 * <p>
	 * The returned array has the same size as data points. Depending on X axis range, some bars can be out of screen. In this case, the rectangles for invisible bars will be <tt>null<tt> in the returned array.
	 * 
	 * @return the array of bar rectangles in pixels.
	 */
	Rectangle[] getBounds();
}