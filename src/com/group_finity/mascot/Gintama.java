package com.group_finity.mascot;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: magi
 * Date: 12-12-20
 * Time: 下午1:30
 */
public class Gintama implements Runnable {
    protected static boolean active = false;
    protected static final boolean disable = false;

    public static void active(ArrayList<Mascot> mascots) {
        active = true;
        new Thread(new Gintama(mascots)).start();
    }

    private final ArrayList<Mascot> mascots;
    private final int stageWith, stageHeight;

    @SuppressWarnings("unchecked")
    private Gintama(ArrayList<Mascot> input) {
        mascots = (ArrayList<Mascot>) input.clone();
        stageWith = mascots.get(0).getEnvironment().getScreen().getWidth();
        stageHeight = mascots.get(0).getEnvironment().getScreen().getHeight();
    }

    @Override
    public void run() {
        final int grid = 60, w = 9, h = 9;
        final byte[][] matrixYin = {{0, 0, 1, 0, 1, 1, 1, 1, 1}, {0, 1, 0, 1, 1, 0, 0, 0, 1}, {1, 0, 0, 0, 1, 1, 1, 1, 1}, {0, 1, 1, 0, 1, 0, 0, 0, 1}, {0, 0, 1, 0, 1, 1, 1, 1, 1}, {1, 1, 1, 1, 1, 0, 1, 0, 0}, {0, 0, 1, 0, 1, 0, 1, 0, 1}, {1, 0, 1, 1, 1, 0, 0, 1, 0}, {1, 1, 1, 0, 1, 1, 1, 0, 1}};
        final byte[][] matrixHun = {{0, 0, 0, 0, 0, 1, 0, 0, 0}, {1, 1, 1, 0, 1, 1, 1, 1, 1}, {0, 0, 0, 0, 1, 0, 1, 0, 1}, {1, 1, 1, 0, 1, 1, 1, 1, 1}, {0, 1, 0, 0, 1, 0, 1, 0, 1}, {0, 1, 0, 0, 1, 1, 1, 1, 1}, {1, 0, 1, 0, 1, 0, 1, 1, 0}, {1, 1, 1, 0, 1, 0, 1, 0, 1}, {0, 0, 0, 1, 0, 0, 1, 1, 1},};
        final byte[][] matrixYi = {{0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 1, 0, 0}, {1, 1, 1, 1, 1, 1, 1, 1, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0},};
        final byte[][] matrixShen = {{0, 1, 0, 0, 1, 0, 0, 0, 0}, {0, 1, 0, 0, 1, 0, 0, 0, 0}, {0, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 0, 0, 0, 1, 0, 0, 0, 0}, {0, 0, 0, 0, 1, 0, 0, 0, 0}, {0, 1, 1, 1, 1, 1, 1, 1, 0}, {0, 0, 0, 0, 1, 0, 0, 0, 0}, {0, 0, 0, 0, 1, 0, 0, 0, 0}, {1, 1, 1, 1, 1, 1, 1, 1, 1},};
        final byte[][] matrixAi = {{0, 1, 1, 1, 1, 1, 1, 1, 0}, {0, 0, 1, 0, 1, 0, 1, 0, 0}, {1, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 0, 1, 1, 0, 1, 0, 0, 1}, {0, 1, 0, 1, 0, 0, 1, 1, 0}, {0, 0, 0, 1, 1, 1, 1, 0, 0}, {0, 1, 1, 1, 0, 0, 1, 0, 0}, {0, 0, 0, 0, 1, 1, 0, 0, 0}, {1, 1, 1, 1, 0, 0, 1, 1, 1},};
        final byte[][] matrixLove = {{0, 0, 0, 0, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1, 1, 1, 1}, {1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0}, {1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1, 1, 1, 1}, {1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0}, {1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 1, 0, 0, 0}, {1, 1, 1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 1, 1}, {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};
        int left, top;
        byte[][] matrix;
        int i;
        left = -200 + (stageWith - grid * w) / 2;
        top = (stageHeight - grid * h) / 2;
        matrix = matrixYin;
        i = 0;
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                if (matrix[y][x] != 0) {
                    mascots.get(i++).setGintama(grid * x + left, grid * y + top);
                }
        while (i < mascots.size()) {
            mascots.get(i++).clearGintama();
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        left = +200 + (stageWith - grid * w) / 2;
        top = (stageHeight - grid * h) / 2;
        matrix = matrixHun;
        i = 0;
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                if (matrix[y][x] != 0) {
                    mascots.get(i++).setGintama(grid * x + left, grid * y + top);
                }
        while (i < mascots.size()) {
            mascots.get(i++).clearGintama();
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final int lovew = 21, loveh = 8;
        left = (stageWith - grid * lovew) / 2;
        top = (stageHeight - grid * loveh) / 2;
        matrix = matrixLove;
        i = 0;
        for (int y = 0; y < loveh; y++)
            for (int x = 0; x < lovew; x++)
                if (matrix[y][x] != 0) {
                    mascots.get(i++).setGintama(grid * x + left, grid * y + top);
                }
        while (i < mascots.size()) {
            mascots.get(i++).clearGintama();
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //        left = - 300 + (stageWith - grid * w) / 2;
        //        top = (stageHeight - grid * h) / 2;
        //        matrix = matrixYi;
        //        i = 0;
        //        for (int y = 0; y < h; y++)
        //            for (int x = 0; x < w; x++)
        //                if (matrix[y][x] != 0) {
        //                    mascots.get(i++).setGintama(grid * x + left, grid * y + top);
        //                }
        //        left = + 300 + (stageWith - grid * w) / 2;
        //        top = (stageHeight - grid * h) / 2;
        //        matrix = matrixShen;
        //        for (int y = 0; y < h; y++)
        //            for (int x = 0; x < w; x++)
        //                if (matrix[y][x] != 0) {
        //                    mascots.get(i++).setGintama(grid * x + left, grid * y + top);
        //                }
        //        while (i < mascots.size()) {
        //            mascots.get(i++).clearGintama();
        //        }
        //        try {
        //            Thread.sleep(5000);
        //        } catch (InterruptedException e) {
        //            e.printStackTrace();
        //        }
        //        left = (stageWith - grid * w) / 2;
        //        top =  (stageHeight - grid * h) / 2;
        //        matrix = matrixAi;
        //        i = 0;
        //        for (int y = 0; y < h; y++)
        //            for (int x = 0; x < w; x++)
        //                if (matrix[y][x] != 0) {
        //                    mascots.get(i++).setGintama(grid * x + left, grid * y + top);
        //                }
        //        while (i < mascots.size()) {
        //            mascots.get(i++).clearGintama();
        //        }
        //        try {
        //            Thread.sleep(5000);
        //        } catch (InterruptedException e) {
        //            e.printStackTrace();
        //        }
        i = 0;
        while (i < mascots.size()) {
            mascots.get(i++).clearGintama();
        }
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        active = false;
    }

}
