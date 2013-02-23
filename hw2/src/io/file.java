package io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;

public class file {

	/**
	 * read file and store sentence pairs
	 * @param string
	 * @param sentPairsList
	 * @throws IOException 
	 * @throws UnsupportedEncodingException 
	 */
	public void readSentencePairs(String fileName, List<String> sentPairsList) throws UnsupportedEncodingException, IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF8"));	
		String line;
		int count = 0;
		while((line = br.readLine()) != null) {
			line = line.toLowerCase();  //make all the sentences lowercase
			line = line.replace(" ||| ", "\t");
			sentPairsList.add(line);
			count++;
		}		
		br.close();		
	}

	/**
	 * write evaluation scores into the output file
	 * @param string
	 * @param scoresList
	 */
	public void writeEvaluation(String outFile,
			HashMap<Integer, String> scoresList) throws IOException{
		
		Writer out = new BufferedWriter(new FileWriter(new File(outFile)));	
		
		for(int i=0; i<scoresList.size(); i++){
			out.write(scoresList.get(i) + "\n");
		}
		out.close();
	}

}
