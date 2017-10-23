package com.ontimize.jee.common.tools;

import java.math.BigInteger;
import java.util.BitSet;

/**
 * Clase de utilidades para operar con byte y array de bytes.
 */
public final class ByteTools {

	/** The Constant BITS_PER_BYE. */
	public static final int BITS_PER_BYTE = 8;

	/**
	 * Instantiates a new byte utils.
	 */
	private ByteTools() {
		super();
	}

	/**
	 * Convierte el byte en su representacion hexadecimal.
	 *
	 * @param impr
	 *            the impr
	 * @return La representacion hexadecimal
	 */
	public static String toHexString(byte impr) {
		StringBuilder sb = new StringBuilder();
		int bajo = impr & 0x0F;
		int alto = impr & 0xF0;
		alto = alto >> 4;
		sb.append(Integer.toHexString(alto));
		sb.append(Integer.toHexString(bajo));
		return sb.toString();
	}

	/**
	 * Convierte el el array de bytes a una cadena con su representacion en hexadecimal. Entre dos valores inserta spliter.
	 *
	 * @param bytes
	 *            el array de bytes
	 * @param offset
	 *            the offset
	 * @param size
	 *            the size
	 * @param spliter
	 *            separador entre 2 caracteres
	 * @return the string
	 */
	public static String toHexString(byte[] bytes, int offset, int size, String spliter) {
		StringBuilder sb = new StringBuilder();
		for (int i = offset, tam = size; i < tam; i++) {
			sb.append(ByteTools.toHexString(bytes[i]));
			sb.append(spliter);
		}
		return sb.toString();
	}

	/**
	 * Convierte el el array de bytes a una cadena con su representacion en hexadecimal. Entre dos valores inserta spliter.
	 *
	 * @param bytes
	 *            el array de bytes
	 * @return the string
	 */
	public static String toHexString(byte[] bytes) {
		return ByteTools.toHexString(bytes, 0, bytes.length, " ");
	}

	/**
	 * Convierte un byte en su representacion binaria.
	 *
	 * @param b
	 *            the b
	 * @return the string
	 */
	private static String toBinaryString(byte b) {
		StringBuilder sb = new StringBuilder();
		for (int sh = 7; sh >= 0; sh--) {
			sb.append((b >> sh) & 1);
		}
		return sb.toString();
	}

	/**
	 * Devuelve una cadena formada por los valores en binario de todos los bytes del array de entrada.
	 *
	 * @param bytes
	 *            byte[]
	 * @return String
	 */

	public static String toBinaryString(byte[] bytes) {
		return ByteTools.toBinaryString(bytes, 0, bytes.length, "");
	}

	/**
	 * Devuelve una cadena formada por los valores en binario de los primeros tam bytes del array de entrada.
	 *
	 * @param bytes
	 *            byte[]
	 * @param offset
	 *            the offset
	 * @param tam
	 *            int
	 * @param spliter
	 *            the spliter
	 * @return String
	 */
	private static String toBinaryString(byte[] bytes, int offset, int tam, String spliter) {
		StringBuilder sb = new StringBuilder();
		for (int i = offset; i < tam; i++) {
			sb.append(ByteTools.toBinaryString(bytes[i]));
			sb.append(spliter);
		}
		return sb.toString();
	}

	/**
	 * Convierte el array de bytes en un {@link BitSet}.
	 *
	 * @param bytes
	 *            the bytes
	 * @return the bit set
	 */
	public static BitSet toBitSet(byte[] bytes) {
		if ((bytes == null) || (bytes.length == 0)) {
			return null;
		}

		BitSet bits = new BitSet(bytes.length * 8);

		for (int i = 0; i < (bytes.length * 8); i++) {
			if ((bytes[bytes.length - (i / 8) - 1] & (1 << (i % 8))) > 0) {
				bits.set(i);
			}
		}
		return bits;
	}

	/**
	 * The most significant bit in the result is guaranteed not to be a 1 (since BitSet does not support sign extension). The byte-ordering of the result is big-endian which means
	 * the most significant bit is in element 0. The bit at index 0 of the bit set is assumed to be the least significant bit.
	 *
	 * @param bits
	 *            the bits
	 * @return the byte[]
	 */
	public static byte[] toByteArray(BitSet bits) {
		return ByteTools.toByteArray(bits, 4);
	}

	/**
	 * The most significant bit in the result is guaranteed not to be a 1 (since BitSet does not support sign extension). The byte-ordering of the result is big-endian which means
	 * the most significant bit is in element 0. The bit at index 0 of the bit set is assumed to be the least significant bit.
	 *
	 * @param bits
	 *            the bits
	 * @param size
	 *            the size
	 * @return the byte[]
	 */
	public static byte[] toByteArray(BitSet bits, int size) {
		if (bits.length() == 0) {
			return new byte[size];
		}
		byte[] bytes = new byte[size];

		for (int i = 0; i < bits.length(); i++) {
			if (bits.get(i)) {
				bytes[bytes.length - (i / 8) - 1] |= 1 << (i % 8);
			}
		}
		return bytes;
	}

	/**
	 * Converts a two byte array to an integer.
	 *
	 * @param b
	 *            a byte array of length 2
	 * @return an int representing the unsigned short
	 */
	public static final int unsignedShortToInt(byte[] b) {
		int i = 0;
		i |= b[0] & 0xFF;
		if (b.length > 1) {
			i <<= 8;
			i |= b[1] & 0xFF;
		}
		return i;
	}

	/**
	 * Convierte un entero en un array de bytes de 4 posiciones.
	 *
	 * @param value
	 *            the value
	 * @return the byte[]
	 */
	public static byte[] intToByteArray(int value) {
		byte[] b = new byte[4];
		for (int i = 0; i < 4; i++) {
			int offset = (b.length - 1 - i) * 8;
			b[i] = (byte) ((value >>> offset) & 0xFF);
		}
		return b;
	}

	/**
	 * Devuelve el valor int del array de bytes.
	 *
	 * @param p
	 *            byte[]
	 * @return int
	 */
	public static int bytesToInt(byte[] p) {
		return ByteTools.bytesToInt(p, p.length);
	}

	/**
	 * Devuelve el valor int de los primeros tam bytes del array.
	 *
	 * @param p
	 *            byte[]
	 * @param tam
	 *            int
	 * @return int
	 */

	public static int bytesToInt(byte[] p, int tam) {
		int res = 0;
		int[] aux = new int[tam];
		for (int i = tam - 1; i >= 0; i--) {
			aux[i] = p[i] < 0 ? 256 + p[i] : p[i];
		}
		for (int i = tam - 1; i >= 0; i--) {
			res = (res * 256) + aux[i];
		}
		return res;

	}

	/**
	 * Devuelve el valor BigInteger del array de bytes.
	 *
	 * @param p
	 *            byte[]
	 * @return BigInteger
	 */
	public static BigInteger bytesToBigInteger(byte[] p) {
		return ByteTools.bytesToBigInteger(p, p.length);
	}

	/**
	 * Devuelve el valor BigInteger de los primeros tam bytes del array de bytes.
	 *
	 * @param p
	 *            byte[]
	 * @param tam
	 *            int
	 * @return BigInteger
	 */

	public static BigInteger bytesToBigInteger(byte[] p, int tam) {
		BigInteger bi = new BigInteger("0");
		int[] aux = new int[tam];
		int[] uno = new int[1];

		for (int i = 0; i < tam; i++) {
			aux[i] = p[i] < 0 ? 256 + p[i] : p[i];
		}

		for (int i = 0; i < tam; i++) {
			bi = bi.shiftLeft(8);
			System.arraycopy(aux, i, uno, 0, 1);
			bi = bi.add(BigInteger.valueOf(uno[0]));
		}

		return bi;
	}

	/**
	 * Compara si dos arrays de bytes son iguales o no. Devuelve true si lo son.
	 *
	 * @param b1
	 *            byte[]
	 * @param b2
	 *            byte[]
	 * @return boolean
	 */

	public static boolean arrayEquals(byte[] b1, byte[] b2) {
		if ((b1 == null) || (b2 == null) || (b1.length != b2.length)) {
			return false;
		}
		for (int i = 0, a = b1.length; i < a; i++) {
			if (b1[i] != b2[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Compara si el array datos esta formado por bytes de valor todosBytes.
	 *
	 * @param datos
	 *            byte[]
	 * @param todosBytes
	 *            byte[]
	 * @return boolean
	 */
	public static boolean compruebaEquals(byte[] datos, byte[] todosBytes) {
		return ByteTools.compruebaEquals(datos, todosBytes, todosBytes, datos.length);

	}

	/**
	 * Compara si los primeros tam bytes del array de datos estan formados por bytes de valor todosBytes.
	 *
	 * @param datos
	 *            byte[]
	 * @param todosBytes
	 *            byte[]
	 * @param tam
	 *            int
	 * @return boolean
	 */
	public static boolean compruebaEquals(byte[] datos, byte[] todosBytes, int tam) {
		return ByteTools.compruebaEquals(datos, todosBytes, todosBytes, tam);
	}

	/**
	 * Compara si el primer byte del array datos es igual a primer byte, y los demas bytes del array son iguales a restoBytes.
	 *
	 * @param datos
	 *            byte[]
	 * @param primerByte
	 *            byte[]
	 * @param restoBytes
	 *            byte[]
	 * @return boolean
	 */
	public static boolean compruebaEquals(byte[] datos, byte[] primerByte, byte[] restoBytes) {
		return ByteTools.compruebaEquals(datos, primerByte, restoBytes, datos.length);
	}

	/**
	 * Comparara si el primer byte del array datos es igual a primerByte y si los siguientes hasta una longitud de tam son iguales a restoBytes.
	 *
	 * @param datos
	 *            byte[]
	 * @param primerByte
	 *            byte[]
	 * @param restoBytes
	 *            byte[]
	 * @param tam
	 *            int
	 * @return boolean
	 */

	public static boolean compruebaEquals(byte[] datos, byte[] primerByte, byte[] restoBytes, int tam) {

		if (datos[0] != primerByte[0]) {
			return false;
		}
		if (datos.length < tam) {
			return false;
		}
		for (int i = 0; i < tam; i++) {
			if (datos[i] != restoBytes[0]) {
				return false;
			}
		}
		return true;
	}

}
