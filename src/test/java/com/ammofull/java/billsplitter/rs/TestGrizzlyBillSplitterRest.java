package com.ammofull.java.billsplitter.rs;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import junit.framework.TestCase;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ammofull.java.billsplitter.engine.BillDetailsTO;


public class TestGrizzlyBillSplitterRest extends TestCase {
	private HttpServer server;
	private WebTarget target;

	@Before
	public void setUp() throws Exception {
		// start the server
		server = BillSplitterRestMain.startServer();
		// create the client
		Client c = ClientBuilder.newClient();        

		target = c.target(BillSplitterRestMain.BASE_URI);
	}

	@After
	public void tearDown() throws Exception {
		server.stop();
	}

	/**
	 * Add 2 users
	 * @throws IOException 
	 */
	@Test
	public void testAddUserResource() 
	{
		BillDetailsTO billDetailsTO = null;	    	
		Response responseMsg = target
				.path("/ammofull/billsplitter/user/amod")
				.request()
				.post(Entity.entity(billDetailsTO, MediaType.APPLICATION_JSON));

		billDetailsTO  = responseMsg.readEntity(BillDetailsTO.class);

		responseMsg = target
				.path("/ammofull/billsplitter/user/ajay")
				.request()
				.post(Entity.entity(billDetailsTO, MediaType.APPLICATION_JSON));
		billDetailsTO  = responseMsg.readEntity(BillDetailsTO.class);

		assertEquals(2,billDetailsTO.getUsers().size());
		assertTrue(billDetailsTO.getUsers().contains("amod"));
		assertTrue(billDetailsTO.getUsers().contains("ajay"));
	}

	/**
	 * Add 2 items
	 * @throws IOException 
	 */
	@Test
	public void testAddItemResource() 
	{

		BillDetailsTO billDetailsTO = null;
		Response responseMsg = target
				.path("/ammofull/billsplitter/item/steak/25")
				.request()
				.post(Entity.entity(billDetailsTO, MediaType.APPLICATION_JSON));

		billDetailsTO  = responseMsg.readEntity(BillDetailsTO.class);

		responseMsg = target
				.path("/ammofull/billsplitter/item/sushi/15.2")
				.request()
				.post(Entity.entity(billDetailsTO, MediaType.APPLICATION_JSON));
		
		billDetailsTO  = responseMsg.readEntity(BillDetailsTO.class);		

		Set<String> items = billDetailsTO.getItemVsAmount().keySet();
		assertEquals(2,items.size());
		assertTrue(items.contains("steak"));
		assertTrue(items.contains("sushi"));
	}		
	
	@Test
	public void testAddContributorsResource()
	{
		BillDetailsTO billDetailsTO = null;
		Response responseMsg = target
				.path("/ammofull/billsplitter/item/steak/25")
				.request()
				.post(Entity.entity(billDetailsTO, MediaType.APPLICATION_JSON));

		billDetailsTO  = responseMsg.readEntity(BillDetailsTO.class);

		responseMsg = target
				.path("/ammofull/billsplitter/contribution/steak")
				.queryParam("user", "Amod")
				.queryParam("user", "Ajay")
				.queryParam("user", "Paresh")
				.request()
				.post(Entity.entity(billDetailsTO, MediaType.APPLICATION_JSON));
		
		billDetailsTO  = responseMsg.readEntity(BillDetailsTO.class);		

		Map<String, Set<String>> itemNameVsContributions = billDetailsTO.getItemsVsParticipants();
		assertEquals(1,itemNameVsContributions.size());
		assertTrue(itemNameVsContributions.containsKey("steak"));
		Set<String> contributors = itemNameVsContributions.get("steak");
		assertEquals(3,contributors.size());
		assertTrue(contributors.contains("Amod"));
		assertTrue(contributors.contains("Ajay"));
		assertTrue(contributors.contains("Paresh"));
	}
}
