package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;
import edu.rice.pcdp.PCDP;

public final class SieveActor extends Sieve {
    
	@Override
    public int countPrimes(final int limit) {
    	int numPrimes = 0;
    	final SieveActorActor sieveActor = new SieveActorActor(2);
        PCDP.finish(() -> {
            for (int i = 3; i <= limit; i += 2) {
                sieveActor.send(i);
            }
            sieveActor.send(0);
        });

        SieveActorActor loopActor = sieveActor;
        while (loopActor != null) {
            numPrimes += loopActor.numLocalPrimes();
            loopActor = loopActor.nextActor();
        }

        return numPrimes;
    }
	
    public static final class SieveActorActor extends Actor {
    	private static int MAX_LOCAL_PRIMES = 1000;
        private int[] localPrimes;
        private int numLocalPrimes;
        private SieveActorActor nextActor;
        
        public SieveActorActor(int localPrime) {
            this.localPrimes = new int[MAX_LOCAL_PRIMES];
            this.localPrimes[0] = localPrime;
            this.numLocalPrimes = 1;
            this.nextActor = null;
        }
        
        public SieveActorActor nextActor() { return nextActor; }

        public int numLocalPrimes() { return numLocalPrimes; }
	
        @Override
        public void process(final Object msg) {
            final int candidate = (Integer) msg;
            if (candidate <= 0) {
                if (nextActor != null) {
                    nextActor.send(msg);
                }
            } else {
                final boolean locallyPrime = isLocallyPrime(candidate);
                if (locallyPrime) {
                    if (numLocalPrimes < MAX_LOCAL_PRIMES) {
                        localPrimes[numLocalPrimes] = candidate;
                        numLocalPrimes += 1;
                    } else if (nextActor == null) {
                        nextActor = new SieveActorActor(candidate);
                    } else {
                        nextActor.send(msg);
                    }
                }
            }
        }

        private boolean isLocallyPrime(final int candidate) {
            for (int i = 0; i < numLocalPrimes; i++) {
                if (candidate % localPrimes[i] == 0) {
                    return false;
                }
            }
            return true;
        
        }    
    }
}
