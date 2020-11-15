// Factory.java
//
// Factory class that creates the bank and each bank customer
// Usage:  java Factory 10 5 7

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.stream.IntStream;

public class Factory {
    public static void main(String[] args) {
        String filename = "src\\infile.txt";
        var threads = new Thread[Customer.COUNT];
        int threadNum = 0;

        try {
            //;eaving it commented it out in case I ever  need it again
            //String resourcesFromArgs = args[0] + "," + args[1] + "," + args[2];

            File f = new File(filename);
            System.out.println(f.getAbsolutePath());
            Scanner scanner = new Scanner(f);
            //same thing here
            //String[] tokensFromFile = resourcesFromArgs.split(",");
            String resourcesFromFile = scanner.nextLine();
            //Hopefully this is good enough since theyre similar to Vectors
            String[] tokensFromFile = resourcesFromFile.split(",");


            int resourceCount = tokensFromFile.length;
            int[] resources = IntStream.range(0, resourceCount).map(i -> Integer.parseInt(tokensFromFile[i].trim())).toArray();
            Bank bank = new BankImpl(resources);
            int[] maxDemand = new int[resourceCount];
            int[] allocated = new int[resourceCount];


            int custCount = 0;
            /*getting all the customers*/
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] tokens = line.split(",");

                IntStream.range(0, tokens.length / 2).forEach(i -> allocated[i] = Integer.parseInt(tokens[i]));
                IntStream.range(tokens.length / 2, tokens.length).forEach(i -> maxDemand[i - resourceCount] = Integer.parseInt(tokens[i]));

                threads[threadNum] = new Thread(new Customer(threadNum, maxDemand, bank));
                System.out.println("adding customer " + custCount++ + "...");
                bank.addCustomer(threadNum++, allocated, maxDemand);
            }
            bank.setN(threadNum);
            scanner.close();
        } catch (FileNotFoundException fileNotFoundException) {
            throw new Error("Unable to find file \"" + filename + "\"");
        }

        /*time to start the threads*/
        System.out.println("FACTORY: created threads");
        IntStream.range(0, threadNum).forEach(i -> threads[i].start());
        System.out.println("FACTORY: started threads");
    }
}
