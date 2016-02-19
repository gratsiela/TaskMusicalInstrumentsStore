import java.awt.List;
import java.awt.Window.Type;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.Box.Filler;

class Store {

	private String name;
	private TreeMap<String, TreeMap<String, Instrument>> catalogue;// type of
																	// instrumen,
																	// name of
																	// instrument,
																	// instrument
	private double money;
	private String orderName;
	private int orderNumber;
	private int numberWhichHasToBeSuppliedWhenAnInstrumentIsNotAvailable=10;

	Store(String name) {
		this.name = name;
		this.catalogue = new TreeMap<String, TreeMap<String, Instrument>>();
	}

	// 1.
	// Продажба на инструмент (по наименование и бройка). Продажбата увеличава
	// парите в
	// наличност на магазина и премахва броят продадени инструменти от наличните
	// в
	// магазина. Продажбата може да се осъществи само ако има достатъчно налични
	// бройки от инструмента. При невъзможност да се продадат инструментите, да
	// се върне
	// подходящо съобщение към потребителя.
	synchronized void sellInstrument(String name, int number) {
			for (Map.Entry<String, TreeMap<String, Instrument>> entry : this.catalogue.entrySet()) {
				if (entry.getValue().containsKey(name)) {
					// while, a не if, защото след като клиентът изчака доставката трябва да му се продаде веднага инструментът в бройката, която е искал. 
					// И продажбата е след while, а не в else, защото ако е в else:
					// Ако няма достатъчна наличност от продукта, текущата нишка ще се блокира 
					// и след нотифицирането й тя ще продължи изпълнението на метода от там, до където е била стигнала преди да бъде блокирана,
					// а това ще я доведе до излизане от if и прескачане на else.
					// Или с други думи ако имаме if(not enough availability) waitSupply(); else sell();, 
					// клиентът ще чака доставката, но няма да му бъде продадена, 
					// а ще дойде нов клиент с друг желан продукт и друго желано количство 
					// и ако от този продукт желаното количество го има в наличност, ще му бъде продадено, 
					// но ако го няма, ще направи поръчка, чиято доставка ще изчака, но няма да му бъде продадена.
					while(entry.getValue().get(name).getAvailability() < number) {// Ако няма такова количество налично от този инструмент, 
						// нотифицира доставчика да достави продукта след като на клиента му се каже "почакай" и после се казва на клиента да почака. 
						// Нотифицирането на другата нишка трябва да стане преди блокирането на текущата, 
						// защото иначе (wait() преди notify()) може да се случи така, че текущата да се блокира, след като преди нея и другата е била блокирана. 
						// И така ще се получи другата нишка да чака текущата да я нотифицира, а текущата, за да нотифицира другата първо трябва да бъде нотифицирана от нея.
						System.out.println(name + ", " + number + " number: Not available!");
						this.orderName = name;
						this.orderNumber = number;
						notify();// нотифицира друга нишка, но тази друга нишка може да run след като текущата wait
						try {
							Random r=new Random();
							int randomHours= r.nextInt(24)+1;
							System.out.println(randomHours+" hours are needed the order to be supplied.");
							Thread.currentThread().sleep(randomHours*100);
						wait();
						} catch (InterruptedException e) {
						}
					}
					// продажба
					entry.getValue().get(name).setAvailability(entry.getValue().get(name).getAvailability() - number);
					this.money += entry.getValue().get(name).getPrice() * number;
					entry.getValue().get(name).setNumberOfSales(entry.getValue().get(name).getNumberOfSales() + number);
					System.out.println(name + ", " + number + " number: Sold!");
				}
			}
	}

	// 2.
	// Приемане на нови инструменти за продажба в магазина (наименование и
	// бройка).
	synchronized void supplyProduct() {
		while (this.orderName == null && this.orderNumber == 0) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		for (Map.Entry<String, TreeMap<String, Instrument>> entry : this.catalogue.entrySet()) {
			if (entry.getValue().containsKey(this.orderName)) {
				entry.getValue().get(this.orderName)
						.setAvailability(entry.getValue().get(this.orderName).getAvailability() + this.orderNumber);
				System.out.println(this.orderName + ", " + this.orderNumber + " number: Supplied!");
				this.orderName = null;
				this.orderNumber = 0;
				break;
			}
		}
		notify();
	}

	void supplyNewProduct(String type, String name, double price, int number) {
		if (number > 0 && price > 0) {
			Instrument newInstrument = new Instrument(name, price, number);
			if (!this.catalogue.containsKey(type)) {
				this.catalogue.put(type, new TreeMap<String, Instrument>());
			}
			this.catalogue.get(type).put(newInstrument.getName(), newInstrument);
		}
	}

	synchronized void supplyProducts() {
		if (getListOfNotAvailableInstruments().size() == 0) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		for (Map.Entry<String, TreeMap<String, Instrument>> entry1 : this.catalogue.entrySet()) {
			for (Map.Entry<String, Instrument> entry2 : entry1.getValue().entrySet()) {
				if (getListOfNotAvailableInstruments().contains(entry2.getValue().getName())) {
					entry2.getValue().setAvailability(entry2.getValue().getAvailability()
							+ this.numberWhichHasToBeSuppliedWhenAnInstrumentIsNotAvailable);
				}
			}
		}
		notify();
		System.out.println("All not available instruments are supplied in the store!");
	}

	private TreeSet<String> getListOfNotAvailableInstruments() {
		TreeSet<String> listOfNotAvailableInstruments = new TreeSet<String>();
		for (Map.Entry<String, TreeMap<String, Instrument>> entry1 : this.catalogue.entrySet()) {
			for (Map.Entry<String, Instrument> entry2 : entry1.getValue().entrySet()) {
				if (entry2.getValue().getAvailability() == 0) {
					listOfNotAvailableInstruments.add(entry2.getValue().getName());
				}
			}
		}
		return listOfNotAvailableInstruments;
	}

	// 3.
	// Изготвяне на каталог на инструментите в магазина:
	// ◦ списък с инструментите, подредени по вид
	// ◦ списък с инструментите, подредени по наименование в азбучен ред
	// ◦ списък с инструментите, подредени по цена (възможност за избор на
	// възходящо
	// или низходящо подреждане)
	// ◦ списък с инструментите, които са налични в магазина в момента
	synchronized void createListSortedByName() {
		TreeSet<Instrument> listSortedByName = new TreeSet<Instrument>(new Comparator<Instrument>() {
			@Override
			public int compare(Instrument o1, Instrument o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		fillTreeSet(listSortedByName);
		System.out.println("CATALOGUE OF INSTRUMENTS SORTED BY NAME");
		printTreeSet(listSortedByName);
	}

	public enum TypeOfOrder {
		ASCENDING, DESCENDING
	};

	synchronized void createListSortedByPrice(TypeOfOrder typeOfOrder) {
		TreeSet<Instrument> listSortedByPrice = new TreeSet<Instrument>(new Comparator<Instrument>() {
			@Override
			public int compare(Instrument o1, Instrument o2) {
				if (o1.getPrice().compareTo(o2.getPrice()) == 0) {
					return o1.getName().compareTo(o2.getName());
				}
				if (typeOfOrder.equals(TypeOfOrder.ASCENDING)) {
					return o1.getPrice().compareTo(o2.getPrice());
				}
				return o2.getPrice().compareTo(o1.getPrice());
			}
		});
		fillTreeSet(listSortedByPrice);
		System.out.println("CATALOGUE OF INSTRUMENTS SORTED BY " + typeOfOrder);
		printTreeSet(listSortedByPrice);
	}

	private void fillTreeSet(TreeSet<Instrument> catalogue) {
		for (Map.Entry<String, TreeMap<String, Instrument>> entry1 : this.catalogue.entrySet()) {
			for (Map.Entry<String, Instrument> entry2 : entry1.getValue().entrySet()) {
				catalogue.add(entry2.getValue());
			}
		}
	}

	private void printTreeSet(TreeSet<Instrument> list) {
		for (Instrument i : list) {
			System.out.println(i.getName() + ", price: " + i.getPrice() + ", availability: " + i.getAvailability());
		}
	}

	synchronized void createListSortedByType() {
		System.out.println("CATALOGUE OF INSTRUMENTS SORTED BY TYPE");
		printTreeMap(this.catalogue);
	}

	synchronized void createListByAvailability() {
		TreeMap<String, TreeMap<String, Instrument>> listByAvailability = new TreeMap<String, TreeMap<String, Instrument>>();
		for (Map.Entry<String, TreeMap<String, Instrument>> entry1 : this.catalogue.entrySet()) {
			for (Map.Entry<String, Instrument> entry2 : entry1.getValue().entrySet()) {
				if (entry2.getValue().getAvailability() > 0) {
					if (!listByAvailability.containsKey(entry1.getKey())) {
						listByAvailability.put(entry1.getKey(), new TreeMap<String, Instrument>());
					}
					listByAvailability.get(entry1.getKey()).put(entry2.getKey(), entry2.getValue());
				}
			}
		}
		System.out.println("CTALOGUE OF AVAILABLE INSTRUMENTS");
		printTreeMap(listByAvailability);
	}

	private void printTreeMap(TreeMap<String, TreeMap<String, Instrument>> map) {
		for (Map.Entry<String, TreeMap<String, Instrument>> entry1 : map.entrySet()) {
			System.out.println(entry1.getKey() + ": ");
			for (Map.Entry<String, Instrument> entry2 : entry1.getValue().entrySet()) {
				System.out.println(entry2.getKey() + ", price: " + entry2.getValue().getPrice() + ", availability: "
						+ entry2.getValue().getAvailability());
			}
		}
	}

	// 4.
	// Изготвяне на справки за работата на магазина
	// ◦генериране на списък с продадени инструменти, подредени по брой продажби
	// ◦генериране на обща сума, получена при продажба на инструменти
	// ◦актуална информация за най-продаван инструмент (като бройка спрямо
	// останалите)
	// ◦актуална информация за най-непродаван инструмент
	// ◦актуална информация за вид инструменти, които най-много се харчат (с
	// най-голяма обща бройка продадени от този вид)
	// ◦актуална информация за вид инструменти, от които магазинът има най-голям
	// приход (с най-голяма обща сума от продажбите)

	synchronized void makeInquireSoldInstrumentsSortedByNumberOfSales() {
		TreeSet<Instrument> instrumentsSortedByNumberOfSales = sortInstrumentsByNumberOfSalles();
		System.out.println("\nINQUIRE: INSTRUMENTS SORTED BY NUMBER OF SALES");
		for (Instrument i : instrumentsSortedByNumberOfSales) {
			System.out.println(i.getName() + ", price: " + i.getPrice() + ", number of sales: " + i.getNumberOfSales());
		}
		System.out.println();
	}

	synchronized private TreeSet<Instrument> sortInstrumentsByNumberOfSalles() {
		TreeSet<Instrument> instrumentsSortedByNumberOfSales = new TreeSet<Instrument>(new Comparator<Instrument>() {
			@Override
			public int compare(Instrument o1, Instrument o2) {
				if (o1.getNumberOfSales().compareTo(o2.getNumberOfSales()) == 0) {
					return o1.getName().compareTo(o2.getName());
				}
				return o2.getNumberOfSales().compareTo(o1.getNumberOfSales());
			}
		});
		fillTreeSet(instrumentsSortedByNumberOfSales);
		return instrumentsSortedByNumberOfSales;
	}

	synchronized void makeInquireTotalSumOfSoldInstrumnts() {
		int totalSum = 0;
		for (Map.Entry<String, TreeMap<String, Instrument>> entry1 : this.catalogue.entrySet()) {
			for (Map.Entry<String, Instrument> entry2 : entry1.getValue().entrySet()) {
				totalSum += entry2.getValue().getNumberOfSales() * entry2.getValue().getPrice();
			}
		}
		System.out.println("\nTOTAL SUM OF SOLD INSTRMENTS: " + totalSum);
	}

	synchronized void makeInquireBestSellerInstrument() {
		Integer maxSales = 0;
		Instrument test = null;
		int numberOfBestSellers = 0;
		for (Instrument i : sortInstrumentsByNumberOfSalles()) {
			if (i.getNumberOfSales() > maxSales) {
				maxSales = i.getNumberOfSales();
				test = i;
				numberOfBestSellers = 1;
			} else if (i.getNumberOfSales() == maxSales) {
				numberOfBestSellers++;
			}
		}
		System.out.print("\nBESTSELLER INSTRUMENT: ");
		if (maxSales == 0) {
			System.out.println("no sold instruments");
		} else if (numberOfBestSellers > 1) {
			System.out.println("there is more than one bestsellers");
		} else {
			System.out.println(
					test.getName() + ", price: " + test.getPrice() + ", number of sales: " + test.getNumberOfSales());
		}
		System.out.println();
	}

	synchronized void makeInquireWorstSellerInstrument() {
		Integer minSales = 0;
		Instrument test = null;
		int numberOfWorstSellers = 0;
		for (Instrument i : sortInstrumentsByNumberOfSalles()) {
			minSales = i.getNumberOfSales();
			test = i;
			numberOfWorstSellers = 1;
			break;
		}
		for (Instrument i : sortInstrumentsByNumberOfSalles()) {
			if (i.getNumberOfSales() < minSales) {
				minSales = i.getNumberOfSales();
				test = i;
				numberOfWorstSellers = 1;
			} else if (i.getNumberOfSales() == minSales) {
				numberOfWorstSellers++;
			}
		}
		System.out.print("\nWORSTSELLER INSTRUMENT: ");
		if (numberOfWorstSellers == 0) {
			System.out.println("no sold instruments");
		} else if (numberOfWorstSellers > 1) {
			System.out.println("there is more than one worstseller");
		} else {
			System.out.println(
					test.getName() + ", price: " + test.getPrice() + ", number of sales: " + test.getNumberOfSales());
		}
		System.out.println();
	}

	synchronized void makeInquireBestSellerType() {
		int maxNumberOfSales = 0;
		String bestSellerType = null;
		int numberOfBestSellerTypes = 0;
		for (Map.Entry<String, TreeMap<String, Instrument>> entry1 : this.catalogue.entrySet()) {
			int numberOfSalesFromThisType = 0;
			for (Map.Entry<String, Instrument> entry2 : entry1.getValue().entrySet()) {
				numberOfSalesFromThisType += entry2.getValue().getNumberOfSales();
			}
			if (numberOfSalesFromThisType > maxNumberOfSales) {
				maxNumberOfSales = numberOfSalesFromThisType;
				bestSellerType = entry1.getKey();
				numberOfBestSellerTypes = 1;
			} else if (numberOfSalesFromThisType == maxNumberOfSales && maxNumberOfSales != 0) {
				numberOfBestSellerTypes++;
			}
		}
		System.out.print("\nBESTSELLER TYPE: ");
		if (numberOfBestSellerTypes == 0) {
			System.out.println("no sold instruments");
		} else if (numberOfBestSellerTypes > 1) {
			System.out.println("there is more than one bestseller type");
		} else {
			System.out.println(bestSellerType);
		}
		System.out.println();
	}

	synchronized void makeInquireHigherTotalSumOfSalesOfType() {
		int higherTotalSum = 0;
		String typeWithHigherTotalSumOfSales = null;
		int numberOfTypesWithHigherTotalSumOfSales = 0;
		for (Map.Entry<String, TreeMap<String, Instrument>> entry1 : this.catalogue.entrySet()) {
			int totalSumOfThisType = 0;
			for (Map.Entry<String, Instrument> entry2 : entry1.getValue().entrySet()) {
				totalSumOfThisType += entry2.getValue().getNumberOfSales() * entry2.getValue().getPrice();
			}
			if (totalSumOfThisType > higherTotalSum) {
				higherTotalSum = totalSumOfThisType;
				typeWithHigherTotalSumOfSales = entry1.getKey();
				numberOfTypesWithHigherTotalSumOfSales = 1;
			} else if (totalSumOfThisType == higherTotalSum && higherTotalSum != 0) {
				numberOfTypesWithHigherTotalSumOfSales++;
			}
		}
		System.out.print("\nTYPE WITH HIGHER TOTAL SUM OF SALES: ");
		if (numberOfTypesWithHigherTotalSumOfSales == 0) {
			System.out.println("no sold instruments");
		} else if (numberOfTypesWithHigherTotalSumOfSales > 1) {
			System.out.println("there is more than one bestseller type");
		} else {
			System.out.println(typeWithHigherTotalSumOfSales);
		}
		System.out.println();
	}
}
