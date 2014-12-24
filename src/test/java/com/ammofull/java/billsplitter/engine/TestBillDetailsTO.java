package com.ammofull.java.billsplitter.engine;

import com.ammofull.java.billsplitter.engine.BillDetailsTO;

import junit.framework.TestCase;

public class TestBillDetailsTO extends TestCase {
	
	BillDetailsTO billDetailsTO;
	
	public void setUp()
	{
		billDetailsTO = new BillDetailsTO();
	}
	
	public void tearDown()
	{
		billDetailsTO = null;
	}
	
	public void testConstructor()
	{
		assertNotNull(billDetailsTO.getItemsVsParticipants());
		assertNotNull(billDetailsTO.getItemsVsPerHeadContributions());
		assertNotNull(billDetailsTO.getUsers());
		assertNotNull(billDetailsTO.getUserVsPercentageContribInTheBill());  
		assertNotNull(billDetailsTO.getUserVsTotalPerHeadContribution());		
	}
}
