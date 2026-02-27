import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class FalseSharing {

    private static final int ITERATIONS = 10_000_000;
    private static final int NUMBER_OF_RUNS = 10;

    // VarHandle lets us perform volatile reads and writes on individual
    // elements of a plain long[], without the CAS overhead of AtomicLongArray.
    private static final VarHandle ELEMENT = MethodHandles.arrayElementVarHandle(long[].class);
    private static final long[] data = new long[128];

    public static void main(String[] args) throws InterruptedException {
        // Warm up the JVM so that JIT compilation doesn't skew results
        benchmarkAdjacentIndices();
        benchmarkDistantIndices();

        long timeAdjacent = 0;
        long timeDistant = 0;

        for (int run = 0; run < NUMBER_OF_RUNS; run++) {
            timeAdjacent += benchmarkAdjacentIndices();
            timeDistant += benchmarkDistantIndices();
        }

        System.out.println("Adjacent indices (data[0] and data[1], same cache line): "
                + (timeAdjacent / NUMBER_OF_RUNS / 1_000_000) + " ms");
        System.out.println("Distant indices (data[0] and data[32], different cache lines): "
                + (timeDistant / NUMBER_OF_RUNS / 1_000_000) + " ms");
    }

    private static long benchmarkAdjacentIndices() throws InterruptedException {
        data[0] = 0;
        data[1] = 0;

        Thread t1 = new Thread(() -> {
            for (int i = 0; i < ITERATIONS; i++) {
                ELEMENT.setVolatile(data, 0, (long) ELEMENT.getVolatile(data, 0) + 1);
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < ITERATIONS; i++) {
                ELEMENT.setVolatile(data, 1, (long) ELEMENT.getVolatile(data, 1) + 1);
            }
        });

        long start = System.nanoTime();
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        return System.nanoTime() - start;
    }

    private static long benchmarkDistantIndices() throws InterruptedException {
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
