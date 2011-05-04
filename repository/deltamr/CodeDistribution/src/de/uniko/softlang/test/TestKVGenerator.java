package de.uniko.softlang.test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.junit.Before;
import org.junit.Test;

import de.uniko.softlang.benchmarks.teraSort.generate.KVGenerator;

public class TestKVGenerator {
	byte[] buffer;
	
	@Before
	public void setUp(){
		buffer = new byte[256];
	}
	
	@Test
	public void testLongConversion() throws IOException{
		
		KVGenerator.longToBytes(buffer, 0, 0);
		long result = KVGenerator.bytesToLong(buffer);
		assertEquals(0, result);
		
		KVGenerator.longToBytes(buffer, 50, 101);
		result = KVGenerator.bytesToLong(buffer, 50);
		assertEquals(101, result);
		
		KVGenerator.longToBytes(buffer, 50, -5);
		result = KVGenerator.bytesToLong(buffer, 50);
		assertEquals(-5, result);
		
		KVGenerator.longToBytes(buffer, buffer.length-9, Long.MAX_VALUE);
		result = KVGenerator.bytesToLong(buffer, buffer.length-9);
		assertEquals(Long.MAX_VALUE, result);
		
		KVGenerator.longToBytes(buffer, 0, Long.MIN_VALUE);
		result = KVGenerator.bytesToLong(buffer, 0);
		assertEquals(Long.MIN_VALUE, result);
		
		
	}
	
	@Test
	public void testIntConversion() throws IOException{
		KVGenerator.intToBytes(buffer, 0, 0);
		int result = KVGenerator.bytesToInt(buffer, 0);
		assertEquals(0, result);
		
		KVGenerator.intToBytes(buffer, 20, -70);
		result = KVGenerator.bytesToInt(buffer, 20);
		assertEquals(-70, result);

		KVGenerator.intToBytes(buffer, buffer.length-5, Integer.MAX_VALUE);
		result = KVGenerator.bytesToInt(buffer, buffer.length-5);
		assertEquals(Integer.MAX_VALUE, result);

		KVGenerator.intToBytes(buffer, 0, Integer.MIN_VALUE);
		result = KVGenerator.bytesToInt(buffer, 0);
		assertEquals(Integer.MIN_VALUE, result);
	}
	
	
	@Test
	public void testGenerateRecord() throws IOException{
		LongWritable key = new LongWritable();
		Text value = new Text();
		long keyLong = 1234567l;
		long rowId = 555;
		boolean[] versions = new boolean[6];
		versions[3] = true;
		KVGenerator.generateDetRecord(key, value, keyLong, rowId, 0, versions);
		assertEquals(keyLong+1, key.get());
		boolean[] storedV = KVGenerator.extractVersionInfo(value);
		assertEquals(versions.length+1, storedV.length);
		for(int i = 0; i<versions.length; i++){
			assertEquals(versions[i], storedV[i]);
		}
		long storedR = KVGenerator.extractRowId(value);
		assertEquals(rowId, storedR);
	}
	
}
