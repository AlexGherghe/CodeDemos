import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class SharedDataBetweenThreads {

    private static final int ITERATIONS = 10_000_000;
    private static final int NUMBER_OF_RUNS = 10;

    // VarHandle lets us perform volatile reads and writes on individual
    // elements of a plain long[], without the CAS overhead of AtomicLongArray.
    private static final VarHandle ELEMENT = MethodHandles.arrayElementVarHandle(long[].class);
    private static final long[] data = new long[128];

    public static void main(String[] args) throws InterruptedException {
        // Warm up the JVM so that JIT compilation doesn't skew results
        benchmarkSameLocation();
        benchmarkDifferentCacheLines();

        long timeSame = 0;
        long timeDifferent = 0;

        for (int run = 0; run < NUMBER_OF_RUNS; run++) {
            timeSame += benchmarkSameLocation();
            timeDifferent += benchmarkDifferentCacheLines();
        }

        System.out.println("Same location (both threads write to data[0]):               "
                + (timeSame / NUMBER_OF_RUNS / 1_000_000) + " ms");
        System.out.println("Different cache lines (threads write to data[0] and data[32]): "
                + (timeDifferent / NUMBER_OF_RUNS / 1_000_000) + " ms");
    }

    private static long benchmarkSameLocation() throws InterruptedException {
        data[0] = 0;

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < ITERATIONS; i++) {
                ELEMENT.setVolatile(data, 0, (long) ELEMENT.getVolatile(data, 0) + 1);
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < ITERATIONS; i++) {
                ELEMENT.setVolatile(data, 0, (long) ELEMENT.getVolatile(data, 0) + 1);
            }
        });

        long start = System.nanoTime();
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        return System.nanoTime() - start;
    }

    private static long benchmarkDifferentCacheLines() throws InterruptedException {
        data[0] = 0;
        data[32] = 0;

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < ITERATIONS; i++) {
                ELEMENT.setVolatile(data, 0, (long) ELEMENT.getVolatile(data, 0) + 1);
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < ITERATIONS; i++) {
                ELEMENT.setVolatile(data, 32, (long) ELEMENT.getVolatile(data, 32) + 1);
            }
        });

        long start = System.nanoTime();
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        return System.nanoTime() - start;
    }
}
