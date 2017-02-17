package com.little.visit.task;

import com.little.visit.listener.IProgressListener;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 重写FilterInputStream，实现进度监听的功能
 * 
 * @author Cow
 * 
 */
public class CountingInputStream extends FilterInputStream {

	private final IProgressListener progressListener;
	private long transferred;

	protected CountingInputStream(final InputStream in,
								  final IProgressListener progressListener) {
		super(in);
		this.progressListener = progressListener;
		this.transferred = 0;
	}

	@Override
	public int read() throws IOException {
		int read = in.read();
		readCount(read);
		return read;
	}

	@Override
	public int read(byte[] buffer) throws IOException {
		int read = in.read(buffer);
		readCount(read);
		return read;
	}

	@Override
	public int read(byte[] buffer, int byteOffset, int byteCount)
			throws IOException {
		int read = in.read(buffer, byteOffset, byteCount);
		readCount(read);
		return read;
	}

	@Override
	public long skip(long byteCount) throws IOException {
		long skip = in.skip(byteCount);
		readCount(skip);
		return skip;
	}

	private void readCount(long read) {
		if (read > 0) {
			this.transferred += read;
			this.progressListener.transferred(this.transferred);
		}
	}
}
