package org.example;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class Main {

    public static int threadCount = 4;
    public static int start = 0;
    public static int winCount = 3;

    public static Map<Long, Integer> roundResults = new ConcurrentHashMap<>();
    public static Map<Long, Integer> statistic = new HashMap<>();

    public static void main(String[] args) throws InterruptedException {

        Semaphore semaphore1 = new Semaphore(threadCount);
        Semaphore semaphore2 = new Semaphore(threadCount);

        MyThread[] threads = new MyThread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new MyThread();
        }

        for (MyThread thread : threads) {
            thread.semaphore1 = semaphore1;
            thread.semaphore2 = semaphore2;
            thread.start();
        }

        while (true) {
            Thread.sleep(10);
            if (start == 1) {
                List<Long> winnersIds = getWinner(roundResults);
                if (winnersIds == null){
                    System.out.println("DROW! REPEAT...");
                    start = 0;
                    continue;
                }
                for (Long winnerId : winnersIds) {
                    Integer IDsWinCount = statistic.get(winnerId);
                    if (IDsWinCount == null) {
                        IDsWinCount = 0;
                    }
                    statistic.put(winnerId, (IDsWinCount + 1));
                    int winner = 0;
                    for (Integer count : statistic.values()){
                        if (count == winCount){
                            winner++;
                            System.out.println("GAME WINNER: " + winnerId);
                        }
                    }
                    if (winner > 0){
                        System.out.println("Game over");
                        for (MyThread thread : threads) {
                            thread.stop();
                        }
                        return;
                    }
                }
                roundResults.clear();
                start = 0;
            }
        }
    }

    private static List<Long> getWinner(Map<Long, Integer> results) {

        List<Long> winners = new ArrayList<>();

        if (results.containsValue(0) && results.containsValue(1) && results.containsValue(2) ||
                results.containsValue(0) && !results.containsValue(1) && !results.containsValue(2) ||
                !results.containsValue(0) && !results.containsValue(1) && results.containsValue(2) ||
                results.containsValue(0) && !results.containsValue(1) && results.containsValue(2)) {
            return null;
        }

        if (!results.containsValue(2)) {
            roundResults.forEach((key, value) -> {
                if (value == 0) {
                    winners.add(key);
                    System.out.println(key + " HAS WON the ROUND!");
                }
            });
            return winners;
        }

        if (!results.containsValue(1)) {
            roundResults.forEach((key, value) -> {
                if (value == 2) {
                    winners.add(key);
                    System.out.println(key + " HAS WON the ROUND!");
                }
            });
            return winners;
        }


        if (!results.containsValue(0)) {
            roundResults.forEach((key, value) -> {
                if (value == 1) {
                    winners.add(key);
                    System.out.println(key + " HAS WON the ROUND!");
                }
            });
        }
        return winners;
    }

    static class MyThread extends Thread {

        Semaphore semaphore1;
        Semaphore semaphore2;
        public int condition = 0;

       public void run(){
           while (true) {
               try {
                   semaphore1.acquire();
                   condition = getRandomNumberUsingInts(0, 3);
                   roundResults.put(currentThread().getId(), condition);
                   printConditions(condition);
                   try {
                       Thread.sleep(getRandomNumberUsingInts(100, 500));
                   } catch (InterruptedException e) {
                       throw new RuntimeException(e);
                   }
                   semaphore2.acquire();
                   if (semaphore2.availablePermits() == 0) {
                       start = 1;
                       Thread.sleep(20);
                       semaphore1.release(threadCount);
                       semaphore2.release(threadCount);
                   }

               } catch (InterruptedException e) {
                   throw new RuntimeException(e);
               }
           }
       }

        synchronized void printConditions(int condition){
           switch (condition){
               case 0:
                   System.out.println(Thread.currentThread().getId() + " - ROCK");
                   break;
               case 1:
                   System.out.println(Thread.currentThread().getId() + " - SCISSORS");
                   break;
               case 2:
                   System.out.println(Thread.currentThread().getId() + " - PAPER");
           }
       }

    }

    public static int getRandomNumberUsingInts(int min, int max) {
        Random random = new Random();
        return random.ints(min, max)
                .findFirst()
                .getAsInt();
    }
}