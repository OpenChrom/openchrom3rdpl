/*******************************************************************************
 * Copyright (c) 2020, 2022 Lablicate GmbH.
 * 
 * All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Christoph Läubrich - initial api and implementation
 * Philip Wenig - reduce compiler warnings
 *******************************************************************************/
package net.openchrom.thirdpartylibraries.oracle;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;
import javax.sql.XADataSource;

import org.osgi.service.jdbc.DataSourceFactory;

public class OracleDataSourceFactory implements DataSourceFactory {

	private URLClassLoader classLoader;

	public OracleDataSourceFactory(URLClassLoader classLoader) {

		this.classLoader = classLoader;
	}

	@Override
	public DataSource createDataSource(Properties props) throws SQLException {

		DataSource oracleDataSource = createInstance(DataSource.class, "oracle.jdbc.pool.OracleDataSource");
		setProperties(oracleDataSource, props);
		return oracleDataSource;
	}

	@Override
	public ConnectionPoolDataSource createConnectionPoolDataSource(Properties props) throws SQLException {

		ConnectionPoolDataSource connectionPoolDataSource = createInstance(ConnectionPoolDataSource.class, "oracle.jdbc.pool.OracleConnectionPoolDataSource");
		setProperties(connectionPoolDataSource, props);
		return connectionPoolDataSource;
	}

	@Override
	public XADataSource createXADataSource(Properties props) throws SQLException {

		XADataSource oracleXADataSource = createInstance(XADataSource.class, "oracle.jdbc.xa.client.OracleXADataSource");
		setProperties(oracleXADataSource, props);
		return oracleXADataSource;
	}

	@Override
	public Driver createDriver(Properties props) throws SQLException {

		return createInstance(Driver.class, "oracle.jdbc.driver.OracleDriver");
	}

	private <T> T createInstance(Class<T> clazz, String implementation) throws SQLException {

		try {
			return clazz.cast(classLoader.loadClass(implementation).getDeclaredConstructor().newInstance());
		} catch(Exception e) {
			throw new SQLException("Can't load the driver. Check provided libraries.", e);
		}
	}

	private static void setProperties(Object dataSource, Properties props) throws SQLException {

		try {
			try {
				Method method = dataSource.getClass().getMethod("setConnectionProperties", Properties.class);
				method.invoke(dataSource, props);
			} catch(IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new SQLException(e);
			}
		} catch(NoSuchMethodException | SecurityException e) {
			// can't set properties
		}
		setProperty(dataSource, props.getProperty(JDBC_DATABASE_NAME), "setDatabaseName");
		setProperty(dataSource, props.getProperty(JDBC_DATASOURCE_NAME), "setDataSourceName");
		setProperty(dataSource, props.getProperty(JDBC_DESCRIPTION), "setDescription");
		// JDBC_INITIAL_POOL_SIZE ?
		// JDBC_MAX_IDLE_TIME ?
		// JDBC_MAX_POOL_SIZE ?
		// JDBC_MIN_POOL_SIZE ?
		// JDBC_PROPERTY_CYCLE?
		setProperty(dataSource, props.getProperty(JDBC_MAX_STATEMENTS), "setMaxStatements");
		setProperty(dataSource, props.getProperty(JDBC_NETWORK_PROTOCOL), "setNetworkProtocol");
		setProperty(dataSource, props.getProperty(JDBC_PASSWORD), "setPassword");
		setProperty(dataSource, props.getProperty(JDBC_PORT_NUMBER), "setPortNumber");
		setProperty(dataSource, props.getProperty(JDBC_ROLE_NAME), "setRoleName");
		setProperty(dataSource, props.getProperty(JDBC_URL), "setURL");
		setProperty(dataSource, props.getProperty(JDBC_USER), "setUser");
	}

	private static void setProperty(Object dataSource, String property, String setter) throws SQLException {

		if(property != null) {
			try {
				Method method = dataSource.getClass().getMethod(setter, String.class);
				try {
					method.invoke(dataSource, property);
				} catch(IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					throw new SQLException(e);
				}
			} catch(NoSuchMethodException | SecurityException e) {
				// can't use setter then
			}
		}
	}

	public void close() throws IOException {

		classLoader.close();
	}
}
