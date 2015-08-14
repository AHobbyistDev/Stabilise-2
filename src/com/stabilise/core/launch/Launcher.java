package com.stabilise.core.launch;

import com.stabilise.core.Resources;


public class Launcher {
	
	private Launcher() {}
	
	public static void main(String[] args) throws Exception {
		//for(Object e : System.getProperties().entrySet())
		//	System.out.println(e);
		
		ProcessBuilder pb = new ProcessBuilder(
				//"\"" + System.getProperty("java.home").replace('\\', '/') + "/bin/javaw\" "
				"\"" + System.getProperty("sun.boot.library.path").replace('\\', '/') + "/javaw\""
				+ " -jar "
				+ " \"" + Resources.APP_DIR.child("Game.jar").path() + "\"",
				"arg1", "arg2");
		//Map<String, String> env = pb.environment();
		//env.put("VAR1", "myValue");
		//env.remove("OTHERVAR");
		//env.put("VAR2", env.get("VAR1") + "suffix");
		pb.directory(Resources.APP_DIR.file());
		pb.inheritIO();
		//File log = new File("log");
		//pb.redirectErrorStream(true);
		//pb.redirectOutput(Redirect.appendTo(log));
		//assert pb.redirectInput() == Redirect.PIPE;
		//assert pb.redirectOutput().file() == log;
		//assert p.getInputStream().read() == -1;
		
		Process p = pb.start();
		p.waitFor();
		
		System.out.println("Application terminated");
	}
	
}
