package blockinterpreter;

public class Transaction
{
	private int version;
	private long txInCount;
	private TxIn inputs[];
	private long txOutCount;
	private TxOut outputs[];
	private int lockTime;
	private int blockIndex;
	
	private boolean isCoinbase;
	private boolean firstBlockTransaction;
	private Block parentBlock;
	private char txHash[];
	
	/**
	 * Parse block and construct a single transaction object starting from the current block parsing position (index)
	 * @param transactionData the block's information
	 * @param block a partially constructed block
	 */
	public Transaction(String transactionData, Block block, int blockIndex)
	{
		parentBlock = block;
		int startIndex, endIndex;
		int index=block.getIndex();
		startIndex=index;
		this.blockIndex = blockIndex;
		firstBlockTransaction = false;
		isCoinbase = false;
		if (index<90)
		{
			firstBlockTransaction = true;
			if (block.getHeight()<6930000) isCoinbase = true;
		}
		version = Block.parseUInt32(index, transactionData);
		index += 4;
		txInCount = Block.parseCompactSizeInteger(index, transactionData);
		index += Block.nBytesFromCompactSizeInteger(txInCount);
		inputs = new TxIn[(int)txInCount];
		int nScriptBytes, intSize;
		char script[];
		Address recipient=null;
		for(int i=0;i<txInCount;i++)
		{
			nScriptBytes = (int)Block.parseCompactSizeInteger(index+36, transactionData);
			intSize = Block.nBytesFromCompactSizeInteger(nScriptBytes);
			inputs[i]=new TxIn(i,Block.extract64HexChars(index, transactionData), Block.parseUInt32(index+32, transactionData), nScriptBytes, Block.extractNChars((index+36+intSize)*2, nScriptBytes*2, transactionData), Block.parseUInt32(index+36+intSize+nScriptBytes, transactionData));
			index += 36+intSize+nScriptBytes+4;
		}
		txOutCount = Block.parseCompactSizeInteger(index, transactionData);
		index += Block.nBytesFromCompactSizeInteger(txOutCount);
		outputs = new TxOut[(int)txOutCount];
		for(int i=0;i<txOutCount;i++)
		{
			nScriptBytes = (int)Block.parseCompactSizeInteger(index+8, transactionData);
			intSize = Block.nBytesFromCompactSizeInteger(nScriptBytes);
			script = Block.extractNChars((index+8+intSize)*2, nScriptBytes*2, transactionData);
			recipient = extractRecipient(script);
			outputs[i]=new TxOut(i,Block.parseUInt64(index, transactionData),(long)nScriptBytes,script,recipient);
			index += 8+intSize+nScriptBytes;
		}
		lockTime = Block.parseUInt32(index, transactionData);
		index += 4;
		endIndex = index-1;
		char tx[] = Block.extractNChars(startIndex*2, (endIndex-startIndex+1)*2, transactionData);
		txHash = Block.computeHash(String.valueOf(tx));
		block.setIndex(index);
	}
	
	private Address extractRecipient(char script[])
	{
		char destination[] = null;
		int len = script.length;
		int keylen=0;
		if ((len==134) && (script[0]=='4') && (script[1]=='1')) //67 bytes length
		{
			destination = new char[130];
			destination = Block.extractNChars(2, 130, String.valueOf(script));
		}
		else if (len==132) //66 bytes length
		{
			destination = new char[130];
			destination = Block.extractNChars(0, 130, String.valueOf(script));
		}
		else if ((len>=50) && (script[0]=='7') && (script[1]=='6') && (script[2]=='a') && (script[3]=='9') && (script[4]=='1') && (script[5]=='4'))
		{
			destination = new char[40];
			destination = Block.extractNChars(6, 40, String.valueOf(script));
		}
		else if ((len==10) && (script[0]=='7') && (script[1]=='6') && (script[2]=='a') && (script[3]=='9') && (script[4]=='0') && (script[5]=='0'))
		{
			//invalid destination information
			destination = null;
		}
		else
		{
			//Scan for 0x67, 0xA9, len, key, 0x88, 0xAC
			for(int i=0;i<script.length;i++)
			{
				if ((script.length-1-i+1==50) && (script[i]=='7') && (script[i+1]=='6') && (script[i+2]=='a') && (script[i+3]=='9'))
				{
					keylen = Block.parseByte(i+4, String.valueOf(script));
					if ((script[i+6+keylen*2]=='8') && (script[i+6+keylen*2+1]=='8') && (script[i+6+keylen*2+2]=='a') && (script[i+6+keylen*2+3]=='c'))
					{
						destination = Block.extractNChars(i+6, keylen*2, String.valueOf(script));
						break;
					}
				}
			}
		}
		Address address = new Address(destination);
		return address;
	}
	
	public long getOutputSatoshis()
	{
		long satoshis=0;
		for(int i=0;i<txOutCount;i++) satoshis += outputs[i].getNSatoshis();
		return satoshis;
	}
	
	public int getVersion()
	{
		return version;
	}

	public long getTxInCount()
	{
		return txInCount;
	}

	public TxIn[] getInputs()
	{
		return inputs;
	}

	public long getTxOutCount()
	{
		return txOutCount;
	}

	public TxOut[] getOutputs()
	{
		return outputs;
	}

	public int getLockTime()
	{
		return lockTime;
	}
	
	public boolean isCoinbase()
	{
		return isCoinbase;
	}
	
	public boolean isFirstBlockTransaction()
	{
		return firstBlockTransaction;
	}
	
	public long getFees()
	{
		if (!firstBlockTransaction) return 0L;
		long satoshis=0;
		for(int i=0;i<txOutCount;i++) satoshis += outputs[i].getNSatoshis();
		if (isCoinbase) satoshis -= parentBlock.getBlockReward();
		return satoshis;
	}
	
	public char[] getTxHash()
	{
		return Block.reverseEndianness(txHash);
	}
	
	public int getBlockIndex()
	{
		return blockIndex;
	}
}
