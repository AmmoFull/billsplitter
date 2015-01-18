package com.ammofull.java.billsplitter.rs;

import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.ammofull.java.billsplitter.api.BillSplitterService;
import com.ammofull.java.billsplitter.engine.BillDetailsTO;
import com.ammofull.java.billsplitter.engine.BillSplitterServiceImpl;




@Path("/ammofull/billsplitter")
public class BillSplitterResource {	
	
	@Path("/user/{user}")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)
	public BillDetailsTO addUser(@PathParam("user") String user , BillDetailsTO billDetailsTO)
	{
		BillSplitterService billSplitterService = new BillSplitterServiceImpl();
		return billSplitterService.addUser(user,billDetailsTO);		
	}
	
	@Path("/item/{itemname}/{amount}")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)
	public BillDetailsTO addItem(@PathParam("itemname") String itemName, @PathParam("amount") Double amount, BillDetailsTO billDetailsTO)
	{
		BillSplitterService billSplitterService = new BillSplitterServiceImpl();
		return billSplitterService.addItem(itemName, amount, billDetailsTO);
	}	
	
	@Path("/contribution/{itemname}/")
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces(MediaType.APPLICATION_JSON)
	public BillDetailsTO addContributionsForItem(@PathParam("itemname") String itemName, @QueryParam("user") Set<String> participants, BillDetailsTO billDetailsTO)
	{
		BillSplitterService billSplitterService = new BillSplitterServiceImpl();
		return billSplitterService.addContributionsForItem(itemName, participants, billDetailsTO);
	}	
}
