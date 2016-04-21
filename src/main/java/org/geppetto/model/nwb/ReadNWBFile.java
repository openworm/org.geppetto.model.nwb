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
import ncsa.hdf.object.DataFormat;
import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;
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
			if(v == null)
			{
				v = (Dataset) FileFormat.findObject(nwbFile, PointerUtility.getPathWithoutTypes(path));
			}
			Object readData = v.read();
			double[] data = null;
			if(readData instanceof double[])
			{
				data = (double[]) readData;
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
		finally
		{

		}
	}

	/*
	 * private static void printGroup(Group g, String indent) throws Exception { if (g == null) return; java.util.List members = g.getMemberList(); // System.out.println("Number of members" +
	 * g.getNumberOfMembersInFile()); int n = members.size(); indent += "    "; HObject obj = null; for (int i = 0; i < n; i++) { obj = (HObject) members.get(i); System.out.println(indent + obj); if
	 * (obj instanceof Group) { if (obj instanceof Group) { printGroup((Group) obj, indent); }
	 * 
	 * } } }
	 */
	private static String getAttribute(H5File nwbFile, String path, String attributeName) throws GeppettoExecutionException
	{
		try
		{
			DataFormat dataset = (Dataset) FileFormat.findObject(nwbFile, path);
			List<Attribute> attributes = dataset.getMetadata();
			for(Attribute a : attributes)
			{
				if(a.getName().equals(attributeName))
				{
					Object obj = a.getValue();
					if(obj instanceof double[])
					{
						Double d = ((double[]) obj)[0];
						return d.toString();
					}
					else if(obj instanceof String[]) return ((String[]) obj)[0];
				}
			}
		}
		catch(Exception e)
		{
			throw new GeppettoExecutionException("Error reading a variable from the recording", e);
		}
		return "";
	}

	public ArrayList<Integer> getSweepNumbers(H5File nwbFile) throws GeppettoExecutionException
	{
		ArrayList<Integer> sweepNumbers = new ArrayList<Integer>();
		try
		{
			openNWBFile(nwbFile);
			Group root = (Group) ((javax.swing.tree.DefaultMutableTreeNode) nwbFile.getRootNode()).getUserObject();
			/*
			 * to ask - getMemberList() function at first level expects all to be Groups, if it finds a dataset file it terminates from that point without returning remaining groups below that data
			 * set file. this happens at level 0 only, levels above this it returns all members
			 */
			// printGroup((Group)root.getMemberList().get(0), "");
			if(root == null) return null;
			List members = root.getMemberList();
			int n = members.size();
			HObject obj = null;
			for(int i = 0; i < n; i++)
			{
				obj = (HObject) members.get(i);
				if(obj instanceof Group && obj.toString().equals("epochs"))
				{
					Group epochs = (Group) obj;
					List allMembers = epochs.getMemberList();
					int len = allMembers.size();
					for(int j = 0; j < len; j++)
					{
						String s = allMembers.get(j).toString();
						if(s.startsWith("Sweep_"))
						{
							String[] num = s.split("_");
							if(num.length > 1)
							{
								if(num[1] != "") sweepNumbers.add(Integer.parseInt(num[1]));
							}
						}
					}
					break;
				}
			}
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

	/*
	 * public NWBObject readNWBFile(String path, H5File nwbFile) throws GeppettoExecutionException { NWBObject nwbObject; try{ nwbObject = readNWBFileHelper(path, nwbFile); for(int i=0;
	 * i<nwbObject.stimulus.length; i++) //converting stimulus to pA, response -> current nwbObject.stimulus[i] = nwbObject.stimulus[i] * 1000000000000.0;
	 * 
	 * for(int i=0; i<nwbObject.response.length; i++) // converting to mV, response -> voltage nwbObject.response[i] = nwbObject.response[i] * 1000.0;
	 * 
	 * nwbObject.sampling_rate = 1.0 / nwbObject.sampling_rate; //calculating sampling rate;
	 * 
	 * } catch(Exception e) { throw new GeppettoExecutionException("Error reading a variable from the recording", e); } return nwbObject;
	 * 
	 * }
	 */
	public NWBObject readNWBFile(String path, H5File nwbFile) throws GeppettoExecutionException
	{
		NWBObject nwbObject = new NWBObject();
		try
		{
			openNWBFile(nwbFile);
			double stimulus[] = readArray(path + "/stimulus/timeseries/data", nwbFile);
			double response[] = readArray(path + "/response/timeseries/data", nwbFile);
			Dataset dataset = (Dataset) FileFormat.findObject(nwbFile, path + "/stimulus/idx_start");
			Object obj = dataset.read();
			nwbObject.swpIdxStart = ((int[]) obj)[0];
			dataset = (Dataset) FileFormat.findObject(nwbFile, path + "/stimulus/count");
			int len = ((int[]) dataset.read())[0];
			nwbObject.swpIdxStop = nwbObject.swpIdxStart + len - 1;

			String stimulusUnit = getAttribute(nwbFile, path + "/stimulus/timeseries/data", "units");
			// String responseUnit = getAttribute(nwbFile, path + "/response/timeseries/data", "units");
			String samplingRate = getAttribute(nwbFile, path + "/response/timeseries/starting_time", "rate");

			nwbObject.samplingRate = Double.parseDouble(samplingRate);
			nwbObject.stimulus = new Double[stimulus.length];
			nwbObject.response = new Double[response.length];

			Double stimulusConversion, responseConversion;
			if(stimulusUnit.equals("Amps"))
			{
				stimulusConversion = 1000000000000.0;
				responseConversion = 1000.0;
				nwbObject.stimulusUnit = "pA";
				nwbObject.responseUnit = "mV";
			}
			else
			{
				stimulusConversion = 1000.0;
				responseConversion = 1000000000000.0;
				nwbObject.stimulusUnit = "mV";
				nwbObject.responseUnit = "pA";
			}

			for(int i = 0; i < stimulus.length; i++)
			{
				nwbObject.stimulus[i] = Double.valueOf(stimulus[i] * stimulusConversion);
			}
			for(int i = 0; i < response.length; i++)
			{
				nwbObject.response[i] = Double.valueOf(response[i] * responseConversion);
			}
			nwbObject.samplingRate = 1.0 / nwbObject.samplingRate;

		}
		catch(Exception e)
		{
			throw new GeppettoExecutionException("Error reading a variable from the recording", e);
		}
		finally
		{
			closeNWBFile(nwbFile);
		}
		return nwbObject;
	}
}
