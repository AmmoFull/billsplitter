package com.ammofull.java.billsplitter.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ammofull.java.billsplitter.api.BillSplitterService;

public class BillSplitterServiceImpl implements BillSplitterService {
	
	/**
	 * Tips and taxes should be in the map only once. As name is the key for item name, it is hard coded here
	 */
	public static final String ITEM_NAME_FOR_TIPS_AND_TAXES = "Tips and taxes";		

	@Override
	public BillDetailsTO addUser(String name, BillDetailsTO oldBillDetailsTO) {	
		
		BillDetailsTO billDetailsTO = oldBillDetailsTO;
		if(billDetailsTO == null)
		{
			billDetailsTO = new BillDetailsTO();
		}
		
		validateUserNameOrItemName(name);		
		
		// Add user to the set
		boolean isAdded = billDetailsTO.getUsers().add(name);		
		if(!isAdded) {
			throw new IllegalArgumentException(name + " already added");
		}	
		
		computeEntireBill(billDetailsTO);
		
		return billDetailsTO;
	}	

	@Override
	public BillDetailsTO addItem(String itemName, Double amount, BillDetailsTO oldBillDetailsTO) {
		
		BillDetailsTO billDetailsTO = oldBillDetailsTO;
		if(billDetailsTO == null)
		{
			billDetailsTO = new BillDetailsTO();
		}
		
		validateUserNameOrItemName(itemName);
		if(amount == null || amount.equals(0.0))
		{
			throw new IllegalArgumentException("Amount cannot be null or zero");
		}
				
		if(billDetailsTO.getItemVsAmount().containsKey(itemName))
		{
			throw new IllegalArgumentException("Item name already present : " + itemName);
		}	
		billDetailsTO.getItemVsAmount().put(itemName, amount);

		// Reset participants for the new item
		Set<String> participants = new HashSet<>();
		billDetailsTO.getItemsVsParticipants().put(itemName, participants);		
		computeEntireBill(billDetailsTO);
		return billDetailsTO;	
				
	}	
	
	@Override
	public BillDetailsTO addContributionsForItem(String itemName,Set<String> participants, BillDetailsTO oldBillDetailsTO) {
		
		if(itemName == null || "".equals(itemName))
		{
			throw new IllegalArgumentException("Name cannot be null or empty");
		}
		if(participants == null || participants.isEmpty())
		{
			throw new IllegalArgumentException("Participants cannot be null or empty");
		}
		if(itemName.equals(ITEM_NAME_FOR_TIPS_AND_TAXES))
		{
			throw new IllegalArgumentException("Participants for tips and taxes are not provided explicitly");
		}
		
		BillDetailsTO billDetailsTO = oldBillDetailsTO;
		if(billDetailsTO == null)
		{
			billDetailsTO = new BillDetailsTO();
		}
		
		Set<String> users = billDetailsTO.getUsers();
		
		// Feature: No need to call addUser explicitly by the client if participants are known
		for(String participant : participants)
		{
			if(!users.contains(participant))
			{
				billDetailsTO = addUser(participant,billDetailsTO);
			}
		}
		
		Set<String> items = billDetailsTO.getItemVsAmount().keySet();
		if(!items.contains(itemName))
		{
			throw new IllegalArgumentException(itemName + " not present. Call addItem method first");
		}
		
		for(String item : items) {
			if(item.equals(itemName)) {
				billDetailsTO.getItemsVsParticipants().put(item, participants);
				break;
			}
		}
		
		computeEntireBill(billDetailsTO);
		
		return billDetailsTO;
	}	
	
	@Override
	public BillDetailsTO addTipsAndTaxes(Double amount, BillDetailsTO oldBillDetailsTO) {
		
		if(amount == null)
		{
			throw new IllegalArgumentException("Amount cannot be null");
		}
		
		BillDetailsTO billDetailsTO = oldBillDetailsTO;
		if(billDetailsTO == null)
		{
			billDetailsTO = new BillDetailsTO();
		}
		
		billDetailsTO.setTipsAndTaxes(amount);			
		computeEntireBill(billDetailsTO); 			
		return billDetailsTO;	
		
				
	}
	
	@Override
	public void printSummaryFinalReport(BillDetailsTO billDetailsTO) {
		
		System.out.println("***********************");
		System.out.println("Per head contribution");
		System.out.println("***********************");
		
		for(String user : billDetailsTO.getUsers()) {
			System.out.print("Participant:\t");
			System.out.print(user);
			System.out.print("\tContribution:\t");
			System.out.println(billDetailsTO.getUserVsTotalPerHeadContribution().get(user));
		}		
	}

	@Override
	public void printDetailedFinalReport(BillDetailsTO billDetailsTO) {				
		System.out.println("***********************");
		System.out.println("Detailed breakdown");
		System.out.println("***********************");
		
		
		Map<String,Map<String,Double>> itemVsPerHeadContrib = billDetailsTO.getItemsVsPerHeadContributions();
		
		for(String item : itemVsPerHeadContrib.keySet())
		{
			Double amount = billDetailsTO.getItemVsAmount().get(item);
			System.out.println(item + " : " + amount);
			
			Map<String,Double> userVsContribForItem = itemVsPerHeadContrib.get(item);
			
			for(String participant : userVsContribForItem.keySet())
			{
				System.out.println("\t" + participant + ":\t" + userVsContribForItem.get(participant));
			}
		}
		
		printSummaryFinalReport(billDetailsTO);
		
	}	
	
	/**
	 * 1) Calculate per head contribution for every item
	 * 2) Calculate total per head contribution for every user using 1
	 * 3) Calculate % contribution for every user so that tips and taxes can be allocated accordingly, using 2
	 * 4) Calculate tips and taxes allocation using 3
	 */
	private void computeEntireBill(BillDetailsTO billDetailsTO) {
		
		// To calculate item contribution, at least one item has to be present
		if(!billDetailsTO.getItemsVsParticipants().isEmpty())
		{
			computePerHeadContributionForItem(billDetailsTO);
		}	

		// To calculate any user based number, there has to be at least one user
		if(!billDetailsTO.getUsers().isEmpty())
		{
			computePerHeadContributionForTotalBill(billDetailsTO);	
			computePerHeadPercentageContributionForBill(billDetailsTO);
		}	
		
		if(!billDetailsTO.getUsers().isEmpty() &&
				!billDetailsTO.getItemsVsParticipants().isEmpty() &&				
				!billDetailsTO.getTipsAndTaxes().equals(0.0))
		{
			computeTipsAndTaxesAllocation(billDetailsTO);
		}
	}

	/**
	 * Method to calculate per head contribution for an item
	 * It will evenly divide the amount among all the participants.
	 */
	private void computePerHeadContributionForItem(BillDetailsTO billDetailsTO) {
		
		Set<String> items = billDetailsTO.getItemVsAmount().keySet();
		Map<String,Set<String>> itemsAndParticipants = billDetailsTO.getItemsVsParticipants();
		Map<String,Map<String,Double>> itemsAndPerHeadContributions = billDetailsTO.getItemsVsPerHeadContributions();
		
		for (Iterator<String> iterator = items.iterator(); iterator.hasNext();) {
			String itemName = iterator.next();
			
			Set<String> participants = itemsAndParticipants.get(itemName);
			
			// Reset per head contributions for this item before recalculating
			itemsAndPerHeadContributions.remove(itemName);
			Map<String,Double> perHeadContibutions = new HashMap<>();			
			
			if(participants.size() != 0) {				
				// Get per head contribution number
				Double amount = billDetailsTO.getItemVsAmount().get(itemName);
				double perHeadContribution = amount / participants.size();
				
				// Allocate the per head contribution to each participant
				for(String participant : participants) {
					perHeadContibutions.put(participant,perHeadContribution);
				}				
			} 
			
			itemsAndPerHeadContributions.put(itemName, perHeadContibutions);			
		}						
	}
	
	/**
	 * This method computes the total per head contribution. It changes when an item is added or a participant is added
	 * @param perHeadContributionsForAnItem
	 */
	private void computePerHeadContributionForTotalBill(BillDetailsTO billDetailsTO) {
		
		Map<String,Map<String,Double>> itemsAndPerHeadContributions = billDetailsTO.getItemsVsPerHeadContributions();
		Set<String> items = itemsAndPerHeadContributions.keySet();
		
		// Reset userVsTotalContribution before calculating
		Map<String,Double> userVsTotalContribution = new HashMap<>();
		for(String user: billDetailsTO.getUsers())
		{
			userVsTotalContribution.put(user, 0.0);
		}
		
		// Loop through all items and get the contribution for every user for the item
		// Increment the total per head contribution for every user using the above traversal
		for(String item : items) {
			Map<String,Double> userVsContributionForItem = itemsAndPerHeadContributions.get(item);
			
			for(String user : userVsContributionForItem.keySet())
			{
				Double contributionForUser = userVsTotalContribution.get(user);
				if(contributionForUser == null) 
				{
					contributionForUser = 0.0;
				}
				contributionForUser += userVsContributionForItem.get(user);
				userVsTotalContribution.put(user, contributionForUser);
			}			
		}
		
		billDetailsTO.setUserVsTotalPerHeadContribution(userVsTotalContribution);
	}	

	/**
	 * In order to divide tips and taxes fairly based on the user's % contribution in the total bill, 
	 * this method computes that percentage per user. Note that total per head contribution should be available
	 * before computing this
	 */
	private void computePerHeadPercentageContributionForBill(BillDetailsTO billDetailsTO) 
	{			
		// Reset user vs percent contrib before calculating
		Map<String,Double> userVsPercentContrib = new HashMap<>();
		for(String user : billDetailsTO.getUsers())
		{
			userVsPercentContrib.put(user, 0.00);
		}

		// Compute total bill before tips and taxes so that % contrib can be determined
		double totalBillBeforeTipsAndTaxes = 0.0;
		Set<String> items = billDetailsTO.getItemsVsParticipants().keySet();

		for(String item : items)
		{	
			Double amount = billDetailsTO.getItemVsAmount().get(item);
			// We dont need any check for tips and taxes here because that item is never added to the map ItemVsParticipants
			totalBillBeforeTipsAndTaxes += amount;			
		}

		
		boolean isTipsAndTaxesRemoved = false;
		if(billDetailsTO.getItemsVsPerHeadContributions().containsKey(ITEM_NAME_FOR_TIPS_AND_TAXES))
		{
			// We need to remove tips and taxes, re-calculate per head contrib before proceeding with % calc
			billDetailsTO.getItemsVsPerHeadContributions().remove(ITEM_NAME_FOR_TIPS_AND_TAXES);
			isTipsAndTaxesRemoved = true;
			computePerHeadContributionForTotalBill(billDetailsTO);
		}
		Map<String,Double> userVsTotalPerHeadContribution = billDetailsTO.getUserVsTotalPerHeadContribution();

		// Compute percentage contrib per user based on total
		if(totalBillBeforeTipsAndTaxes != 0)
		{
			for(String user : userVsTotalPerHeadContribution.keySet())
			{
				Double totalContribForThisUser = userVsTotalPerHeadContribution.get(user);
				Double percentAllocationForThisUser = totalContribForThisUser/totalBillBeforeTipsAndTaxes*100;
				
				// Math to remove extra decimals and keep it to 2 decimal places
				Integer percentAllocTemp = (int)(percentAllocationForThisUser * 100);
				percentAllocationForThisUser = percentAllocTemp / 100.0;
				
				userVsPercentContrib.put(user, percentAllocationForThisUser);				
			}										
		}
		
		billDetailsTO.setUserVsPercentageContribInTheBill(userVsPercentContrib);
		
		if(isTipsAndTaxesRemoved)
		{
			computeTipsAndTaxesAllocation(billDetailsTO);
		}
						
	}	
	
	/**
	 * Using percentage contrib, allocate tips and taxes among all users
	 * Re-calculate per head total contrib based on this allocation
	 */
	private void computeTipsAndTaxesAllocation(BillDetailsTO billDetailsTO)
	{
		Double tipsAndTaxes = billDetailsTO.getTipsAndTaxes();
		
		Map<String,Double> userVsPercentageContrib = billDetailsTO.getUserVsPercentageContribInTheBill();
		
		Map<String,Double> userVsTipsAndTaxesContrib = new HashMap<>();
		
		// Allocate amount as per percentage contrib for each user
		for(String user : userVsPercentageContrib.keySet()) 
		{
			Double tipAllocationForThisUser = userVsPercentageContrib.get(user)*tipsAndTaxes/100;
			
			// Restrict to 2 decimals.
			Integer tipAllocationTemp = (int)(tipAllocationForThisUser * 100);
			tipAllocationForThisUser = tipAllocationTemp / 100.0;
			userVsTipsAndTaxesContrib.put(user, tipAllocationForThisUser);
		}
		
		billDetailsTO.getItemsVsPerHeadContributions().put(ITEM_NAME_FOR_TIPS_AND_TAXES, userVsTipsAndTaxesContrib);
		
		// Once tips and taxes are calculated, the per head contribution will change. 
		// The percent contrib will not change as it does not include tips and taxes
		computePerHeadContributionForTotalBill(billDetailsTO);		
	}
	
	/**
	 * If input is null or empty, it throws IllegalArgumentException
	 * @param name
	 */
	private void validateUserNameOrItemName(String name) {
		if(name == null) {
			throw new IllegalArgumentException("name cannot be null");
		}
		if("".equals(name)) {
			throw new IllegalArgumentException("name cannot be empty");
		}
		if(name.equals(ITEM_NAME_FOR_TIPS_AND_TAXES)) {
			throw new IllegalArgumentException(ITEM_NAME_FOR_TIPS_AND_TAXES + " is a reserved name. Use something else");
		}
	}

	@Override
	public BillDetailsTO deleteItem(String itemName, BillDetailsTO oldBillDetailsTO) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BillDetailsTO editItem(String oldName, String newName,double newAmount, BillDetailsTO oldBillDetailsTO) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BillDetailsTO deleteTipsAndTaxes(BillDetailsTO oldBillDetailsTO) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BillDetailsTO editTipsAndTaxes(double newAmount, BillDetailsTO oldBillDetailsTO) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BillDetailsTO editContributionsForItem(String itemName,List<Boolean> participants, BillDetailsTO oldBillDetailsTO) {
		// TODO Auto-generated method stub
		return null;
	}	
}
