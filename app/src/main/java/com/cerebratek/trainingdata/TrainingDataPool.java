package com.cerebratek.trainingdata;

public class TrainingDataPool {

	private static double[][][] trainingData;
	private static double[][][] testData;
	private static double[][] weight;
	private static double[][][] filterbank_weight;
	private static double[][][][] filterbank_trainingData;

	public static double[][][] getTrainingData() {
		return trainingData;
	}

	public static void setTrainingData(double[][][] data) {
		trainingData = data;
	}

	public static double[][] getWeight() {
		return weight;
	}

	public static void setWeight(double[][] weight) {
		TrainingDataPool.weight = weight;
	}

	public static double[][][] getTestData() {
		return testData;
	}

	public static void setTestData(double[][][] testData) {
		TrainingDataPool.testData = testData;
	}

	public static void setFilterbankWeight(double[][][] w) {filterbank_weight = w; }

	public static double[][][] getFilterbankWeight() {return filterbank_weight; }

	public static void setFilterbankTrainingData(double[][][][] data) {filterbank_trainingData = data;}

	public static double[][][][] getFilterbankTrainingData() {return filterbank_trainingData;}
	
}
