package com.stabilise.tests;

public class Ciphers {

	private Ciphers() {
		// non-instantiable
	}
	
	private static final char[] ALPHABET = new char[] {
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
	};
	
	private static void alphabeticShift(String cryptoText) {
		cryptoText = cryptoText.toLowerCase();
		
		for(int i = 1; i < ALPHABET.length; i++) {
			StringBuilder sb = new StringBuilder(cryptoText.length());
			for(int j = 0; j < cryptoText.length(); j++) {
				int index = indexOfAlphabet(cryptoText.charAt(j));
				if(index == -1)
					sb.append(cryptoText.charAt(j));
				else
					sb.append(ALPHABET[(index + i) % ALPHABET.length]);
			}
			System.out.println(sb.toString());
		}
	}
	
	private static int indexOfAlphabet(char c) {
		for(int i = 0; i < ALPHABET.length; i++) {
			if(ALPHABET[i] == c)
				return i;
		}
		return -1;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		alphabeticShift("IRZHVPMXJJBO FP PL ZLLI. XKVTXVP, QEB RPBOKXJB FP ZXBPXOZYX");
	}
	
}
