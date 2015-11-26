package blockinterpreter;

import org.bouncycastle.crypto.digests.RIPEMD160Digest;

public class Address
{
	private char publicKey[];
	private char address[];
	private String pszBase58 = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
	
	public Address(char destination[])
	{
		publicKey=null;
		address=null;
		if (destination!=null)
		{
			//if 65 bytes destination => we have the public key otherwise we have the Bitcoin address
			if (destination.length==130)
			{
				this.publicKey = destination;
				byte compactKey[] = Block.compactHex(String.valueOf(publicKey));
				compactKey = Block.computeSHA256Hash(compactKey);
				byte[] ripemdHash = new byte[20];
		    	RIPEMD160Digest digest = new RIPEMD160Digest();
		    	digest.update(compactKey,0,compactKey.length);
		    	digest.doFinal(ripemdHash,0);
		    	byte[] preHash = new byte[ripemdHash.length+1];
		    	preHash[0]=0;
		    	for(int i=0;i<ripemdHash.length;i++) preHash[i+1]=ripemdHash[i];
		    	byte hash[] = Block.computeSHA256Hash(Block.computeSHA256Hash(preHash));
		    	byte byteHash[] = new byte[preHash.length+4];
		    	for(int i=0;i<preHash.length;i++) byteHash[i]=preHash[i];
		    	for(int i=0;i<4;i++) byteHash[i+preHash.length]=hash[i];
		    	String addressStr = encodeBase58(byteHash);
		    	address = addressStr.toCharArray();
			}
			else
			{
				address = destination;
				publicKey = null;
			}
		}
	}
	
	private String encodeBase58(byte input[])
	{
	    // Skip & count leading zeroes.
	    int zeroes = 0;
	    for(int i=0;i<input.length && input[i]==0;i++) zeroes++;
	    // Allocate enough space in big-endian base58 representation.
	    byte res[] = new byte[input.length*138/100+1]; // log(256) / log(58), rounded up.
	    // Process the bytes.
	    int carry;
	    for(int i=0;i<input.length;i++)
	    {
	    	carry = (int)input[i];
	    	if (carry<0) carry += 128+1+127;
	    	for (int j=res.length-1;j>=0;j--)
	    	{
	            carry += 256 * res[j];
	            res[j] = (byte)(carry % 58);
	            carry /= 58;
	        }
	    }
	    // Skip leading zeroes in base58 result.
	    int i=0;
	    while (i<res.length && res[i] == 0) i++;
	    // Translate the result into a string.
	    StringBuilder sb = new StringBuilder(zeroes+res.length-i);
	    for(int j=0;j<zeroes;j++) sb.append('1');
	    while (i< res.length) sb.append(pszBase58.charAt((int)res[i++]));
	    return sb.toString();
	}
	
	public char[] getPublicKey()
	{
		return publicKey;
	}
	
	public char[] getAddress()
	{
		return address;
	}
}
