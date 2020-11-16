/*BankImpl.java

        implementation of the Bank
*/

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.stream.IntStream;

public class BankImpl implements Bank {
    public static PrintStream o;
    private final int m;                      /*the number of resources*/
    private final int[] available;            /*the amount available of each resource*/
    private final int[][] maximum;            /*the maximum demand of each thread*/
    private final int[][] allocation;         /*the amount currently allocated to each thread*/
    private final int[][] need;               /*the remaining needs of each thread*/
    private final boolean[] released;         /*released customers*/
    private int n;                            /*the number of threads in the system*/

    /*create a new bank*/


    // I hope you don't mind me using IntStream because I am utilizing new stuff i have learned
    //also using varargs
    public BankImpl(int[] resources, String file) {
        //The commented out code that is here is so that we can print the output to a file,
        //so that it is easier to check the output
//        try {

//            o = new PrintStream(new File(file+"_output.txt")); //if we include the loop in Factory.java
//            o = new PrintStream(new File("output.txt")); //if we dont include it
//            System.setOut(o);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
        m = resources.length;
        n = Customer.COUNT;
        available = new int[m];
        System.arraycopy(resources, 0, available, 0, m);
        maximum = new int[n][m];
        allocation = new int[n][m];
        need = new int[n][m];
        released = new boolean[n];
        Arrays.fill(released, true);
    }


    //print out the vector we give it, it is a helper function
    //Professor gave us the parameter "string msg" but i removed it
    //hopefully points are not removed, i just found it simpler to modify it to get the closest output
    //to the rubric as i could, but if I lose points I will resubmit with the parameter included
    private void showVector(int[] arrayToPrint) {
        System.out.print("[");
        IntStream.range(0, m).forEach(i -> {
            if (i == 0) {
                System.out.print(Integer.toString(arrayToPrint[i]) + ' ');
            } else if (i == m - 1) {
                System.out.print(' ' + Integer.toString(arrayToPrint[i]));
            } else {
                System.out.print(arrayToPrint[i]);
            }
        });
        System.out.print("]");
    }


    public void setN(int x){
        this.n = x;
    }


    //output state for each state
    public void getState() {
        System.out.print("\tALLOCATION\tMAXIMUM\t\t NEED\n");
        IntStream.range(0, n).forEach(i -> {
            System.out.print("\t");
            if (released[i]) {
                System.out.print("-------\t\t--------\t--------\n");
            } else {
                showVector(this.allocation[i]);
                System.out.print("\t\t");
                showVector(this.maximum[i]);
                System.out.print("\t\t");
                showVector(this.need[i]);
                System.out.print("\n");
            }
        });
    }

    public void addCustomer(int threadNum, int[] allocated, int... maxDemand) {
        IntStream.range(0, m).forEach(i -> {
            allocation[threadNum][i] = allocated[i];
            maximum[threadNum][i] = maxDemand[i];
            need[threadNum][i] = maxDemand[i] - allocated[i];
        });
        released[threadNum] = false;
    }


    //decided to use varargs because I just learned about them, and it doesn't change anything so I left it
    private boolean isSafeState(int threadNum, int... request) {
        int[] currentAvailable = new int[m];
        System.arraycopy(available, 0, currentAvailable, 0, m);
        int[][] currentAlloc = new int[n][m];
        int[][] currentNeed = new int[n][m];
        IntStream.range(0, n).forEach(i -> {
            System.arraycopy(allocation[i], 0, currentAlloc[i], 0, m);
            System.arraycopy(need[i], 0, currentNeed[i], 0, m);
        });

        boolean[] finish = new boolean[n];
        Arrays.fill(finish, false);

        IntStream.range(0, m).forEach(i -> {
            currentAvailable[i] -= request[i];
            currentAlloc[threadNum][i] += request[i];
            currentNeed[threadNum][i] -= request[i];
        });

        while (true) {
            int index = -1;
            for (int i = 0; i < n; ++i) {
                boolean hasEnoughResource = true;
                for (int j = 0; j < m; ++j) {
                    if (currentNeed[i][j] > currentAvailable[j]) {
                        hasEnoughResource = false;
                        break;
                    }
                }
                if (!finish[i] && hasEnoughResource) {
                    index = i;
                    break;
                }
            }

            if (index > -1) {
                for (int i = 0; i < m; ++i) {
                    currentAvailable[i] += currentAlloc[index][i];
                    finish[index] = true;
                }
            } else break;
        }

        /* if it is not finished return false */
        return IntStream.range(0, n).allMatch(i -> finish[i]);
    }

    /*
    make request for resources. will block until request is satisfied safely
     */
    public synchronized boolean requestResources(int threadNum, int[] request) {
        IntStream.range(0, m).filter(i -> request[i] > need[threadNum][i]).forEach(i -> request[i] = need[threadNum][i]);
        System.out.print("#P" + threadNum + " RQ:");
        showVector(request);
        System.out.print(",needs: ");
        showVector(need[threadNum]);
        System.out.print(", available=");
        showVector(available);
        System.out.print(" ");

        /*
        check for available resource
         */
        boolean isDone = false;
        /* i didnt use for each everywhere though */
        for (int i = 0; i < m; ++i) {
            if (request[i] > available[i]) {
                System.out.println("DENIED");
                isDone = true;
                break;
            }
        }
        boolean result = false;
        if (!isDone) {
            if (isSafeState(threadNum, request)) {
                System.out.print("---> APPROVED, #P" + threadNum + " now at:");
                IntStream.range(0, m).forEach(i -> {
                    available[i] -= request[i];
                    allocation[threadNum][i] += request[i];
                    need[threadNum][i] -= request[i];
                });

                showVector(allocation[threadNum]);
                System.out.print("\navailable ");

                showVector(available);
                System.out.print("\n");
                getState();

                if (IntStream.range(0, m).anyMatch(i -> need[threadNum][i] != 0)) {
                    isDone = true;
                }
                if (!isDone) {
                    result = true;
                    isDone = true;
                    /*customer is  done now we are  waiting to be released*/
                }
            }
            if (!isDone) {
                System.out.println("--->DENIED");  /*request doesn't lead to a safe state */
            }
        }
        return result;
    }

    public synchronized void releaseResources(int threadNum, int... release) {
        System.out.print("------------------------------> #P" + threadNum + " has all its resources!" +
                "\tRELEASING ALL and SHUTTING DOWN...\n");
        System.out.print("======================== customer #" + threadNum + " releasing: ");
        showVector(allocation[threadNum]);
        System.out.print(", allocated=");
        IntStream.range(0, m).forEach(i -> {
            available[i] += allocation[threadNum][i];
            allocation[threadNum][i] = 0;
        });
        showVector(allocation[threadNum]);
        System.out.print("\n");
        released[threadNum] = true;
    }
}
