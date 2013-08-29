package de.dainel.cleanqualifiedtypes.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *  This Comparator will invoke other Comparators as necessary in an attempt
 *  to sort on multiple fields from the same object.
 *
 *  The first Comparator in the List is always invoked. The other Comparators
 *  are only invoked as required when the previous Comparator test returns
 *  an "equal" result.
 */
public class GroupComparator<T> implements Comparator<T>
{
	private List<Comparator<T>> comparators = new ArrayList<Comparator<T>>();

	/*
	 *  A GroupComparator will always have at least two Comparators
	 */
	public GroupComparator(Comparator<T> first, Comparator<T> second)
	{
		comparators.add( first );
		comparators.add( second );
	}

	/*
	 *  Add another Comparator to the sort mix.
	 */
	public void addComparator(Comparator<T> comparator)
	{
		comparators.add( comparator );
	}

	/*
	 *  Implement the Comparator interface
	 */
	public int compare(T object1, T object2)
	{
		//  As long as the compare() result is equal (ie. 0),
		//  invoke the next Comparator

		for (Comparator<T> comparator : comparators)
		{
			int returnValue = comparator.compare(object1, object2);

			if (returnValue != 0)
				return returnValue;
		}

		return 0;
	}
}