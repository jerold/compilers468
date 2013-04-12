import java.io.*;
import java.net.*;
import java.nio.channels.Channel;

import com.jcraft.jsch.*;

public class ServerUpload {
	
	private File file = new File("target/uMachine_code.il");
	private String username = "logan.perreault";
	private String password = "aKJ4382e";
	private String host = "esus.cs.montana.edu";
	private String directory = "compilers";
	private JSch jsch;
	private Session session;
	private boolean stripmessage = false;
	
	public ServerUpload() {
		
	}
	
	/**
	 * Strips the default message displayed by the VM. 
	 * Won't work if there is a long period of "-" characters printed by the program, but this is for our benefit anyway.
	 */
	public void stripMessage() {
		stripmessage = true;
	}
	
	public void showMessage() {
		stripmessage = false;
	}
	
	// Custom
	public ServerUpload(String localfile, String host, String username, String password) {
		file = new File(localfile);
		this.host = host;
		this.username = username;
		this.password = password;
	}
	
	public boolean go() {
		boolean success = connect();
		if (success) success = upload();
		if (success) success = execute();
		if (success) success = disconnect();
		return success;
	}
	
	public boolean connect() {
		jsch = new JSch();
        try {
            session = jsch.getSession(username, host, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(password);
            session.connect();
        } catch (JSchException e) {
            return false;
        }
        return true;
	}
	
	public boolean disconnect() {
		session.disconnect();
		return true;
	}
	
	// This is pretty cool. If we need, we can check for an exception and even chmod it beforehand.
	public boolean upload() {
		if (jsch==null) {
			if (connect()==false) {
				return false;
			}
		}
        try {

            com.jcraft.jsch.Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;
            sftpChannel.put(file.getPath(), directory+"/"+file.getName());
            sftpChannel.exit();
        } catch (JSchException e) {
            return false;
        } catch (SftpException e) {
        	return false;
        }
		return true;
	}
	
	public boolean execute () {

		if (jsch==null) {
			if (connect()==false) {
				return false;
			}
		}
		
	    ChannelExec channel = null;
		try {
			channel = (ChannelExec) session.openChannel("exec");
		} catch (JSchException e) {
			return false;
		}
		
		String command = "cd "+directory+"; "+"./execute "+file.getName();
	    channel.setCommand(command);
	    channel.setInputStream(null);
	    ((ChannelExec) channel).setErrStream(System.err);
	    InputStream in = null;
		try {
			in = channel.getInputStream();
		} catch (IOException e) {
			return false;
		}
	    try {
			channel.connect();
		} catch (JSchException e) {
			return false;
		}
	    
	    byte[] tmp = new byte[1024];
	    while (true){
	        try {
				while (in.available() > 0) {
				    int i = in.read(tmp, 0, 1024);
				    if (i < 0) {
				        break;
				    }
				    String line = new String(tmp, 0, i);
				    if (stripmessage) {
					    String sep = "-------------------------------";
					    line = line.substring(line.indexOf(sep)+sep.length()+1);
					    line = line.substring(0,line.indexOf(sep)-1);
				    }
				    System.out.println(line);
				}
			} catch (IOException e) {
				return false;
			}
	        if (channel.isClosed()){
	            break;
	        }
	        try {
	            Thread.sleep(1000);
	        } catch (Exception ee){
	            //ignore
	        }
	    }
	    channel.disconnect();
	    
	    return true;
	}  
	
}