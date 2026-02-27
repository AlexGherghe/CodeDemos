import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;

public class CacheLocality {
    public static void main(String[] args) {
        int size = 1_000_000;
        int numberOfRuns = 100;

        List<Integer> arrayList = new ArrayList<>(size);
        List<Integer> linkedList = new LinkedList<>();
        int[] primitiveArray = new int[size];
        for (int i = 0; i < size; i++) {
            arrayList.add(i);
            linkedList.add(i);
            primitiveArray[i] = i;
        }

        long timeArrayList = 0;
        long timeLinkedList = 0;
        long timePrimitiveArray = 0;
        long startTime = 0;
        long endTime = 0;
        for (int i = 0; i < numberOfRuns; i++) {
            // System.nanoTime is a monotonic clock and should be good enough for our purposes
            // ArrayList
            startTime = System.nanoTime();
            iterateAndSum(arrayList);
            endTime = System.nanoTime();
            timeArrayList += (endTime - startTime);

            // LinkedList
            startTime = System.nanoTime();
            iterateAndSum(linkedList);
            endTime = System.nanoTime();
            timeLinkedList += (endTime - startTime);

            // Primitive array
            startTime = System.nanoTime();
            sumPrimitive(primitiveArray);
            endTime = System.nanoTime();
            timePrimitiveArray += (endTime - startTime);
        }
        System.out.println("Average time for ArrayList: " + (timeArrayList / numberOfRuns) / 1000 + " µs");
        System.out.println("Average time for LinkedList: " + (timeLinkedList / numberOfRuns) / 1000 + " µs");
        System.out.println("Average time for a primitive array: " + (timePrimitiveArray / numberOfRuns) / 1000 + " µs");
    }

    private static long iterateAndSum(List<Integer> list) {
        // summing is not really important, I just wanted to prevent the compiler from optimizing away the loop
        long sum = 0;
        for (Integer integer : list) {
            sum += integer;
        }
        return sum;
    }

    private static long sumPrimitive(int[] array) {
        long sum = 0;
        for (int j : array) {
            sum += j;
        }
        return sum;
    }
}