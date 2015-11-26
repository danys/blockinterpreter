package blockinterpreter;

public class BlockHeader
{
	//Block header information
	private int version;
	private char prevBlockHeaderHash[];
	private char merkleRootHash[];
	private int time;
	private int nBits;
	private int nonce;
	
	/**
	 * Constructs an interpreted block given a block in hex-coded block string
	 * @param block the block as a string
	 */
	public BlockHeader(String block)
	{
		
		version = Block.parseUInt32(0,block);
		prevBlockHeaderHash = Block.extract64HexChars(4, block);
		merkleRootHash = Block.extract64HexChars(36, block);
		time = Block.parseUInt32(68,block);
		nBits = Block.parseUInt32(72,block);
		nonce = Block.parseUInt32(76,block);
	}

	//Getters
	
	public int getVersion()
	{
		return version;
	}

	public char[] getPrevBlockHeaderHash()
	{
		return Block.reverseEndianness(prevBlockHeaderHash);
	}

	public char[] getMerkleRootHash()
	{
		return Block.reverseEndianness(merkleRootHash);
	}

	public int getTime()
	{
		return time;
	}

	public int getnBits()
	{
		return nBits;
	}

	public int getNonce()
	{
		return nonce;
	}
	
}
