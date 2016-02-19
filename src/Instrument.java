
class Instrument {

	private String name;
	private Double price;
	private int availability;
	private Integer numberOfSales;

	Instrument(String name, double price, int availability) {
		this.name = name;
		this.price = price;
		this.availability = availability;
		this.numberOfSales = 0;
	}

	public String getName() {
		return name;
	}

	public Double getPrice() {
		return price;
	}

	public int getAvailability() {
		return availability;
	}

	public void setAvailability(int availability) {
		this.availability = availability;
	}

	public Integer getNumberOfSales() {
		return numberOfSales;
	}

	public void setNumberOfSales(int numberOfSales) {
		this.numberOfSales = numberOfSales;
	}
}
