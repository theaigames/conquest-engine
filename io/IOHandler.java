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

public class IOHandler {
	
	Process child;
	InStream out, err;
	OutStream in;
	
	public IOHandler(String command) throws IOException
	{
		System.out.println(command);
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
