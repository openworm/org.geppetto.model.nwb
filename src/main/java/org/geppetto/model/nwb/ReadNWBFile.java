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
package org.geppetto.model.nwb;

import java.util.ArrayList;
import java.util.List;

import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.h5.H5File;

import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.model.util.PointerUtility;


public class ReadNWBFile
{
	boolean fileOpened = false;
	private void openNWBFile(H5File nwbFile) throws GeppettoExecutionException
	{
		try
		{
			nwbFile.open();
		}
		catch(Exception e1)
		{
			throw new GeppettoExecutionException(e1);
		}

		this.fileOpened = true;
	}

	public void closeNWBFile(H5File nwbFile) throws GeppettoExecutionException
	{
		if(this.fileOpened)
		{
			try
			{
				nwbFile.close();
			}
			catch(Exception e1)
			{
				throw new GeppettoExecutionException(e1);
			}

			this.fileOpened = false;
		}
	}

	private static double[] readArray(String path, H5File nwbFile) throws GeppettoExecutionException 
	{
		try
		{
			Dataset v = (Dataset) FileFormat.findObject(nwbFile, path);
			if(v==null)
			{
				v=(Dataset) FileFormat.findObject(nwbFile, PointerUtility.getPathWithoutTypes(path));
			}
			Object readData = v.read();
			double[] data = null;
			if(readData instanceof double[])
			{
					data  = (double[]) readData;				
			}
			else if(readData instanceof float[])
			{
					throw new GeppettoExecutionException("Only double values supported");
			}
			else if(readData instanceof int[])
			{
					throw new GeppettoExecutionException("Only double values supported");
			}
			return data;

		}
		catch(Exception e)
			{
				throw new GeppettoExecutionException("Error reading a variable inside readArray()", e);
			}
		finally{
			
		}
	}
	public ArrayList<Integer> getSweepNumbers(H5File nwbFile) throws GeppettoExecutionException
	{
		ArrayList<Integer> sweepNumbers = new ArrayList<Integer>();
		try{
			openNWBFile(nwbFile);
			Group root = (Group) ((javax.swing.tree.DefaultMutableTreeNode) nwbFile.getRootNode()).getUserObject();
			Dataset dataset = (Dataset) root.getMemberList();
			//nwbFile.get
//			Dataset dataset = (Dataset) FileFormat.findObject(nwbFile, "/epochs");
//			Object obj = dataset.read();
//			
//			for (int i = 0; i < mystring.size(); ++i)
//			{
//				if(mystring.get(i).startsWith("Sweep_"))
//				{
//					String [] num =  mystring.get(i).split("_");
//            	
//					if (num.length >1)
//					{
//						if (num[1] != "")
//							sweep_numbers.add(Integer.parseInt(num[1]));
//					}
//				}	
//			}
		}
		catch(Exception e)
		{
			throw new GeppettoExecutionException("Error reading a variable from the recording", e);
		}
		
		finally
		{
			closeNWBFile(nwbFile);
		}
        
        return sweepNumbers;
	}

	public NWBObject readNWBFile(String path, H5File nwbFile) throws GeppettoExecutionException
	{
		NWBObject nwbObject;
		try{
			nwbObject = readNWBFileHelper(path, nwbFile);
			for(int i=0; i<nwbObject.stimulus.length; i++)	//converting stimulus to pA, response -> current
				nwbObject.stimulus[i] =  nwbObject.stimulus[i] * 1000000000000.0;
			
			for(int i=0; i<nwbObject.response.length; i++)	// converting to mV, response -> voltage 
		    	   nwbObject.response[i] = nwbObject.response[i] * 1000.0; 
			
			nwbObject.sampling_rate = 1.0 / nwbObject.sampling_rate; //calculating sampling rate;
		
		}
		catch(Exception e)
		{
			throw new GeppettoExecutionException("Error reading a variable from the recording", e);
		}
		return nwbObject;
		
	}
	public NWBObject readNWBFileHelper(String path, H5File nwbFile) throws GeppettoExecutionException
	{
		NWBObject nwbObject = new NWBObject();
		try
		{
			openNWBFile(nwbFile);
			nwbObject.stimulus = readArray(path + "/stimulus/timeseries/data", nwbFile);
			nwbObject.response = readArray(path + "/response/timeseries/data", nwbFile);
		    Dataset dataset = (Dataset) FileFormat.findObject(nwbFile, path + "/stimulus/idx_start");
			Object obj = dataset.read();
			nwbObject.swp_idx_start = ((int[]) obj)[0];
			dataset = (Dataset) FileFormat.findObject(nwbFile, path + "/stimulus/count");
			int len  = ((int[]) dataset.read())[0];
			nwbObject.swp_idx_stop = nwbObject.swp_idx_start + len - 1;
			dataset = (Dataset) FileFormat.findObject(nwbFile, path + "/stimulus/timeseries/starting_time");
			List<Attribute> attributes = dataset.getMetadata();
			for(Attribute a : attributes)
			{
				if(a.getName().equals("rate"))
				{
					nwbObject.sampling_rate = ((double[]) a.getValue())[0];
					System.out.println("samplig rate : " + nwbObject.sampling_rate);
				}			
			}
		}
		catch(Exception e)
		{
			throw new GeppettoExecutionException("Error reading a variable from the recording", e);
		}
		finally{
			closeNWBFile(nwbFile);
		}
		return nwbObject;
	}
}
