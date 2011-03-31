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

	
	public static void generateRecord(LongWritable key, Text value, long keyLong, long rowId) throws IOException{
		//key
		key.set(keyLong);

		//value
		addSeparator(valueBuffer);
		longToBytes(valueBuffer,rowId);
		int start =  (int) Math.round(Math.random()*SortInputFormat.VALUE_LENGTH);
		for(int i = Generate.LONG_SIZE+SEPARATOR_SIZE; i < SortInputFormat.VALUE_LENGTH; i++){
			valueBuffer[i] = fillerBytes[(start+i)%fillerBytes.length]; 
		}
		value.set(valueBuffer, 0, SortInputFormat.VALUE_LENGTH);
		
	}
	
	public static void addSeparator(byte[] buffer){
		for (int i = 0; i < SEPARATOR_SIZE; i++) {
			buffer[i] = separator.getBytes()[i];
		}
	}

	/**
	 * Writes a long value to a buffer of size {@link MyGen.LONG_SIZE}.
	 */
	public static void longToBytes(byte[] buffer, long l) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeLong(l);
		dos.flush();
		int i;
		for (i = 0; i < Generate.LONG_SIZE; i++) {
			buffer[i] = bos.toByteArray()[i];
		}
	}

	/**
	 * Reads a long value from the 8 first bytes of a give buffer.
	 */
	public static long bytesToLong(byte[] buffer) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
		DataInputStream dis = new DataInputStream(bis);
		return dis.readLong(); 
	}
	
	public static String printKey(Text k) throws IOException{
		String result = Long.toString(bytesToLong(k.getBytes()));
		result.concat("00");
		return result;
	}
	
	public static String printValue(Text v) throws IOException{
		String result = separator;
		result += Long.toString(bytesToLong(v.getBytes()));
		result += Text.decode(v.getBytes(), Generate.LONG_SIZE + SEPARATOR_SIZE, SortInputFormat.VALUE_LENGTH-(Generate.LONG_SIZE+SEPARATOR_SIZE));
		return result;
	}

}

