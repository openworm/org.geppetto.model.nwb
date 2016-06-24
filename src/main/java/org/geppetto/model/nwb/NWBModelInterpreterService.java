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
import org.geppetto.model.values.ImportValue;
import org.geppetto.model.values.Pointer;
import org.geppetto.model.values.TimeSeries;
import org.geppetto.model.values.Value;
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
	private H5File nwbFile = null;
	private ReadNWBFile reader = new ReadNWBFile();
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
		CompositeType nwbModelType = TypesFactory.eINSTANCE.createCompositeType();
		nwbModelType.setId(url.getFile());
		nwbModelType.setName(url.getFile());
		CompositeType nwbModelMetadataType = TypesFactory.eINSTANCE.createCompositeType();
		nwbModelMetadataType.setId("nwbMetadata");
		nwbModelMetadataType.setName("nwbMetadata");
		library.getTypes().add(nwbModelMetadataType);
		try
		{
			try
			{
				SetNatives.getInstance().setHDF5Native(System.getProperty("user.dir"));
			}
			catch(IOException e)
			{
				throw new ModelInterpreterException(e);
			}

			nwbFile =  HDF5Reader.readHDF5File(url, -1l);
			reader.openNWBFile(nwbFile);
			
			ArrayList<Integer> sweepNumber = reader.getSweepNumbers(nwbFile);
			String path = "/epochs/Sweep_" + sweepNumber.get(10);
			reader.readNWBFile(nwbFile, path, nwbModelType, commonLibraryAccess);
			reader.getNWBMetadata(nwbFile, "/general", nwbModelMetadataType, commonLibraryAccess);
			
			Variable var = VariablesFactory.eINSTANCE.createVariable();
			var.getTypes().add(nwbModelMetadataType);
			var.setId("metadata");
			var.setName("metadata");
			nwbModelType.getVariables().add(var);
		}
		catch(GeppettoExecutionException e)
		{
			throw new ModelInterpreterException(e);
		}
		return nwbModelType;
	}

	@Override
	public File downloadModel(Pointer pointer, ModelFormat format, IAspectConfiguration aspectConfiguration) throws ModelInterpreterException
	{
		throw new ModelInterpreterException("Download model not implemented for NWB model interpreter");
	}


	@Override
	public Value importValue(ImportValue importValue) throws ModelInterpreterException {
		// parse path to get sweep number and type of time series to return
		
		String path = "/epochs/Sweep_10/response/timeseries/data";
		TimeSeries myTimeSeries;
		try {
			myTimeSeries = reader.getTimeSeriesData(nwbFile, path);
		} catch (GeppettoExecutionException e) {
			throw new ModelInterpreterException("Exception while reading timeseries");
		}
		return myTimeSeries;
	}
	
}
