// Cities that the company has branches in
public class City extends HasName {
    public HashTable<Branch> branches;  // Hash table for keeping all the branches in a city object

    // Constructors
    public City() {
        this("None");
    }
    public City(String name) {
        super(name);
        this.branches = new HashTable<>();
    }
}
