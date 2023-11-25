import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

public class project2 {
    public static void main(String[] args) throws IOException {
        // Create an output file to log information regarding requests
        FileWriter output = new FileWriter(args[2], true);

        // Open the file that contains information regarding initial state of the company
        File file = new File(args[0]);
        Scanner input = new Scanner(file);

        // Main hash table that contains city objects
        HashTable<City> company = new HashTable<>();

        // Insert data from input file - 1 to hash tables
        String[] employeeInfo;
        City currentCity;
        Branch currentBranch;
        while(input.hasNextLine()) {
            employeeInfo = input.nextLine().split(",");  // Employee info
            for(int i = 0; i < 4; i++) {  // Remove whitespace from the input file
                employeeInfo[i] = employeeInfo[i].strip();
            }

            // Obtain the city object to be modified
            if(company.contains(employeeInfo[0].strip()) == -1) {  // If a city doesn't exist in the company hashtable, insert it
                currentCity = new City(employeeInfo[0]);
                company.insert(employeeInfo[0], currentCity);
            }
            currentCity = company.getValue(employeeInfo[0]);

            // Obtain the branch object to be modified
            if(currentCity.branches.contains(employeeInfo[1]) == -1) {  // If a branch doesn't exist in the city object's hashtable, insert it
                currentBranch = new Branch(employeeInfo[1]);
                currentCity.branches.insert(employeeInfo[1], currentBranch);
            }
            currentBranch = currentCity.branches.getValue(employeeInfo[1]);

            // Add a new employee to a specified branch
            addEmployee(currentBranch, employeeInfo[2], employeeInfo[3], output);
        }
        input.close();

        // Open the file that contains information about the changes happening in the company
        file = new File(args[1]);
        input = new Scanner(file);

        // Process requests
        input.nextLine();  // Skip the first line of the file which contains first month name
        while(input.hasNextLine()) {
            String[] operationAndChanges = input.nextLine().split(":");
            if(operationAndChanges.length == 1) {  // New month
                resetMonthlyBonuses(company);
                continue;
            }
            String operation = operationAndChanges[0];
            String[] changes = operationAndChanges[1].split(",");
            for(int i = 0; i < changes.length; i++) {  // Remove whitespace from the input file
                changes[i] = changes[i].strip();
            }

            switch (operation) {
                case "PERFORMANCE_UPDATE" -> performanceUpdate(changes[0], changes[1], changes[2], Integer.parseInt(changes[3]), company, output);
                case "ADD" -> addEmployee(company.getValue(changes[0]).branches.getValue(changes[1]), changes[2], changes[3], output);
                case "LEAVE" -> leave(company.getValue(changes[0]).branches.getValue(changes[1]), changes[2], output);
                case "PRINT_OVERALL_BONUSES" -> printOverallBonus(company.getValue(changes[0]).branches.getValue(changes[1]), output);
                case "PRINT_MONTHLY_BONUSES" -> printMonthlyBonus(company.getValue(changes[0]).branches.getValue(changes[1]), output);
                case "PRINT_MANAGER" -> printManager(company.getValue(changes[0]).branches.getValue(changes[1]), output);
            }
        }
        input.close();
        output.close();
    }

    // Update the monthly score of an employee
    public static void performanceUpdate(String city, String branch, String name, int score, HashTable<City> company, FileWriter output) throws IOException {
        Branch currentBranch = company.getValue(city).branches.getValue(branch);
        HashTable<Employee>[] employeeList = currentBranch.employees;
        for(HashTable<Employee> occupationType : employeeList) {
            if (occupationType.contains(name) != -1 || currentBranch.branchManager.name.equals(name)) {  // If the employee we are looking for exists
                Employee employee;
                if(occupationType.contains(name) != -1) {
                    employee = occupationType.getValue(name);  // courier - cashier - cook
                }
                else {
                    employee = currentBranch.branchManager;  // Manager
                }
                employee.promotionPoint += score / 200;  // For each 200 scores, employee gets 1 promotion point
                if(score > 0) {
                    currentBranch.monthlyBonus += score % 200;
                }

                // Check whether the employee deserves promotion
                if(employee.occupation.equals("CASHIER")) {
                    if(employee.promotionPoint >= 3) {  // If the cashier has promotion points >= 3 then add it to the waiting list
                        if(!currentBranch.cashierToCook.contains(employee)) {  // If already waiting in queue don't add again, otherwise add
                            currentBranch.cashierToCook.offer(employee);
                        }
                    }
                    else {
                        currentBranch.cashierToCook.remove(employee);
                    }
                }

                else if(employee.occupation.equals("COOK")) {
                    if(employee.promotionPoint >= 10) {
                        if(!currentBranch.cookToManager.contains(employee)) {
                            currentBranch.cookToManager.offer(employee);
                        }
                    }
                    else {
                        currentBranch.cookToManager.remove(employee);
                    }
                }

                // Check whether the employee deserves dismissal
                if(employee.promotionPoint <= -5) {  // Deserves dismissal
                    if(currentBranch.dismissalWaitingList.contains(employee.name) == -1) {
                        currentBranch.dismissalWaitingList.insert(employee.name, employee);
                    }
                }
                else {  // Does not deserve to be dismissed anymore
                    if(currentBranch.dismissalWaitingList.contains(employee.name) != -1) {
                        currentBranch.dismissalWaitingList.remove(employee.name);
                    }
                }

                currentBranch.promotionAndDismissal(output);
                return;
            }
        }
        output.write("There is no such employee.\n");
    }

    // Method for creating and adding an employee object to a given branch according to its job
    public static void addEmployee(Branch branch, String name, String occupation, FileWriter output) throws IOException {
        Employee employee = new Employee(name, occupation);

        switch (occupation) {
            case "COURIER" -> {
                if((branch.employees[0].contains(name) == -1)) {  // Add this employee only if it doesn't already exist
                    branch.employees[0].insert(name, employee);
                }
                else {
                    output.write("Existing employee cannot be added again.\n");
                }
            }
            case "CASHIER" -> {
                if((branch.employees[1].contains(name) == -1)) {  // Add this employee only if it doesn't already exist
                    branch.employees[1].insert(name, employee);
                }
                else {
                    output.write("Existing employee cannot be added again.\n");
                }
            }
            case "COOK" -> {
                if((branch.employees[2].contains(name) == -1)) {  // Add this employee only if it doesn't already exist
                    branch.employees[2].insert(name, employee);
                }
                else {
                    output.write("Existing employee cannot be added again.\n");
                }
            }
            case "MANAGER" -> {
                if(branch.branchManager == null) {
                    branch.branchManager = employee;
                }
                else {
                    output.write("Existing employee cannot be added again.\n");
                }
            }
        }

        branch.promotionAndDismissal(output);
    }

    // Method for printing the total bonus amount given at the specified branch that month
    public static void printMonthlyBonus(Branch branch, FileWriter output) throws IOException {
        String branchName = branch.name;
        int monthlyBonus = branch.monthlyBonus;
        output.write("Total bonuses for the " + branchName + " branch this month are: " + monthlyBonus + "\n");
    }

    // Method for printing total amount of bonus that have been given from the start till to now in a specified branch
    public static void printOverallBonus(Branch branch, FileWriter output) throws IOException {
        String branchName = branch.name;
        int overallBonuses = branch.overallBonus;
        int monthlyBonus = branch.monthlyBonus;
        output.write("Total bonuses for the " + branchName + " branch are: " + (overallBonuses + monthlyBonus) + "\n");
    }

    // Method for printing the manager of a specified branch
    public static void printManager(Branch branch, FileWriter output) throws IOException {
        String branchName = branch.name;
        String managerName = branch.branchManager.name;
        output.write("Manager of the " + branchName + " branch is " + managerName + ".\n");
    }

    // Add monthly bonus of each branch in every city to overall bonus and reset
    // This method iterates every city object
    public static void resetMonthlyBonuses(HashTable<City> company) {
        for(LinkedList<City> city : company.table) {
            if(!city.isEmpty()) {
                for(City c : city) {
                    resetMonthlyBonus(c);
                }
            }
        }
    }

    // Method used for resetting the monthly bonus of each branch in a city object
    public static void resetMonthlyBonus(City city) {
        for(LinkedList<Branch> branch : city.branches.table) {
            if(!branch.isEmpty()) {
                for(Branch b : branch) {
                    b.resetBonus();
                }
            }
        }
    }

    // Remove an employee wanting to leave if possible
    public static void leave(Branch branch, String name, FileWriter output) throws IOException {
        boolean result = branch.removeEmployee(name, false, output);
        if(result) {  // If the employee is able to leave
            output.write(name + " is leaving from branch: " + branch.name + ".\n");
        }
        branch.promotionAndDismissal(output);
    }
}