package com.stabilise.tests;

import com.stabilise.character.CharacterData;

public class TestCreateCharacter {
    
    public static void main(String[] args) {
        CharacterData d = new CharacterData("Test char");
        d.create();
        System.out.println(d.toString());
        System.out.println(d.getDimensionName());
    }
    
}
