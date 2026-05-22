import java.util.*;
import java.io.*;

public class StockTradingPlatform {
    private static ArrayList<Stock> market = new ArrayList<>();
    private static Portfolio portfolio = new Portfolio();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        initializeMarket();
        portfolio.loadPortfolio();

        while (true) {
            System.out.println("\n" + "=".repeat(55));
            System.out.println("       STOCK TRADING PLATFORM");
            System.out.println("=".repeat(55));
            System.out.println("1. View Market Data");
            System.out.println("2. Buy Stock");
            System.out.println("3. Sell Stock");
            System.out.println("4. View Portfolio");
            System.out.println("5. Transaction History");
            System.out.println("6. Exit");
            System.out.print("Enter choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> viewMarket();
                case 2 -> buyStock();
                case 3 -> sellStock();
                case 4 -> portfolio.viewPortfolio();
                case 5 -> portfolio.showHistory();
                case 6 -> {
                    portfolio.savePortfolio();
                    System.out.println("Thank you for using Stock Trading Platform!");
                    return;
                }
                default -> System.out.println("Invalid option!");
            }
        }
    }

    private static void initializeMarket() {
        market.add(new Stock("RELIANCE", 2450.75, 1200));
        market.add(new Stock("TCS", 3850.50, 850));
        market.add(new Stock("HDFCBANK", 1680.25, 950));
        market.add(new Stock("INFY", 1820.00, 700));
        market.add(new Stock("ICICIBANK", 1245.80, 1100));
    }

    private static void viewMarket() {
        System.out.println("\n--- MARKET DATA ---");
        System.out.printf("%-12s | %-10s | %-8s%n", "Symbol", "Price (₹)", "Volume");
        System.out.println("-".repeat(45));
        for (Stock s : market) {
            System.out.printf("%-12s | ₹%-8.2f | %d%n", s.symbol, s.price, s.volume);
        }
    }

    private static void buyStock() {
        viewMarket();
        System.out.print("\nEnter Stock Symbol: ");
        String symbol = scanner.nextLine().toUpperCase();

        Stock stock = findStock(symbol);
        if (stock == null) {
            System.out.println("Stock not found!");
            return;
        }

        System.out.print("Enter quantity to buy: ");
        int qty = scanner.nextInt();

        double totalCost = stock.price * qty;
        System.out.printf("Total Cost: ₹%.2f%n", totalCost);

        System.out.print("Confirm buy? (y/n): ");
        String confirm = scanner.next().toLowerCase();

        if (confirm.equals("y")) {
            portfolio.buyStock(stock.symbol, stock.price, qty);
            System.out.println("✅ Purchase Successful!");
        }
    }

    private static void sellStock() {
        portfolio.viewPortfolio();
        System.out.print("\nEnter Stock Symbol to sell: ");
        String symbol = scanner.nextLine().toUpperCase();

        System.out.print("Enter quantity to sell: ");
        int qty = scanner.nextInt();

        portfolio.sellStock(symbol, qty);
    }

    private static Stock findStock(String symbol) {
        for (Stock s : market) {
            if (s.symbol.equals(symbol)) return s;
        }
        return null;
    }
}

// Stock Class
class Stock {
    String symbol;
    double price;
    int volume;

    public Stock(String symbol, double price, int volume) {
        this.symbol = symbol;
        this.price = price;
        this.volume = volume;
    }
}

// Portfolio Class
class Portfolio {
    private ArrayList<Holding> holdings = new ArrayList<>();
    private ArrayList<Transaction> transactions = new ArrayList<>();

    public void buyStock(String symbol, double price, int qty) {
        holdings.add(new Holding(symbol, price, qty));
        transactions.add(new Transaction("BUY", symbol, price, qty));
    }

    public void sellStock(String symbol, int qty) {
        for (int i = 0; i < holdings.size(); i++) {
            Holding h = holdings.get(i);
            if (h.symbol.equals(symbol) && h.quantity >= qty) {
                double sellPrice = h.avgBuyPrice * 1.05; // Simulated 5% gain
                transactions.add(new Transaction("SELL", symbol, sellPrice, qty));
                
                if (h.quantity == qty) {
                    holdings.remove(i);
                } else {
                    h.quantity -= qty;
                }
                System.out.println("✅ Sold successfully!");
                return;
            }
        }
        System.out.println("Not enough shares or stock not found!");
    }

    public void viewPortfolio() {
        if (holdings.isEmpty()) {
            System.out.println("Your portfolio is empty.");
            return;
        }

        System.out.println("\n--- YOUR PORTFOLIO ---");
        double totalValue = 0;
        System.out.printf("%-10s | %-8s | %-10s | %-12s%n", "Symbol", "Qty", "Avg Price", "Current Value");
        System.out.println("-".repeat(50));

        for (Holding h : holdings) {
            double currentValue = h.quantity * h.avgBuyPrice * 1.08; // Simulated market change
            totalValue += currentValue;
            System.out.printf("%-10s | %-8d | ₹%-8.2f | ₹%-10.2f%n", 
                h.symbol, h.quantity, h.avgBuyPrice, currentValue);
        }
        System.out.printf("\nTotal Portfolio Value: ₹%.2f%n", totalValue);
    }

    public void showHistory() {
        System.out.println("\n--- TRANSACTION HISTORY ---");
        for (Transaction t : transactions) {
            t.display();
        }
    }

    public void savePortfolio() {
        try (PrintWriter writer = new PrintWriter("portfolio.txt")) {
            for (Holding h : holdings) {
                writer.println("HOLDING|" + h.symbol + "|" + h.avgBuyPrice + "|" + h.quantity);
            }
            for (Transaction t : transactions) {
                writer.println("TX|" + t.type + "|" + t.symbol + "|" + t.price + "|" + t.quantity);
            }
        } catch (Exception e) {}
    }

    public void loadPortfolio() {
        try (Scanner file = new Scanner(new File("portfolio.txt"))) {
            while (file.hasNextLine()) {
                String[] data = file.nextLine().split("\\|");
                if (data[0].equals("HOLDING")) {
                    holdings.add(new Holding(data[1], Double.parseDouble(data[2]), Integer.parseInt(data[3])));
                }
            }
        } catch (Exception e) {}
    }
}

class Holding {
    String symbol;
    double avgBuyPrice;
    int quantity;

    public Holding(String symbol, double avgBuyPrice, int quantity) {
        this.symbol = symbol;
        this.avgBuyPrice = avgBuyPrice;
        this.quantity = quantity;
    }
}

class Transaction {
    String type;
    String symbol;
    double price;
    int quantity;
    Date date;

    public Transaction(String type, String symbol, double price, int quantity) {
        this.type = type;
        this.symbol = symbol;
        this.price = price;
        this.quantity = quantity;
        this.date = new Date();
    }

    public void display() {
        System.out.printf("[%s] %s | %s | Qty: %d | Price: ₹%.2f | %s%n", 
            type, symbol, type.equals("BUY") ? "Bought" : "Sold", quantity, price, date);
    }
}