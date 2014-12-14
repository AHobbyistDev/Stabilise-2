package com.stabilise.tests;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

public class TeamAssigner {
	
	public TeamAssigner() {
		// TODO Auto-generated constructor stub
	}
	
	private static void assign(String[] players, int teamSize, int numTeams) {
		List<String> playerList = Lists.newArrayList(players);
		final String[][] teams = new String[numTeams][teamSize];
		Random rnd = new Random();
		
		for(int t = 0; t < numTeams; t++) {
			teams[t] = new String[teamSize];
			for(int p = 0; p < teamSize; p++) {
				teams[t][p] = playerList.remove(rnd.nextInt(playerList.size()));
			}
		}
		
		StringBuilder sb = new StringBuilder();
		for(int t = 0; t < teams.length; t++) {
			sb.append("Team " + t + "\n"); // defeats the purpose of a stringbuilder but idc
			for(int p = 0; p < teamSize; p++) {
				sb.append("    " + teams[t][p] + "\n");
			}
		}
		
		if(playerList.size() > 0) {
			sb.append("Subs:\n");
			for(String s : playerList) {
				sb.append("    " + s + "\n");
			}
		}
		
		System.out.println(sb.toString());
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		assign(new String[] {
				"Acti", "Noob", "Parka", "Sunna",
				"Bobbay", "Drogo", "Ayy", "Rawkt",
				"Max", "Demo", "Bug", "Truth",
				"Walrus", "Vin", "Iloominati", "Elfi",
				"Newfren", "Sir Reg", "One", "Contrail",
				"Balljobby", "Moose"
		}, 4, 4);
	}
	
}
