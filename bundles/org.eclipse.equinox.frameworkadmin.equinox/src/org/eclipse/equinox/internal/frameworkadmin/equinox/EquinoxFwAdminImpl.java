/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.internal.frameworkadmin.equinox;

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import org.eclipse.equinox.internal.frameworkadmin.equinox.utils.EclipseVersion;
import org.eclipse.equinox.internal.provisional.configuratormanipulator.ConfiguratorManipulator;
import org.eclipse.equinox.internal.provisional.configuratormanipulator.ConfiguratorManipulatorFactory;
import org.eclipse.equinox.internal.provisional.frameworkadmin.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

public class EquinoxFwAdminImpl implements FrameworkAdmin {

	/**
	 * If both the vendor and the Bundle-Version in the manifest match, 
	 * return true. Otherwise false.
	 *  
	 * @return flag true if the ManipulatorAdmin object can handle currently running fw launch. 
	 */
	static boolean isRunningFw(BundleContext context) {
		//TODO implementation for Eclipse.exe and for Equinox
		String fwVendor = context.getProperty(Constants.FRAMEWORK_VENDOR);
		if (!"Eclipse".equals(fwVendor))
			return false;
		//TODO decide if this version can be supported by this bundle.
		Dictionary header = context.getBundle(0).getHeaders();
		String versionSt = (String) header.get("Bundle-Version");
		EclipseVersion version = new EclipseVersion(versionSt);
		int value = version.compareTo(new EclipseVersion(EquinoxConstants.FW_VERSION));
		if (value > 0) {
			return true;
		}
		// TODO need to identify the version of eclipse.exe used for this launch, if used. 

		//		String eclipseCommandsSt = context.getProperty(EquinoxConstants.PROP_ECLIPSE_COMMANDS);
		//	StringTokenizer tokenizer = new StringTokenizer(eclipseCommandsSt,"\n");
		return false;
	}

	BundleContext context = null;

	boolean active = false;

	private boolean runningFw = false;
	private ConfiguratorManipulator configuratorManipulator = null;

	//	private String configuratorManipulatorFactoryName = null;

	EquinoxFwAdminImpl() {
		this(null, false);
	}

	EquinoxFwAdminImpl(BundleContext context) {
		this(context, false);
	}

	EquinoxFwAdminImpl(String configuratorManipulatorFactoryName) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		this.context = null;
		this.active = true;
		this.runningFw = false;
		//		this.configuratorManipulatorFactoryName = configuratorManipulatorFactoryName;
		loadConfiguratorManipulator(configuratorManipulatorFactoryName);
	}

	private void loadConfiguratorManipulator(String configuratorManipulatorFactoryName) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		if (configuratorManipulatorFactoryName == null)
			this.configuratorManipulator = null;
		else
			this.configuratorManipulator = ConfiguratorManipulatorFactory.getInstance(configuratorManipulatorFactoryName);
		return;
	}

	EquinoxFwAdminImpl(BundleContext context, boolean runningFw) {
		this.context = context;
		this.active = true;
		this.runningFw = runningFw;
	}

	void deactivate() {
		active = false;
	}

	public Manipulator getManipulator() {
		return new EquinoxManipulatorImpl(context, this);
	}

	public Manipulator getRunningManipulator() {
		if (this.runningFw)
			return new EquinoxManipulatorImpl(context, this, true);
		return null;
	}

	public boolean isActive() {
		return active;
	}

	public Process launch(Manipulator manipulator, File cwd) throws IllegalArgumentException, FrameworkAdminRuntimeException, IOException {
		//return new EclipseLauncherImpl(context, this).launch(manipulator, cwd);
		return new EclipseLauncherImpl(this).launch(manipulator, cwd);
	}

	public ConfiguratorManipulator getConfiguratorManipulator() {
		return configuratorManipulator;
	}

}