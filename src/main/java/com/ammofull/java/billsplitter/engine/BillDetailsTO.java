package com.ammofull.java.billsplitter.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * All the details of the current bill
 * @author Amod
 *
 */
public class BillDetailsTO {
	
	//////////////////////////
	/// USER ENTERED DATA ///
	////////////////////////
	
	/**
	 * Participants in this bill
	 */
	private Set<String> users;
	
	/**
	 * Key: 	ItemTO
	 * Value:	Map of user name versus a set of participants for the items. If user name is present in the set, the
	 * 			user is a participant in the item. Else not.
	 */
	private Map<ItemTO, Set<String>> itemsVsParticipants;
	
	////////////////////////
	/// CALCULATED DATA ///
	//////////////////////
	
	/**
	 * Key: 	ItemTO
	 * Value:	Map of user name vs a double representing the user's $ contribution in the item
	 */
	private Map<ItemTO, Map<String,Double>> itemsVsPerHeadContributions;
	
	
	
	/**
	 * The list represents the percentage contribution for each user. It is used to allocate tips and taxes accordingly
	 */
	private Map<String,Double> userVsPercentageContribInTheBill;
	
	/**
	 * Participant name vs. total contribution in the bill. This is the final figure for users
	 */
	private Map<String,Double> userVsTotalPerHeadContribution;
	
	/**
	 * Instantiates all the above data structures
	 */
	public BillDetailsTO() {
		users = new HashSet<>();		
		itemsVsPerHeadContributions = new HashMap<>();
		userVsTotalPerHeadContribution = new HashMap<>();
		itemsVsParticipants = new HashMap<>();
		userVsPercentageContribInTheBill = new HashMap<>();		
	}
	
	public Set<String> getUsers() {
		return users;
	}


	public void setUsers(Set<String> users) {
		this.users = users;
	}

	public Map<ItemTO, Set<String>> getItemsVsParticipants() {
		return itemsVsParticipants;
	}

	public void setItemsVsParticipants(Map<ItemTO, Set<String>> itemsVsParticipants) {
		this.itemsVsParticipants = itemsVsParticipants;
	}

	public Map<ItemTO, Map<String, Double>> getItemsVsPerHeadContributions() {
		return itemsVsPerHeadContributions;
	}

	public void setItemsVsPerHeadContributions(
			Map<ItemTO, Map<String, Double>> itemsVsPerHeadContributions) {
		this.itemsVsPerHeadContributions = itemsVsPerHeadContributions;
	}

	public Map<String, Double> getUserVsPercentageContribInTheBill() {
		return userVsPercentageContribInTheBill;
	}

	public void setUserVsPercentageContribInTheBill(Map<String, Double> userVsPercentageContribInTheBill) {
		this.userVsPercentageContribInTheBill = userVsPercentageContribInTheBill;
	}

	public Map<String, Double> getUserVsTotalPerHeadContribution() {
		return userVsTotalPerHeadContribution;
	}

	public void setUserVsTotalPerHeadContribution(
			Map<String, Double> userVsTotalPerHeadContribution) {
		this.userVsTotalPerHeadContribution = userVsTotalPerHeadContribution;
	}
		
}
