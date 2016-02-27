package com.castrovala.fraser.orbwar.data;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class BinaryParser {
	
	public BinaryParser() {
		
	}
	
	public byte[] convertToBinary(Object obj) {
		byte type = 0;
		if (obj instanceof String) {
			type = 1;
		} else if (obj instanceof Integer) {
			type = 2;
		} else if (obj instanceof Long) {
			type = 3;
		} else if (obj instanceof Double) {
			type = 4;
		} else if (obj instanceof Float) {
			type = 5;
		} else {
			throw new IllegalArgumentException("Invalid type");
		}
		
		ArrayList<Byte> bytes = new ArrayList<>();
		
		ByteBuffer value_buf;
		switch (type) {
			case 1:
				String s = (String) obj;
				for (byte b : s.getBytes()) {
					bytes.add(new Byte(b));
				}
				break;
			case 2:
				int i = (int) obj;
				value_buf = ByteBuffer.allocate(4);
				value_buf.putInt(i);
				for (byte b : value_buf.array()) {
					bytes.add(new Byte(b));
				}
				
				break;
				
			case 3:
				long l = (long) obj;
				value_buf = ByteBuffer.allocate(8);
				value_buf.putLong(l);
				for (byte b : value_buf.array()) {
					bytes.add(new Byte(b));
				}
				
				break;
				
			case 4:
				double d = (double) obj;
				value_buf = ByteBuffer.allocate(8);
				value_buf.putDouble(d);
				for (byte b : value_buf.array()) {
					bytes.add(new Byte(b));
				}
				break;
			case 5:
				float f = (float) obj;
				value_buf = ByteBuffer.allocate(4);
				value_buf.putFloat(f);
				for (byte b : value_buf.array()) {
					bytes.add(new Byte(b));
				}
				break;
			default:
				System.out.println("Error");
		}
		
		ByteBuffer buff = ByteBuffer.allocate(5 + bytes.size());
		buff.putInt(bytes.size());
		buff.put(type);
		byte[] bytearray = new byte[bytes.size()];
		int i = 0;
		
		
		for (Byte b : bytes) {
			bytearray[i++] = b.byteValue();
		}
		
		buff.put(bytearray);
		
		return buff.array();
		
	}
	
	public Object convertFromBinary(byte[] array) {
		ByteBuffer buff = ByteBuffer.allocate(array.length);
		buff.put(array);
		byte type = buff.get();
		int size = buff.getInt();
		
		switch (type) {
			case 1:
				byte[] str_bytes = null;
				buff.get(array, 5, size);
				return new String(str_bytes);
			case 2:
				
		}
		
		return null;
	}

}
