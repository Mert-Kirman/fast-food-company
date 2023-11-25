import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class Branch extends HasName {
    // Array which consist of three hash tables representing 3 different types of employees
    // 0th index hashtable holds couriers
    // 1st index hashtable holds cashiers
    // 2nd index hashtable holds cooks
    public HashTable<Employee>[] employees;
    public Employee branchManager;
    public int monthlyBonus;
    public int overallBonus;
    public Queue<Employee> cashierToCook;  // Cashiers with promotion point >= 3
    public Queue<Employee> cookToManager;  // Cooks with promotion point >= 10
    public HashTable<Employee> dismissalWaitingList;  // Employees here will be dismissed at first opportunity due to promotion point <= -5

    // Constructors
    public Branch() {
        this("None");
    }
    public Branch(String name) {
        super(name);  // Set the branch name

        this.employees = new HashTable[3];
        for(int i = 0; i < 3; i++) {
            employees[i] = new HashTable<>();
        }

        this.branchManager = null;
        this.monthlyBonus = 0;
        this.overallBonus = 0;
        this.cashierToCook = new LinkedList<>();
        this.cookToManager = new LinkedList<>();
        this.dismissalWaitingList = new HashTable<>();
    }

    // Add monthly bonus to overall bonus for this branch
    public void resetBonus() {
        this.overallBonus += this.monthlyBonus;
        this.monthlyBonus = 0;
    }

    // Process promotions and dismissals (Due to promotion point <= -5) if possible
    public void promotionAndDismissal(FileWriter output) throws IOException {
        Employee employee;

        // Promotions of employees
        // Promotions of cashiers
        while(!this.cashierToCook.isEmpty() && this.employees[1].elementCount > 1) {  // While there is more than 1 cashier in the branch
            employee = this.cashierToCook.poll();
            employee.promotionPoint -= 3;
            employee.occupation = "COOK";
            this.employees[1].remove(employee.name);  // No longer a cashier
            this.employees[2].insert(employee.name, employee);  // It is a cook from now on
            output.write(employee.name + " is promoted from Cashier to Cook.\n");
        }

        // Promotions of cooks
        // If a cook is waiting for promotion and the manager's promotion point <= -5 and there is more than 1 cook
        if(!this.cookToManager.isEmpty() && (this.branchManager == null || this.dismissalWaitingList.contains(this.branchManager.name) != -1) && this.employees[2].elementCount > 1) {
            if(this.branchManager != null) {  // Manager is to be dismissed, otherwise this field would be null (leave case)
                this.dismissalWaitingList.remove(this.branchManager.name);  // Dismiss the manager
                output.write(this.branchManager.name + " is dismissed from branch: " + this.name + ".\n");
            }

            this.branchManager = this.cookToManager.poll();  // Make the cook the new manager
            this.branchManager.promotionPoint -= 10;
            this.branchManager.occupation = "MANAGER";
            this.employees[2].remove(this.branchManager.name);  // No longer a cook
            output.write(this.branchManager.name + " is promoted from Cook to Manager.\n");
        }

        // Dismissal of employees
        for(LinkedList<Employee> employeeToDismiss : this.dismissalWaitingList.table) {
            if(!employeeToDismiss.isEmpty()) {
                for(Employee e : employeeToDismiss) {
                    employee = e;
                    removeEmployee(employee.name,true, output);
                }
            }
        }
    }

    // Remove an employee if possible (Leave or Dismissal)
    public boolean removeEmployee(String name,boolean dismissal, FileWriter output) throws IOException {
        // Courier - cashier - cook
        Employee employee;  // Employee to be removed
        for(HashTable<Employee> occupationType : this.employees) {
            if(occupationType.contains(name) != -1) {
                employee = occupationType.getValue(name);
                if(occupationType.elementCount == 1) {  // If there is only one courier - cashier - cook, it cant leave;
                    if(employee.promotionPoint > -5) {  // If the employee is not to be dismissed
                        this.monthlyBonus += 200;  // Give 200 USD bonus
                    }
                    return false;
                }
                else {  // More than one employee for that role
                    occupationType.remove(name);
                    if(dismissal) {
                        output.write(name + " is dismissed from branch: " + this.name + ".\n");
                    }

                    // Also remove the employee if it is in any promotion waiting queues (LEAVE case)
                    if(employee.occupation.equals("CASHIER")) {
                        this.cashierToCook.remove(employee);
                    }
                    else if(employee.occupation.equals("COOK")) {
                        this.cookToManager.remove(employee);
                    }

                    // Remove employee from the dismissal waiting list if it is to be dismissed (DISMISSAL case)
                    this.dismissalWaitingList.remove(employee.name);

                    return true;  // The employee can leave or be dismissed
                }
            }
        }

        // Manager
        if(this.branchManager.name.equals(name)) {
            // If there is no cook waiting to be promoted or there is only 1 cook in the branch, manager can neither leave nor be dismissed
            if(this.cookToManager.isEmpty() || this.employees[2].elementCount == 1) {
                if(this.branchManager.promotionPoint > -5) {  // If the manager is not to be dismissed
                    this.monthlyBonus += 200;
                }
                return false;
            }
            // At least one cook is waiting to be promoted to manager; manager can leave or be dismissed
            else {
                if(this.branchManager.promotionPoint <= -5) {
                    this.dismissalWaitingList.remove(name);
                }
                this.branchManager = null;
                return true;  // The employee can leave or be dismissed
            }
        }

        output.write("There is no such employee.\n");
        return false;
    }
}
