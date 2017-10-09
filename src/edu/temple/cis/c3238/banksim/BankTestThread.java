package edu.temple.cis.c3238.banksim;

/**
 * @author davidseverns
 * Creating thread object for testing the sum in the bank sim
 * Utilizing the ReentrantLock from the bank to protect the test threads from the
 * transfer threads
 */
public class BankTestThread extends Thread{

    private Bank bank;
    private Account[] accounts;
    private Long activeTransactions;

    public BankTestThread(Bank bank, Account[] accounts, Long activeTransactions){
        this.bank = bank;
        this.accounts = accounts;
        this.activeTransactions = activeTransactions;
    }

    /**
     * Moved logic for test method to this new thread in run method when a BankTransferThread calls
     * start this code will operate.
     */
    @Override
    public void run(){

        //using the Reentrant lock from bank class to lock down all other threads using the bank object,
        //unlocking object after critical section where the sum is being altered
        bank.lock.lock();
        int sum = 0;
        for (Account account : accounts) {
            System.out.printf("%s %s%n",
                    Thread.currentThread().toString(), account.toString());
            sum += account.getBalance();
        }
        //sum is calculated and we can test the sum and allow any waiting transfer threads to execute
        bank.lock.unlock();
        System.out.println(Thread.currentThread().toString() +
                " Sum: " + sum);
        if (sum != bank.getNumAccounts() * bank.getInitialBalance()) {
            System.out.println(Thread.currentThread().toString() +
                    " Money was gained or lost");
            System.exit(1);
        } else {
            System.out.println(Thread.currentThread().toString() +
                    " The bank is in balance");
        }
    }

}
