// Abstract class used for inheritance to typecast and compare subclass objects by name
public abstract class HasName {
    public String name;

    protected HasName() {}
    protected HasName(String name) {
        this.name = name;
    }
}
