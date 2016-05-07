package org.geppetto.model.nwb.test;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;

import ncsa.hdf.object.h5.H5File;

import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.common.HDF5Reader;
import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.model.nwb.NWBObject;
import org.geppetto.model.nwb.ReadNWBFile;
import org.geppetto.model.types.CompositeType;
import org.geppetto.model.util.GeppettoVisitingException;
import org.junit.Assert;
import org.junit.Test;

public class ReadNWBDataTest {

	@Test
	public void nwbDataExtractionTest() throws MalformedURLException, GeppettoExecutionException, GeppettoVisitingException
	{
		//this.setup();
		H5File file=HDF5Reader.readHDF5File(new File("./src/main/resources/313862020.nwb").toURI().toURL(),-1l);
		String path = "/epochs/Sweep_12";
		ReadNWBFile rd = new ReadNWBFile();
		ArrayList<Integer> sweepNumber = rd.getSweepNumbers(file);
		GeppettoModelAccess commonLibrayAccess = null;
		CompositeType nwbModelType = null;
		rd.readNWBFile(file, path, nwbModelType, commonLibrayAccess);
		CompositeType nwbModelType2 = null;
		rd.getNWBMetadata(file, "/general", nwbModelType2, commonLibrayAccess);
		Assert.assertNotNull(file);
		Assert.assertNotNull(nwbModelType);
		Assert.assertNotNull(nwbModelType2);
		Assert.assertNotNull(sweepNumber);
	
		for (int i=0; i< sweepNumber.size(); i++)
			System.out.println("Sweep_" + sweepNumber.get(i));
//		for(int i=0; i<3000; i++)
//			System.out.println("stimulus " + nwb.stimulus[i]);
//		for(int i=0; i<nwb.response.length; i++){
//			if (nwb.response[i] > 2000.0)
//				System.out.println("response "+ i + " " + nwb.response[i]);
//		}
//		System.out.println("sampling_rate " + nwb.samplingRate);
//		System.out.println("Stimulus Unit " + nwb.stimulusUnit);
//		System.out.println("Response Unit " + nwb.responseUnit);
//		System.out.println("start_index " + nwb.swpIdxStart);
//		System.out.println("stop_index " + nwb.swpIdxStop);
		
	}
}
