package org.geppetto.model.nwb;

import java.util.ArrayList;
import java.util.List;

import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.hdf5lib.exceptions.HDF5FileNotFoundException;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5SimpleReader;
import ch.systemsx.cisd.hdf5.IHDF5Reader;


public class GetNWBData {
	public String  file_name;
	
	public GetNWBData(String file_name) 
	{
		this.file_name = file_name;
	}
	
	public ArrayList<Integer> get_sweep_numbers(){
		ArrayList<Integer> sweep_numbers = new ArrayList<Integer>();
		IHDF5SimpleReader reader = null;
		try{
			reader = HDF5Factory.openForReading(file_name);
			List<String> mystring =  reader.getGroupMembers("/epochs");
			for (int i = 0; i < mystring.size(); ++i)
			{
				if(mystring.get(i).startsWith("Sweep_"))
				{
					String [] num =  mystring.get(i).split("_");
            	
					if (num.length >1)
					{
						if (num[1] != "")
							sweep_numbers.add(Integer.parseInt(num[1]));
					}
				}	
			}
		}
//		catch(HDF5Exception ex )
//		{
//			ex.printStackTrace();
//		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			reader.close();
		}
        
        return sweep_numbers;
	}
	
	NWBObject get_sweep(int sweep_number) 
	{
		double[] stimulus = null;
		double [] response = null;
		double sampling_rate = 0.0;
		int swp_idx_start = 0, swp_idx_stop = 0;
		
		IHDF5Reader my_reader = null;
		try
		{
			/*reader2 = HDF5Factory.openForReading(file_name);
			String path = "/epochs/Sweep_" + sweep_number;
			stimulus = reader.readFloatArray(path + "/stimulus/timeseries/data");
			response = reader.readFloatArray(path + "/response/timeseries/data");
			swp_idx_start = reader.readInt(path + "/stimulus/idx_start");
			int swp_length = reader.readInt(path + "/stimulus/count");
			sampling_rate = reader.readFloat(path + "/stimulus/timeseries/starting_time");
			List<String> st = reader.getGroupMembers(path + "/stimulus/timeseries");
			swp_idx_stop = swp_idx_start + swp_length -1;
			*/
			my_reader = HDF5Factory.openForReading(file_name);
			String path = "/epochs/Sweep_" + sweep_number;
			stimulus = my_reader.float64().readArray(path + "/stimulus/timeseries/data");
			System.out.println("Nitesh is here ");
			response = my_reader.float64().readArray(path + "/response/timeseries/data");
			swp_idx_start = my_reader.int32().read(path + "/stimulus/idx_start");
			int swp_length = my_reader.int32().read(path + "/stimulus/count");
			sampling_rate  = my_reader.float64().getAttr(path + "/stimulus/timeseries/starting_time", "rate");
			swp_idx_stop = swp_idx_start + swp_length - 1;
			/*
			attrReader.float64().getAttr(arg0, arg1)
			for(int i=0; i<st.size(); i++)
				System.out.println(st.get(i));
			System.out.println("sampling_rate " + sampling_rate);
			System.out.println(swp_idx_start+ " " + swp_idx_stop + " " + swp_length);
			double sampling_rate = reader.readFloat(path + "/stimulus/timeseries/starting_time");
			*/
		}
//		catch(HDF5FileNotFoundException ex)
//		{
//			ex.printStackTrace();
//		}
		finally
		{
			my_reader.close();
		}
		return new NWBObject(stimulus, response, swp_idx_start, swp_idx_stop, sampling_rate);
	
	}
	
	
}
