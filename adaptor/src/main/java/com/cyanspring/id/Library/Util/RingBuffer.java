package com.cyanspring.id.Library.Util;

public class RingBuffer implements AutoCloseable {
	protected Object m_lock = new Object();
	protected byte[] m_buffer = new byte[0];
	protected int m_nSize = 0;
	protected int m_nReadPtr = 0;
	protected int m_nWritePtr = 0;
	protected boolean m_fOverlap = false;

	/**
	 * # of bytes for this buffer
	 */
	public final int getBufSize() {
		synchronized (m_lock) {
			return m_nSize;
		}
	}

	/**
	 * # of bytes that is queued
	 */
	public final int getQueuedSize() {
		synchronized (m_lock) {
			if (m_nWritePtr >= m_nReadPtr) {
				return m_nWritePtr - m_nReadPtr;
			} else {
				return m_nWritePtr - m_nReadPtr + m_nSize;
			}
		}
	}

	/**
	 * # of bytes that can be safely writen without overrun
	 */
	public final int getFreeSize() {
		synchronized (m_lock) {
			if (m_nReadPtr > m_nWritePtr) {
				return m_nReadPtr - m_nWritePtr - 1;
			} else {
				return m_nReadPtr - m_nWritePtr - 1 + m_nSize;
			}
		}
	}

	/**
	 * 
	 */
	public RingBuffer() {
	}

	/**
	 * 
	 * @param nSize
	 */
	public final void init(int nSize) {
		init(nSize, true);
	}

	/**
	 * 
	 * @param nSize
	 * @param fOverlap
	 */
	public final void init(int nSize, boolean fOverlap) {
		synchronized (m_lock) {
			m_nSize = nSize;
			m_buffer = new byte[nSize];
			m_nReadPtr = m_nWritePtr = 0;
			m_fOverlap = fOverlap;
		}
	}

	void uninit() {
		m_buffer = null;
		m_nReadPtr = m_nWritePtr = 0;
	}

	/**
	 * close implement AutoCloseable
	 */
	public final void close() {
		uninit();
		FinalizeHelper.suppressFinalize(this);
	}

	/**
	 * Read data into buf, and purge data.
	 * 
	 * @param buf
	 *            destination buffer
	 * @param nBytes
	 *            # of bytes to read
	 * @return number of bytes actually read
	 */
	public final int read(byte[] buf, int nBytes) {
		return read(buf, nBytes, true);
	}

	/**
	 * 
	 * @param buf
	 * @param nBytes
	 * @param Purge
	 * @return
	 */
	public final int read(byte[] buf, int nBytes, boolean Purge) {
		synchronized (m_lock) {
			nBytes = Math.min(nBytes, this.getQueuedSize());
			if (nBytes <= 0) {
				return 0;
			}

			// First chunk: before wrap around
			//
			int nRight = m_nSize - m_nReadPtr;
			int nFirstChunk = Math.min(nRight, nBytes);
			if (nFirstChunk > 0) {
				System.arraycopy(m_buffer, m_nReadPtr, buf, 0, nFirstChunk);
				if (Purge) {
					m_nReadPtr += nFirstChunk;
					if (m_nReadPtr >= m_nSize) {
						m_nReadPtr = 0;
					}
				}
			}

			// Second chunk
			//
			int nLeft = nBytes - nFirstChunk;
			if (nLeft > 0) {
				System.arraycopy(m_buffer, 0, buf, nFirstChunk, nLeft);
				if (Purge) {
					m_nReadPtr = nLeft;
				}
			}

			// If buffer is empty, reset R/W points to avoid wrap around
			//
			if (this.getQueuedSize() == 0) {
				m_nReadPtr = m_nWritePtr = 0;
			}

			assert m_nReadPtr >= 0;
			assert m_nReadPtr < m_nSize;

			return nBytes;
		}
	}

	/**
	 * Clear nBytes from the buffer
	 * 
	 * @param nBytes
	 *            # of bytes to purge. If nBytes is less than 0, clear
	 *            everything
	 */
	public final void purge(int nBytes) {
		synchronized (m_lock) {
			if (nBytes < 0) {
				m_nReadPtr = m_nWritePtr = 0;
			} else {
				nBytes = Math.min(nBytes, this.getQueuedSize());
				if (nBytes > 0) {
					int nRight = m_nSize - m_nReadPtr;
					int nFirstChunk = Math.min(nRight, nBytes);
					m_nReadPtr += nFirstChunk;

					int nLeft = nBytes - nFirstChunk;
					if (nLeft > 0) {
						m_nReadPtr = nLeft;
					}

					if (m_nReadPtr >= m_nSize) {
						m_nReadPtr = 0;
					}

					assert m_nReadPtr >= 0;
					assert m_nReadPtr < m_nSize;
				}
			}
		}
	}

	/**
	 * Write data into buffer. Note overrun can occur.
	 * 
	 * @param buf
	 *            source data
	 * @param nBytes
	 *            # of bytes to write
	 */
	public final int write(byte[] buf, int nBytes) {
		synchronized (m_lock) {
			nBytes = (m_fOverlap) ? nBytes : this.getFreeSize();
			nBytes = Math.min(nBytes, m_nSize);

			// First chunk: before wrap around
			//
			int nRight = m_nSize - m_nWritePtr;
			assert 0 <= nRight && nRight <= m_nSize;
			int nFirstChunk = Math.min(nBytes, nRight);
			if (nFirstChunk > 0) {
				System.arraycopy(buf, 0, m_buffer, m_nWritePtr, nFirstChunk);
				m_nWritePtr += nFirstChunk;
				if (m_nWritePtr >= m_nSize) {
					m_nWritePtr = 0;
				}
			}

			// Second chunk
			//
			int nLeft = nBytes - nFirstChunk;
			if (nLeft <= 0) {
				return nFirstChunk;
			}

			System.arraycopy(buf, nFirstChunk, m_buffer, 0, nLeft);
			m_nWritePtr = nLeft;

			assert m_nWritePtr >= 0;
			assert m_nWritePtr < m_nSize;

			return nBytes;
		}
	}
}
