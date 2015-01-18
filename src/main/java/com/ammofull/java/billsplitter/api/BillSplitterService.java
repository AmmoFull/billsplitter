package com.ammofull.java.billsplitter.api;

import java.util.List;
import java.util.Set;

import com.ammofull.java.billsplitter.engine.BillDetailsTO;



public interface BillSplitterService {
	
	/**
	 * Adds a new participant to the bill. 
	 * If number of users is 1, the new user by default contributes in each item. Else, he contributes in no item	 
	 * @param name
	 * @return
	 */
	public BillDetailsTO addUser(String name, BillDetailsTO oldBillDetailsTO);
	
	/**
	 * Adds a new item to the bill with the amount. Resets contribution for all users
	 * @param itemName
	 * @param amount
	 * @param participants
	 * @return
	 */
	public BillDetailsTO addItem(String itemName, Double amount, BillDetailsTO oldBillDetailsTO);
	
	/**
	 * Deletes an item from the bill. 
	 * @param itemName
	 * @return
	 */
	public BillDetailsTO deleteItem(String itemName, BillDetailsTO oldBillDetailsTO);
	
	/**
	 * Edits the name and the amount of an already added item. Does not touch contributors
	 * @param oldName
	 * @param newName
	 * @return
	 */
	public BillDetailsTO editItem(String oldName, String newName, double newAmount, BillDetailsTO oldBillDetailsTO);	
	
	/**
	 * Adds tips and taxes to the bill. Per head contribution is determined from the percentage contribution in total bill
	 * @param amount
	 * @return
	 */
	public BillDetailsTO addTipsAndTaxes(Double amount, BillDetailsTO oldBillDetailsTO);
	
	/**
	 * Removes tips and taxes from the bill
	 * @return
	 */
	public BillDetailsTO deleteTipsAndTaxes(BillDetailsTO oldBillDetailsTO);
	
	/**
	 * Edits tips and taxes to a new amount
	 * @param newAmount
	 * @return
	 */
	public BillDetailsTO editTipsAndTaxes(double newAmount, BillDetailsTO oldBillDetailsTO);
	
	/**
	 * Adds contribution to an existing item
	 * @param itemName
	 * @param participants
	 * @return
	 */
	public BillDetailsTO addContributionsForItem(String itemName,Set<String> participants, BillDetailsTO oldBillDetailsTO);
	
	/**
	 * Edit who is participating and who is not participating in an item. The order of the list is the same as the order in which users were added
	 * @param itemName
	 * @param participants
	 * @return
	 */
	public BillDetailsTO editContributionsForItem(String itemName, List<Boolean> participants, BillDetailsTO oldBillDetailsTO);
	
	/**
	 * Prints a summary of every users's contribtion in the bill
	 */
	public void printSummaryFinalReport(BillDetailsTO billDetailsTO);
	
	/**
	 * Detailed report of the bill
	 */
	public void printDetailedFinalReport(BillDetailsTO BillDetailsTO);

}
