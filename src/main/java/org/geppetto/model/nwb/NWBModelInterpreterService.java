/*******************************************************************************
. * The MIT License (MIT)
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

package org.geppetto.model.nwb;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.geppetto.core.beans.ModelInterpreterConfig;
import org.geppetto.core.data.model.IAspectConfiguration;
import org.geppetto.core.model.AModelInterpreter;
import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.core.model.ModelInterpreterException;
import org.geppetto.core.services.registry.ServicesRegistry;
import org.geppetto.model.GeppettoLibrary;
import org.geppetto.model.ModelFormat;
import org.geppetto.model.types.CompositeType;
import org.geppetto.model.types.Type;
import org.geppetto.model.types.TypesFactory;
import org.geppetto.model.types.TypesPackage;
import org.geppetto.model.util.GeppettoVisitingException;
import org.geppetto.model.values.Pointer;
import org.geppetto.model.values.Text;
import org.geppetto.model.values.TimeSeries;
import org.geppetto.model.values.ValuesFactory;
import org.geppetto.model.variables.Variable;
import org.geppetto.model.variables.VariablesFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author matteocantarelli
 * 
 */
@Service
public class NWBModelInterpreterService extends AModelInterpreter
{

	@Autowired
	private ModelInterpreterConfig nwbModelInterpreterConfig;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geppetto.core.model.IModelInterpreter#getName()
	 */
	@Override
	public String getName()
	{
		return this.nwbModelInterpreterConfig.getModelInterpreterName();
	}

	@Override
	public void registerGeppettoService()
	{
		List<ModelFormat> modelFormats = new ArrayList<ModelFormat>(Arrays.asList(ServicesRegistry.registerModelFormat("NWB")));
		ServicesRegistry.registerModelInterpreterService(this, modelFormats);
	}

	@Override
	public List<ModelFormat> getSupportedOutputs(Pointer pointer) throws ModelInterpreterException
	{
		List<ModelFormat> supportedOutputs = super.getSupportedOutputs(pointer);
		supportedOutputs.add(ServicesRegistry.getModelFormat("NWB"));
		return supportedOutputs;
	}

	@Override
	public Type importType(URL url, String typeName, GeppettoLibrary library, GeppettoModelAccess commonLibraryAccess) throws ModelInterpreterException
	{

		dependentModels.clear();
		
		CompositeType nwcModelType = TypesFactory.eINSTANCE.createCompositeType();

		try
		{
			

			// Nitesh: convert from NWB to Type
			// 1 - Open the NWB file using the HDF5Reader
			// 2 - Extract from the NWB the information as per Rick email
			// a) create root CompositeType
			// b) iterate the H5File and build the subtypes for the different nodes
			// c) create TimeSeries variable/values for the Time series data, text/url/html variables for the rest
			
			//H5File file = HDF5Reader.readHDF5File(new File("./src/test/resources/H5DatasetCreate.h5").toURI().toURL(),-1l);
			//Sample pattern for populating the type
			/* Getting data from NWB*/
			//logger.debug("Logging on to the server for the first time");
			
			
			GetNWBData nwb = new GetNWBData("313862020.nwb");
			//System.out.println(nwb.file_name);
		    ArrayList<Integer> all_sweep_nums = nwb.get_sweep_numbers();
		    int sweep_number =  all_sweep_nums.get(1);
		    //System.out.println(sweep_number);
		    NWBObject nwbObj = nwb.get_sweep(sweep_number); 
		    for(int i=0; i<nwbObj.stimulus.length; i++){
		    	nwbObj.stimulus[i] =  nwbObj.stimulus[i] * 1000000000000.0; // converting to pA, response -> current
	       }
	       for(int i=0; i<nwbObj.response.length; i++){
	    	   nwbObj.response[i] = nwbObj.response[i] * 1000.0; // converting to mV, response -> voltage 
	    	 }
	       double dt = 1.0 / nwbObj.sampling_rate;
	       System.out.println(dt);
	       double [] t = new double[nwbObj.response.length];
	       for(int i=0; i<nwbObj.response.length; i++) // generating time axis A.P.
	       {
	    	   t[i] = (i*dt);
	       }
	       
	       		for(int i=0; i<3000; i++){
	    	   		System.out.println(i + "   " + nwbObj.stimulus[i]); // current
	       		}
	       		
	       	
	       		/*for(int i=0; i<3000; i++){
	    	   		System.out.println(i + "   " + nwbObj.response[i]); //voltage
	       		}*/
	       		
	       		/*for(int i=0; i<=50000; i++){
	    	    System.out.println(i + "   " + t[i]); //voltage
	       		} 
	       	*/
			//H5File file = HDF5Reader.readHDF5File(new File("./src/test/resources/H5DatasetCreate.h5").toURI().toURL(),-1l);
			
			
			
			

			Variable stimulus = VariablesFactory.eINSTANCE.createVariable();
			stimulus.getTypes().add(commonLibraryAccess.getType(TypesPackage.Literals.STATE_VARIABLE_TYPE));
			stimulus.setId("stimulus");
			stimulus.setName("stimulus");
			
			TimeSeries stimulusTimeSeries=ValuesFactory.eINSTANCE.createTimeSeries();
			stimulusTimeSeries.getValue().addAll(Arrays.asList(nwbObj.stimulus));
			
			stimulus.getInitialValues().put(
					commonLibraryAccess.getType(TypesPackage.Literals.STATE_VARIABLE_TYPE), 
					stimulusTimeSeries);
			
			nwcModelType.getVariables().add(stimulus);
			
			
			
			
		}
		catch(GeppettoVisitingException e)
		{
			throw new ModelInterpreterException(e);
		}

		return nwcModelType;

	}

	@Override
	public File downloadModel(Pointer pointer, ModelFormat format, IAspectConfiguration aspectConfiguration) throws ModelInterpreterException
	{
		throw new ModelInterpreterException("Download model not implemented for NWB model interpreter");
	}

}
