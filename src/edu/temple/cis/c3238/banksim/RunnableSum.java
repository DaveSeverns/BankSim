package edu.temple.cis.c3238.banksim;



/*
@author David Severns
This runnable object will be used to perform the critical actions for out test threads
These Test Threads should tell the sum of each account in the bank sum with out interfering with transfer threads
or without being interfered with by them
 */

import java.util.ArrayList;

public class RunnableSum implements Runnable {

    private volatile int sum;

    private final ArrayList<Account> accountArrayList;

    //constructor for the RunnableSum odj
    //takes a list of account and sets the sum of them to 0 initially
    public RunnableSum(ArrayList accountList){

        this.sum = 0;
        this.accountArrayList = accountList;

    }

    @Override
    public void run() {
        System.out.println("The sum of all the accounts is: "+getSum());
    }

    /**
     * the synchronized will keep this method thread safe meaning that any other running thread should not interfere
     * with the actions taken by this thread
     * @return
     */
    public synchronized int getSum(){
        //iterate through account and add the balance to the sum
        for(int i =0 ; i < accountArrayList.size(); i++){
            sum += accountArrayList.get(i).getBalance();
        }
        return sum;
    }
}
