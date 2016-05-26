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
import java.util.Arrays;
import java.util.List;

import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.DataFormat;
import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5File;

import org.geppetto.core.common.GeppettoExecutionException;
import org.geppetto.core.model.GeppettoModelAccess;
import org.geppetto.model.types.CompositeType;
import org.geppetto.model.types.Type;
import org.geppetto.model.types.TypesPackage;
import org.geppetto.model.util.GeppettoVisitingException;
import org.geppetto.model.util.PointerUtility;
import org.geppetto.model.values.Text;
import org.geppetto.model.values.TimeSeries;
import org.geppetto.model.values.Unit;
import org.geppetto.model.values.ValuesFactory;
import org.geppetto.model.variables.Variable;
import org.geppetto.model.variables.VariablesFactory;

public class ReadNWBFile
{
	boolean fileOpened = false;
	int numberOfRecordings;

	private void openNWBFile(H5File nwbFile) throws GeppettoExecutionException
	{
		if(!this.fileOpened)
		{
			try
			{
				nwbFile.open();
			}
			catch(Exception e1)
			{
				throw new GeppettoExecutionException(e1);
			}
			finally
			{
				this.fileOpened = true;
			}
		}
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
			finally
			{
				this.fileOpened = false;
			}
		}
	}

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

	public Variable createTextVariable(String name, String value, String id, GeppettoModelAccess commonLibraryAccess) throws GeppettoVisitingException
	{
		Variable var = VariablesFactory.eINSTANCE.createVariable();
		Type textType = commonLibraryAccess.getType(TypesPackage.Literals.TEXT_TYPE);
		var.getTypes().add(textType);
		var.setId(id);
		var.setName(name);
		Text val = ValuesFactory.eINSTANCE.createText();
		val.setText(value);
		var.getInitialValues().put(textType, val);
		return var;

	}

	public void getNWBMetadataHelper(H5File nwbFile, String path, CompositeType nwbModelMetadataType, GeppettoModelAccess commonLibrayAccess) throws OutOfMemoryError, Exception
	{
		/*
		 * CompositeType nwbMetadata = TypesFactory.eINSTANCE.createCompositeType(); nwbMetadata.setName("nwb_metadata"); nwbMetadata.setId("nwb_metadata_id");
		 */

		Group general = (Group) nwbFile.get(path);
		List<HObject> members = general.getMemberList();
		int n = members.size();
		HObject obj = null;
		for(int i = 0; i < n; i++)
		{
			obj = (HObject) members.get(i);
			if(obj instanceof Group)
			{
				String newPath = path + "/" + obj.toString();
				System.out.println(newPath);
				// Group epochs = (Group) obj;
				getNWBMetadataHelper(nwbFile, newPath, nwbModelMetadataType, commonLibrayAccess);
			}
			else
			{
				String name = obj.toString();
				String id = name + "_id";
				Dataset data = (Dataset) FileFormat.findObject(nwbFile, path + "/" + name);
				if(data == null)
				{
					data = (Dataset) FileFormat.findObject(nwbFile, PointerUtility.getPathWithoutTypes(path + name));
				}
				String value = ((String[]) data.read())[0];
				Variable var = createTextVariable(name, value, id, commonLibrayAccess);
				nwbModelMetadataType.getVariables().add(var);
				System.out.println(obj.toString());
			}
		}
	}

	public void getNWBMetadata(H5File nwbFile, String path, CompositeType nwbModelMetadataType, GeppettoModelAccess commonLibrayAccess) throws GeppettoExecutionException
	{
		try
		{
			openNWBFile(nwbFile);
			getNWBMetadataHelper(nwbFile, path, nwbModelMetadataType, commonLibrayAccess);

		}
		catch(Exception e)
		{
			throw new GeppettoExecutionException("Error while reading metadata from NWB file", e);
		}
		finally
		{
			closeNWBFile(nwbFile);
		}

	}

	public ArrayList<Integer> getSweepNumbers(H5File nwbFile) throws GeppettoExecutionException
	{
		ArrayList<Integer> sweepNumbers = new ArrayList<Integer>();
		try
		{
			openNWBFile(nwbFile);
			Group root = (Group) ((javax.swing.tree.DefaultMutableTreeNode) nwbFile.getRootNode()).getUserObject();
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
		finally
		{
			closeNWBFile(nwbFile);
		}

		return sweepNumbers;
	}

	private void getTimeSeriesData(H5File nwbFile, String path, String str, CompositeType nwbModelType, GeppettoModelAccess commonLibraryAccess) throws GeppettoExecutionException
	{
		try
		{
			String unit = getAttribute(nwbFile, path, "unit");
			if(unit == null || unit == "")
			{
				unit = getAttribute(nwbFile, path, "units");
			}
			Double unitCoverter = null;
			String convertedUnit;
			if(unit.equals("Amps"))
			{
				unitCoverter = 1000000000000.0;
				convertedUnit = "pA";
			}
			else if(unit.equals("Volts"))
			{
				unitCoverter = 1000.0;
				convertedUnit = "mV";
			}
			else
			{
				throw new GeppettoExecutionException("Only Volts/Amps values supported");
			}
			Variable var = VariablesFactory.eINSTANCE.createVariable();
			var.getTypes().add(commonLibraryAccess.getType(TypesPackage.Literals.STATE_VARIABLE_TYPE));
			var.setId(str);
			var.setName(str);
			TimeSeries myTimeSeries = ValuesFactory.eINSTANCE.createTimeSeries();
			Unit myUnit = ValuesFactory.eINSTANCE.createUnit();
			myUnit.setUnit(convertedUnit);
			myTimeSeries.setUnit(myUnit);

			Dataset v = (Dataset) FileFormat.findObject(nwbFile, path);
			if(v == null)
			{
				v = (Dataset) FileFormat.findObject(nwbFile, PointerUtility.getPathWithoutTypes(path));
			}
			Object readData = v.read();
			if(readData instanceof double[])
			{
				double[] data = (double[]) readData;
				numberOfRecordings = data.length;
				Double timeSeriesData[] = new Double[numberOfRecordings];
				for(int i = 0; i < data.length; i++)
				{
					timeSeriesData[i] = Double.valueOf(data[i] * unitCoverter);
				}
				myTimeSeries.getValue().addAll(Arrays.asList(timeSeriesData));
				var.getInitialValues().put(commonLibraryAccess.getType(TypesPackage.Literals.STATE_VARIABLE_TYPE), myTimeSeries);
				nwbModelType.getVariables().add(var);
			}
			else if(readData instanceof float[])
			{
				// since addAll function does not support float values converting float to double for time being.
				float[] data = (float[]) readData;
				numberOfRecordings = data.length;
				Double[] timeSeriesData = new Double[numberOfRecordings];
				for(int i = 0; i < data.length; i++)
				{
					float f = ((float) (data[i] * unitCoverter));
					timeSeriesData[i] = Double.valueOf((double) f);

				}
				myTimeSeries.getValue().addAll(Arrays.asList(timeSeriesData));
				var.getInitialValues().put(commonLibraryAccess.getType(TypesPackage.Literals.STATE_VARIABLE_TYPE), myTimeSeries);
				nwbModelType.getVariables().add(var);
			}
			else
			{
				throw new GeppettoExecutionException("Only double/float values supported");
			}

		}
		catch(Exception e)
		{
			throw new GeppettoExecutionException("Error reading a variable inside readArray()", e);
		}

	}

	public void readNWBFile(H5File nwbFile, String path, CompositeType nwbModelType, GeppettoModelAccess commonLibraryAccess) throws GeppettoExecutionException
	{
		try
		{
			openNWBFile(nwbFile);
			getTimeSeriesData(nwbFile, path + "/stimulus/timeseries/data", "stimulus", nwbModelType, commonLibraryAccess);
			getTimeSeriesData(nwbFile, path + "/response/timeseries/data", "response", nwbModelType, commonLibraryAccess);
			Dataset dataset = (Dataset) FileFormat.findObject(nwbFile, path + "/stimulus/idx_start");
			Object obj = dataset.read();
			int swpIdxStart = ((int[]) obj)[0];
			dataset = (Dataset) FileFormat.findObject(nwbFile, path + "/stimulus/count");
			int len = ((int[]) dataset.read())[0];
			len = swpIdxStart + len - 1;
			String samplingRate_str = getAttribute(nwbFile, path + "/response/timeseries/starting_time", "rate");
			Double samplingRate = Double.parseDouble(samplingRate_str);
			double dt = 1.0 / samplingRate;
			Double[] timeAxis = new Double[numberOfRecordings];
			for(int i = 0; i < numberOfRecordings; i++)
				timeAxis[i] = Double.valueOf(i * dt);
			Variable var = VariablesFactory.eINSTANCE.createVariable();
			var.getTypes().add(commonLibraryAccess.getType(TypesPackage.Literals.STATE_VARIABLE_TYPE));
			var.setId("time");
			var.setName("time");
			TimeSeries myTimeSeries = ValuesFactory.eINSTANCE.createTimeSeries();
			myTimeSeries.getValue().addAll(Arrays.asList(timeAxis));
			var.getInitialValues().put(commonLibraryAccess.getType(TypesPackage.Literals.STATE_VARIABLE_TYPE), myTimeSeries);
			nwbModelType.getVariables().add(var);
		}
		catch(Exception e)
		{
			throw new GeppettoExecutionException("Error while reading stimulus and response from NWB file", e);
		}
		finally
		{
			closeNWBFile(nwbFile);
		}

	}
}
