package android.device.eeg;

import android.util.Log;

import java.util.ArrayList;

public class CognionicsQ30DataParser {

	private final static String TAG = "CognionicsQ30DataParser";
	
	static byte[] data;
	
	static byte[] incompletePacket = new byte[0]; // content from incomplete packet
	
	static CognionicsQ30DataSample prevSample; // duplicate from this sample if the current sample is lost
	
//	final static int PACKET_NUM_TO_IGNORE = 500;
//	static int packetNumToIgnoreCounter = 0;
	
	// interpolation
	static float weight;
	static double[] interpolateChannelData;
	
	public static CognionicsQ30DataSample[] parse(byte[] iData) {
		ArrayList<CognionicsQ30DataSample> samples = new ArrayList<CognionicsQ30DataSample>();
		
		// an internal buffer to store the content from partial packet + current buffer
//		Log.d(TAG, "iData size=" + iData.length);
		data = new byte[incompletePacket.length + iData.length];
//		Log.d(TAG, "data size=" + data.length);
		
		// fill in the buffer with the incomplete packet data (if any from previous parsing)
		for (int x = 0 ; x < incompletePacket.length ; x++)
			data[x] = incompletePacket[x];

		// append the remaining data with the buffer received in current parsing
		// [Data from previous incomplete packet] [Data from current received buffer]
		for (int x = incompletePacket.length, i = 0 ; i < iData.length ; x++, i++)
			data[x] = iData[i];
		
		int nextPacketHeaderIdx; // the index of the next header in the buffer
		int i = 0; // current index in the buffer
		
		do {
			// find the packet header 0xFF
			if (CognionicsQ30.Packet.HEADER.hex != data[i]) {
//				Log.d(TAG, "No HEADER found!");
				i++;
				continue; // continue to next index if no header is found
			}
			
			// header is found, check whether this is a complete packet (96 bytes for Cognionics Q30)
			else {
				// ignore the first n packet for the connection to settle down
//				if (packetNumToIgnoreCounter++ < PACKET_NUM_TO_IGNORE) {
//					Log.d(TAG, "Ignoring packet=" + packetNumToIgnoreCounter);
//					continue;
//				}
				
//				Log.d(TAG, "HEADER is found!");
				// calculate the next index to check for 0xFF. 
				// If 0xFF is found on data[5], the next 0xFF to check is data[101]
				nextPacketHeaderIdx = i + EegDeviceSpec.COGNIONICS_Q30.packetSize;
				
				// make sure the next index to check is less than the buffer received
				// Log.d(TAG, "Checking whether next header index=" + nextPacketHeaderIdx + " is greater than data.length=" + data.length);
				if (nextPacketHeaderIdx >= data.length) { // the next header is not in the current received buffer
//					Log.d(TAG, "YES");

					// still check for the remaining bytes for header just to be safe
					int tmpIdx = i;
					for ( ; tmpIdx < data.length ; tmpIdx++) {
						// if for some unknown reason there is header found
						if (CognionicsQ30.Packet.HEADER.hex == data[tmpIdx]) {
							/**
							 *  Hex		... 0xFF	... 0xFF 0x44   
							 *	Index		 95			 98	  99	END_OF_BUFFER
							 */
							i = tmpIdx; // update the index in the current buffer to the header found
						}
					}
					
					// store the incomplete data in a temporary buffer so that
					// it can be checked in next receive when parse is called again
					incompletePacket = new byte[data.length - i];
					for (int j = 0 ; j < incompletePacket.length ; j++) {
						incompletePacket[j] = data[i];
						// incompletePacket[0] must be 0xFF
						i++;
					}
					
					// debug
					for (int iP = 0 ; iP < incompletePacket.length ; iP++) {
						String msg = String.format("incompletePacket[%d]=%02x", iP, incompletePacket[iP]);
						// Log.d(TAG, msg);
					}
						
					break;
				} // end of the next header is not in the current received buffer
				
				// good, the next index to check is somewhere in this buffer
				// now check whether it is 0xFF
				if (CognionicsQ30.Packet.HEADER.hex == data[nextPacketHeaderIdx]) {
					/**
					 * Byte		 ... 0xFF ... 0xFF
					 * Index		  [32] 	   [128]	
					 */
					// complete packet is found, now start to parse each field
					CognionicsQ30DataSample sample = new CognionicsQ30DataSample();
					
					sample.setPacketCounter(data[++i]);
					double[] channel = new double[EegDeviceSpec.COGNIONICS_Q30.totalChannels];
					
					// do bitwise shifting
					/**
					 * http://cognionics.com/wiki/uploads/Main/channel_bytes.png
					 * | a1 a2 a3 a4 a5 a6 a7 b1 | b2 b3 b4 b5 b6 b7 c1 c2 | c3 c4 c5 c6 c7 0 0 0 |
					 */
					int channelData = 0; // int = 4 bytes
					int[] chDataTmp = new int[3];
					for (int chIdx = 0 ; chIdx < channel.length ; chIdx++) {
						channelData = 0;
						
//						channelData = ( (data[++i] & 0xFF) << 24 | (data[++i] & 0xFF) << 17 | (data[++i] & 0xFF) << 10 );
						
						chDataTmp[0] = data[++i] & 0xFF; // msb
						chDataTmp[1] = data[++i] & 0xFF; // lsb1
						chDataTmp[2] = data[++i] & 0xFF; // lsb2
						channelData = channelData | (chDataTmp[0] << 24);
						channelData = channelData | (chDataTmp[1] << 17);
						channelData = channelData | (chDataTmp[2] << 10);
						
//						channelData = channelData | data[++i]&(0xFF) << 24;
//						channelData = channelData | data[++i]&(0xFF) << 17;
//						channelData = channelData | data[++i]&(0xFF) << 10;
						channel[chIdx] = channelData * (double)(5.0f/3.0f) * 1.0f/(Math.pow(2, 32));
					}
					
//					Log.d(TAG, "Channel Data[8]=" + channel[7]);
					
					// finished parsing all 30 channels
					sample.setChannel(channel);
					
					byte impedenceOnStatus = data[++i];
                    sample.setImpedanceCheckStatus(CognionicsQ30.Packet.IMPEDENCE_ON.hex == impedenceOnStatus);
					sample.setBatteryVoltage(data[++i]);
					
					int trigger = 0;
					trigger = trigger | data[++i] << 8;
					trigger = trigger | data[++i];
					sample.setTrigger(trigger);
					
					// start interpolation
					if (prevSample != null && false) { // skip the check if this is the 1st packet
						// check whether the previous packet counter is curr-1
						if (sample.getPacketCounter() - prevSample.getPacketCounter() == 1 || 
								sample.getPacketCounter() - prevSample.getPacketCounter() == -127) {
						}
	
						else { // the current packet counter is not prev packet counter-1
							int packetLossNum;
							
							/** Step 1: Calculate how many packet is loss **/
							// [78] 			[91]
							// prevSample		sample
							if (sample.getPacketCounter() > prevSample.getPacketCounter()) {
								packetLossNum = sample.getPacketCounter() - prevSample.getPacketCounter() - 1;
							}
							
							//  [126] 			[10]
							//	prevSample		sample
							else {
								packetLossNum = sample.getPacketCounter() + (127-prevSample.getPacketCounter());
							}
							
							Log.d(TAG, "Prev sample packetnum=" + prevSample.getPacketCounter());
							Log.d(TAG, "Curr sample packetnum=" + sample.getPacketCounter());
							Log.d(TAG, "Packet loss=" + packetLossNum);
							
							/** Step 2: Linear interpolation (weighed) **/
//							int prevPacketSampleCounter = prevSample.getPacketCounter();
							
							CognionicsQ30DataSample tmpSample = prevSample;
							interpolateChannelData = new double[tmpSample.getChannel().length];
							for (int pL = 1 ; pL <= packetLossNum ; pL++) {
//								prevSample.setPacketCounter((++prevPacketSampleCounter)%127);
								
								weight = (float)pL/(float)(packetLossNum+1);
								
								/**
								 * interpolate[ch] = prevCh[ch] + (alpha * (currCh[ch] - prevCh[ch]) )
								 */
								for(int ch = 0 ; ch < prevSample.getChannel().length ; ch++) {
									interpolateChannelData[ch] = prevSample.getChannel()[ch] + ( weight * (sample.getChannel()[ch] - prevSample.getChannel()[ch]));
								}
								
								tmpSample.setChannel(interpolateChannelData);
								samples.add(tmpSample);
							}
						}
					}
						
					prevSample = sample; // the next missing packet will use this sample
					// end of interpolation
					
					samples.add(sample);

					
				} // end of parsing 1 packet (96 bytes)
				
				// the next index to check is not 0xFF, probably the current packet is incomplete, 
				// ignore and skip to next packet.
				else {
					/**
					 * Example: i=32, it only contains 95 bytes instead of 96 bytes, missing 1 byte
					 * Byte		 ... 0xFF ...	0xFF	0x44
					 * Index		  [32] 	    127		[128]
					 */
					i++; // move to the next index in data buffer
					continue;
				}
					
			} // end of header is found
			i++; // move to the next index in data buffer
		} while (i < data.length);
		
//		Log.d(TAG, "Finished parsing " + samples.size() + " samples!");
		return samples.toArray(new CognionicsQ30DataSample[samples.size()]);
		
	}
}
