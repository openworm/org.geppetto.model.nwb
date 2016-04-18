package org.geppetto.model.nwb;

public class NWBObject {
	public Double[] stimulus;
	public Double [] response;
	public double samplingRate;
	public int swpIdxStart;
	public int swpIdxStop;
	public String stimulusUnit;
	public String responseUnit;
//	public NWBObject(Double [] stimulus, double [] response, int swp_idx_start,int swp_idx_stop, double sampling_rate) {
//		this.stimulus = stimulus;
//		this.response = response;
//		this.sampling_rate = sampling_rate;
//		this.swp_idx_start = swp_idx_start;
//		this.swp_idx_stop = swp_idx_stop;
//	}
}