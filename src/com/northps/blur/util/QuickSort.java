/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2012 Mike Patterson and In Koo
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights 
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all 
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A 
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION 
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.northps.blur.util;

import java.util.List;

/**
 * @author mikepatterson
 * @param <T>
 *
 */
public class QuickSort<T> {
	private List <T> values;
	private int number;

	public void sort(List <T> values) {
		// Check for empty or null array
		if (values ==null || values.size()==0){
			return;
		}
		this.values = values;
		number = values.size();
		quicksort(0, number - 1);
	}

	private void quicksort(int low, int high) {
		int i = low, j = high;
		// Get the pivot element from the middle of the list
		T pivot = (T) values.get(low + (high-low)/2);
		
		// Divide into two lists
		while (i <= j) {
			// If the current value from the left list is larger then the pivot
			// element then get the next element from the left list
			while (((Comparable<T>) values.get(i)).compareTo(pivot) > 0) {
				i++;
			}
			// If the current value from the right list is smaller then the pivot
			// element then get the next element from the right list
			while (((Comparable<T>) values.get(j)).compareTo(pivot) < 0) {
				j--;
			}

			// If we have found a values in the left list which is smaller then
			// the pivot element and if we have found a value in the right list
			// which is larger then the pivot element then we exchange the
			// values.
			// As we are done we can increase i and j
			if (i <= j) {
				exchange(i, j);
				i++;
				j--;
			}
		}
		// Recursion
		if (low < j)
			quicksort(low, j);
		if (i < high)
			quicksort(i, high);
	}

	private void exchange(int i, int j) {
		T temp = values.get(i);
		values.set(i, values.get(j));
		values.set(j, temp);
	}

}
