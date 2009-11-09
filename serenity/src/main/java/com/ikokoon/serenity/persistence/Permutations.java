package com.ikokoon.serenity.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * This class generates the permutations for parameters.
 * 
 * @author Michael Couck
 * @since 06.10.09
 * @version 01.00
 */
public class Permutations {

	private Logger logger = Logger.getLogger(Permutations.class);

	public void getPermutations(Object[] objects) {
		int N = objects.length;
		Object[] newObjects = new Object[N];
		for (int i = 0; i < N; i++) {
			newObjects[i] = objects[i];
		}
		List<Object[]> permutations = new ArrayList<Object[]>();
		getPermutations(newObjects, permutations, N);
	}

	public void getPermutations(Object[] objects, List<Object[]> permutations, int n) {
		if (n == 1) {
			for (Object object : objects) {
				logger.debug(object + ":");
			}
			permutations.add(Arrays.copyOf(objects, objects.length));
			return;
		}
		for (int i = 0; i < n; i++) {
			swap(objects, i, n - 1);
			getPermutations(objects, permutations, n - 1);
			swap(objects, i, n - 1);
		}
	}

	private Object[] swap(Object[] objects, int i, int j) {
		Object s = objects[i];
		objects[i] = objects[j];
		objects[j] = s;
		return objects;
	}

}