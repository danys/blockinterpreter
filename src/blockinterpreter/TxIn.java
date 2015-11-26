package blockinterpreter;

public class TxIn
{
	private char txHash[]; //32 character hash -> 64 hex characters hash
	private int index;
	private long nScriptBytes;
	private char signatureScript[];
	private int sequence;
	private int inputIndex;
	//Address or public key is not present in the transaction input. Can look it up in previous transaction output, no guarantees that address is still valid.
	
	/**
	 * Transaction input
	 * @param txHash the hash of the transaction that contains the output that is to be spent
	 * @param index the index of the referenced output
	 * @param nScriptBytes the number of bytes in the script
	 * @param signatureScript the script that satisfies the conditions outlined in the referenced output
	 * @param sequence sequence number, 0xffffffff by default
	 */
	public TxIn(int inputIndex, char txHash[], int index, long nScriptBytes, char signatureScript[], int sequence)
	{
		this.txHash = txHash;
		this.index = index;
		this.nScriptBytes = nScriptBytes;
		this.signatureScript = signatureScript;
		this.sequence = sequence;
		this.inputIndex = inputIndex;
	}

	public char[] getTxHash()
	{
		return txHash;
	}

	public int getIndex()
	{
		return index;
	}

	public long getNScriptBytes()
	{
		return nScriptBytes;
	}

	public char[] getSignatureScript()
	{
		return signatureScript;
	}

	public int getSequence()
	{
		return sequence;
	}
	
	public int getInputIndex()
	{
		return inputIndex;
	}
}
