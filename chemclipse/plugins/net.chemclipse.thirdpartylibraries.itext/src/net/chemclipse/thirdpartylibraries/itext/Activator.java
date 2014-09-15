/*******************************************************************************
 * Copyright (c) 2008, 2014 Philip (eselmeister) Wenig. This program is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version. This
 * program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details. You should have received a copy of the GNU Affero General Public
 * License along with this program. If not, see http://www.gnu.org/licenses/.
 * ******NOTE****** This plugins encapsulates the iText pdf library. Take care
 * of the iText license.
 * --------------------------------------------------License iText Copyright (c)
 * 1998-2009 1T3XT BVBA Authors: Bruno Lowagie, Paulo Soares, et al. This
 * program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Affero General Public License version 3 as published by the
 * Free Software Foundation with the addition of the following permission added
 * to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED WORK
 * IN WHICH THE COPYRIGHT IS OWNED BY 1T3XT, 1T3XT DISCLAIMS THE WARRANTY OF NON
 * INFRINGEMENT OF THIRD PARTY RIGHTS. This program is distributed in the hope
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details. You should have received a
 * copy of the GNU Affero General Public License along with this program; if
 * not, see http://www.gnu.org/licenses or write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA, 02110-1301
 * USA, or download the license from the following URL:
 * http://itextpdf.com/terms-of-use/ The interactive user interfaces in modified
 * source and object code versions of this program must display Appropriate
 * Legal Notices, as required under Section 5 of the GNU Affero General Public
 * License. In accordance with Section 7(b) of the GNU Affero General Public
 * License, you must retain the producer line in every PDF that is created or
 * manipulated using iText. You can be released from the requirements of the
 * license by purchasing a commercial license. Buying such a license is
 * mandatory as soon as you develop commercial activities involving the iText
 * software without disclosing the source code of your own applications. These
 * activities include: offering paid services to customers as an ASP, serving
 * PDFs on the fly in a web application, shipping iText with a closed source
 * product. For more information, please contact iText Software Corp. at this
 * address: sales@itextpdf.com
 * --------------------------------------------------License iText
 *******************************************************************************/
package net.chemclipse.thirdpartylibraries.itext;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "net.chemclipse.thirdpartylibraries.itext";
	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {

		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {

		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {

		return plugin;
	}
}
