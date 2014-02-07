// Copyright 2014 theaigames.com (developers@theaigames.com)

//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at

//        http://www.apache.org/licenses/LICENSE-2.0

//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//	
//    For the full copyright and license information, please view the LICENSE
//    file that was distributed with this source code.

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
