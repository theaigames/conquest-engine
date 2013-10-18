package io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class OutStream {
	
	StringBuffer buffer;
	Writer out;
	
	public OutStream(OutputStream outputStream)
	{
		out = new OutputStreamWriter(outputStream);
		buffer = new StringBuffer();
	}
	
	public void flush() throws IOException {
		out.flush();
	}
	
	public void writeLine(String line) throws IOException {
		out.write(line + "\n");
		buffer.append(line + "\n");
	}
	
	public String getData() {
		return buffer.toString();
	}
	
	public void close() throws IOException {
		out.close();
	}

}
