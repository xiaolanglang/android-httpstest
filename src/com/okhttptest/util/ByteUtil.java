package com.okhttptest.util;

/*
 *数据处理类
 */
public class ByteUtil {

	public static byte[] subBytes(byte[] bytes, int offset, int size) {
		byte[] b = new byte[size];
		int a = 0;
		for (int i = 0; i < size; i++) {
			b[a++] = bytes[offset + i];
		}
		return b;
	}

	public static String bytes2Ip(byte[] bytes, int offset, int size) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append((int) (bytes[offset + i] & 0xFF) + ".");
		}
		String ip = sb.toString();
		return ip.substring(0, ip.length() - 1);
	}

	public static byte[] charToByte(char c) {
		byte[] b = new byte[2];
		b[0] = (byte) ((c & 0xFF00) >> 8);
		b[1] = (byte) (c & 0xFF);
		return b;
	}

	public static char byteToChar(byte[] b) {
		char c = (char) (((b[0] & 0xFF) << 8) | (b[1] & 0xFF));
		return c;
	}

	public static String toHex(byte b) {
		return ("" + "0123456789ABCDEF".charAt(0xf & b >> 4) + "0123456789ABCDEF".charAt(b & 0xf));
	}

	public static String toUpperString(byte[] bytes, int index, int size) {
		return toString(bytes, index, size).replace(" ", "").toUpperCase();
	}

	public static String toString(byte[] bytes, int index, int size) {
		String ss = "";
		for (int i = 0; i < size; i++) {
			ss += Byte2S(bytes[index + i]) + " ";
		}
		return ss.trim();
	}

	public static String toString(byte[] bytes) {
		return toString(bytes, 0, bytes.length);
	}

	public static int toInt(String hex) {
		int ss = 0;
		if ((hex.charAt(0) - 'A') >= 0) {
			ss += (hex.charAt(0) - 'A' + 10) * 16;
		} else {
			ss += (hex.charAt(0) - '0') * 16;
		}
		if ((hex.charAt(1) - 'A') >= 0) {
			ss += hex.charAt(1) - 'A' + 10;
		} else {
			ss += hex.charAt(1) - '0';
		}
		return ss;
	}

	public static int[] byte2int(byte[] bytes, int index, int size) {
		int[] temp = new int[size * 2];
		int k = 0;
		for (int i = 0; i < size; i++) {
			temp[k++] = 0xF0 & bytes[index + i];
			temp[k++] = 0xF0 & (0x0F & bytes[index + i]) << 4;
		}
		return temp;
	}

	public static int[] str2int(String[] strings) {
		int[] temp = new int[strings.length * 2];
		int k = 0;
		for (int i = 0; i < strings.length; i++) {
			temp[k++] = hex2int(strings[i].charAt(0));
			temp[k++] = hex2int(strings[i].charAt(1));
		}
		return temp;
	}

	public static int hex2int(char c) {
		if (c >= '0' && c <= '9') {
			return (c - '0') * 16;
		}
		if (c >= 'A' && c <= 'F') {
			return (c - 'A' + 10) * 16;
		}
		return 0;
	}

	public static int toInt(byte b) {
		return (int) b & 0xFF;
	}

	public static int hexByte2int(byte b) {
		return toInt(toHex(b));
	}

	public static String Byte2S(byte b) {
		String s = Integer.toHexString(b);
		if (s.length() > 2) {
			s = s.substring(s.length() - 2, s.length());
		} else if (s.length() == 1) {
			s = "0" + s;
		}
		return s;
	}

	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) ((char2Byte(hexChars[pos]) << 4) | char2Byte(hexChars[pos + 1]));
		}
		return d;
	}

	private static byte char2Byte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	/**
	 * 
	 * @param h
	 *            高8位
	 * @param l
	 *            低8位
	 * @return int
	 */
	public static int bytes2int(byte h, byte l) {
		char c = (char) (((h & 0xFF) << 8) | (l & 0xFF));
		return (int) c & 0xffff;
	}

	public static int bytes2int(byte[] bytes, int index, int length) {
		int sum = 0;
		for (int i = 0; i < length; i++) {
			sum = sum * 256 + bytes[index + i];
		}
		return sum;
	}

	public static byte[] toBytes(int n, int len) {
		byte[] bytes = new byte[len];
		for (int i = 0; i < len; i++) {
			bytes[len - i - 1] = (byte) (n % 256);
			n = n / 256;
		}
		return bytes;
	}

	public static byte CheckSum(byte[] bytes, int index, int len) {
		byte sum = 0;
		for (int i = 0; i < len; i++) {
			sum += bytes[index + i];
		}
		sum = (byte) (~sum + 1);
		return sum;
	}

	/**
	 * 校验
	 * 
	 * @param param
	 * @return
	 */
	public static byte jiaoyan(byte[] param) {
		byte temp = 0;
		for (int i = 0; i < param.length; i++) {
			if (i == 0) {
				continue;
			}
			if (i == 1) {
				temp = (byte) (param[i - 1] ^ param[i]);
			}
			if (i > 1) {
				temp = (byte) (temp ^ param[i]);
			}
		}

		byte t = (byte) (~temp);
		return t;

	}

	/**
	 * 将十进制转换为16进制，高位在前，低位在后
	 * 
	 * @param dec
	 * @return
	 */
	public static String decToHex(int dec) {
		String hex = "";
		while (dec != 0) {
			String h = Integer.toString(dec & 0xff, 16);
			if ((h.length() & 0x01) == 1)
				h = '0' + h;
			hex = hex + h;
			dec = dec >> 8;
		}
		return hex;
	}

	/**
	 * 反转字符串以两个为单位(123456——>214365)
	 * 
	 * @param input
	 * @return
	 */
	public static String reverse(String input) {
		byte[] temp = input.getBytes();
		int length = temp.length;
		byte[] result = new byte[length];

		for (int i = 0; i < length; i++) {
			if (i % 2 == 0) {
				if (i != (length - 1)) {
					result[i] = temp[i + 1];
				} else {
					result[i] = temp[i];
				}
			} else {
				result[i] = temp[i - 1];
			}
		}

		return new String(result);
	}
}
