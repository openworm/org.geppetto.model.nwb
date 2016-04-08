package org.geppetto.model.nwb.test;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;

import ncsa.hdf.object.h5.H5File;

import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.common.HDF5Reader;
import org.geppetto.model.nwb.NWBObject;
import org.geppetto.model.nwb.ReadNWBFile;
import org.geppetto.model.util.GeppettoVisitingException;
import org.junit.Assert;
import org.junit.Test;

public class ReadNWBDataTest {

	@Test
	public void nwbDataExtractionTest() throws MalformedURLException, GeppettoExecutionException, GeppettoVisitingException
	{
		//this.setup();
		H5File file=HDF5Reader.readHDF5File(new File("./src/main/resources/354190011.nwb").toURI().toURL(),-1l);
		String path = "/epochs/Sweep_0";
		ReadNWBFile rd = new ReadNWBFile();
		ArrayList<Integer> sweepNumber = rd.getSweepNumbers(file);
		NWBObject nwb  = rd.readNWBFile(path, file);
		
		Assert.assertNotNull(file);
		Assert.assertNotNull(nwb);
		Assert.assertNotNull(sweepNumber);
	
//		for (int i=0; i< sweepNumber.size(); i++)
//			System.out.println("Sweep_" + sweepNumber.get(i));
//		for(int i=0; i<3000; i++)
//			System.out.println("stimulus " + nwb.stimulus[i]);
//		for(int i=0; i<3000; i++)
//			System.out.println("response " + nwb.response[i]);
		System.out.println("sampling_rate " + nwb.sampling_rate);
		System.out.println("start_index " + nwb.swp_idx_start);
		System.out.println("stop_index " + nwb.swp_idx_stop);
		
	}
}
