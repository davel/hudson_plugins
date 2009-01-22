package hudson.plugins.synergy.impl;

import java.util.Arrays;

/**
 * A start session command.
 * A start session command returns the CCM_ADDR to use for the following commands.
 */
public class StartCommand extends Command {
	private String ccmAddr;
	private String login;
	private String database;
	private String engine;
	private String password;
	private boolean remoteClient;
	
	/**
	 * Builds a start session command.
	 * A start session command returns the CCM_ADDR to use for the following commands.
	 * 
	 * @param database		The database path
	 * @param login			The user login
	 * @param password		The user password
	 * @param remoteClient	Use remote client flag
	 * @return				The start command. The last part of the command is the logon password.
	 */
	public StartCommand(String database, String engine, String login, String password, boolean remoteClient) {
		this.database = database;
		this.engine = engine;
		this.login = login;
		this.password = password;
		this.remoteClient = remoteClient;
	}

	
	@Override
	public String[] buildCommand(String ccmAddr) {
		String[] commands = new String[]{ccmAddr, "start", "-d", database, "-h", engine, "-n", login, "-nogui", "-m", "-q", "-pw", password};
		
		// Add "-rc" parameter if required.
		if (remoteClient) {
			commands = Arrays.copyOf(commands, commands.length+1);
			commands[commands.length-1] = "-rc";
		}
		return commands;
	}
	
	@Override
	public boolean[] buildMask() {
		boolean[] result = super.buildMask();
		int pwdIndex = 12;
		result[pwdIndex] = true;
		return result;
	}
	
	@Override
	public void parseResult(String result) {
		ccmAddr = result;
	}
	
	public String getCcmAddr() {
		return ccmAddr;
	}
	
	
}
