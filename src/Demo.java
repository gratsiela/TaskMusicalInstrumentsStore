import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class Demo {
	static Store chickChirick = new Store("Chick Chirick");
	static ArrayList<String> instruments = new ArrayList<String>();

	public static void main(String[] args) {

		instruments.add("cigulka");
		instruments.add("viola");
		instruments.add("baraban");
		instruments.add("tarambuka");
		instruments.add("trompet");
		instruments.add("fleita");
		instruments.add("piano");
		instruments.add("akordeon");
		instruments.add("bas-kitara");
		instruments.add("elektricheska cigulka");

		chickChirick.supplyNewProduct("strunni", instruments.get(0), 200, 3);
		chickChirick.supplyNewProduct("strunni", instruments.get(1), 150, 3);
		chickChirick.supplyNewProduct("udarni", instruments.get(2), 300, 6);
		chickChirick.supplyNewProduct("udarni", instruments.get(3), 80, 4);
		chickChirick.supplyNewProduct("duhovni", instruments.get(4), 500, 2);
		chickChirick.supplyNewProduct("duhovni", instruments.get(5), 120, 2);
		chickChirick.supplyNewProduct("klavishni", instruments.get(6), 1500, 1);
		chickChirick.supplyNewProduct("klavishni", instruments.get(7), 300, 3);
		chickChirick.supplyNewProduct("elektronni", instruments.get(8), 400, 4);
		chickChirick.supplyNewProduct("elektronni", instruments.get(9), 450, 6);

		Thread supplierAllNotAvailableInstruments = new Thread(new SupplierAllNotAvailableInstruments());
		supplierAllNotAvailableInstruments.start();
		Thread supplierOrder = new Thread(new SupplierOrder());
		supplierOrder.start();
		Thread client = new Thread(new Client());
		client.start();

		chickChirick.makeInquireBestSellerInstrument();
		chickChirick.makeInquireBestSellerType();
		chickChirick.makeInquireHigherTotalSumOfSalesOfType();
		chickChirick.makeInquireSoldInstrumentsSortedByNumberOfSales();
		chickChirick.makeInquireTotalSumOfSoldInstrumnts();
		chickChirick.makeInquireWorstSellerInstrument();
		chickChirick.makeInquireBestSellerInstrument();
		chickChirick.makeInquireSoldInstrumentsSortedByNumberOfSales();

	}

	static class SupplierAllNotAvailableInstruments implements Runnable {
		@Override
		public void run() {
			while (true) {
				try {
					Thread.currentThread().sleep(10000);
				} catch (InterruptedException e) {
				}
				chickChirick.supplyProducts();
			}
		}
	}

	static class SupplierOrder implements Runnable {
		@Override
		public void run() {
			while (true) {
				chickChirick.supplyProduct();
			}
		}
	}

	static class Client implements Runnable {
		@Override
		public void run() {
			while (true) {
				Random instrument = new Random();
				Random number = new Random();
				chickChirick.sellInstrument(instruments.get(instrument.nextInt(instruments.size() - 1)),
						number.nextInt(10) + 1);
			}
		}
	}
}
