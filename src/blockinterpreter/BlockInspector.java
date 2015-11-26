package blockinterpreter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class BlockInspector
{	
	
	
	public static void main(String args[])
	{
		StringBuilder sb = new StringBuilder();
		String fileName = "block_0.txt";
		try(BufferedReader br = new BufferedReader(new FileReader("/Users/Dany/Downloads/"+fileName)))
		{
			String line;
			while((line = br.readLine())!=null)
			{
				sb.append(line);
				sb.append(System.lineSeparator());
			}
		}
		catch(FileNotFoundException e)
		{
			System.out.println("Block file not found!");
			System.exit(0);
		}
		catch(IOException e)
		{
			System.out.println("An error occurred while reading the file!");
			System.exit(0);
		}
		String fileData = sb.toString();
		Block block = new Block(fileName,fileData);
		block.printBlock();
	}
}
