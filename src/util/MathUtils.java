package util;

import java.util.Arrays;

public final class MathUtils 
{	
	private MathUtils() {}
	
	public static int[] convertToIntPercentages(double[] percentages)
	{
		return convertPercentageToIntValues(percentages, 100);
	}
	
	public static int[] convertPercentageToIntValues(double[] percentages, int size)
	{
		int[] intPercentages = new int[percentages.length];
		double[] intRemainders = new double[percentages.length];
		
		// First go through and separate the decimal portions from the numbers and determine
		// the sum of all the integer portions
		int sum = 0;
		double adjustedForSize;
		for (int percentageIndex = 0; percentageIndex < percentages.length; percentageIndex++)
		{
			adjustedForSize = percentages[percentageIndex] * size;
			intPercentages[percentageIndex] = (int) Math.floor(adjustedForSize);
			intRemainders[percentageIndex] = adjustedForSize - intPercentages[percentageIndex];
			sum += intPercentages[percentageIndex];
		}		
		
		// ensure we haven't already overshot
		if (sum > size)
		{
			throw new IllegalArgumentException("Provided numbers cannot not equal specified size of " + 
					size + " even when all are rounded down: " + Arrays.toString(percentages));
		}
		
		// If that sum is less than 100, then we want to keep choosing the value with the
		// next highest decimal portion to increase since they are the "closest" to being the
		// higher percentages
		for (; sum < size; sum++)
		{
			int highestIndex = -1;
			double highestRemainder = 0;
			
			for (int percentageIndex = 0; percentageIndex < intRemainders.length; percentageIndex++)
			{
				if (intRemainders[percentageIndex] != 0 && intRemainders[percentageIndex] > highestRemainder)
				{
					highestRemainder = intRemainders[percentageIndex];
					highestIndex = percentageIndex;
				}
			}
			
			// If we ran out of numbers to increase, break so we can tell we failed
			if (highestIndex < 0)
			{
				throw new IllegalArgumentException("Provided numbers cannot not equal specified size of " + 
						size + " even when all are rounded up: " + Arrays.toString(percentages));
			}
			
			intPercentages[highestIndex]++;
			intRemainders[highestIndex] = 0;
		}
		
		return intPercentages;
	}

	public static double[] convertNumbersToPercentages(int[] numbers)
	{
		double[] doubles = new double[numbers.length];
		for (int numIndex = 0; numIndex < numbers.length; numIndex++)
		{
			doubles[numIndex] = numbers[numIndex];
		}
		return convertNumbersToPercentages(doubles);
	}
	
	public static double[] convertNumbersToPercentages(double[] numbers)
	{
		double[] percentages = new double[numbers.length];
		double sum = 0;
		for (double num : numbers)
		{
			sum += num;
		}

		if (sum == 0)
		{
			throw new IllegalArgumentException("Summed numbers equal 0!: " + Arrays.toString(numbers));
		}
		
		for (int numIndex = 0; numIndex < numbers.length; numIndex++)
		{
			percentages[numIndex] = numbers[numIndex] / sum;
		}
		
		return percentages;
	}
}
