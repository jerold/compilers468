import java.io.*;
import java.net.*;
import java.nio.channels.Channel;

import com.jcraft.jsch.*;

public class ServerUpload {
	
	private String defaultname = "uMachine_code.il";
	private String filename = "target/"+defaultname;
	private File file = new File(filename);
	private String username = "logan.perreault";
	private String password = "aKJ4382e";
	private String host = "esus.cs.montana.edu";
	private String directory = "compilers";
	private JSch jsch;
	private Session session;
	private String output;
	private boolean stripmessage = false;
	private boolean showResults = true;
	
	public ServerUpload() {
		
	}
	
	// Custom
	public ServerUpload(String localfile, String host, String username, String password, String directory) {
		file = new File(localfile);
		this.host = host;
		this.username = username;
		this.password = password;
		this.directory = directory;
	}
	
	/**
	 * Don't show any results from server.
	 */
	public void noShow() {
		showResults = false;
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
	
	public String getOutput() {
		return this.output;
	}
	
	public void setFile(String filename) {
		this.filename = filename;
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
		
		// refresh file
		file = new File(filename);
		
		if (jsch==null) {
			if (connect()==false) {
				return false;
			}
		}
		
        try {
            com.jcraft.jsch.Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;
            System.out.println(file.getPath()+"  -->  "+directory+"/"+file.getName());
            sftpChannel.put(file.getPath(), directory+"/"+file.getName());
            sftpChannel.exit();
            channel.disconnect();
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
		
		System.out.println("EXECUTE: "+file.getName());
		String command = "cd "+directory+" && "+"./execute "+file.getName();
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
	    
	    boolean success = true;
	    
	    int maxcount = 5;
	    
	    byte[] tmp = new byte[1024];
	    int count = 0;
	    while (count<maxcount) {
	    	count++;
	        try {
				while (in.available() > 0) {
				    int i = in.read(tmp, 0, 1024);
				    if (i < 0) {
				        break;
				    }
				    String line = new String(tmp, 0, i);
				    String sep = "-------------------------------";
				    int start = line.indexOf(sep)+sep.length()+1;
				    int end = -1;
				    
				    if (start>0) {
				    	end = line.indexOf(sep,start)+sep.length()+1;
				    	// started but not ended, must be looking for a read
				    	if (end<0) {
				    		
				    	}
				    } else {
				    	// runtime error
				    	System.out.println("RUNTIME ERROR!!!!!!!!!!!!!!!!!");
				    	success = true;
				    }
				    
				    
				    
				    output = line;
				    if (stripmessage) {
				    	output = output.substring(start,end-sep.length()-1);
				    }
				    
				    if (showResults) {
				    	System.out.println(output);
				    }
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
	    
	    if (count>=maxcount) {
	    	success = false;
	    }
	    
	    channel.disconnect();
	    
	    return success;
	}  
	
}