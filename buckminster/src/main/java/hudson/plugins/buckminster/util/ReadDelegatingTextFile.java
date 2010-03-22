package hudson.plugins.buckminster.util;

import java.io.File;
import java.io.IOException;

import hudson.util.TextFile;

public class ReadDelegatingTextFile extends TextFile{

	private TextFile readDelegate;

	public ReadDelegatingTextFile(TextFile main, TextFile readDelegate) {
		super(main.file);
		this.readDelegate = readDelegate;
	}
	
	@Override
	public String read() throws IOException {
		if(readDelegate!=null && readDelegate.exists())
			return readDelegate.read();
		return super.read();
	}
	
	@Override
	public boolean exists() {
		if(readDelegate==null)
			return super.exists();
		return super.exists() || readDelegate.exists();
	}

}
