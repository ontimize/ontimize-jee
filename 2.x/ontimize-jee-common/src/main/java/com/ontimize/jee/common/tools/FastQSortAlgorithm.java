/*
 *
 */
package com.ontimize.jee.common.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class FastQSortAlgorithm.
 */
public class FastQSortAlgorithm {

	private static final Logger logger = LoggerFactory.getLogger(FastQSortAlgorithm.class);

	/**
	 * This is a generic version of C.A.R Hoare's Quick Sort algorithm. This will handle arrays that are already sorted, and arrays with duplicate keys.<BR> If you think of a one
	 * dimensional array as going from the lowest index on the left to the highest index on the right then the parameters to this function are lowest index or left and highest
	 * index or right. The first time you call this function it will be with the parameters 0, a.length - 1.
	 *
	 * @param a
	 *            an integer array
	 * @param l
	 *            the l
	 * @param r
	 *            the r
	 * @param indexes
	 *            the indexes
	 */
	private static void quickSort(Object a[], int l, int r, int[] indexes) {
		int M = 4;
		int i;
		int j;
		Object v;

		if ((r - l) > M) {
			i = (int) ((r + (long) l) / 2);
			if (FastQSortAlgorithm.compare(a[l], a[i]) > 0) {
				FastQSortAlgorithm.swap(a, l, i, indexes); // Tri-Median Methode!
			}
			if (FastQSortAlgorithm.compare(a[l], a[r]) > 0) {
				FastQSortAlgorithm.swap(a, l, r, indexes);
			}
			if (FastQSortAlgorithm.compare(a[i], a[r]) > 0) {
				FastQSortAlgorithm.swap(a, i, r, indexes);
			}

			j = r - 1;
			FastQSortAlgorithm.swap(a, i, j, indexes);
			i = l;
			v = a[j];
			for (;;) {
				while (FastQSortAlgorithm.compare(a[++i], v) < 0) {
				}
				while (FastQSortAlgorithm.compare(a[--j], v) > 0) {
				}
				if (j < i) {
					break;
				}
				FastQSortAlgorithm.swap(a, i, j, indexes);
			}
			FastQSortAlgorithm.swap(a, i, r - 1, indexes);
			FastQSortAlgorithm.quickSort(a, l, j, indexes);
			FastQSortAlgorithm.quickSort(a, i + 1, r, indexes);
		}
	}

	/**
	 * Compare.
	 *
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the int
	 */
	private static int compare(Object a, Object b) {
		if ((a == null) && (b == null)) {
			return 0;
		}
		if (a == null) {
			return -1;
		} else if (b == null) {
			return 1;
		} else {
			return ((Comparable) a).compareTo(b);
		}
	}

	/**
	 * Swap.
	 *
	 * @param a
	 *            the a
	 * @param i
	 *            the i
	 * @param j
	 *            the j
	 * @param indexes
	 *            the indexes
	 */
	private static void swap(Object a[], int i, int j, int[] indexes) {
		int tmp;
		tmp = indexes[i];
		indexes[i] = indexes[j];
		indexes[j] = tmp;

		Object T;
		T = a[i];
		a[i] = a[j];
		a[j] = T;
	}

	/**
	 * Insertion sort.
	 *
	 * @param a
	 *            the a
	 * @param lo0
	 *            the lo0
	 * @param hi0
	 *            the hi0
	 * @param indexes
	 *            the indexes
	 */
	private static void insertionSort(Object a[], int lo0, int hi0, int[] indexes) {
		int i;
		int j;
		Object v;
		int tmp;

		for (i = lo0 + 1; i <= hi0; i++) {
			v = a[i];
			tmp = indexes[i];
			j = i;
			while ((j > lo0) && (FastQSortAlgorithm.compare(a[j - 1], v) > 0)) {
				a[j] = a[j - 1];
				indexes[j] = indexes[j - 1];
				j--;
			}
			a[j] = v;
			indexes[j] = tmp;
		}
	}

	/**
	 * Sort.
	 *
	 * @param a
	 *            the a
	 * @return the int[]
	 */
	public static int[] sort(Object a[]) {
		int[] indexes = new int[a.length];
		for (int i = 0; i < indexes.length; i++) {
			indexes[i] = i;
		}
		FastQSortAlgorithm.quickSort(a, 0, a.length - 1, indexes);
		FastQSortAlgorithm.insertionSort(a, 0, a.length - 1, indexes);
		return indexes;
	}

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		try {
			Integer[] aa = new Integer[] { 3, 24, 542, 1234, 56346, 642, 3414, 5436, 473, 2412, 4121, 41234, 54, 67, 4, 87676, 674646, 346, 342423, 42154, 36, 54, 34643, 64 };
			String[] a = new String[] { "hola", "adios", "caracola", "flor", null, "traza", "identificador", "marcador" };

			FastQSortAlgorithm.logger.debug("%20s:", "Cabecera");
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < a.length; i++) {
				sb.append(String.format("%8d", i));
			}
			FastQSortAlgorithm.logger.info(sb.toString());
			FastQSortAlgorithm.logger.info("");

			FastQSortAlgorithm.logger.info(String.format("%20s:", "Inicial"));
			sb = new StringBuilder();
			for (int i = 0; i < a.length; i++) {
				sb.append(String.format("%8s", a[i]));
			}
			FastQSortAlgorithm.logger.info(sb.toString());
			FastQSortAlgorithm.logger.info("");

			int[] indexes = FastQSortAlgorithm.sort(a);
			FastQSortAlgorithm.logger.info(String.format("%20s:", "Ordenado"));
			sb = new StringBuilder();
			for (int i = 0; i < a.length; i++) {
				sb.append(String.format("%8s", a[i]));
			}
			FastQSortAlgorithm.logger.info(sb.toString());
			FastQSortAlgorithm.logger.info("");

			FastQSortAlgorithm.logger.info(String.format("%20s:", "Indices"));
			sb = new StringBuilder();
			for (int i = 0; i < a.length; i++) {
				sb.append(String.format("%8d", indexes[i]));
			}
			FastQSortAlgorithm.logger.info(sb.toString());
			FastQSortAlgorithm.logger.info("");
		} catch (Exception e) {
			FastQSortAlgorithm.logger.error(null, e);
		}
	}
}
