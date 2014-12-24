package com.ammofull.java.billsplitter.engine;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ammofull.java.billsplitter.engine.BillDetailsTO;
import com.ammofull.java.billsplitter.engine.BillSplitterServiceImpl;
import com.ammofull.java.billsplitter.engine.ItemTO;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestBillSplitterServiceImpl extends TestCase {
	
	private BillSplitterServiceImpl billSplitterServiceImpl;
	
	public TestBillSplitterServiceImpl(String testName)
	{
		super(testName);
	}
	
	public static Test suite()
	{
		return new TestSuite(TestBillSplitterServiceImpl.class);
	}
	
	public void setUp()
	{
		billSplitterServiceImpl = new BillSplitterServiceImpl();
	}
	
	public void tearDown()
	{
		billSplitterServiceImpl = null;
	}
	
	///////////////
	// ADD USER //
	/////////////
	
	public void testAddUserWhenNameIsInvalid()
	{
		try
		{
			billSplitterServiceImpl.addUser("");
			fail("Exception was expected for empty name");
		}
		catch(IllegalArgumentException e)
		{
			assertTrue(true);
		}
		
		try
		{
			billSplitterServiceImpl.addUser(null);
			fail("Exception was expected for null name");
		}
		catch(IllegalArgumentException e)
		{
			assertTrue(true);
		}
		
		try
		{
			billSplitterServiceImpl.addUser(BillSplitterServiceImpl.ITEM_NAME_FOR_TIPS_AND_TAXES);
			fail("Exception was expected for reserved name");
		}
		catch(IllegalArgumentException e)
		{
			assertTrue(true);
		}
		
		try
		{
			billSplitterServiceImpl.addUser("User");
			billSplitterServiceImpl.addUser("User");
			fail("Exception was expected for name already present");
		}
		catch(IllegalArgumentException e)
		{
			assertTrue(true);
		}
	}
	
	public void testAddUserWhenNoItems()
	{
		// Given
		String user1 = "User1";
		String user2 = "User2";
		
		// When
		BillDetailsTO billDetailsTO = billSplitterServiceImpl.addUser(user1);
		billDetailsTO = billSplitterServiceImpl.addUser(user2);
		
		// Then
		Set<String> users = billDetailsTO.getUsers();
		assertEquals(2,users.size());
		assertTrue(users.contains(user1));
		assertTrue(users.contains(user2));
		
		assertTrue(billDetailsTO.getItemsVsParticipants().isEmpty());
		assertTrue(billDetailsTO.getItemsVsPerHeadContributions().isEmpty());
		assertEquals(2,billDetailsTO.getUserVsPercentageContribInTheBill().size());
		assertEquals(2,billDetailsTO.getUserVsTotalPerHeadContribution().size());
		
		Map<String,Double> userVsPercentageContrib = billDetailsTO.getUserVsPercentageContribInTheBill();
		assertEquals(0.00,userVsPercentageContrib.get(user1).doubleValue());
		assertEquals(0.00,userVsPercentageContrib.get(user2).doubleValue());
		
		Map<String,Double> userVsTotalContribution = billDetailsTO.getUserVsTotalPerHeadContribution();
		assertEquals(0.0,userVsTotalContribution.get(user1));
		assertEquals(0.0,userVsTotalContribution.get(user2));		
	}
	
	///////////////
	// ADD ITEM //
	/////////////
	public void testAddItemInvalidName()
	{
		try
		{
			billSplitterServiceImpl.addItem("",10D);
			fail("Exception was expected for empty name");
		}
		catch(IllegalArgumentException e)
		{
			assertTrue(true);
		}
		
		try
		{
			billSplitterServiceImpl.addItem(null,10D);
			fail("Exception was expected for null name");
		}
		catch(IllegalArgumentException e)
		{
			assertTrue(true);
		}
		
		try
		{
			billSplitterServiceImpl.addItem("Item",null);
			fail("Exception was expected for null amount");
		}
		catch(IllegalArgumentException e)
		{
			assertTrue(true);
		}
		
		try
		{
			billSplitterServiceImpl.addItem(BillSplitterServiceImpl.ITEM_NAME_FOR_TIPS_AND_TAXES,10D);
			fail("Exception was expected for reserved name");
		}
		catch(IllegalArgumentException e)
		{
			assertTrue(true);
		}	
		
		try
		{
			billSplitterServiceImpl.addItem("Item",1D);
			billSplitterServiceImpl.addItem("Item",2D);
			fail("Exception was expected for name already present");
		}
		catch(IllegalArgumentException e)
		{
			assertTrue(true);
		}
		
		try
		{
			billSplitterServiceImpl.addItem("Item",0.0);
			fail("Exception was expected for zero amount");
		}
		catch(IllegalArgumentException e)
		{
			assertTrue(true);
		}
	}
	
	public void testAddItemWhenNoUser()
	{
		// When
		BillDetailsTO billDetailsTO = billSplitterServiceImpl.addItem("Item1", 10.0);
		billDetailsTO = billSplitterServiceImpl.addItem("Item2", 20.0);
		
		// Then
		assertTrue(billDetailsTO.getUserVsPercentageContribInTheBill().isEmpty());
		assertTrue(billDetailsTO.getUserVsTotalPerHeadContribution().isEmpty());
		
		Map<ItemTO,Set<String>> itemVsParticipants = billDetailsTO.getItemsVsParticipants();
		assertEquals(2,itemVsParticipants.size());
		assertTrue(itemVsParticipants.get(new ItemTO("Item1", 1D)).isEmpty());
		assertTrue(itemVsParticipants.get(new ItemTO("Item2", 1D)).isEmpty());
		
		Map<ItemTO,Map<String,Double>> itemVsContributions = billDetailsTO.getItemsVsPerHeadContributions();
		assertEquals(2,itemVsContributions.size());
		assertTrue(itemVsContributions.get(new ItemTO("Item1",1D)).isEmpty());
		assertTrue(itemVsContributions.get(new ItemTO("Item2",1D)).isEmpty());
	}
	
	////////////////////////////////
	// ADD CONTRIBUTION FOR ITEM //
	//////////////////////////////
	public void testAddContributionsInvalidArguments()
	{
		Set<String> participants = new HashSet<>();
		participants.add("User");
		try
		{
			billSplitterServiceImpl.addContributionsForItem(null, participants);
			fail("Exception expected for null item name");
		}
		catch(IllegalArgumentException e)
		{
			assertTrue(true);
		}
		
		try
		{
			billSplitterServiceImpl.addContributionsForItem("Item", null);
			fail("Exception expected for null participants");
		}
		catch(IllegalArgumentException e)
		{
			assertTrue(true);
		}
		
		try
		{
			Set<String> empty = new HashSet<>();
			billSplitterServiceImpl.addContributionsForItem("Item",empty);
			fail("Exception expected for empty participants");
		}
		catch(IllegalArgumentException e)
		{
			assertTrue(true);
		}
		
		try
		{
			billSplitterServiceImpl.addContributionsForItem(BillSplitterServiceImpl.ITEM_NAME_FOR_TIPS_AND_TAXES, participants);
			fail("Exception expected for reserved name");
		}
		catch(IllegalArgumentException e)
		{
			assertTrue(true);
		}		
	}
	
	public void testAddParticipantsWhenItemIsNotPresent()
	{
		// Given
		billSplitterServiceImpl.addUser("User1");
		Set<String> participants = new HashSet<>();
		participants.add("User1");
		
		// When
		try 
		{
			billSplitterServiceImpl.addContributionsForItem("Item1", participants);	
			fail("Exception expected for item not present");			
		}
		catch(IllegalArgumentException e)
		{
			assertTrue(true);
		}
		
	}
	
	public void testAddParticipantsWhenUserIsNotPresent()
	{
		// Given
		billSplitterServiceImpl.addItem("Item1",10.0);
		Set<String> participants = new HashSet<>();
		participants.add("User1");

		// When
		BillDetailsTO billDetailsTO = billSplitterServiceImpl.addContributionsForItem("Item1", participants);
		
		// Then
		assertTrue(billDetailsTO.getUsers().contains("User1"));
		
	}
	
	public void testAddParticipantsAllGood()
	{
		// Given
		String user1 = "User1";
		String user2 = "User2";
		Set<String> participants = new HashSet<>();
		participants.add(user1); participants.add(user2);
		
		String item1 = "Item1";
		Double amount = 10.0;
		
		billSplitterServiceImpl.addItem(item1, amount);
		
		// When
		BillDetailsTO billDetailsTO = billSplitterServiceImpl.addContributionsForItem(item1, participants);
		
		// Then
		assertTrue(billDetailsTO.getUsers().contains(user1));
		assertTrue(billDetailsTO.getUsers().contains(user2));
		
		Map<ItemTO,Set<String>> itemVsParticipants = billDetailsTO.getItemsVsParticipants();
		assertEquals(1,itemVsParticipants.size());
		Set<String> actualParticipants = itemVsParticipants.get(new ItemTO(item1, amount));
		assertEquals(2,actualParticipants.size());
		assertTrue(actualParticipants.contains(user1));
		assertTrue(actualParticipants.contains(user2));
		
		Map<ItemTO,Map<String,Double>> itemVsPerHeadContribution = billDetailsTO.getItemsVsPerHeadContributions();
		assertEquals(1, itemVsPerHeadContribution.size());
		Map<String,Double> userVsContribution = itemVsPerHeadContribution.get(new ItemTO(item1, amount));
		assertEquals(2,userVsContribution.size());
		assertEquals(amount/2.0,userVsContribution.get(user1));
		assertEquals(amount/2.0,userVsContribution.get(user2));	
		
		Map<String,Double> userVsPercentageContrib = billDetailsTO.getUserVsPercentageContribInTheBill();
		assertEquals(2,userVsPercentageContrib.size());
		assertEquals(50.00,userVsPercentageContrib.get(user1).doubleValue());
		assertEquals(50.00,userVsPercentageContrib.get(user2).doubleValue());
		
		Map<String,Double> userVsPerHeadTotal = billDetailsTO.getUserVsTotalPerHeadContribution();
		assertEquals(2,userVsPerHeadTotal.size());
		assertEquals(amount/2.0,userVsPerHeadTotal.get(user1));
		assertEquals(amount/2.0,userVsPerHeadTotal.get(user2));
	}
	
	/////////////////////////
	// ADD TIPS AND TAXES //
	///////////////////////
	public void testAddTipsAndTaxesInvalidArguments()
	{
		try
		{
			billSplitterServiceImpl.addTipsAndTaxes(null);
			fail("Exception expected for null amount");
		}
		catch(IllegalArgumentException e)
		{
			assertTrue(true);
		}
	}	
	
	public void testAddTipsAndTaxesWithoutUsers()
	{
		// Given
		billSplitterServiceImpl.addItem("Item", 10.0);
		
		// When
		BillDetailsTO billDetailsTO = billSplitterServiceImpl.addTipsAndTaxes(1.2);
		
		// Then
		assertEquals(1,billDetailsTO.getItemsVsPerHeadContributions().size());
		assertTrue(billDetailsTO.getItemsVsPerHeadContributions().containsKey(new ItemTO("Item",0.0)));		
		
	}
	
	public void testAddTipsAndTaxesWithoutItems()
	{
		// Given
		billSplitterServiceImpl.addUser("User1");

		// When
		BillDetailsTO billDetailsTO = billSplitterServiceImpl.addTipsAndTaxes(1.2);

		// Then
		assertTrue(billDetailsTO.getItemsVsPerHeadContributions().isEmpty());
		assertEquals(0.0, billDetailsTO.getUserVsTotalPerHeadContribution().get("User1"));
	}
	
	public void testAddTipsAndTaxesWithoutContributions()
	{
		// Given
		billSplitterServiceImpl.addUser("User1");
		billSplitterServiceImpl.addItem("Item1", 10.0);

		// When
		BillDetailsTO billDetailsTO = billSplitterServiceImpl.addTipsAndTaxes(1.2);

		// Then
		assertEquals(2,billDetailsTO.getItemsVsPerHeadContributions().size());
		assertEquals(0.0, billDetailsTO.getUserVsTotalPerHeadContribution().get("User1"));
	}
	
	public void testAddTipsAndTaxesAllGoodCase()
	{		
		// Given
		String user1 = "User1";
		String user2 = "User2";
		Set<String> participants = new HashSet<>();
		participants.add(user1); participants.add(user2);
		
		String item1 = "Item1";
		Double amount = 10.0;
						
		billSplitterServiceImpl.addUser(user1);
		billSplitterServiceImpl.addUser(user2);
		billSplitterServiceImpl.addItem(item1, amount);
		billSplitterServiceImpl.addContributionsForItem(item1, participants);
		
		Double expectedPerHeadContribution = (amount / participants.size()) + (1.2 * (amount / participants.size())/amount);

		// When
		BillDetailsTO billDetailsTO = billSplitterServiceImpl.addTipsAndTaxes(1.2);

		// Then
		Map<String,Double> userVsTotalBill = billDetailsTO.getUserVsTotalPerHeadContribution();
		assertEquals(expectedPerHeadContribution, userVsTotalBill.get(user1));
		assertEquals(expectedPerHeadContribution, userVsTotalBill.get(user2));		
	}
	
	///////////////////////////////
	// TEST DIFFERENT SEQUENCES //
	/////////////////////////////
	public void test_AddUsers_AddItems_AddContribs_AddTips()
	{
		// Given
		String user1 = "User1";
		String user2 = "User2";
		String user3 = "User3";
		
		String item1 = "Item1";
		String item2 = "Item2";
		String item3 = "Item3";
		
		Double amount1 = 10.0;
		Double amount2 = 20.0;
		Double amount3 = 30.0;
		
		Set<String> participants1 = new HashSet<>();
		participants1.add(user1); participants1.add(user2);
		
		Set<String> participants2 = new HashSet<>();
		participants2.add(user2); participants2.add(user3);
		
		Set<String> participants3 = new HashSet<>();
		participants3.add(user3);
		
		Double tip = 25.0;
		
		// When
		billSplitterServiceImpl.addUser(user1);
		billSplitterServiceImpl.addUser(user2);
		billSplitterServiceImpl.addUser(user3);
		billSplitterServiceImpl.addItem(item1, amount1);
		billSplitterServiceImpl.addItem(item2, amount2);
		billSplitterServiceImpl.addItem(item3, amount3);
		billSplitterServiceImpl.addContributionsForItem(item1, participants1);
		billSplitterServiceImpl.addContributionsForItem(item2, participants2);
		billSplitterServiceImpl.addContributionsForItem(item3, participants3);
		BillDetailsTO billDetailsTO = billSplitterServiceImpl.addTipsAndTaxes(tip);
		
		// Then
		Map<String,Double> userVsTotalContrib = billDetailsTO.getUserVsTotalPerHeadContribution();
		assertEquals(7.08,userVsTotalContrib.get(user1));
		assertEquals(21.25,userVsTotalContrib.get(user2));
		assertEquals(56.66,userVsTotalContrib.get(user3));
	}
	
	public void test_AddTips_AddItems_AddContribs()
	{
		// Given
		String user1 = "User1";
		String user2 = "User2";
		String user3 = "User3";

		String item1 = "Item1";
		String item2 = "Item2";
		String item3 = "Item3";

		Double amount1 = 10.0;
		Double amount2 = 20.0;
		Double amount3 = 30.0;

		Set<String> participants1 = new HashSet<>();
		participants1.add(user1); participants1.add(user2);

		Set<String> participants2 = new HashSet<>();
		participants2.add(user2); participants2.add(user3);

		Set<String> participants3 = new HashSet<>();
		participants3.add(user3);

		Double tip = 25.0;

		// When
		billSplitterServiceImpl.addTipsAndTaxes(tip);
		billSplitterServiceImpl.addItem(item1, amount1);
		billSplitterServiceImpl.addItem(item2, amount2);
		billSplitterServiceImpl.addItem(item3, amount3);
		billSplitterServiceImpl.addContributionsForItem(item1, participants1);
		billSplitterServiceImpl.addContributionsForItem(item2, participants2);
		BillDetailsTO billDetailsTO = billSplitterServiceImpl.addContributionsForItem(item3, participants3);
		

		// Then
		Map<String,Double> userVsTotalContrib = billDetailsTO.getUserVsTotalPerHeadContribution();
		assertEquals(7.08,userVsTotalContrib.get(user1));
		assertEquals(21.25,userVsTotalContrib.get(user2));
		assertEquals(56.66,userVsTotalContrib.get(user3));
	}
	
	public void test_AddTips_AddItems_AddContribs_EditTips()
	{
		// Given
		String user1 = "User1";
		String user2 = "User2";
		String user3 = "User3";

		String item1 = "Item1";
		String item2 = "Item2";
		String item3 = "Item3";

		Double amount1 = 10.0;
		Double amount2 = 20.0;
		Double amount3 = 30.0;

		Set<String> participants1 = new HashSet<>();
		participants1.add(user1); participants1.add(user2);

		Set<String> participants2 = new HashSet<>();
		participants2.add(user2); participants2.add(user3);

		Set<String> participants3 = new HashSet<>();
		participants3.add(user3);

		Double tip1 = 25.0;
		Double tip2 = 60.0;

		// When
		billSplitterServiceImpl.addTipsAndTaxes(tip2);
		billSplitterServiceImpl.addItem(item1, amount1);
		billSplitterServiceImpl.addItem(item2, amount2);
		billSplitterServiceImpl.addItem(item3, amount3);
		billSplitterServiceImpl.addContributionsForItem(item1, participants1);
		billSplitterServiceImpl.addContributionsForItem(item2, participants2);
		billSplitterServiceImpl.addContributionsForItem(item3, participants3);
		BillDetailsTO billDetailsTO = billSplitterServiceImpl.addTipsAndTaxes(tip1);

		// Then
		Map<String,Double> userVsTotalContrib = billDetailsTO.getUserVsTotalPerHeadContribution();
		assertEquals(7.08,userVsTotalContrib.get(user1));
		assertEquals(21.25,userVsTotalContrib.get(user2));
		assertEquals(56.66,userVsTotalContrib.get(user3));
	}
	

}
