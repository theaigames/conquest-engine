package io;

import java.io.IOException;

public class IOHandler {
	
	Process child;
	InStream out, err;
	OutStream in;
	
	public IOHandler(String command) throws IOException
	{
		child = Runtime.getRuntime().exec(command);
		in = new OutStream(child.getOutputStream());
		out = new InStream(child.getInputStream());
		err = new InStream(child.getErrorStream());
		out.start(); err.start();
	}
	
	public void stop()
	{
		try { in.close(); } catch(IOException e) {}
		child.destroy();
		out.finish();
		err.finish();
		
		if(out.isAlive())
			out.interrupt();
		if(err.isAlive())
			err.interrupt();
		
		try {
			child.waitFor();
			out.join(110);
			err.join(110);
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public String readLine(long timeOut)
	{
		if(!isRunning()) { return null; }
		try { in.flush(); } catch(IOException e) {}
		return out.readLine(timeOut);
	}
	
	public boolean writeLine(String line)
	{
		if(!isRunning()) { return false; }
		try { 
			in.writeLine(line.trim());
			return true;
		} 
		catch(IOException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	public boolean isRunning()
	{
		try {
			child.exitValue();
			return false;
		}
		catch(IllegalThreadStateException ex) {
			return true;
		}
	}
	
	public String getStdin() {
		return in.getData();
	}
	
	public String getStdout() {
		return out.getData();
	}
	
	public String getStderr() {
		return err.getData();
	}

}
