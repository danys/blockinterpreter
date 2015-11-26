package blockinterpreter;

public class TxOut
{
	private long nSatoshis;
	private long nScriptBytes;
	private char script[];
	private Address address;
	private int outputIndex;
	
	/**
	 * Transaction output
	 * @param nSatoshis the number of Satoshis to spend
	 * @param nScriptBytes the number of bytes in the script
	 * @param script the conditions that are to be satisfied by the future spender of this output
	 */
	public TxOut(int outputIndex, long nSatoshis, long nScriptBytes, char script[], Address recipient)
	{
		this.nSatoshis = nSatoshis;
		this.nScriptBytes = nScriptBytes;
		this.script = script;
		this.address = recipient;
		this.outputIndex = outputIndex;
	}
	
	public long getNSatoshis()
	{
		return nSatoshis;
	}
	
	public long getNScriptBytes()
	{
		return nScriptBytes;
	}
	
	public char[] getScript()
	{
		return script;
	}
	
	public Address getAddress()
	{
		return address;
	}
	
	public int getOutputIndex()
	{
		return outputIndex;
	}
}
