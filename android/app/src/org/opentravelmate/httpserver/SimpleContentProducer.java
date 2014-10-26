package org.opentravelmate.httpserver;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.entity.ContentProducer;

/**
 * Simply write a byte array into an output stream.
 * 
 * @author Marc Plouhinec
 */
class SimpleContentProducer implements ContentProducer {
	
	private final byte[] content;
	
	public SimpleContentProducer(byte[] content) {
		this.content = content;
	}
	
	@Override public void writeTo(OutputStream outstream) throws IOException {
		outstream.write(content);
	}
}