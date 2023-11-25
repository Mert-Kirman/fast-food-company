import java.util.LinkedList;

// HashTable Implementation For Storing Company Data
// Do the searching with a String type key(name of a city, branch, employee) to find an object inside the hash table
// V is the object we want to store in the hash table (City object, branch object or employee object)
public class HashTable<V> {
    private static final int DEFAULT_SIZE = 101;  // Table size unless specified otherwise
    public LinkedList<V>[] table;
    public int elementCount;  // How many values are stored in the hashtable currently

    // HashTable Constructors
    public HashTable() {
        this(DEFAULT_SIZE);
    }
    public HashTable(int size) {
        this.table = new LinkedList[size];
        for(int i = 0; i < size; i++) {
            this.table[i] = new LinkedList<>();
        }
        this.elementCount = 0;
    }

    // Method to find if a number is prime
    private boolean isPrime(int num) {
        if(num == 2 || num == 3) {
            return true;
        }
        if(num == 1 || num % 2 == 0) {
            return false;
        }

        for(int i = 3; i*i <= num; i+=2) {
            if(num % i == 0) {
                return false;
            }
        }
        return true;
    }

    // Method to find next prime number
    private int nextPrime(int num) {
        if(num % 2 == 0) {
            num++;
        }
        while(!isPrime(num)) {
            num += 2;
        }
        return num;
    }

    // Find the hash of a name, which will be used as the index to find an object value
    private int hash(String name) {
        int hashVal = name.hashCode();
        hashVal %= this.table.length;
        if(hashVal < 0) {
            hashVal += this.table.length;
        }
        return hashVal;
    }

    // Increase the size of the hash table and reinsert previous values to this new table
    private void rehash() {
        int newSize = nextPrime(this.table.length * 2);
        LinkedList<V>[] previousTable = this.table;

        // Create a new hashtable
        this.table = new LinkedList[newSize];
        for(int i = 0; i < this.table.length; i++) {
            this.table[i] = new LinkedList<>();
        }

        this.elementCount = 0;

        // Insert old values to the new hashtable
        for(LinkedList<V> list : previousTable) {
            for(V val : list) {
                if(val instanceof HasName) {  // Use inheritance to reach the "name" data field of the subclasses
                    insert(((HasName)val).name, val);  // Typecast city, branch or employee object to HasName
                }
            }
        }
    }

    // Insert a value to the hash table
    public void insert(String name, V val) {
        LinkedList<V> listToInsert = this.table[hash(name)];
        if(!listToInsert.contains(val)) {
            listToInsert.add(val);
            this.elementCount++;
            if(this.elementCount > this.table.length) {
                rehash();
            }
        }
    }

    // Remove a value from the table
    public void remove(String name) {
        int hashTableIndex = hash(name);
        int result = contains(name);
        if(result != -1) {
            this.table[hashTableIndex].remove(result);
            this.elementCount--;  // Decrease the number of elements in the hash table
        }
    }

    // Check if a value(object) exists in a Linked List, if the object exists method returns the index of it
    // -1 means value is not found
    public int contains(String name) {
        int index = hash(name);
        LinkedList<V> list =  this.table[index];
        for(int i = 0; i < list.size(); i++) {
            if(((HasName)list.get(i)).name.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    // Method for returning a value(city, branch or employee object) from the hashtable
    public V getValue(String name) {
        int index = hash(name);
        LinkedList<V> list = this.table[index];
        for(V val : list) {
            if(((HasName)val).name.equals(name)) {
                return val;
            }
        }
        return null;
    }
}
