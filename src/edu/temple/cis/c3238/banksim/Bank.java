package edu.temple.cis.c3238.banksim;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Cay Horstmann
 * @author Modified by Paul Wolfgang
 *
 * @author davidseverns --added ReentrantLock to control access to the sum of the accounts between threads and transfers
 * If a transfer is taking place the transfer thread will lock other threads from testing and if the transfers are done the
 * they will unlock the lock and allow for a test thread to start. before the test thread starts the they will try and aquire the lock
 * if its free they will lock the transfer threads so they can compute the sum and do the test.
 *
 * SUCCESSFUL in calculations for 100000 transaction transfer threads minus the ones that return cause the bank is closed error discused
 * in class but not resolved
 */
public class Bank {

    public static final int NTEST = 10;
    private final Account[] accounts;
    private long ntransacts = 0;
    private final int initialBalance;
    private final int numAccounts;
    private boolean open;
    private long activeTransfers = 0;
    //protect critical sections in code with this lock
    public ReentrantLock lock;

    //will act as a "door man between the transfer and test threads. will allow 10 transfer threads in for
    // 10 permits but a test thread will need all 10 permits to run
    //trying this because skeptical if the reentrant lock is concurrent
    public final Semaphore semaDoorMan;


    public Bank(int numAccounts, int initialBalance) {
        open = true;
        this.initialBalance = initialBalance;
        this.numAccounts = numAccounts;
        accounts = new Account[numAccounts];
        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = new Account(this, i, initialBalance);
        }
        ntransacts = 0;
        semaDoorMan = new Semaphore(numAccounts);
        lock = new ReentrantLock();
    }

    public void transfer(int from, int to, int amount) {
        accounts[from].waitForAvailableFunds(amount);

        if (!open) return;
        //activeTransfers++;

        //try to aquire a permit from the door man

        try{
            semaDoorMan.acquire();
            //try to aquire lock and lock out any testing threads
            //lock.lock();
            if (accounts[from].withdraw(amount)) {
                accounts[to].deposit(amount);
                //activeTransfers--;
            }
        }catch (InterruptedException e){

        }finally {
            //releases a permit to be aquired by another thread(test thread)
            semaDoorMan.release();
        }

        //lock.unlock();
        if (shouldTest() ){

            test();

        }
    }

   public void test() {
        Thread testThread = new BankTestThread(this,accounts,activeTransfers);
        testThread.start();
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

    public int getInitialBalance() {
        return initialBalance;
    }

    public int getNumAccounts() {
        return numAccounts;
    }
}
