package com.witsi.setting.hardwaretest;

public class HexCodec {
	static final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
			'9', 'A', 'B', 'C', 'D', 'E', 'F' };

	// 16进制编码
	public static String hexEncode(byte[] buffer) {
		if (buffer.length == 0) {
			return "";
		}
		int holder = 0;
		char[] chars = new char[buffer.length * 2];
		for (int i = 0; i < buffer.length; i++) {
			holder = (buffer[i] & 0xF0) >> 4;
			chars[(i * 2)] = HEX[holder];
			holder = buffer[i] & 0xF;
			chars[(i * 2 + 1)] = HEX[holder];
		}
		return new String(chars);
	}

	// 16进制解码
	public static byte[] hexDecode(String hex) {
		if ((hex == null) || (hex.length() == 0))
			return new byte[0];
		if (hex.length() < 3) {
			return new byte[] { (byte) (Integer.parseInt(hex, 16) & 0xFF) };
		}

		int count = hex.length();
		int nibble = 0;
		if (count % 2 != 0) {
			count++;
			nibble = 1;
		}
		byte[] buf = new byte[count / 2];
		char c = '\000';
		int holder = 0;
		int pos = 0;
		for (int i = 0; i < buf.length; i++) {
			for (int z = 0; (z < 2) && (pos < hex.length()); z++) {
				c = hex.charAt(pos++);
				if ((c >= 'A') && (c <= 'F'))
					c = (char) (c - '7');
				else if ((c >= '0') && (c <= '9'))
					c = (char) (c - '0');
				else if ((c >= 'a') && (c <= 'f')) {
					c = (char) (c - 'W');
				}
				if (nibble == 0) {
					holder = c << '\004';
				} else {
					holder |= c;
					buf[i] = (byte) holder;
				}
				nibble = 1 - nibble;
			}
		}
		return buf;
	}
}