// (C) 2009 Ralf Laemmel

package data.account;


/**
 * Use pre-, postconditions and invariants to maintain this account.
 */
public class AssertiveAccount {

	private float balance;

	private void invariant() {
		assert balance >= 0;
	}
	
	/**
	 * Return balance in account
	 */
	public float getBalance() {
		// invariant(); // Not needed!
		return balance;
	}
	
	/**
	 * Deposit some money into account
	 */
	public void deposit(float amount) {
	
		// invariant(); // Not needed!
		
		// Precondition
		assert amount >= 0;
		
		// Housekeeping
		float oldBalance = balance;
		
		balance += amount;

		invariant();
		
		// Postcondition
		assert balance >= oldBalance
		    && oldBalance + amount == balance;		
	}
	
	/**
	 * Withdraw some money from account
	 */
	public void withdraw(float amount) {
		
		invariant();
		
		// Precondition
		assert amount >= 0
		    && balance >= amount;

		// Housekeeping
		float oldBalance = balance;
   
		balance -= amount;

		invariant();
		
		// Postcondition
		assert balance <= oldBalance
		    && oldBalance - amount == balance;		
	}	
}
