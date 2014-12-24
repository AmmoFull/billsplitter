package com.ammofull.java.billsplitter.client;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import com.ammofull.java.billsplitter.api.BillSplitterService;
import com.ammofull.java.billsplitter.engine.BillDetailsTO;
import com.ammofull.java.billsplitter.engine.BillSplitterServiceImpl;


/**
 * Client for the bill splitter service using the console (command line)
 * @author Amod
 *
 */
public class BillSplitterConsoleClient {
	
	private static BillSplitterService billSplitterService;
	
	private static Scanner keyboardReader;

	public static void main(String[] args) {
		
		billSplitterService = new BillSplitterServiceImpl();
		String yesNo;
		keyboardReader = new Scanner(System.in);
		
		System.out.println("************************");
		System.out.println("Adding participants to split the bill");
		System.out.println("************************");
		// Add users
		while(true) {
			System.out.println("Add participant? (y/n) : ");
			yesNo = keyboardReader.nextLine();
			if(isYes(yesNo)) {
				addUser();				
			} else if(isNo(yesNo)) {
				break;
			} else {
				System.out.println("y,Y,n,N are the only expected values");
			}
		}
		
		System.out.println("************************");
		System.out.println("Participants done. Now adding items. Do not include tips and taxes. In this section");
		System.out.println("************************");
		
		while(true) {
			System.out.println("Add item? (y/n) : ");
			yesNo = keyboardReader.nextLine();
			if(isYes(yesNo)) {
				addItem();				
			} else if(isNo(yesNo)) {
				break;
			} else {
				System.out.println("y,Y,n,N are the only expected values");
			}
		}
		
		System.out.println("************************");
		System.out.println("Items done.");
		System.out.println("************************");
		
		
		System.out.println("Add tips and taxes? (y/n) : ");
		yesNo = keyboardReader.nextLine();
		if(isYes(yesNo)) {
			addTipsAndTaxes();				
		} 
		
		System.out.println("************************");
		System.out.println("Tips and taxes done.");
		System.out.println("************************");
		
		billSplitterService.printSummaryFinalReport();
		
		System.out.println("************************");
		System.out.println("Show more details? (y/n) : ");
		yesNo = keyboardReader.nextLine();
		if(isYes(yesNo)) {
			billSplitterService.printDetailedFinalReport();
		} else if(isNo(yesNo)) {
			System.out.println("Thank you for using Bill Splitter.");
		} else {
			System.out.println("y,Y,n,N are the only expected values");
		}
		
	}	

	private static void addUser() {
		System.out.println("Name: ");
		String name = keyboardReader.nextLine();
		try {
			billSplitterService.addUser(name);
			System.out.println(name + " added");
		} catch(Exception e) {
			System.out.println("Error: " + e.getMessage());
		}		
	}
	
	private static void addItem() {
		String yesNo;
		System.out.println("Enter item name : ");
		String itemName = keyboardReader.nextLine();
		System.out.println("Enter amount (numbers and decimals only): ");
		try {
			String amountInString = keyboardReader.nextLine(); 
			Double amount = Double.valueOf(amountInString);
			BillDetailsTO billDetailsTO = billSplitterService.addItem(itemName, amount);
			
			Set<String> participantsForThisItem = new HashSet<>();
			
			Set<String> users = billDetailsTO.getUsers();
			
			for(String user : users) {
				System.out.println("Did " + user + " participate in " + itemName + "? (y/n) : ");
				yesNo = keyboardReader.nextLine();
				System.out.println("Input string: " + yesNo);
				if(isYes(yesNo)) {
					participantsForThisItem.add(user);
				} else if(isNo(yesNo)) {
					// Do nothing
				} else {
					System.out.println("y,Y,n,N are the only expected values. This item was not added. Try again");
					break;
				}
			}			
			billSplitterService.addContributionsForItem(itemName, participantsForThisItem);
		} catch(NumberFormatException e) {
			System.out.println("A numeric input was expected");
		}
		
	}
	
	private static void addTipsAndTaxes() {
		System.out.println("Enter total amount for tips and taxes: ");
		String tipString = keyboardReader.nextLine();
		try
		{
			Double tipAmount = Double.valueOf(tipString);
			billSplitterService.addTipsAndTaxes(tipAmount);
		} catch(NumberFormatException e) {
			System.out.println("A numeric input was expected");
		}
		
	}
	
	private static boolean isYes(String keyboardInput) {
		return "y".equalsIgnoreCase(keyboardInput);
	}
	
	private static boolean isNo(String keyboardInput) {
		return "n".equalsIgnoreCase(keyboardInput);
	}
	
	
		

}
