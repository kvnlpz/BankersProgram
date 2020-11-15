import java.util.stream.IntStream;

public class Customer implements Runnable {
    public static final int COUNT = 10;    // maximum number of threads

    private final int numOfResources;     // N different resources
    private final int[] maxDemand;        // maximum this thread will demand
    private final int customerNum;        // customer number
    private final int[] request;          // request it is making

    private final java.util.Random rand;  // random number generator

    private final Bank theBank;           // synchronizing object

    public Customer(int customerNum, int[] maxDemand, Bank theBank) {
        this.customerNum = customerNum;
        this.maxDemand = new int[maxDemand.length];
        this.theBank = theBank;

        System.arraycopy(maxDemand, 0, this.maxDemand, 0, maxDemand.length);
        numOfResources = maxDemand.length;
        request = new int[numOfResources];
        rand = new java.util.Random();
    }

    public void run() {
        boolean canRun = true;
        while (canRun) {
            try {
                SleepUtilities.nap();       // take a nap
                // ... then, make a resource request
                IntStream.range(0, numOfResources).forEach(i -> request[i] = rand.nextInt(maxDemand[i] + 1));

                if (theBank.requestResources(customerNum, request)) {   // if customer can proceed
                    SleepUtilities.nap();   // use and release the resources
                    theBank.releaseResources(customerNum, request);
                    return; // finish thread after releasing resource
                }
            } catch (InterruptedException ie) {
                canRun = false;
            }
        }
        System.out.println("Thread # " + customerNum + " I'm interrupted.");
    }
}


