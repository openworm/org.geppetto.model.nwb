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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.utils.SetNatives;

import org.geppetto.core.beans.ModelInterpreterConfig;
import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.common.HDF5Reader;
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
import org.geppetto.model.values.FunctionPlot;
import org.geppetto.model.values.Pointer;
import org.geppetto.model.values.TimeSeries;
import org.geppetto.model.values.Unit;
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
	private Variable createMyVariable(Double[] data, String id, String name, String unit, GeppettoModelAccess commonLibraryAccess) throws GeppettoVisitingException
	{
		Variable var = VariablesFactory.eINSTANCE.createVariable();
		var.getTypes().add(commonLibraryAccess.getType(TypesPackage.Literals.STATE_VARIABLE_TYPE));
		var.setId(id);
		var.setName(name);
		
		TimeSeries myTimeSeries=ValuesFactory.eINSTANCE.createTimeSeries();
		Unit myUnit = ValuesFactory.eINSTANCE.createUnit();
		myUnit.setUnit(unit);
		myTimeSeries.setUnit(myUnit);
		myTimeSeries.getValue().addAll(Arrays.asList(data));
		var.getInitialValues().put(
				commonLibraryAccess.getType(TypesPackage.Literals.STATE_VARIABLE_TYPE), 
				myTimeSeries);
		return var;
		
	}
	@Override
	public Type importType(URL url, String typeName, GeppettoLibrary library, GeppettoModelAccess commonLibraryAccess) throws ModelInterpreterException
	{
		dependentModels.clear();
		CompositeType nwcModelType = TypesFactory.eINSTANCE.createCompositeType();
		try
		{
			try {
				SetNatives.getInstance().setHDF5Native(System.getProperty("user.dir"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			String file_path = "./src/main/resources/354190011.nwb";
			H5File nwbFile = HDF5Reader.readHDF5File(url,-1l);
			ReadNWBFile reader = new ReadNWBFile();
			ArrayList<Integer> sweepNumber = reader.getSweepNumbers(nwbFile); // returns list of sweep numbers
			String path  = "/epochs/Sweep_" + sweepNumber.get(0);	// path should point to data set in which you are interested.
			
			NWBObject nwbObject = reader.readNWBFile(path, nwbFile);
			double dt = nwbObject.sampling_rate;
			Double [] t = new Double[nwbObject.response.length];
		    for(int i=0; i<nwbObject.response.length; i++) // generating time axis A.P.
		    	t[i] = Double.valueOf(i*dt);
		    
			Variable stimulus = createMyVariable(nwbObject.stimulus, "stimulus", "stimulus", "current pA", commonLibraryAccess);
			nwcModelType.getVariables().add(stimulus);
			
			Variable response = createMyVariable(nwbObject.response, "response", "response", "volt mV", commonLibraryAccess);
			nwcModelType.getVariables().add(response);
			
			Variable time = createMyVariable(t, "time", "time", "time mS", commonLibraryAccess);
			nwcModelType.getVariables().add(time);
		}
		catch (GeppettoExecutionException e) {
			
			throw new ModelInterpreterException(e);
			
		} catch (GeppettoVisitingException e) {
			
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
