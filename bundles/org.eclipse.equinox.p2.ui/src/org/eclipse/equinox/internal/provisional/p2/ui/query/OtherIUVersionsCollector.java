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
package org.eclipse.equinox.internal.provisional.p2.ui.query;

import org.eclipse.equinox.internal.provisional.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.internal.provisional.p2.query.IQueryable;
import org.eclipse.equinox.internal.provisional.p2.ui.model.QueriedElementCollector;
import org.eclipse.equinox.internal.provisional.p2.ui.policy.IQueryProvider;

/**
 * Collector that includes only those versions of an IU other than the
 * one known.
 *  
 * @since 3.4
 */
public class OtherIUVersionsCollector extends QueriedElementCollector {

	private IInstallableUnit iu;

	public OtherIUVersionsCollector(IInstallableUnit iu, IQueryProvider queryProvider, IQueryable queryable, QueryContext queryContext) {
		super(queryProvider, queryable, queryContext);
		this.iu = iu;
	}

	/**
	 * Accepts a result that matches the query criteria.
	 * 
	 * @param match an object matching the query
	 * @return <code>true</code> if the query should continue,
	 * or <code>false</code> to indicate the query should stop.
	 */
	public boolean accept(Object match) {
		if (!(match instanceof IInstallableUnit))
			return true;
		IInstallableUnit otherIU = (IInstallableUnit) match;
		if (!otherIU.equals(iu))
			return super.accept(match);
		return true;
	}

}