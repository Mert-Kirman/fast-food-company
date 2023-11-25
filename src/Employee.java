// Java class for storing employee data
public class Employee extends HasName {
    public String occupation;
    public int promotionPoint;  // Current promotion points

    // Constructors
    public Employee() {
        this("None", "None");
    }

    public Employee(String name, String occupation) {
        super(name);
        this.occupation = occupation;
        this.promotionPoint = 0;
    }
}
