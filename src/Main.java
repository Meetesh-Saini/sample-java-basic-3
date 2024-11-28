import java.util.*;
import java.util.stream.Collectors;

public class Main {
    
    static class Item {
        private final String id;
        private final String name;
        private final String category;
        private int quantity;

        public Item(String id, String name, String category, int quantity) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.quantity = quantity;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getCategory() {
            return category;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        @Override
        public String toString() {
            return String.format("Item{id='%s', name='%s', category='%s', quantity=%d}", id, name, category, quantity);
        }
    }

    
    private final Map<String, Item> inventoryById = new HashMap<>();
    private final Map<String, TreeSet<Item>> categoryMap = new HashMap<>();
    private final int restockThreshold;

    public Main(int restockThreshold) {
        this.restockThreshold = restockThreshold;
    }

    
    private final Comparator<Item> quantityComparator = (a, b) -> b.quantity - a.quantity;

    
    public void addItem(String id, String name, String category, int quantity) {
        if (inventoryById.containsKey(id)) {
            
            Item item = inventoryById.get(id);
            categoryMap.get(item.getCategory()).remove(item);
            item.setQuantity(quantity);
            categoryMap.get(category).add(item);
        } else {
            
            Item newItem = new Item(id, name, category, quantity);
            inventoryById.put(id, newItem);
            categoryMap.computeIfAbsent(category, k -> new TreeSet<>(quantityComparator)).add(newItem);
        }
        checkRestockNotification(id);
    }

    
    public void removeItem(String id) {
        if (inventoryById.containsKey(id)) {
            Item item = inventoryById.remove(id);
            categoryMap.get(item.getCategory()).remove(item);
        }
    }

    
    public List<Item> getItemsByCategory(String category) {
        return categoryMap.getOrDefault(category, new TreeSet<>())
                .stream()
                .collect(Collectors.toList());
    }

    
    public List<Item> getTopKItems(int k) {
        PriorityQueue<Item> pq = new PriorityQueue<>(quantityComparator);
        pq.addAll(inventoryById.values());
        List<Item> result = new ArrayList<>();
        for (int i = 0; i < k && !pq.isEmpty(); i++) {
            result.add(pq.poll());
        }
        return result;
    }

    
    public void mergeInventory(Main otherInventory) {
        for (Item otherItem : otherInventory.inventoryById.values()) {
            if (inventoryById.containsKey(otherItem.getId())) {
                Item currentItem = inventoryById.get(otherItem.getId());
                if (otherItem.getQuantity() > currentItem.getQuantity()) {
                    addItem(otherItem.getId(), otherItem.getName(), otherItem.getCategory(), otherItem.getQuantity());
                }
            } else {
                addItem(otherItem.getId(), otherItem.getName(), otherItem.getCategory(), otherItem.getQuantity());
            }
        }
    }

    
    private void checkRestockNotification(String id) {
        Item item = inventoryById.get(id);
        if (item.getQuantity() < restockThreshold) {
            System.out.println("Restock Notification: Item " + item.getName() + " (ID: " + item.getId() + ") is below the restock threshold.");
        }
    }

    
    public void displayInventory() {
        inventoryById.values().forEach(System.out::println);
    }

    
    public static void main(String[] args) {
        Main inventory = new Main(10);

        inventory.addItem("101", "Laptop", "Electronics", 50);
        inventory.addItem("102", "Chair", "Furniture", 20);
        inventory.addItem("103", "Apple", "Groceries", 5);
        inventory.addItem("104", "Table", "Furniture", 15);

        System.out.println("\nInventory after adding items:");
        inventory.displayInventory();

        System.out.println("\nItems in Furniture:");
        inventory.getItemsByCategory("Furniture").forEach(System.out::println);

        System.out.println("\nTop 2 items by quantity:");
        inventory.getTopKItems(2).forEach(System.out::println);

        inventory.addItem("103", "Apple", "Groceries", 25); 
        System.out.println("\nInventory after updating Apple:");
        inventory.displayInventory();

        Main anotherInventory = new Main(10);
        anotherInventory.addItem("105", "Fan", "Electronics", 30);
        anotherInventory.addItem("102", "Chair", "Furniture", 25); 

        System.out.println("\nMerging another inventory:");
        inventory.mergeInventory(anotherInventory);
        inventory.displayInventory();
    }
}
