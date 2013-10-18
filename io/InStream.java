package io;

import java.io.IOException;
import java.io.InputStream;

public class InStream extends Thread 
{
	StringBuffer buffer;
	int bufferIndex;
	InputStream in;
	Boolean stopping;
	
	String[] newLines = new String[] { "\r\n", "\r", "\n"};
	
	public InStream(InputStream inputStream)
	{
		in = inputStream;
		buffer = new StringBuffer();
		bufferIndex = 0;
		stopping = false; //for ending the threads
	}
	
	public String readLine(long timeout)
	{
		long timeStart = System.currentTimeMillis();
		synchronized(buffer) {
			while(true)
			{
				int newlineType = 0, index = -1;
				while(newlineType < newLines.length) //find the right line separator
				{
					index = buffer.indexOf(newLines[newlineType], bufferIndex);
					if(index >= 0) { break; }
					++newlineType;
				}
				
				if(index < 0)
				{
					long timeNow = System.currentTimeMillis();
					long timeElapsed = timeNow - timeStart;
					if(timeElapsed >= timeout)
						return null;
					try { buffer.wait(timeout - timeElapsed); } catch(InterruptedException e) {}
					
				}
				else
				{
					String line = (String) buffer.subSequence(bufferIndex, index);
					bufferIndex = index + newLines[newlineType].length();
					return line;
				}
			}
		}
	}
	
	@Override
	public void run()
	{
		try {
			while(true)
			{
				synchronized(stopping) {
					if(stopping)
						break;
				}
				int ch = in.read();
				if(ch >= 0)
				{
					synchronized(buffer)
					{
						buffer.append((char) ch);
						buffer.notify();
					}
				}
			}
		}
		catch(IOException e) {
			synchronized(stopping) {
				if(stopping)
					return;
			}
			e.printStackTrace();
		}	
	}
	
	public void finish()
	{
		synchronized(stopping){
			stopping = true;
		}
	}
	
	public String getData()
	{
		synchronized(buffer){
			return buffer.toString();
		}
	}
}
