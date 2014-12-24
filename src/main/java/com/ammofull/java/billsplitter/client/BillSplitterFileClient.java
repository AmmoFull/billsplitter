package com.ammofull.java.billsplitter.client;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import com.ammofull.java.billsplitter.api.BillSplitterService;
import com.ammofull.java.billsplitter.engine.BillDetailsTO;
import com.ammofull.java.billsplitter.engine.BillSplitterServiceImpl;


/**
 * Client for the bill splitter service that reads all the bill data from a txt or a csv file. 
 * File format is as below ("-----" only for clarity. Do not include in file)
 * ------
 * Item1,100,X,Y,Z
 * Item2,125,X
 * Item3,20,A,B
 * T&T,25
 * ------
 * 
 * Row format: ItemName,ItemAmount,[<Comma separated list of participants>]
 * T&T is a reserved name. It stands for Tips&Taxes
 * 
 * For example, line 1 above means, Item1 is worth 100, shared evenly by X, Y and Z
 * 
 * @author Amod
 *
 */

public class BillSplitterFileClient {
	
	private static final String ITEM_NAME_FOR_TIPS_AND_TAXES = "T&T";

	public static void main(String[] args) {
		
		Scanner keyboardReader = new Scanner(System.in);
		System.out.println("Enter filename with full path:");
		String file = keyboardReader.nextLine();
		
		try
		{
			processFile(file);	
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
		finally
		{
			keyboardReader.close();	
		}
	}

	private static void processFile(String fileName) throws IOException 
	{
		
			Path path = Paths.get(fileName);
			
			List<String> lines = Files.readAllLines(path,  Charset.defaultCharset());	
			
			BillSplitterService billSplitterService = new BillSplitterServiceImpl();
			BillDetailsTO billDetailsTO = null;
			
			for(String line : lines)
			{
				String[] words = line.split(",");
				
				String itemName = words[0];
				
				if(itemName.equals(ITEM_NAME_FOR_TIPS_AND_TAXES))
				{
					Double amount = validateLineForTipsAndTaxes(words);
					billDetailsTO = billSplitterService.addTipsAndTaxes(amount);
				}
				else
				{
					Double amount = validateLineForItem(words);
					billDetailsTO = billSplitterService.addItem(itemName, amount);
					
					Set<String> participants = new HashSet<>();
					
					for(int i = 2 ; i < words.length ; i++)
					{
						String participant = words[i];
						participants.add(participant);
					}
					billDetailsTO = billSplitterService.addContributionsForItem(itemName, participants);
				}
				
			}
			
			billSplitterService.printDetailedFinalReport();
		
		
		
	}

	/**
	 * 1) Words length should be at least 3 as expected usage is Item,Amount,[Participants+]
	 * 2) Amount should be a parseable double
	 * @param words
	 * @return
	 */
	private static Double validateLineForItem(String[] words) {
		Double amount = null;
		if(words.length < 3)
		{
			throw new RuntimeException("Expected usage: ItemName,Amount,X,Y,Z. For item: " + words[0]);
		}
		try 
		{
			amount = Double.parseDouble(words[1]);
			return amount;
		} 
		catch(NumberFormatException e)
		{
			throw new RuntimeException("T&T amount should be a readable number. Found : " + words[1]);
		}
	}

	/**
	 * 1) Words count should be 2 as expected usage is T&T,amount
	 * 2) The amount should be a parseable Double
	 * @param words
	 * @return
	 */
	private static Double validateLineForTipsAndTaxes(String[] words) {
		Double amount = null;
		if(words.length != 2)
		{
			throw new RuntimeException("Error in line with tips and taxes. Expected usage: T&T,Amount");
		}
		try 
		{
			amount = Double.parseDouble(words[1]);
			return amount;
		} 
		catch(NumberFormatException e)
		{
			throw new RuntimeException("T&T amount should be a readable number. Found : " + words[1]);
		}
	}

}
