package blockinterpreter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class Block
{	
	//Filename information
	private int height;
	//Information computed based on above information
	private char blockHash[]; //computed over block header: SHA-256(SHA-256(blockHeader))
	private long outputTotal; //in Satoshis
	private long transactionSatoshis; //in Satoshis
	private long fees; //in Satoshis
	private long blockReward; //in Satoshis
	private int blockSize; //block size in bytes
	private double difficulty;
	private Date timestamp;
	
	private BlockHeader blockHeader;
	private long nTransactions;
	private Transaction transactions[];
	
	private int index;
	
	public Block(String fileName, String blockData)
	{
		height = heightFromFileName(fileName);
		index=0;
		blockHash = computeBlockHash(blockData);
		//Extract block header information
		blockHeader = new BlockHeader(blockData);
		nTransactions = (int)parseCompactSizeInteger(80,blockData);
		index = 80+nBytesFromCompactSizeInteger(nTransactions);
		blockReward = computeBlockReward();
		blockSize = blockData.length()/2;
		difficulty = computeDifficulty();
		timestamp  = new Date((long)blockHeader.getTime()*1000);
		//Parse block transactions
		transactions = new Transaction[(int)nTransactions];
		for(int i=0;i<nTransactions;i++) transactions[i]=new Transaction(blockData,this,i);
		outputTotal = getOutputSatoshis(blockData);
		fees = transactions[0].getFees();
		transactionSatoshis = outputTotal - blockReward - fees;
	}
	
	public int getIndex()
	{
		return index;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public long getBlockReward()
	{
		return blockReward;
	}
	
	public void setIndex(int index)
	{
		this.index = index;
	}
	
	private long getOutputSatoshis(String block)
	{
		long totalSatoshis = 0;
		for(int i=0;i<nTransactions;i++) totalSatoshis += transactions[i].getOutputSatoshis();
		return totalSatoshis;
	}
	
	private int heightFromFileName(String fileName)
	{
		int dotPos = fileName.indexOf(".");
		if (dotPos==-1) return -1;
		String heightStr = fileName.substring(6, dotPos);
		return Integer.parseInt(heightStr);
	}
	
	public static byte[] computeSHA256Hash(byte input[])
	{
		MessageDigest md;
		try
		{
			md = MessageDigest.getInstance("SHA-256");
		}
		catch (NoSuchAlgorithmException e)
		{
			return null;
		}
		md.update(input);
		return md.digest();
	}
	
	public static char convertIntToHexChar(int x)
	{
		if (x<0) return '0';
		if (x<10) return (char)('0'+x);
		if ((x>=10) && (x<=15)) return (char)('a'+x-10);
		return '0';
	}
	
	public static byte[] compactHex(String input)
	{
		input = input.toLowerCase();
		byte res[] = new byte[input.length()/2];
		int first, second, byteVal;
		for(int i=0;i<input.length() && i+1<input.length();i+=2)
		{
			first = convertHexCharToDecimalInt(input.charAt(i));
			second = convertHexCharToDecimalInt(input.charAt(i+1));
			byteVal = first*16+second;
			if (first>127) byteVal = -128+(first-128);
			res[i/2]=(byte)byteVal;
		}
		return res;
	}
	
	public static int nBytesFromCompactSizeInteger(long x)
	{
		if (x<=252L) return 1;
		if (x<=65535L) return 3;
		if (x<=4294967296L) return 5;
		return 9;
	}
	
	/**
	 * Extracts n chars from the given string starting from position pos
	 * @param pos
	 * @param n
	 * @param s
	 * @return
	 */
	public static char[] extractNChars(int pos, int n, String s)
	{
		char res[] = new char[n];
		for(int i=0;i<n;i++) res[i]=s.charAt(pos+i);
		return res;
	}
	
	/**
	 * Computes the block's hash value
	 * @param block
	 * @return
	 */
	public static char[] computeBlockHash(String block)
	{
		String header = block.substring(0, 160); //block hash only computed over the block header
		return computeHash(header);
	}
	
	/**
	 * Computes a hash using two rounds of SHA-256 passes over the whole hex-coded data string
	 * @param data a hex-coded string
	 * @return SHA-256(SHA-256(data)) in hex format
	 */
	public static char[] computeHash(String data)
	{
		char hash[] = null;
		byte bytes[] = computeSHA256Hash(computeSHA256Hash(compactHex(data)));
		hash = new char[64];
		for(int i=0;i<32;i++)
		{
			hash[i*2] = convertIntToHexChar((byte)(bytes[i] >>> 4) & 0x0F);
			hash[i*2+1] = convertIntToHexChar(bytes[i] & 0x0F);
		}
		return hash;
	}
	
	/**
	 * Method parses a compactsize integer
	 * @param startpos the starting byte position
	 * @param block the block to be analyzed
	 * @return a variable length integer (1, 3, 4 or 8 bytes)
	 */
	public static long parseCompactSizeInteger(int startpos,String block)
	{
		long firstByte = parseByte(startpos,block);
		if (firstByte<=252) return firstByte;
		long res = 0;
		long byte1 = parseByte(startpos+1,block);
		long byte2 = parseByte(startpos+2,block);
		long byte3 = parseByte(startpos+3,block);
		if (firstByte==253) //Read three bytes
		{
			
			res = byte1+256*byte2+256*256*byte3;
		}
		else if (firstByte==254) //Read 4 bytes
		{
			long byte4 = parseByte(startpos+4,block);
			res = byte1+256*byte2+256*256*byte3+256*256*256*byte4;
		}
		else if (firstByte==255) //Read 8 bytes
		{
			long byte4 = parseByte(startpos+4,block);
			long byte5 = parseByte(startpos+5,block);
			long byte6 = parseByte(startpos+6,block);
			long byte7 = parseByte(startpos+7,block);
			long byte8 = parseByte(startpos+8,block);
			res = byte1+256*byte2+256*256*byte3+256*256*256*byte4+256*256*256*256*byte5+256*256*256*256*256*byte6+256*256*256*256*256*256*byte7+256*256*256*256*256*256*256*byte8;
		}
		return res;
	}
	
	/**
	 * Computes the difficulty which is computed as 65536*2^(208)/mult*2^(8*(exp-3))
	 * <ol>
	 * <li>First byte of nBits stores the exponent e</li>
	 * <li>Three remaining bytes store mult</li>
	 * </ol>
	 * @param nBits
	 * @return
	 */
	private double computeDifficulty()
	{
		long nBitsL = convertUInt32ToLong(blockHeader.getnBits());
		long nBitsExp = nBitsL >> 24;
		long nBitsMult = nBitsL & 0x00FFFFFF;
		return (double)((65535L*power(2,208-8*(nBitsExp-3)))/nBitsMult);
	}
	
	/**
	 * Computes the block reward in Satoshis given the block's height
	 * @param height
	 * @return
	 */
	private long computeBlockReward()
	{
		int era = height / 210000;
		return 5000000000L/(power(2,era));
	}
	
	private static int convertHexCharToDecimalInt(char c)
	{
		int x = (int)(c-'0');
		if ((x>=0) && (x<=9)) return x;
		if ((x>=49) && (x<=54)) return (int)(c-'a')+10;
		return -1;
	}
	
	/**
	 * Extracts a single byte from the given hex-encoded block string
	 * @param startpos the given byte starting position
	 * @param s the block string
	 * @return the requested byte in decimal representation
	 */
	public static int parseByte(int startpos, String s)
	{
		return convertHexCharToDecimalInt(s.charAt(startpos*2))*16+convertHexCharToDecimalInt(s.charAt(startpos*2+1));
	}
	
	/**
	 * Extract a UInt32 from a little-endian hex-encoded string
	 * @param startpos the position of the byte from which the extraction should start
	 * @param s the block's string value
	 * @return The UInt32 value starting at position startpos in string s
	 */
	public static int parseUInt32(int startpos, String s)
	{
		long res = 0;
		long fact = 1;
		for(int i=startpos*2;i<=startpos*2+6;i+=2)
		{
			res += ((long)convertHexCharToDecimalInt(s.charAt(i))*16+(long)convertHexCharToDecimalInt(s.charAt(i+1)))*fact;
			fact *= 256;
		}
		if (res>Integer.MAX_VALUE)
		{
			res = Integer.MIN_VALUE+(res-Integer.MAX_VALUE-1);
		}
		return (int)res;
	}
	
	public static long parseUInt64(int startpos, String s)
	{
		long res = 0;
		long fact = 1;
		for(int i=startpos*2;i<=startpos*2+14;i+=2)
		{
			res += ((long)convertHexCharToDecimalInt(s.charAt(i))*16+(long)convertHexCharToDecimalInt(s.charAt(i+1)))*fact;
			fact *= 256;
		}
		return res;
	}
	
	/**
	 * Interpret a signed integer as an unsigned 
	 * @param x
	 * @return
	 */
	public static long convertUInt32ToLong(int x)
	{
		if (x>=0) return (long)x;
		return (long)Integer.MAX_VALUE+(long)(x-Integer.MIN_VALUE);
	}
	
	public static char[] extract64HexChars(int startpos, String s)
	{
		char chars[] = new char[64];
		for(int i=0;i<64;i++) chars[i]=s.charAt(startpos*2+i);
		return chars;
	}
	
	/**
	 * Reverses the endianness of the given char array
	 * @param chars
	 * @return
	 */
	public static char[] reverseEndianness(char chars[])
	{
		int len = chars.length;
		char x, y;
		for(int i=0;i<len/2 && i+1<len/2;i+=2)
		{
			x = chars[i];
			y = chars[i+1];
			chars[i]=chars[len-2-i];
			chars[i+1]=chars[len-1-i];
			chars[len-2-i]=x;
			chars[len-1-i]=y;
		}
		return chars;
	}
	
	/**
	 * Function computes base^exp using the fast exponentiation algorithm
	 * @param base
	 * @param exp
	 * @return
	 */
	private long power(long base, long exp)
	{
		if (exp<=0) return 1;
		if (exp==1) return base;
		if (exp%2==0) return power(base*base,exp/2);
		else return base*power(base,exp-1);
	}
	
	public char[] getBlockHash()
	{
		return reverseEndianness(blockHash);
	}
	
	public void printBlock()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Block #"+height+"\n");
		sb.append("Version "+blockHeader.getVersion()+"\n");
		sb.append("Block hash "+String.valueOf(getBlockHash())+"\n");
		sb.append("Previous block header hash "+String.valueOf(blockHeader.getPrevBlockHeaderHash())+"\n");
		sb.append("Merkle root hash "+String.valueOf(blockHeader.getMerkleRootHash())+"\n");
		sb.append("Time "+timestamp.toString()+"\n");
		sb.append("NBits "+blockHeader.getnBits()+"\n");
		sb.append("Difficulty "+difficulty+"\n");
		sb.append("Nonce "+blockHeader.getNonce()+"\n");
		sb.append("Number of transactions "+nTransactions+"\n");
		sb.append("Output total "+outputTotal+" Satoshis\n");
		sb.append("Fees "+fees+" Satoshis\n");
		sb.append("Block reward "+blockReward+" Satoshis\n");
		sb.append("Block size "+blockSize+" bytes\n");
		sb.append("Non coinbase transaction volume "+transactionSatoshis+" Satoshis\n");
		sb.append("Output script "+String.valueOf(transactions[0].getOutputs()[0].getScript())+"\n");
		sb.append("Input script "+String.valueOf(transactions[0].getInputs()[0].getSignatureScript())+"\n");
		sb.append("Transaction 0 hash "+String.valueOf(transactions[0].getTxHash())+"\n");
		sb.append("Output address "+String.valueOf(transactions[0].getOutputs()[0].getAddress().getAddress())+"\n");
		System.out.println(sb.toString());
	}
}
