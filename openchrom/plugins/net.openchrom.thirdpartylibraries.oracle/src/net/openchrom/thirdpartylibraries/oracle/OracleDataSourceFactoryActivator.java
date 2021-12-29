/*******************************************************************************
 * Copyright (c) 2020, 2021 Lablicate GmbH.
 * 
 * All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Christoph Läubrich - initial api and implementation
 * Philip Wenig - refactoring logger
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
import org.osgi.service.log.Logger;
import org.osgi.util.tracker.ServiceTracker;

public class OracleDataSourceFactoryActivator implements BundleActivator {

	private static final String ORACLE_JDBC_LIB_PATH = "ORACLE_JDBC_LIB_PATH";
	private static final int INFO = 0;
	private static final int WARNING = 1;
	private static final int ERROR = 2;
	private OracleDataSourceFactory oracleDataSourceFactory;

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext bundleContext) throws Exception {

		ServiceTracker<LogService, LogService> tracker = new ServiceTracker<>(bundleContext, LogService.class, null);
		try {
			File dataFile = getLibPath(bundleContext);
			log(tracker, INFO, "Searching for Oracle JDBC libraries @ " + dataFile.getAbsolutePath());
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
				log(tracker, WARNING, "No library files found, OracleDataSourceFactory service will not be avaiable");
				return;
			}
			oracleDataSourceFactory = new OracleDataSourceFactory(new URLClassLoader(urls.toArray(new URL[0]), getClass().getClassLoader()));
			try {
				Driver driver = oracleDataSourceFactory.createDriver(new Properties());
				Dictionary<String, Object> properties = new Hashtable<>();
				properties.put(DataSourceFactory.OSGI_JDBC_DRIVER_CLASS, driver.getClass().getName());
				properties.put(DataSourceFactory.OSGI_JDBC_DRIVER_NAME, driver.getClass().getSimpleName());
				properties.put(DataSourceFactory.OSGI_JDBC_DRIVER_VERSION, driver.getMajorVersion() + "." + driver.getMinorVersion());
				log(tracker, INFO, "Register OracleDataSourceFactory service with properties " + properties);
				bundleContext.registerService(DataSourceFactory.class, oracleDataSourceFactory, properties);
			} catch(SQLException e) {
				log(tracker, ERROR, "Can't create driver, OracleDataSourceFactory service will not be avaiable (" + e.getCause() + ")");
			}
		} finally {
			tracker.close();
		}
	}

	private File getLibPath(BundleContext bundleContext) {

		String property = System.getProperty(ORACLE_JDBC_LIB_PATH);
		if(property != null && !property.isEmpty()) {
			return new File(property.replace("@user.home", System.getProperty("user.home")));
		}
		String env = System.getenv(ORACLE_JDBC_LIB_PATH);
		if(env != null && !env.isEmpty()) {
			return new File(env);
		}
		return bundleContext.getDataFile("");
	}

	private void log(ServiceTracker<LogService, LogService> tracker, int level, String message) {

		LogService logService = tracker.getService();
		if(logService != null) {
			Logger logger = logService.getLogger(OracleDataSourceFactoryActivator.class);
			if(logger != null) {
			}
			switch(level) {
				case WARNING:
					logger.warn(message);
					break;
				case ERROR:
					logger.error(message);
					break;
				default:
					logger.info(message);
					break;
			}
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
