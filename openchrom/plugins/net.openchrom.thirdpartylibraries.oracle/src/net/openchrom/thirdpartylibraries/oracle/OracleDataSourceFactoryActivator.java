/*******************************************************************************
 * Copyright (c) 2020 Lablicate GmbH.
 * 
 * All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Christoph Läubrich - initial api and implementation
 *******************************************************************************/
package net.openchrom.thirdpartylibraries.oracle;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

public class OracleDataSourceFactoryActivator implements BundleActivator {

	private OracleDataSourceFactory oracleDataSourceFactory;

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext bundleContext) throws Exception {

		ServiceTracker<LogService, LogService> tracker = new ServiceTracker<>(bundleContext, LogService.class, null);
		try {
			File dataFile = bundleContext.getDataFile("");
			log(tracker, LogService.LOG_INFO, "Searching for Oracle JDBC libraries @ " + dataFile.getAbsolutePath());
			List<URL> urls = new ArrayList<>();
			if(dataFile.isDirectory()) {
				File[] listFiles = dataFile.listFiles();
				if(listFiles != null) {
					for(File file : listFiles) {
						if(file.getName().toLowerCase().endsWith(".jar")) {
							urls.add(file.toURI().toURL());
						}
					}
				}
			}
			if(urls.isEmpty()) {
				log(tracker, LogService.LOG_WARNING, "No library files found, OracleDataSourceFactory service will not be avaiable");
				return;
			}
			oracleDataSourceFactory = new OracleDataSourceFactory(new URLClassLoader(urls.toArray(new URL[0]), getClass().getClassLoader()));
			try {
				Driver driver = oracleDataSourceFactory.createDriver(new Properties());
				Dictionary<String, Object> properties = new Hashtable<>();
				properties.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, driver.getClass().getName());
				properties.put(DataSourceFactory.OSGI_JDBC_DRIVER_NAME, driver.getClass().getSimpleName());
				properties.put(DataSourceFactory.OSGI_JDBC_DRIVER_VERSION, driver.getMajorVersion() + "." + driver.getMinorVersion());
				log(tracker, LogService.LOG_INFO, "Register service with proerties " + properties);
				bundleContext.registerService(DataSourceFactory.class, oracleDataSourceFactory, properties);
			} catch(SQLException e) {
				log(tracker, LogService.LOG_ERROR, "Can't create driver, OracleDataSourceFactory service will not be avaiable (" + e.getCause() + ")");
			}
		} finally {
			tracker.close();
		}
	}

	private void log(ServiceTracker<LogService, LogService> tracker, int level, String message) {

		LogService logService = tracker.getService();
		if(logService != null) {
			logService.log(level, message);
		} else {
			System.out.println("[" + getClass().getSimpleName() + "] " + message);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext bundleContext) throws Exception {

		if(oracleDataSourceFactory != null) {
			oracleDataSourceFactory.close();
		}
	}
}
