package org.geppetto.model.nwb.test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import ncsa.hdf.object.h5.H5File;

import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.common.GeppettoInitializationException;
import org.geppetto.core.common.HDF5Reader;
import org.geppetto.core.manager.SharedLibraryManager;
import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.model.GeppettoFactory;
import org.geppetto.model.GeppettoLibrary;
import org.geppetto.model.GeppettoModel;
import org.geppetto.model.nwb.ReadNWBFile;
import org.geppetto.model.types.CompositeType;
import org.geppetto.model.types.TypesFactory;
import org.geppetto.model.util.GeppettoVisitingException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ReadNWBDataTest
{
	private ReadNWBFile reader = new ReadNWBFile();
	private H5File nwbFile = null;
	private URL url;
	@Before
	public void setup() throws GeppettoExecutionException{
		
		try {
			url = new File("./src/main/resources/313862020.nwb").toURI().toURL();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			throw new GeppettoExecutionException("Exception in JUNIT Test");
		}
		nwbFile = HDF5Reader.readHDF5File(url, -1l);
		reader.openNWBFile(nwbFile);
	}
	@Test
	public void nwbDataExtractionTest() throws MalformedURLException, GeppettoExecutionException, GeppettoVisitingException, GeppettoInitializationException
	{
		// this.setup();
		
		String path = "/epochs/Sweep_12";
		ArrayList<Integer> sweepNumber = reader.getSweepNumbers(nwbFile);
		GeppettoLibrary library = GeppettoFactory.eINSTANCE.createGeppettoLibrary();
		library.setId("NWB");
		GeppettoLibrary commonLibrary = SharedLibraryManager.getSharedCommonLibrary();
		GeppettoModel geppettoModel = GeppettoFactory.eINSTANCE.createGeppettoModel();
		geppettoModel.getLibraries().add(library);
		geppettoModel.getLibraries().add(commonLibrary);
		GeppettoModelAccess commonLibraryAccess = new GeppettoModelAccess(geppettoModel);
		CompositeType nwbModelType = TypesFactory.eINSTANCE.createCompositeType();
		nwbModelType.setId(url.getFile());
		nwbModelType.setName(url.getFile());
		reader.readNWBFile(nwbFile, path, nwbModelType, commonLibraryAccess);

		reader.getNWBMetadata(nwbFile, "/general", nwbModelType, commonLibraryAccess);
		Assert.assertNotNull(nwbFile);
		Assert.assertNotNull(nwbModelType);
		Assert.assertNotNull(sweepNumber);

		for(int i = 0; i < sweepNumber.size(); i++)
			System.out.println("Sweep_" + sweepNumber.get(i));
		// for(int i=0; i<3000; i++)
		// System.out.println("stimulus " + nwb.stimulus[i]);
		// for(int i=0; i<nwb.response.length; i++){
		// if (nwb.response[i] > 2000.0)
		// System.out.println("response "+ i + " " + nwb.response[i]);
		// }
		// System.out.println("sampling_rate " + nwb.samplingRate);
		// System.out.println("Stimulus Unit " + nwb.stimulusUnit);
		// System.out.println("Response Unit " + nwb.responseUnit);
		// System.out.println("start_index " + nwb.swpIdxStart);
		// System.out.println("stop_index " + nwb.swpIdxStop);

	}
	
}
