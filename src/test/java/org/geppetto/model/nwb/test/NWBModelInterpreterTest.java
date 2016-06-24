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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.utils.SetNatives;

import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.common.GeppettoInitializationException;
import org.geppetto.core.common.HDF5Reader;
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
import org.geppetto.model.values.ImportValue;
import org.geppetto.model.values.TimeSeries;
import org.geppetto.model.values.ValuesFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
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
	
	private static NWBModelInterpreterService nwbModelInterpreter;
	@BeforeClass
	public static void setUp() throws Exception
	{
		ServicesRegistry.registerModelFormat("NWB");
		try {
			SetNatives.getInstance().setHDF5Native(System.getProperty("user.dir"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		nwbModelInterpreter = new NWBModelInterpreterService();
		
	}

	@Test
	public void testExample4() throws MalformedURLException, GeppettoExecutionException
	{
		//this.setup();
		H5File file=HDF5Reader.readHDF5File(new File("./src/main/resources/313862020.nwb").toURI().toURL(),-1l);
		Assert.assertNotNull(file);
	}
	
	@Test
	public void testReadNWB() throws ModelInterpreterException, GeppettoInitializationException, GeppettoVisitingException
	{
		URL url = this.getClass().getResource("/313862020.nwb");
		GeppettoLibrary library = GeppettoFactory.eINSTANCE.createGeppettoLibrary();
		library.setId("NWB");
		GeppettoLibrary commonLibrary = SharedLibraryManager.getSharedCommonLibrary();
		GeppettoModel geppettoModel = GeppettoFactory.eINSTANCE.createGeppettoModel();
		geppettoModel.getLibraries().add(library);
		geppettoModel.getLibraries().add(commonLibrary);
		GeppettoModelAccess commonLibraryAccess = new GeppettoModelAccess(geppettoModel);
		Type nwbType = nwbModelInterpreter.importType(url, "testName", library, commonLibraryAccess);
		Assert.assertNotNull(nwbType);
		//test that everything that should be extracted is available through the type
		//System.out.println("Running");
	
		
	}
	
	@Test
	public void importValueTest() throws ModelInterpreterException{
		//Value val = ValuesFactory.
		ImportValue imp = null;
		TimeSeries mt = (TimeSeries) nwbModelInterpreter.importValue(imp);
		Assert.assertNotNull(mt);
	}

}
