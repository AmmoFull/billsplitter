package com.ammofull.java.billsplitter.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ammofull.java.billsplitter.api.BillSplitterService;

public class BillSplitterServiceImpl implements BillSplitterService {
	
	/**
	 * Tips and taxes should be in the map only once. As name is the key for item name, it is hard coded here
	 */
	public static final String ITEM_NAME_FOR_TIPS_AND_TAXES = "Tips and taxes";
	
	private BillDetailsTO billDetailsTO;
	
	private Double tipsAndTaxes = 0.0;
	
	public BillSplitterServiceImpl() {
		billDetailsTO = new BillDetailsTO();		
	}

	@Override
	public BillDetailsTO addUser(String name) {	
		
		validateUserNameOrItemName(name);
		
		// Add user to the set
		boolean isAdded = billDetailsTO.getUsers().add(name);		
		if(!isAdded) {
			throw new IllegalArgumentException(name + " already added");
		}	
		
		computeEntireBill();
		
		return billDetailsTO;
	}	

	@Override
	public BillDetailsTO addItem(String itemName, Double amount) {
		
		validateUserNameOrItemName(itemName);
		if(amount == null || amount.equals(0.0))
		{
			throw new IllegalArgumentException("Amount cannot be null or zero");
		}
		ItemTO itemTO = new ItemTO(itemName, amount);
		Set<String> participants = new HashSet<>();
		
		if(billDetailsTO.getItemsVsParticipants().containsKey(itemTO))
		{
			throw new IllegalArgumentException("Item name already present : " + itemName);
		}	

		billDetailsTO.getItemsVsParticipants().put(itemTO, participants);
		computeEntireBill();
		return billDetailsTO;	
				
	}	
	
	@Override
	public BillDetailsTO addContributionsForItem(String itemName,Set<String> participants) {
		
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
		
		Set<String> users = billDetailsTO.getUsers();
		
		// Feature: No need to call addUser explicitly by the client if participants are known
		for(String participant : participants)
		{
			if(!users.contains(participant))
			{
				billDetailsTO = addUser(participant);
			}
		}
		
		Set<ItemTO> items = billDetailsTO.getItemsVsParticipants().keySet();
		if(!items.contains(new ItemTO(itemName, 10D)))
		{
			throw new IllegalArgumentException(itemName + " not present. Call addItem method first");
		}
		
		for(ItemTO item : items) {
			if(item.getItemName().equals(itemName)) {
				billDetailsTO.getItemsVsParticipants().put(item, participants);
				break;
			}
		}
		
		computeEntireBill();
		
		return billDetailsTO;
	}	
	
	@Override
	public BillDetailsTO addTipsAndTaxes(Double amount) {
		
		if(amount == null)
		{
			throw new IllegalArgumentException("Amount cannot be null");
		}
		
		tipsAndTaxes = amount;			
		computeEntireBill(); 			
		return billDetailsTO;	
		
				
	}
	
	@Override
	public void printSummaryFinalReport() {
		
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
	public void printDetailedFinalReport() {				
		System.out.println("***********************");
		System.out.println("Detailed breakdown");
		System.out.println("***********************");
		
		
		Map<ItemTO,Map<String,Double>> itemVsPerHeadContrib = billDetailsTO.getItemsVsPerHeadContributions();
		
		for(ItemTO item : itemVsPerHeadContrib.keySet())
		{
			System.out.println(item.getItemName() + " : " + item.getAmount());
			
			Map<String,Double> userVsContribForItem = itemVsPerHeadContrib.get(item);
			
			for(String participant : userVsContribForItem.keySet())
			{
				System.out.println("\t" + participant + ":\t" + userVsContribForItem.get(participant));
			}
		}
		
		printSummaryFinalReport();
		
	}	
	
	/**
	 * 1) Calculate per head contribution for every item
	 * 2) Calculate total per head contribution for every user using 1
	 * 3) Calculate % contribution for every user so that tips and taxes can be allocated accordingly, using 2
	 * 4) Calculate tips and taxes allocation using 3
	 */
	private void computeEntireBill() {
		
		// To calculate item contribution, at least one item has to be present
		if(!billDetailsTO.getItemsVsParticipants().isEmpty())
		{
			computePerHeadContributionForItem();
		}	

		// To calculate any user based number, there has to be at least one user
		if(!billDetailsTO.getUsers().isEmpty())
		{
			computePerHeadContributionForTotalBill();	
			computePerHeadPercentageContributionForBill();
		}	
		
		if(!billDetailsTO.getUsers().isEmpty() &&
				!billDetailsTO.getItemsVsParticipants().isEmpty() &&				
				!tipsAndTaxes.equals(0.0))
		{
			computeTipsAndTaxesAllocation();
		}
	}

	/**
	 * Method to calculate per head contribution for an item
	 * It will evenly divide the amount among all the participants.
	 */
	private void computePerHeadContributionForItem() {
		
		Set<ItemTO> items = billDetailsTO.getItemsVsParticipants().keySet();
		Map<ItemTO,Set<String>> itemsAndParticipants = billDetailsTO.getItemsVsParticipants();
		Map<ItemTO,Map<String,Double>> itemsAndPerHeadContributions = billDetailsTO.getItemsVsPerHeadContributions();
		
		for(ItemTO itemTO : items) {
			Set<String> participants = itemsAndParticipants.get(itemTO);
			
			// Reset per head contributions for this item before recalculating
			itemsAndPerHeadContributions.remove(itemTO);
			Map<String,Double> perHeadContibutions = new HashMap<>();			
			
			if(participants.size() != 0) {				
				// Get per head contribution number
				double perHeadContribution = itemTO.getAmount() / participants.size();
				
				// Allocate the per head contribution to each participant
				for(String participant : participants) {
					perHeadContibutions.put(participant,perHeadContribution);
				}				
			} 
			
			itemsAndPerHeadContributions.put(itemTO, perHeadContibutions);			
		}			
	}
	
	/**
	 * This method computes the total per head contribution. It changes when an item is added or a participant is added
	 * @param perHeadContributionsForAnItem
	 */
	private void computePerHeadContributionForTotalBill() {
		
		Map<ItemTO,Map<String,Double>> itemsAndPerHeadContributions = billDetailsTO.getItemsVsPerHeadContributions();
		Set<ItemTO> items = itemsAndPerHeadContributions.keySet();
		
		// Reset userVsTotalContribution before calculating
		Map<String,Double> userVsTotalContribution = new HashMap<>();
		for(String user: billDetailsTO.getUsers())
		{
			userVsTotalContribution.put(user, 0.0);
		}
		
		// Loop through all items and get the contribution for every user for the item
		// Increment the total per head contribution for every user using the above traversal
		for(ItemTO item : items) {
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
	private void computePerHeadPercentageContributionForBill() 
	{			
		// Reset user vs percent contrib before calculating
		Map<String,Double> userVsPercentContrib = new HashMap<>();
		for(String user : billDetailsTO.getUsers())
		{
			userVsPercentContrib.put(user, 0.00);
		}

		// Compute total bill before tips and taxes so that % contrib can be determined
		double totalBillBeforeTipsAndTaxes = 0.0;
		Set<ItemTO> items = billDetailsTO.getItemsVsParticipants().keySet();

		for(ItemTO item : items)
		{					
			// We dont need any check for tips and taxes here because that item is never added to the map ItemVsParticipants
			totalBillBeforeTipsAndTaxes += item.getAmount();			
		}

		
		boolean isTipsAndTaxesRemoved = false;
		if(billDetailsTO.getItemsVsPerHeadContributions().containsKey(new ItemTO(ITEM_NAME_FOR_TIPS_AND_TAXES, 0.0)))
		{
			// We need to remove tips and taxes, re-calculate per head contrib before proceeding with % calc
			billDetailsTO.getItemsVsPerHeadContributions().remove(new ItemTO(ITEM_NAME_FOR_TIPS_AND_TAXES, 0.0));
			isTipsAndTaxesRemoved = true;
			computePerHeadContributionForTotalBill();
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
			computeTipsAndTaxesAllocation();
		}
						
	}	
	
	/**
	 * Using percentage contrib, allocate tips and taxes among all users
	 * Re-calculate per head total contrib based on this allocation
	 */
	private void computeTipsAndTaxesAllocation()
	{
		ItemTO tipsAndTaxesItem = new ItemTO(ITEM_NAME_FOR_TIPS_AND_TAXES, tipsAndTaxes);
		
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
		
		billDetailsTO.getItemsVsPerHeadContributions().put(tipsAndTaxesItem, userVsTipsAndTaxesContrib);
		
		// Once tips and taxes are calculated, the per head contribution will change. 
		// The percent contrib will not change as it does not include tips and taxes
		computePerHeadContributionForTotalBill();		
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
	public BillDetailsTO deleteItem(String itemName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BillDetailsTO editItem(String oldName, String newName,
			double newAmount) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BillDetailsTO deleteTipsAndTaxes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BillDetailsTO editTipsAndTaxes(double newAmount) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BillDetailsTO editContributionsForItem(String itemName,
			List<Boolean> participants) {
		// TODO Auto-generated method stub
		return null;
	}	
}
