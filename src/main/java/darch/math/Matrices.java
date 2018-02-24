package darch.math;

import javafx.geometry.Point2D;

public class Matrices {

    public static double[] add(double[] m1, double... m2) {
        if (m1.length != m2.length)
            throw new IllegalArgumentException("Arrays not same length");
        final double[] result = new double[m1.length];
        for (int i=0; i < m1.length; i++)
            result[i] = m1[i] + m2[i];
        return result;
    }

    public static double[] multiply(double[] m1, double... m2) {
        if (m1.length != m2.length)
            throw new IllegalArgumentException("Arrays not same length");
        final double[] result = new double[m1.length];
        for (int i=0; i < m1.length; i++)
            result[i] = m1[i] * m2[i];
        return result;
    }


    public static final Point2D multiply(Point2D p1, double x, double y) {
        return new Point2D(p1.getX()*x, p1.getY()*y);
    }

    public static final Point2D multiply(Point2D p1, Point2D p2) {
        return new Point2D(p1.getX()*p2.getX(), p1.getY()*p2.getY());
    }

    public static final Point2D reflect(Point2D point) {
        return new Point2D(point.getY(), point.getX());
    }

    public static double[][] multiply(double[][] m1, double[][] m2) {
        if (m1[0].length != m2.length)
            throw new IllegalArgumentException("Column length not same as multiplier row length");
        final double[][] result = new double[m1.length][m2[0].length];
        for(int i = 0; i < m1.length; i++) {
            for(int j = 0; j < m2[0].length; j++)
                for(int k = 0; k < m1[0].length; k++)
                    result[i][j] += m1[i][k] * m2[k][j];
        }
        return result;
    }



}
