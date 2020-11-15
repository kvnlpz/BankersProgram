public interface Bank {
    void addCustomer(int threadNum, int[] maxDemand, int[] allocated);    // add customer to Bank


    //Since N is private and I dont want to make it public, I just created a setter for it
    void setN(int x);

    void getState();     // outputs available, allocation, max, and need matrices

    // request resources; specify number of customer being added, maxDemand for customer
    //      returns if request is grant
    boolean requestResources(int threadNum, int[] request);

    // release resources
    void releaseResources(int threadNum, int[] release);
}
