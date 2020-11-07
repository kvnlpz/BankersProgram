//-------------------------------------------------------------------------------------------------
// BankImpl.java
//
// implementation of the Bank
//



// NEED = MAX - ALLOCATION


import java.io.*;
import java.util.*;

public class BankImpl implements Bank {
    private int n;            // the number of threads in the system
    private int m;            // the number of resources

    private int[] available;    // the amount available of each resource
    private int[][] maximum;    // the maximum demand of each thread
    private int[][] allocation;    // the amount currently allocated to each thread
    private int[][] need;        // the remaining needs of each thread

    private void showAllMatrices(int[][] alloc, int[][] max, int[][] need, String msg) {
        // todo
        System.out.println(msg);
        System.out.println(Arrays.deepToString(alloc));
        System.out.println(Arrays.deepToString(max));
        System.out.println(Arrays.deepToString(need));

    }

    private void showMatrix(int[][] matrix, String title, String rowTitle) {
        // todo
        System.out.printf(title);
        System.out.printf(rowTitle);
        System.out.printf(Arrays.deepToString(matrix));

    }

    private void showVector(int[] vect, String msg) {
        // todo
        System.out.println(msg);
        for(int x : vect){
            System.out.println(x);
        }
    }

    public BankImpl(int[] resources) {      // create a new bank (with resources)
        // todo
        this.available = resources;
        this.n = Customer.COUNT;
        this.m = resources.length;
        this.maximum = new int[n][m];
        this.allocation = new int[n][m];
        this.need = new int[n][m];
        //Might not need this if i can just set available = resources
//        for(int i = 0; i < resources.length; i++){
//            this.available[i] = resources[i];
//        }
    }

    // invoked by a thread when it enters the system;  also records max demand
    public void addCustomer(int threadNum, int[] allocated, int[] maxDemand) {
        // todo
        for(int i = 0; i < m; i++){
            allocation[threadNum][i] =  allocated[i];
            maximum[threadNum][i] = maxDemand[i];
            need[threadNum][i] = maxDemand[i] - allocated[i];
        }

    }

    public void getState() {        // output state for each thread
        // todo
        showAllMatrices(allocation, maximum, need, "ALLOCATED   MAXIMUM            NEED");

    }

    private boolean isSafeState(int threadNum, int[] request) {
        // todo -- actual banker's algorithm
    }

    // make request for resources. will block until request is satisfied safely
    public synchronized boolean requestResources(int threadNum, int[] request) {
        // todo

        System.out.print("#P" + threadNum + " RQ:[");

        System.out.print(request[0]);
        for(int i = 1; i < request.length; i++){
            System.out.print(" " + request[i]);
        }
        System.out.print("]");

        System.out.print(", needs:[" + need[threadNum][0]);
        for(int i = 1; i < need[threadNum].length; i++){
            System.out.print(" " + need[threadNum][i]);
        }
        System.out.print("]");

        System.out.print(", available:[" + available[0]);
        for(int i = 1; i < available.length; i++){
            System.out.print(" " + available[i]);
        }
        System.out.print("]  ");

        for(int i = 0; i < request.length; i++){
            if(request[i] > available[i] || request[i] > need[threadNum][i]){
                System.out.println("   DENIED!");
                return false;
            }
        }

        System.out.print("   ---> APPROVED, #P" + threadNum + " now at:");

        for(int i = 0; i < request.length; i++){
            allocation[threadNum][i] += request[i];
            available[i] -= request[i];
            need[threadNum][i] = need[threadNum][i] - request[i] <= 0 ? 0 : need[threadNum][i] - request[i];
        }


        System.out.print("[" + allocation[threadNum][0]);
        for(int i = 1; i < allocation[threadNum].length; i++){
            System.out.print(" " + allocation[threadNum][i]);
        }
        System.out.println("]");

        showAllMatrices(allocation, maximum, need, "ALLOCATED    MAXIMUM           NEED");


        boolean found = false;
        for(int i = 0; i < need[threadNum].length && !found; i++){
            if(need[threadNum][i] != 0){
                found = true;
            }
        }

        if(!found){
            System.out.println("---------------->#P" + threadNum + " has all its resources! RELEASING ALL and SHUTTING DOWN...");
            System.out.print("----------------Customer #" + threadNum + " releasing:" + Arrays.toString(allocation[threadNum]));
            this.releaseResources(threadNum, allocation[threadNum]);
            System.out.println(" allocated =" + Arrays.toString(allocation[threadNum]));

            System.out.print(", available:[" + available[0]);
            for(int i = 1; i < available.length; i++){
                System.out.print(" " + available[i]);
            }

            throw new InterruptedException();
        }

        return true;

    }

    public synchronized void releaseResources(int threadNum, int[] release) {
        // todo
        for(int i = 0; i < release.length; i++){
            allocation[threadNum][i] -= release[i];
            available[i] += release[i];
        }
    }