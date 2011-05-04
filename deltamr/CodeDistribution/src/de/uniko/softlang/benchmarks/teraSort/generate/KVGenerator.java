package de.uniko.softlang.benchmarks.teraSort.generate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

import de.uniko.softlang.benchmarks.teraSort.sort.SortInputFormat;

public class KVGenerator {
	public static int LONG_SIZE = 8;
	public static int INT_SIZE = 4;
	private static String separator = " - ";
	private static final int SEPARATOR_SIZE = separator.getBytes().length;
	private static byte[] valueBuffer = new byte[SortInputFormat.VALUE_LENGTH];
	private static byte[] byteValues = { 'A', 'B', 'C', 'D', 'E', 'F', 'G',
			'H', 'I', 'J' };
	private static byte[] fillerBytes = new byte[SortInputFormat.VALUE_LENGTH];
	static {
		for (int i = 0; i < fillerBytes.length; i++) {
			fillerBytes[i] = byteValues[i / 10];
		}
	}
	
	/**
	 * This method creates keys and random values.
	 * @param key
	 *          the object to write to.
	 * @param value
	 *          the object to write to.
	 * @param keyLong
	 *          the long value to use.
	 * @param rowId
	 *          the rowId, that is written into the value (for identification).
	 * @throws IOException
	 */
	public static void generateRecord(LongWritable key, Text value, long keyLong, long rowId) throws IOException{
		int valueOffset =  (int) Math.round(Math.random()*SortInputFormat.VALUE_LENGTH);
		generateDetRecord(key, value, keyLong, rowId, valueOffset, new boolean[0]);
	}
	
	/**
	 * This method deterministically creates keys and values. Keys are generated
	 * using an initial long value. Each time the data changes in a generation of
	 * data, <code>versions</code> will be set to <code>true</code> for that
	 * generation. The key is then simply incremented once for each changed
	 * version. This ensures, that we can control changes between keys from
	 * generation to generation.
	 * 
	 * @param key
	 *          the object to write to.
	 * @param value
	 *          the object to write to.
	 * @param keyLong
	 *          the long value to use.
	 * @param rowId
	 *          the rowId, that is written into the value (for identification).
	 * @param valueOffset
	 *          an offset to pseudo-randomly generate character sequences.
	 * @param versions
	 *          indicates for each generation, if the data has changed.
	 * @throws IOException
	 */
	public static void generateDetRecord(LongWritable key, Text value, long keyLong, long rowId, int valueOffset, boolean[]versions) throws IOException{
		//key
		int increments = 0;
		for(int i = 0; i < versions.length; i++){
			if(versions[i])
				increments++;
		}
		key.set(keyLong+increments);

		//value
		int offset = addSeparator(valueBuffer, 0);
		offset = longToBytes(valueBuffer, offset, rowId);
		offset = intToBytes(valueBuffer, offset, versions.length);
		for(int i = 0; i < versions.length; i++){
			if(versions[i]){
				valueBuffer[offset+i] = (byte)1;
			}else{
				valueBuffer[offset+i] = (byte)0;
			}
		}
		offset += versions.length;
		
		int start = valueOffset;
		for(int i = 0; i < SortInputFormat.VALUE_LENGTH-offset; i++){
			valueBuffer[offset+i] = fillerBytes[(start+i)%fillerBytes.length]; 
		}
		value.set(valueBuffer, 0, SortInputFormat.VALUE_LENGTH);
	}
	
	public static int addSeparator(byte[] buffer, int start){
		for (int i = 0; i < SEPARATOR_SIZE; i++) {
			buffer[start+i] = separator.getBytes()[i];
		}
		return start+SEPARATOR_SIZE;
	}
	
	public static boolean[] extractVersionInfo(Text value) throws IOException{
		int offset = SEPARATOR_SIZE + LONG_SIZE;
		byte[] buffer =  value.getBytes();
		int version = bytesToInt(buffer, offset);
		offset += INT_SIZE; 
		boolean[] retVal = new boolean[version+1];
		for (int i = 0; i < version; i++) {
			if(buffer[offset+i] == (byte)0){
				retVal[i] = false;
			}else{
				retVal[i] = true;
			}
		}
		return retVal;
	}

	/**
	 * Writes a long value to a buffer of size {@link MyGen.LONG_SIZE}.
	 */
	public static int longToBytes(byte[] buffer, int start, long l) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeLong(l);
		dos.flush();
		int i;
		for (i = 0; i < LONG_SIZE; i++) {
			buffer[start+i] = bos.toByteArray()[i];
		}
		return start+LONG_SIZE;
	}
	
	/**
	 * Writes a long value to a buffer of size {@link MyGen.LONG_SIZE}.
	 */
	public static int  intToBytes(byte[] buffer, int start, int i) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeInt(i);
		dos.flush();
		int j;
		for (j = 0; j < INT_SIZE; j++) {
			buffer[start+j] = bos.toByteArray()[j];
		}
		return start+INT_SIZE;
	}
	
	/**
	 * Reads a long value from the first 8 bytes of a give buffer.
	 */
	public static long bytesToLong(byte[] buffer) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
		DataInputStream dis = new DataInputStream(bis);
		return dis.readLong(); 
	}
	
	/**
	 * Reads a long value from the first 8 bytes from a given offset of a give buffer.
	 */
	public static long bytesToLong(byte[] buffer, int offset) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(buffer, offset, LONG_SIZE);
		DataInputStream dis = new DataInputStream(bis);
		return dis.readLong(); 
	}
	
	/**
	 * Reads an int value from the first 4 bytes from a given offset of a give buffer.
	 */
	public static int bytesToInt(byte[] buffer, int offset) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(buffer, offset, INT_SIZE);
		DataInputStream dis = new DataInputStream(bis);
		return dis.readInt(); 
	}
	
	public static long extractRowId(Text value) throws IOException{
		return bytesToLong(value.getBytes(), SEPARATOR_SIZE);
	}
	
	public static String printKey(Text k) throws IOException{
		String result = Long.toString(bytesToLong(k.getBytes()));
		result.concat("00");
		return result;
	}
	
	public static String printValue(Text v) throws IOException{
		String result = separator;
		result += Long.toString(bytesToLong(v.getBytes()));
		result += Text.decode(v.getBytes(), LONG_SIZE + SEPARATOR_SIZE, SortInputFormat.VALUE_LENGTH-(KVGenerator.LONG_SIZE+SEPARATOR_SIZE));
		return result;
	}

}

