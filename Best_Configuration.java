package PMOM;

import java.util.ArrayList;

public class Best_Configuration {

	String nameData;
	double threshold_min;
	double threshold_max;
	ArrayList<String> properties_1;

	String nameData2;
	double threshold2_min;
	double threshold2_max;
	
	int min_support1;
	int min_support2;
	ArrayList<String> properties_2;

	double thresholdSim;

	int found_mappings;
	int all_mappings;
	int correct;

	double Precision;
	double Recall;
	double F_measure;

}
