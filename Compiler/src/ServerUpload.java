import java.io.*;
import java.net.*;
import java.nio.channels.Channel;

import com.jcraft.jsch.*;

public class ServerUpload {
	
	private String ftpAddress;
	private File file;
	private String username = "logan.perreault";
	private String password = "aKJ4382e";
	private String host = "esus.cs.montana.edu";
	private String directory = "compilers";
	private boolean secure = true;
	private JSch jsch;
	private Session session;
	
	// Default
	public ServerUpload() {
		file = new File("target/uMachine_code.il");
		ftpAddress = generateAddress();
	}

	// Custom
	public ServerUpload(String localfile, String serverAddress) {
		file = new File(localfile);
		ftpAddress = serverAddress;
	}
	
	// Custom
	public ServerUpload(String localfile, String host, String username, String password) {
		file = new File(localfile);
		this.host = host;
		this.username = username;
		this.password = password;
		ftpAddress = generateAddress();
	}
	
	private String generateAddress() {
		return (secure?"sftp":"ftp")+"://"+username+":"+password+"@"+host+directory;
	}
	
	public boolean connect() {
		jsch = new JSch();
        try {
            session = jsch.getSession(username, host, 22);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(password);
            session.connect();
        } catch (JSchException e) {
            e.printStackTrace();  
        }
        return true;
	}
	
	public boolean disconnect() {
		session.disconnect();
		return true;
	}
	
	// This is pretty cool. If we need, we can check for an exception and even chmod it beforehand with this sucker.
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
            //System.out.println("Pushed to "+directory+"/"+file.getName());
            sftpChannel.exit();
        } catch (JSchException e) {
            e.printStackTrace();  
        } catch (SftpException e) {
            e.printStackTrace();
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
			e.printStackTrace();
		}
		
		String command = "cd "+directory+"; "+"./execute "+file.getName();
	    channel.setCommand(command);
	    channel.setInputStream(null);
	    ((ChannelExec) channel).setErrStream(System.err);
	    InputStream in = null;
		try {
			in = channel.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    try {
			channel.connect();
		} catch (JSchException e) {
			e.printStackTrace();
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
				    System.out.println("Unix system console output: " +line);
				}
			} catch (IOException e) {
				e.printStackTrace();
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