package com.ammofull.java.billsplitter.engine;

/**
 * Class representing an item. Note that item name has to be unique
 * @author Amod
 *
 */
public class ItemTO {
	
	private String itemName;
	private double amount;
	
	public ItemTO(String itemName, double amount) {
		super();
		this.itemName = itemName;
		this.amount = amount;
	}

	public String getItemName() {
		return itemName;
	}

	public double getAmount() {
		return amount;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((itemName == null) ? 0 : itemName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ItemTO other = (ItemTO) obj;
		if (itemName == null) {
			if (other.itemName != null)
				return false;
		} else if (!itemName.equals(other.itemName))
			return false;
		return true;
	}	
	
	

}
