package com.stabilise.tests;


public class Vigenere {
    
    public static void main(String[] args) {
        String ciphertext = "NZYNCYYFYJYUCHBWLMMWMSMWLAYKIXWSYKUJWAJZ"
                + "YJMTUKYVIFNZYDYLNWLKIXUCYQQGLVZGLFYSLDSL"
                + "BJYWBMHVLWXQYSLKNZYNCYYFYJYUCHBWLOUKWGHK"
                + "CVYJYVOFVJYSESVD";
        String key = "US";
        System.out.println(decrypt(ciphertext, key));
    }
    
    public static String decrypt(String ciphertext, String key) {
        char[] message = new char[ciphertext.length()];
        for(int i = 0; i < ciphertext.length(); i++) {
            char c = ciphertext.charAt(i);
            char k = (char)(key.charAt(i % key.length()) - 'A');
            char m = (char)(c - k);
            if(m < 'A') m += 26;
            message[i] = m;
        }
        return new String(message);
    }
    
}
