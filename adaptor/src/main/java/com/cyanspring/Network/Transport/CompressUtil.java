package com.cyanspring.Network.Transport;

import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CompressUtil 
{
	protected Deflater	m_deflater;
	protected Inflater 	m_inflater;
	protected final int LOCAL_BUF_SIZE = 1024;

	public CompressUtil()
	{
		m_deflater = new Deflater();
		m_inflater = new Inflater();
	}
	
	public int compress(byte[] inBuf, int inSize, byte[] outBuf) 
	{
        int inPosn = 0;
        int outPosn = 0;

        while (!m_deflater.finished()) 
        {
            int want = -1;

            if (m_deflater.needsInput() && inSize != 0) 
            {
                want = (inSize < LOCAL_BUF_SIZE) ? inSize : LOCAL_BUF_SIZE;

                m_deflater.setInput(inBuf, inPosn, want);

                inSize -= want;
                inPosn += want;
                if (inSize == 0) 
                {
                    m_deflater.finish();
                }
            }

            int compCount = m_deflater.deflate(outBuf, outPosn, LOCAL_BUF_SIZE);
            outPosn += compCount;
        }
        
        m_deflater.reset();
        return outPosn;
    }

    public int deCompress(byte[] inBuf, int inCount, byte[] outBuf) throws DataFormatException 
    {
        int inPosn = 0;
        int outPosn = 0;

        while (!m_inflater.finished()) 
        {
            int want = -1;

            if (m_inflater.needsInput() && inCount != 0) 
            {
                want = (inCount < LOCAL_BUF_SIZE) ? inCount : LOCAL_BUF_SIZE;

                m_inflater.setInput(inBuf, inPosn, want);

                inCount -= want;
                inPosn += want;
            }

            int compCount = m_inflater.inflate(outBuf, outPosn, LOCAL_BUF_SIZE);
            outPosn += compCount;
        }
        
        m_inflater.reset();
        return outPosn;       
    }	
}
