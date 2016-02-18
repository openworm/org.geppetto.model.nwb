/*******************************************************************************
 * The MIT License (MIT)
 * 
 * Copyright (c) 2011 - 2015 OpenWorm.
 * http://openworm.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License
 * which accompanies this distribution, and is available at
 * http://opensource.org/licenses/MIT
 *
 * Contributors:
 *     	OpenWorm - http://openworm.org/people.html
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights 
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 * copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE 
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package org.geppetto.model.nwb.test;

import java.net.URL;

import org.geppetto.core.common.GeppettoInitializationException;
import org.geppetto.core.manager.SharedLibraryManager;
import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.model.GeppettoFactory;
import org.geppetto.model.GeppettoLibrary;
import org.geppetto.model.GeppettoModel;
import org.geppetto.model.nwb.NWBModelInterpreterService;
import org.geppetto.model.types.Type;
import org.geppetto.model.util.GeppettoVisitingException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author matteocantarelli
 *
 */
public class NWBModelInterpreterTest
{

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		ServicesRegistry.registerModelFormat("NWB");
	}

	@Test
	public void testReadNWB() throws ModelInterpreterException, GeppettoInitializationException, GeppettoVisitingException
	{
		NWBModelInterpreterService nwbModelInterpreter = new NWBModelInterpreterService();
		URL nwbFile = this.getClass().getResource("/nwbSample.nwb");
		GeppettoLibrary library = GeppettoFactory.eINSTANCE.createGeppettoLibrary();
		library.setId("NWB");
		GeppettoLibrary commonLibrary = SharedLibraryManager.getSharedCommonLibrary();
		GeppettoModel geppettoModel = GeppettoFactory.eINSTANCE.createGeppettoModel();
		geppettoModel.getLibraries().add(library);
		geppettoModel.getLibraries().add(commonLibrary);
		GeppettoModelAccess commonLibraryAccess = new GeppettoModelAccess(geppettoModel);

		Type nwbType = nwbModelInterpreter.importType(nwbFile, "testName", library, commonLibraryAccess);
		Assert.assertNotNull(nwbType);
		//test that everything that should be extracted is available through the type
	}

}
