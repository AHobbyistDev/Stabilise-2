package com.stabilise.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Coding {
	
	public static void main(String[] args) {
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static void doubleMyNumbers() {
		int nums = 10;
		print("Give me " + nums + " numbers and I'll double them!");
		int[] numbers = new int[nums];
		
		for(int i = 0; i < numbers.length; i++)
			numbers[i] = getNumber();
		for(int i = 0; i < numbers.length; i++)
			print("Number " + (i+1) + ": " + (2*numbers[i]));
	}
	
	public static void enterTwoNumbers() {
		boolean same = true;
		
		while(same) {
			print("Give me one number");
			int number1 = getNumber();
			print("Give me another number");
			int number2 = getNumber();
			
			if(number1 > number2) {
				print("The bigger number is " + number1);
				same = false;
			} else if(number1 < number2) {
				print("The bigger number is " + number2);
				same = false;
			} else if(number1 == number2) {
				print("They are the same! Let's play again!\n");
				same = true;
			}
		}
	}
	
	public static void guessMyNumber(int answer) {
		System.out.println("Try guessing a number between 1 and 10!");
		
		boolean gotNumber = false;
		while(!gotNumber) {
			int number = getNumber();
			if(number == answer) {
				System.out.println("You're right!");
				gotNumber = true;
			} else if(number < answer) {
				System.out.println("Too low! Try again!");
			} else if(number > answer) {
				System.out.println("Too high! Try again!");
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static final BufferedReader consoleIn =
			new BufferedReader(new InputStreamReader(System.in));
	
	public static void print(String sentence) {
		System.out.println(sentence);
	}
	
	public static int getNumber() {
		while(true) {
			try {
				return Integer.parseInt(getInput());
			} catch(NumberFormatException e) {
				System.out.println("That's not a number! Try again!");
			}
		}
	}
	
	public static String getInput() {
		try {
			return consoleIn.readLine();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
