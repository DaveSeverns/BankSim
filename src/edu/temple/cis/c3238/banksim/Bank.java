package edu.temple.cis.c3238.banksim;

/**
 * @author Cay Horstmann
 * @author Modified by Paul Wolfgang
 * @author modified for synchronization by davidseverns
 */
public class Bank {

    public static final int NTEST = 10;
    private final Account[] accounts;
    private long ntransacts = 0;
    private final int initialBalance;
    private final int numAccounts;
    private boolean open;
    private boolean testingFlag;
    private long runningTransfers;

    public Bank(int numAccounts, int initialBalance) {
        open = true;
        this.initialBalance = initialBalance;
        this.numAccounts = numAccounts;
        accounts = new Account[numAccounts];
        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = new Account(this, i, initialBalance);
        }
        ntransacts = 0;
        //set to false as not testing when the bank is instantiated
        testingFlag=false;
        runningTransfers = 0;
    }

    public void transfer(int from, int to, int amount) {

        //spin lock- busy waiting not efficient but should work until more eloquent solution found
        //@TODO find more efficient solution than spin lock, wait() did not work :(
        while(testingFlag){
            Thread.holdsLock(testingFlag);
        }
        //if not testing transfer transactions can take place, increment ntransacts to block testing while transfer is taking place
        runningTransfers++;
        accounts[from].waitForAvailableFunds(amount);
        if (!open) return;
        if (accounts[from].withdraw(amount)) {
            accounts[to].deposit(amount);
        }
        //when transfer is complete take the number of transactions down so test method know it can run
        runningTransfers--;
        if (shouldTest()) test();
    }

    public void test() {
        //if no transactions we can beging the test of the sum
        testingFlag = true;
        System.out.println(""+ runningTransfers);
        //spin lock on the test method, in transactions in progress method will not try to test
        while(runningTransfers>0)
        {
            Thread.holdsLock(testingFlag);
        }

        int sum = 0;
        for (Account account : accounts) {
            System.out.printf("%s %s%n",
                    Thread.currentThread().toString(), account.toString());
            sum += account.getBalance();
        }
        System.out.println(Thread.currentThread().toString() +
                " Sum: " + sum);
        if (sum != numAccounts * initialBalance) {
            System.out.println(Thread.currentThread().toString() +
                    " Money was gained or lost");
            System.exit(1);
        } else {
            System.out.println(Thread.currentThread().toString() +
                    " The bank is in balance");
        }
        //test complete
        testingFlag = false;
    }

    public int size() {
        return accounts.length;
    }

    public synchronized boolean isOpen() {return open;}

    public void closeBank() {
        synchronized (this) {
            open = false;
        }
        for (Account account : accounts) {
            synchronized(account) {
                account.notifyAll();
            }
        }
    }

    public synchronized boolean shouldTest() {
        return ++ntransacts % NTEST == 0;
    }

}
