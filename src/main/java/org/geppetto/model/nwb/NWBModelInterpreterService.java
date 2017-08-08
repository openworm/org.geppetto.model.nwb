

package org.geppetto.model.nwb;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

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
		reader.setParameters(nwbModelType, library, commonLibraryAccess);
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
			reader.readNWBFile(nwbFile, path);
			reader.getNWBMetadata(nwbFile, "/general");
			reader.getInitialData(nwbFile);
			
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
		String path = ((Variable)importValue.eContainer().eContainer()).getPath();
		StringTokenizer st = new StringTokenizer(path, ".");
		String dataPath = null;
		while(st.hasMoreElements()){
			String token = st.nextToken();
			if(token.startsWith("stimulus") || token.startsWith("response")){
				String[] parts = token.split("_");
				if (parts.length != 3)
					break;
				 dataPath =  "/epochs/" + parts[1] + "_" + parts[2] + "/" + parts[0].replace("T", "") + "/timeseries/data";
				 break; 
			}
		}
		TimeSeries myTimeSeries;
		try {
			if (dataPath == null)
				throw new ModelInterpreterException("Exception while reading time series for lazy loading");
			myTimeSeries = reader.getTimeSeriesData(nwbFile, dataPath);
		} catch (GeppettoExecutionException e) {
			throw new ModelInterpreterException("Exception while reading time series for lazy loading");
		}
		return (Value)myTimeSeries;
	}
	
}
