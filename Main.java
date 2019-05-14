package PMOM;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileManager;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;

public class Main {

	public static ArrayList<Instance> Reading_File_RDF(String filename, int code)
			throws IOException {
		System.out.println("BEGIN Reading_File_RDF");

		Model model = ModelFactory.createDefaultModel();
		// FileManager to find the input file
		InputStream in = FileManager.get().open(filename);
		if (in == null) {
			throw new IllegalArgumentException("File: " + filename
					+ " not found");
		}
		// read the file RDF/XML
		model.read(in, null);
		StmtIterator item = model.listStatements();
		ArrayList<Instance> list = new ArrayList<Instance>();
		ArrayList<Instance> list_rest = new ArrayList<Instance>();

		// Select PERSON into a list and the others into rest list (address,
		// state subsurb)
		while (item.hasNext()) {
			Statement stmt = item.nextStatement();
			Resource subject = stmt.getSubject();
			Property predicate = stmt.getPredicate();
			RDFNode object = stmt.getObject();
			String h = subject.getLocalName().toString();
			h = h.replace("person" + code + "-", "");
			if (h.contains("Person")) {
				boolean bool = false;
				int indice = -1;
				for (int i = 0; i < list.size(); i++) {
					if (h.equals(list.get(i).id)) {
						bool = true;
						indice = i;
					}
				}
				if (bool == false) {
					Instance new_ins = new Instance();
					new_ins.id = h;
					new_ins.Prop_Val = new ArrayList<Prop_Val>();

					Prop_Val new_one = new Prop_Val();
					new_one.Property = predicate.getLocalName();
					if (object instanceof Resource) {
						new_one.Value = ((Resource) object).getLocalName()
								.toString();
					} else {
						new_one.Value = object.toString();
					}
					if (!(new_one.Value.equals(""))
							&& !(new_one.Value.equals(null))) {
						new_ins.Prop_Val.add(new_one);
					}
					list.add(new_ins);
				} else {
					Prop_Val new_one = new Prop_Val();
					new_one.Property = predicate.getLocalName();
					if (object instanceof Resource) {
						new_one.Value = ((Resource) object).getLocalName()
								.toString();
					} else {
						new_one.Value = object.toString();
					}
					if (!(new_one.Value.equals(""))
							&& !(new_one.Value.equals(null))) {
						list.get(indice).Prop_Val.add(new_one);
					}
				}
			} else {
				boolean bool = false;
				int indice = -1;
				for (int i = 0; i < list_rest.size(); i++) {
					if (h.equals(list_rest.get(i).id)) {
						bool = true;
						indice = i;
					}
				}
				if (bool == false) {
					Instance new_ins = new Instance();
					new_ins.id = h;
					new_ins.Prop_Val = new ArrayList<Prop_Val>();
					Prop_Val new_one = new Prop_Val();
					new_one.Property = predicate.getLocalName();
					if (object instanceof Resource) {
						new_one.Value = ((Resource) object).getLocalName()
								.toString();
					} else {
						new_one.Value = object.toString();
					}
					if (!(new_one.Value.equals(""))
							&& !(new_one.Value.equals(null))) {

						new_ins.Prop_Val.add(new_one);
					}
					list_rest.add(new_ins);
				} else {
					Prop_Val new_one = new Prop_Val();
					new_one.Property = predicate.getLocalName();
					if (object instanceof Resource) {
						new_one.Value = ((Resource) object).getLocalName()
								.toString();
					} else {
						new_one.Value = object.toString();
					}
					if (!(new_one.Value.equals(""))
							&& !(new_one.Value.equals(null))) {
						list_rest.get(indice).Prop_Val.add(new_one);
					}
				}
			}
		}

		// Complete of the predicate with the rest of list
		for (int i = 0; i < list.size(); i++) {
			for (int j = 0; j < list.get(i).Prop_Val.size(); j++) {
				if (list.get(i).Prop_Val.get(j).Value.contains("person" + code
						+ "-")) {
					for (int i1 = 0; i1 < list_rest.size(); i1++) {
						if (list_rest.get(i1).id.equals(list.get(i).Prop_Val
								.get(j).Value
								.replace("person" + code + "-", ""))) {
							for (int j1 = 0; j1 < list_rest.get(i1).Prop_Val
									.size(); j1++) {
								if (!(list_rest.get(i1).Prop_Val.get(j1).Property
										.equals("type"))) {
									Prop_Val new_one = new Prop_Val();
									new_one.Property = list_rest.get(i1).Prop_Val
											.get(j1).Property.concat("_"
											+ list_rest.get(i1).id);
									new_one.Value = list_rest.get(i1).Prop_Val
											.get(j1).Value;
									if (!(new_one.Value.equals(""))
											&& !(new_one.Value.equals(null))) {
										list.get(i).Prop_Val.add(new_one);
									}
								}
							}
							list_rest.remove(i1);
						}
						list.get(i).Prop_Val.get(j).Value = list.get(i).Prop_Val
								.get(j).Value
								.replace("person" + code + "-", "");
					}
				}
			}
		}

		System.out.println("END Reading_File_RDF");
		return list;
	} // END Reading_File_RDF

	// detecting the type property
	public static ArrayList<Properties> Stat_Properties(
			ArrayList<Instance> DataSet) {
		System.out.println("Begin Stat_Properties");

		ArrayList<Properties> DataSet_Prop = new ArrayList<Properties>();
		for (int i = 0; i < DataSet.size(); i++) {
			for (int j = 0; j < DataSet.get(i).Prop_Val.size(); j++) {
				if (i == 0 & j == 0) {
					Properties e = new Properties();
					e.id = new ArrayList<String>();
					e.variation = new ArrayList<String>();
					e.name = DataSet.get(i).Prop_Val.get(j).Property;
					e.id.add(DataSet.get(i).id);
					e.variation.add(DataSet.get(i).Prop_Val.get(j).Value);
					if (!(e.name.equals("type"))) {
						DataSet_Prop.add(e);
					}
				}
				int k = 0;
				while (k < DataSet_Prop.size()) {
					if (DataSet_Prop.get(k).name.toLowerCase().equals(
							DataSet.get(i).Prop_Val.get(j).Property.replaceAll(
									"[\\d]", "").toLowerCase())) {
						if (!(DataSet_Prop.get(k).variation.contains(DataSet
								.get(i).Prop_Val.get(j).Value))
								& !(DataSet.get(i).Prop_Val.get(j).Value
										.equals(""))) {
							DataSet_Prop.get(k).variation
									.add(DataSet.get(i).Prop_Val.get(j).Value);
						}
						if (!(DataSet.get(i).Prop_Val.get(j).Value.equals(""))) {
							DataSet_Prop.get(k).id.add(DataSet.get(i).id);
						}
						k = DataSet_Prop.size() + 100;

					} else {
						k++;
					}
				}// while
				if (!(k == DataSet_Prop.size() + 100)) {
					Properties e = new Properties();
					e.id = new ArrayList<String>();
					e.variation = new ArrayList<String>();
					e.name = DataSet.get(i).Prop_Val.get(j).Property
							.replaceAll("[\\d]", "");
					e.id.add(DataSet.get(i).id);
					e.variation.add(DataSet.get(i).Prop_Val.get(j).Value);
					if (!(e.name.equals("type"))) {
						DataSet_Prop.add(e);
					}
				}
			}// for
		}// for

		// calcul of the degree
		for (int j = 0; j < DataSet_Prop.size(); j++) {
			DataSet_Prop.get(j).degree = (float) DataSet_Prop.get(j).variation
					.size() / DataSet_Prop.get(j).id.size();
		}
		System.out.println("END Stat_Properties");
		return DataSet_Prop;
	} // the end of the fuction Stat_Properties

	public static Properties[] Sort_Stat_Properties(ArrayList<Properties> Table) {
		System.out.println("Begin Sort_Stat_Properties");
		Properties Table_sort[] = new Properties[Table.size()];
		int index_table = 0;
		for (int i = 0; i < Table.size(); i++) {
			if (!(Table.get(i).degree == -1)) {
				float max = Table.get(i).degree;
				int index = i;
				for (int j = 0; j < Table.size(); j++) {
					if (Table.get(j).degree > max) {
						max = Table.get(j).degree;
						index = j;
					}
				}
				Table_sort[index_table] = new Properties();
				Table_sort[index_table].id = new ArrayList<String>();
				Table_sort[index_table].variation = new ArrayList<String>();

				Table_sort[index_table].name = Table.get(index).name;
				Table_sort[index_table].id = Table.get(index).id;
				Table_sort[index_table].variation = Table.get(index).variation;
				Table_sort[index_table].degree = Table.get(index).degree;
				Table.get(index).degree = -1;
				index_table++;
				i = -1;

			}
		}
		// calcul of the degree
		for (int j = 0; j < Table.size(); j++) {
			Table.get(j).degree = (float) Table.get(j).variation.size()
					/ Table.get(j).id.size();
		}
		System.out.println("END Sort_Stat_Properties");
		return Table_sort;
	} // the end of the function Sort_Stat_Properties

	// this function from
	// https://introcs.cs.princeton.edu/java/31datatype/Soundex.java.html
	public static String soundex(String s) {
		char[] x = s.toUpperCase().toCharArray();
		char firstLetter = x[0];
		// convert letters to numeric code
		for (int i = 0; i < x.length; i++) {
			switch (x[i]) {
			case 'B':
			case 'F':
			case 'P':
			case 'V':
				x[i] = '1';
				break;
			case 'C':
			case 'G':
			case 'J':
			case 'K':
			case 'Q':
			case 'S':
			case 'X':
			case 'Z':
				x[i] = '2';
				break;
			case 'D':
			case 'T':
				x[i] = '3';
				break;
			case 'L':
				x[i] = '4';
				break;
			case 'M':
			case 'N':
				x[i] = '5';
				break;
			case 'R':
				x[i] = '6';
				break;
			default:
				x[i] = '0';
				break;
			}
		}
		// remove duplicates
		String output = "" + firstLetter;
		for (int i = 1; i < x.length; i++)
			if (x[i] != x[i - 1] && x[i] != '0')
				output += x[i];
		// pad with 0's or truncate
		output = output + "0000";
		return output.substring(0, 4);
	}// the end of the function soundex

	@SuppressWarnings("deprecation")
	public static String Encodind_Date_of_birth(String s) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date date = dateFormat.parse(s);
		int age;
		age = 2007 - (date.getYear() + 1900);
		return Integer.toString(age);
	} // the end of the function Encodind_Date_of_birth

	public static ArrayList<Common_instances> Basic_Matching(
			ArrayList<Instance> DataSet1, ArrayList<Instance> DataSet2,
			double threshold) {

		System.out.println("Begin Basic_Matching");
		ArrayList<Common_instances> common = new ArrayList<Common_instances>();
		for (int i = 0; i < DataSet1.size(); i++) {
			for (int k = 0; k < DataSet2.size(); k++) {
				for (int j = 0; j < DataSet1.get(i).Prop_Val.size(); j++) {
					for (int l = 0; l < DataSet2.get(k).Prop_Val.size(); l++) {
						if ((!(DataSet1.get(i).Prop_Val.get(j).Encoding
								.equals("")))
								&& (!(DataSet2.get(k).Prop_Val.get(l).Encoding
										.equals("")))) {
							if (DataSet1.get(i).Prop_Val.get(j).Encoding
									.equals(DataSet2.get(k).Prop_Val.get(l).Encoding)) {
								// The same encoding
								// research the 2 individuals if they
								// existe in the common array
								int c = 0;
								while (c < common.size()) {
									if (DataSet1.get(i).id
											.equals(common.get(c).Individual1.id)
											&& DataSet2.get(k).id.equals(common
													.get(c).Individual2.id)) {
										c = common.size() + 100;
									}
									c++;
								}
								if (c <= common.size()) {
									// the two individuals not exist
									// creation a new common instance
									Common_instances in = new Common_instances();
									in.Individual1 = new Instance();
									in.Individual2 = new Instance();
									in.Individual1.Prop_Val = new ArrayList<Prop_Val>();
									in.Individual2.Prop_Val = new ArrayList<Prop_Val>();
									in.Individual1.Prop_Val = DataSet1.get(i).Prop_Val;
									in.Individual1.id = DataSet1.get(i).id;
									in.Individual2.Prop_Val = DataSet2.get(k).Prop_Val;
									in.Individual2.id = DataSet2.get(k).id;

									common.add(in);
								}
							}
						}
					}
				}
			}
		}

		for (int i = 0; i < common.size(); i++) {
			common.get(i).common = new Instance();
			common.get(i).common.Prop_Val = new ArrayList<Prop_Val>();

			for (int j = 0; j < common.get(i).Individual1.Prop_Val.size(); j++) {
				for (int k = 0; k < common.get(i).Individual2.Prop_Val.size(); k++) {

					if (common.get(i).Individual1.Prop_Val.get(j).Value
							.equals(common.get(i).Individual2.Prop_Val.get(k).Value)
							&& !(common.get(i).Individual1.Prop_Val.get(j).Value
									.equals(""))) {
						Prop_Val e = new Prop_Val();
						e.Value = common.get(i).Individual1.Prop_Val.get(j).Value;
						e.Property = common.get(i).Individual1.Prop_Val.get(j).Property;
						e.Encoding = common.get(i).Individual1.Prop_Val.get(j).Encoding
								.concat(" "
										+ common.get(i).Individual2.Prop_Val
												.get(k).Encoding);
						common.get(i).common.Prop_Val.add(e);
					}
				}
			}
			common.get(i).Similarité_degree = (float) common.get(i).common.Prop_Val
					.size()
					/ (common.get(i).Individual1.Prop_Val.size() + common
							.get(i).Individual2.Prop_Val.size());

			// // threashold of selilarities
			if (common.get(i).Similarité_degree < threshold) {
				common.remove(i);
				i = -1;
			}

		}
		System.out.println("END Basic_Matching");
		return common;
	} // end function Basic_MAtching

	public static ArrayList<Common_instances> Basic_Matching_specific(
			ArrayList<Instance> DataSet1, ArrayList<String> Prop1,
			ArrayList<Instance> DataSet2, ArrayList<String> Prop2,
			double threshold) {

		System.out.println("Begin Basic_Matching_specific");
		ArrayList<Common_instances> common = new ArrayList<Common_instances>();

		// select the two data set
		for (int i = 0; i < DataSet1.size(); i++) {
			for (int k = 0; k < DataSet2.size(); k++) {
				for (int j = 0; j < DataSet1.get(i).Prop_Val.size(); j++) {

					// the condition to check if the property is in the selected
					// properties
					if (Prop1
							.contains(DataSet1.get(i).Prop_Val.get(j).Property)) {

						for (int l = 0; l < DataSet2.get(k).Prop_Val.size(); l++) {

							if (Prop2
									.contains(DataSet2.get(k).Prop_Val.get(l).Property)) {

								if ((!(DataSet1.get(i).Prop_Val.get(j).Encoding
										.equals("")))
										&& (!(DataSet2.get(k).Prop_Val.get(l).Encoding
												.equals("")))) {
									if (DataSet1.get(i).Prop_Val.get(j).Encoding
											.equals(DataSet2.get(k).Prop_Val
													.get(l).Encoding)) {
										// The same encoding
										// research the 2 individuals if they
										// existe in the common array
										int c = 0;
										while (c < common.size()) {
											if (DataSet1.get(i).id
													.equals(common.get(c).Individual1.id)
													&& DataSet2.get(k).id
															.equals(common
																	.get(c).Individual2.id)) {
												c = common.size() + 100;
											}
											c++;
										}
										if (c <= common.size()) {
											// the two individuals not exist
											// creation a new common instance
											Common_instances in = new Common_instances();
											in.Individual1 = new Instance();
											in.Individual2 = new Instance();
											in.Individual1.Prop_Val = new ArrayList<Prop_Val>();
											in.Individual2.Prop_Val = new ArrayList<Prop_Val>();
											in.Individual1.Prop_Val = DataSet1
													.get(i).Prop_Val;
											in.Individual1.id = DataSet1.get(i).id;
											in.Individual2.Prop_Val = DataSet2
													.get(k).Prop_Val;
											in.Individual2.id = DataSet2.get(k).id;

											common.add(in);
										}
									}
								}
							}
						}
					}
				}
			}
		}

		for (int i = 0; i < common.size(); i++) {
			common.get(i).common = new Instance();
			common.get(i).common.Prop_Val = new ArrayList<Prop_Val>();

			for (int j = 0; j < common.get(i).Individual1.Prop_Val.size(); j++) {
				for (int k = 0; k < common.get(i).Individual2.Prop_Val.size(); k++) {

					if (common.get(i).Individual1.Prop_Val.get(j).Value
							.equals(common.get(i).Individual2.Prop_Val.get(k).Value)
							&& !(common.get(i).Individual1.Prop_Val.get(j).Value
									.equals(""))) {
						Prop_Val e = new Prop_Val();
						e.Value = common.get(i).Individual1.Prop_Val.get(j).Value;
						e.Property = common.get(i).Individual1.Prop_Val.get(j).Property;
						e.Encoding = common.get(i).Individual1.Prop_Val.get(j).Encoding
								.concat(" "
										+ common.get(i).Individual2.Prop_Val
												.get(k).Encoding);
						common.get(i).common.Prop_Val.add(e);
					}
				}
			}
			common.get(i).Similarité_degree = (float) common.get(i).common.Prop_Val
					.size()
					/ (common.get(i).Individual1.Prop_Val.size() + common
							.get(i).Individual2.Prop_Val.size());
			// // threashold of selilarities
			if (common.get(i).Similarité_degree < threshold) {
				common.remove(i);
				i = -1;
			}
		}
		System.out.println("END Basic_Matching_specific");
		return common;
	} // end function Basic_Matching_specific

	public static Common_instances[] Sort_Basic_Matching(
			ArrayList<Common_instances> Common) {
		System.out.println("Begin Sort_Basic_Matching");
		Common_instances Sort_common[] = new Common_instances[Common.size()];
		int cas = 0;

		for (int i = 0; i < Common.size(); i++) {
			if (Common.get(i).common.Prop_Val != null) {
				int max = Common.get(i).common.Prop_Val.size();
				int index = i;
				for (int j = 0; j < Common.size(); j++) {
					if (Common.get(j).common.Prop_Val != null
							&& Common.get(j).common.Prop_Val.size() > max) {
						max = Common.get(j).common.Prop_Val.size();
						index = j;
					}
				}
				Sort_common[cas] = new Common_instances();
				Sort_common[cas].common = new Instance();
				Sort_common[cas].Individual1 = new Instance();
				Sort_common[cas].Individual2 = new Instance();
				Sort_common[cas].Individual1.Prop_Val = new ArrayList<Prop_Val>();
				Sort_common[cas].Individual2.Prop_Val = new ArrayList<Prop_Val>();
				Sort_common[cas].common.Prop_Val = new ArrayList<Prop_Val>();
				Sort_common[cas].common.id = Common.get(index).common.id;
				Sort_common[cas].common.Prop_Val = Common.get(index).common.Prop_Val;
				Sort_common[cas].Individual1.id = Common.get(index).Individual1.id;
				Sort_common[cas].Individual1.Prop_Val = Common.get(index).Individual1.Prop_Val;
				Sort_common[cas].Individual2.id = Common.get(index).Individual2.id;
				Sort_common[cas].Individual2.Prop_Val = Common.get(index).Individual2.Prop_Val;
				Sort_common[cas].Similarité_degree = (float) Sort_common[cas].common.Prop_Val
						.size()
						/ (Sort_common[cas].Individual1.Prop_Val.size() + Sort_common[cas].Individual2.Prop_Val
								.size());
				cas++;
				i = -1;
				Common.get(index).common.Prop_Val = null;
			}
		}

		// After we renisialise the prop_val with null , we re wirite the list
		// here
		for (int i = 0; i < Common.size(); i++) {
			Common.get(i).common = new Instance();
			Common.get(i).common.Prop_Val = new ArrayList<Prop_Val>();

			for (int j = 0; j < Common.get(i).Individual1.Prop_Val.size(); j++) {
				for (int k = 0; k < Common.get(i).Individual2.Prop_Val.size(); k++) {

					if (Common.get(i).Individual1.Prop_Val.get(j).Value
							.equals(Common.get(i).Individual2.Prop_Val.get(k).Value)
							&& !(Common.get(i).Individual1.Prop_Val.get(j).Value
									.equals(""))) {

						Prop_Val e = new Prop_Val();
						e.Value = Common.get(i).Individual1.Prop_Val.get(j).Value;
						e.Property = Common.get(i).Individual1.Prop_Val.get(j).Property;
						e.Encoding = Common.get(i).Individual1.Prop_Val.get(j).Encoding
								.concat(" "
										+ Common.get(i).Individual2.Prop_Val
												.get(k).Encoding);
						Common.get(i).common.Prop_Val.add(e);

					}
				}
			}
			Common.get(i).Similarité_degree = (float) Common.get(i).common.Prop_Val
					.size()
					/ (Common.get(i).Individual1.Prop_Val.size() + Common
							.get(i).Individual2.Prop_Val.size());
		}
		System.out.println("END Sort_Basic_Matching");
		return Sort_common;
	} // end function Basic_MAtching

	public static Common_instances[] Sort_Basic_Matching_Degree(
			ArrayList<Common_instances> Common) {
		System.out.println("Begin Sort_Basic_Matching_Degree");
		Common_instances Sort_common[] = new Common_instances[Common.size()];
		int cas = 0;
		for (int i = 0; i < Common.size(); i++) {
			if (!Common.get(i).Similarité_degree.equals((float) 0)) {
				float max = Common.get(i).Similarité_degree;
				int index = i;
				for (int j = 0; j < Common.size(); j++) {
					if (!(Common.get(j).Similarité_degree.equals((float) 0))
							&& Common.get(j).Similarité_degree > max) {
						max = Common.get(j).Similarité_degree;
						index = j;
					}
				}
				Sort_common[cas] = new Common_instances();
				Sort_common[cas].common = new Instance();
				Sort_common[cas].Individual1 = new Instance();
				Sort_common[cas].Individual2 = new Instance();
				Sort_common[cas].Individual1.Prop_Val = new ArrayList<Prop_Val>();
				Sort_common[cas].Individual2.Prop_Val = new ArrayList<Prop_Val>();
				Sort_common[cas].common.Prop_Val = new ArrayList<Prop_Val>();
				Sort_common[cas].common.id = Common.get(index).common.id;
				Sort_common[cas].common.Prop_Val = Common.get(index).common.Prop_Val;
				Sort_common[cas].Individual1.id = Common.get(index).Individual1.id;
				Sort_common[cas].Individual1.Prop_Val = Common.get(index).Individual1.Prop_Val;
				Sort_common[cas].Individual2.id = Common.get(index).Individual2.id;
				Sort_common[cas].Individual2.Prop_Val = Common.get(index).Individual2.Prop_Val;
				Sort_common[cas].Similarité_degree = Common.get(index).Similarité_degree;
				cas++;
				i = -1;
				Common.get(index).Similarité_degree = (float) 0;
			}
		}

		System.out.println("END Sort_Basic_Matching_Degree");
		return Sort_common;
	} // end function Basic_MAtching_Degree

	public static Boolean Recherche_Items_Freq(Items_frequente New,
			ArrayList<Items_frequente> Old) {

		for (int j = 0; j < Old.size(); j++) {
			if (New.List_item.size() == Old.get(j).List_item.size()) {
				int length = 0;
				for (int j2 = 0; j2 < Old.get(j).List_item.size(); j2++) {

					for (int i = 0; i < New.List_item.size(); i++) {

						if ((Old.get(j).List_item.get(j2).Property
								.equals(New.List_item.get(i).Property))
								& (Old.get(j).List_item.get(j2).Value
										.equals(New.List_item.get(i).Value))) {
							length++;
							// i = New.List_item.size() + 1;
						}
					}

				} // end of for Old list d'item j2
				if (length == Old.get(j).List_item.size()) {
					return true;
				}

			} // end of if the same size
		}// end of for Old j

		return false;
	} // end function Recherche_Items_Freq

	public static ArrayList<Items_frequente> Items_Freq(
			ArrayList<Instance> DataSet, Properties[] PropSet, int min_support,
			ArrayList<String> Prop_String) {
		System.out.println("Begin Items_Freq");
		ArrayList<Items_frequente> items_frequent = new ArrayList<Items_frequente>();

		System.out.println("items size 1");
		// select all the properties
		for (int i = 0; i < PropSet.length; i++) {
			// creat for each variation item of size 1
			for (int i1 = 0; i1 < PropSet[i].variation.size(); i1++) {
				// creation of new item
				Items_frequente e = new Items_frequente();
				e.List_item = new ArrayList<Prop_Val>();
				e.Instance_item = new ArrayList<Instance>();
				Prop_Val p = new Prop_Val();
				p.Property = PropSet[i].name;
				p.Value = PropSet[i].variation.get(i1);

				if (Prop_String.contains(PropSet[i].name)) {
					p.Encoding = soundex(p.Value);
				} else {
					p.Encoding = "";
				}
				e.List_item.add(p);
				// recherche if the item it exist
				if (Recherche_Items_Freq(e, items_frequent) == false) {
					// no exist
					// recherche the set of instance into the data set
					// e.Instance_item.add(DataSet.get(k));
					/**  **/
					for (int k = 0; k < DataSet.size(); k++) {
						for (int k2 = 0; k2 < DataSet.get(k).Prop_Val.size(); k2++) {
							if (Prop_String.contains(PropSet[i].name)) {
								if (DataSet.get(k).Prop_Val.get(k2).Property
										.replaceAll("[\\d]", "").equals(
												p.Property)
										&& DataSet.get(k).Prop_Val.get(k2).Encoding
												.equals(soundex(p.Value))) {
									e.Instance_item.add(DataSet.get(k));
								}
							} else {
								if (DataSet.get(k).Prop_Val.get(k2).Property
										.replaceAll("[\\d]", "").equals(
												p.Property)
										&& DataSet.get(k).Prop_Val.get(k2).Value
												.equals(p.Value)) {
									e.Instance_item.add(DataSet.get(k));
								}
							}
						}
					}

					// add the new item
					if (e.Instance_item.size() > min_support) {
						items_frequent.add(e);
					}
				}
			}
		} // end for variation

		// items size 2
		int end_item1 = items_frequent.size();
		System.out.println("items size 2 \t" + end_item1);

		for (int i = 0; i < end_item1; i++) {
			for (int i1 = i + 1; i1 < end_item1; i1++) {
				// creation the item
				Items_frequente e = new Items_frequente();
				e.List_item = new ArrayList<Prop_Val>();
				e.Instance_item = new ArrayList<Instance>();

				for (int j = 0; j < items_frequent.get(i).List_item.size(); j++) {
					Prop_Val p = new Prop_Val();
					p.Property = items_frequent.get(i).List_item.get(j).Property;
					p.Value = items_frequent.get(i).List_item.get(j).Value;
					p.Encoding = items_frequent.get(i).List_item.get(j).Encoding;
					e.List_item.add(p);
				}
				for (int j = 0; j < items_frequent.get(i1).List_item.size(); j++) {
					for (int j2 = 0; j2 < e.List_item.size(); j2++) {
						if (!(e.List_item.get(j2).Property
								.equals(items_frequent.get(i1).List_item.get(j).Property))) {
							Prop_Val p = new Prop_Val();
							p.Property = items_frequent.get(i1).List_item
									.get(j).Property;
							p.Value = items_frequent.get(i1).List_item.get(j).Value;
							p.Encoding = items_frequent.get(i1).List_item
									.get(j).Encoding;
							e.List_item.add(p);
						}
					}
				}
				// recherche if the item it exist
				if (Recherche_Items_Freq(e, items_frequent) == false) {
					// not exist
					// recherche the set of instance into the data set
					//
					if (e.List_item.size() > 1) {
						// research the instance
						// i & i1 are the index of the two items
						for (int j = 0; j < items_frequent.get(i).Instance_item
								.size(); j++) {
							for (int j2 = 0; j2 < items_frequent.get(i1).Instance_item
									.size(); j2++) {
								if (items_frequent.get(i).Instance_item.get(j).id
										.equals(items_frequent.get(i1).Instance_item
												.get(j2).id)) {
									e.Instance_item
											.add(items_frequent.get(i).Instance_item
													.get(j));
								}
							}
						}
						// add the new item
						if (e.Instance_item.size() > min_support) {
							items_frequent.add(e);
						}
					}
				}

			}
		} // end for

		// items size 3 is a commbinason of size 2 with one common element
		int end_item2 = items_frequent.size();
		System.out.println("items size 3 \t" + end_item2);

		for (int i = end_item1 + 1; i < end_item2; i++) {
			for (int i1 = i + 1; i1 < end_item2; i1++) {
				boolean exist = false;
				// check if we have an element in common
				for (int j = 0; j < items_frequent.get(i).List_item.size(); j++) {
					for (int j2 = 0; j2 < items_frequent.get(i1).List_item
							.size(); j2++) {
						if (items_frequent.get(i).List_item.get(j).Property
								.equals(items_frequent.get(i1).List_item
										.get(j2).Property)) {
							if (items_frequent.get(i).List_item.get(j).Encoding
									.equals("")
									|| items_frequent.get(i1).List_item.get(j2).Encoding
											.equals("")) {
								if (items_frequent.get(i).List_item.get(j).Value
										.equals(items_frequent.get(i1).List_item
												.get(j2).Value)) {
									exist = true;
									j2 = items_frequent.get(i1).List_item
											.size() + 10;
									j = items_frequent.get(i).List_item.size() + 10;
								}
							} else {
								if (items_frequent.get(i).List_item.get(j).Encoding
										.equals(items_frequent.get(i1).List_item
												.get(j2).Encoding)) {
									exist = true;
									j2 = items_frequent.get(i1).List_item
											.size() + 10;
									j = items_frequent.get(i).List_item.size() + 10;
								}
							}
						}
					}
				}

				if (exist == true) {
					// creation new item
					Items_frequente e = new Items_frequente();
					e.List_item = new ArrayList<Prop_Val>();
					e.Instance_item = new ArrayList<Instance>();

					for (int j = 0; j < items_frequent.get(i).List_item.size(); j++) {
						Prop_Val p = new Prop_Val();
						p.Property = items_frequent.get(i).List_item.get(j).Property;
						p.Value = items_frequent.get(i).List_item.get(j).Value;
						p.Encoding = items_frequent.get(i).List_item.get(j).Encoding;
						e.List_item.add(p);
					}
					for (int j2 = 0; j2 < items_frequent.get(i1).List_item
							.size(); j2++) {
						boolean y = false;
						for (int j = 0; j < e.List_item.size(); j++) {
							if (e.List_item.get(j).Property
									.equals(items_frequent.get(i1).List_item
											.get(j2).Property)) {
								y = true;
							}
						}
						if (y == false) {
							Prop_Val p = new Prop_Val();
							p.Property = items_frequent.get(i1).List_item
									.get(j2).Property;
							p.Value = items_frequent.get(i1).List_item.get(j2).Value;
							p.Encoding = items_frequent.get(i1).List_item
									.get(j2).Encoding;
							e.List_item.add(p);
						}

					}
					if (Recherche_Items_Freq(e, items_frequent) == false) {
						// research the instance
						// i & i1 are the index of the two items
						if (e.List_item.size() == 3) {
							for (int j = 0; j < items_frequent.get(i).Instance_item
									.size(); j++) {
								for (int j2 = 0; j2 < items_frequent.get(i1).Instance_item
										.size(); j2++) {
									if (items_frequent.get(i).Instance_item
											.get(j).id.equals(items_frequent
											.get(i1).Instance_item.get(j2).id)) {
										e.Instance_item.add(items_frequent
												.get(i).Instance_item.get(j));
									}
								}
							}
							if (e.Instance_item.size() > min_support) {
								items_frequent.add(e);
							}
						}
					}
				} // enf if exist == true
			}
		} // end for

		System.out.println("END Items_Freq");
		return items_frequent;
	} // end function Items_Freq

	public static ArrayList<RdfAlign> Lecture_rdfalign(String Filename) {
		System.out.println("Begin Lecture_rdfalign");

		ArrayList<RdfAlign> Array = new ArrayList<RdfAlign>();
		final DocumentBuilderFactory factory = DocumentBuilderFactory
				.newInstance();
		try {
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document document = builder.parse(new File(Filename));
			org.w3c.dom.Element racine = document.getDocumentElement();

			// Affichage de l'élément racine
			// System.out.println("\n*************RACINE************");
			// System.out.println((racine.getNodeName()));

			final NodeList racineNoeuds0 = racine.getChildNodes();
			for (int i = 0; i < racineNoeuds0.getLength(); i++) {
				org.w3c.dom.Node Cell0 = racineNoeuds0.item(i);
				if (Cell0.getNodeName().equals("Alignment")) {
					NodeList racineNoeuds1 = Cell0.getChildNodes();
					// System.out.println("1- " + Cell0.getNodeName());
					for (int j = 0; j < racineNoeuds1.getLength(); j++) {
						org.w3c.dom.Node Cell1 = racineNoeuds1.item(j);
						if (Cell1.getNodeName().equals("map")) {
							NodeList racineNoeuds2 = Cell1.getChildNodes();
							// System.out.println("\t 2- " +
							// Cell1.getNodeName());
							for (int k = 0; k < racineNoeuds2.getLength(); k++) {
								org.w3c.dom.Node Cell2 = racineNoeuds2.item(k);
								if (Cell2.getNodeName().equals("Cell")) {
									NodeList racineNoeuds3 = Cell2
											.getChildNodes();
									// System.out.println("\t\t 3- "
									// + Cell2.getNodeName());
									RdfAlign e = new RdfAlign();

									for (int l = 0; l < racineNoeuds3
											.getLength(); l++) {
										org.w3c.dom.Node Cell3 = racineNoeuds3
												.item(l);

										if (Cell3.getNodeName().equals(
												"entity1")) {
											String entity = ((org.w3c.dom.Element) Cell3)
													.getAttribute("rdf:resource");
											String chaineASupprimer = "http://www.okkam.org/oaie/person1-";
											entity = entity.replace(
													chaineASupprimer, "");
											// System.out.println(entity);
											e.entity1 = entity;
										}
										if (Cell3.getNodeName().equals(
												"entity2")) {
											String entity = ((org.w3c.dom.Element) Cell3)
													.getAttribute("rdf:resource");
											String chaineASupprimer = "http://www.okkam.org/oaie/person2-";
											entity = entity.replace(
													chaineASupprimer, "");
											// System.out.println(entity);
											// System.out.println();
											e.entity2 = entity;
										}
									}
									Array.add(e);

								}

							}
						}
					}

				}

			}
		} catch (final ParserConfigurationException e) {
			e.printStackTrace();
		} catch (final SAXException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}

		System.out.println("END Lecture_rdfalign");

		return Array;
	} // the end of the function Lecture_rdfalig

	public static void main(String[] args) throws IOException, ParseException {

		/*
		 * ** Parameters of the program **
		 */
		String filename1 = "C:/Users/DZCOMP/workspace/feature test/person1/person11.rdf";
		String filename2 = "C:/Users/DZCOMP/workspace/feature test/person1/person12.rdf";

		String filename_rdfalign = "C:/Users/DZCOMP/workspace/feature test/person1/dataset11_dataset12_goldstandard_person.xml";

		String nameData = "Personne11";
		String nameData2 = "Personne12";

		int code1 = 1;
		int code2 = 2;


		// the encoding Properties
		ArrayList<String> Prop_Date = new ArrayList<String>();
		Prop_Date.add("date_of_birth");

		ArrayList<String> Prop_String = new ArrayList<String>();
		Prop_String.add("surname");
		Prop_String.add("given_name");
		Prop_String.add("street_Address");
		Prop_String.add("name_Suburb");
		Prop_String.add("name_State");
		Prop_String.add("suburb_Address");
		Prop_String.add("state_Address");

		// the matching process
		// thresold semilarity

		double threshold_sim = 0.15;

		// the minimum support of item set
		int min_support = 1;
		int min_support2 = 5;

		// measures
		int all_mappings = 0;
		int found_mappings = 0;
		int correct = 0;
		double Precision = 0.0;
		double Recall = 0.0;
		double F_measure = 0.0;

		// The selection of the best configuration in the selection of the
		// threshold
		Best_Configuration best = new Best_Configuration();
		best.properties_1 = new ArrayList<String>();
		best.properties_2 = new ArrayList<String>();
		all_mappings = 0;
		found_mappings = 0;
		correct = 0;
		Precision = 0.0;
		Recall = 0.0;
		F_measure = 0.0;

		Date aujourdhui = new Date();
		DateFormat shortDateFormat = DateFormat.getDateTimeInstance(
				DateFormat.SHORT, DateFormat.SHORT);
		System.out.println(shortDateFormat.format(aujourdhui));

		long startTime = System.currentTimeMillis();

		/*
		 * ** Read the file of dataset **
		 */
		ArrayList<Instance> DataSet1 = Reading_File_RDF(filename1, code1);
		ArrayList<Instance> DataSet2 = Reading_File_RDF(filename2, code2);

		// The data into a file
		File Data = new File(nameData + ".txt");
		FileWriter Dataset = new FileWriter(Data, false);
		for (int i = 0; i < DataSet1.size(); i++) {
			Dataset.write(DataSet1.get(i).id);
			for (int j = 0; j < DataSet1.get(i).Prop_Val.size(); j++) {
				Dataset.write("\t" + DataSet1.get(i).Prop_Val.get(j).Property);
				Dataset.write("\t " + DataSet1.get(i).Prop_Val.get(j).Value
						+ "\n");
			}
		}
		Dataset.close();
		File Data2 = new File(nameData2 + ".txt");
		FileWriter Dataset2 = new FileWriter(Data2, false);
		for (int i = 0; i < DataSet2.size(); i++) {
			Dataset2.write(DataSet2.get(i).id);
			for (int j = 0; j < DataSet2.get(i).Prop_Val.size(); j++) {
				Dataset2.write("\t" + DataSet2.get(i).Prop_Val.get(j).Property);
				Dataset2.write("\t " + DataSet2.get(i).Prop_Val.get(j).Value
						+ "\n");
			}
		}
		Dataset2.close();

		/*
		 * ** Read the file of rdfAlign**
		 */

		ArrayList<RdfAlign> Fich_rdfalign = Lecture_rdfalign(filename_rdfalign);
		// Print into a file the rdf align
		File R = new File("rdf_align.txt");
		FileWriter Rf = new FileWriter(R, false);
		for (int i = 0; i < Fich_rdfalign.size(); i++) {
			Rf.write(i + "\t" + Fich_rdfalign.get(i).entity1 + "\t"
					+ Fich_rdfalign.get(i).entity2 + "\n");
		}
		Rf.close();

		/*
		 * ** Encoding properties **
		 */

		for (int i = 0; i < DataSet1.size(); i++) {
			for (int j = 0; j < DataSet1.get(i).Prop_Val.size(); j++) {
				if (Prop_Date
						.contains(DataSet1.get(i).Prop_Val.get(j).Property)) {
					if (!DataSet1.get(i).Prop_Val.get(j).Value.equals("")) {
						DataSet1.get(i).Prop_Val.get(j).Encoding = Encodind_Date_of_birth(DataSet1
								.get(i).Prop_Val.get(j).Value);
					} else {
						DataSet1.get(i).Prop_Val.get(j).Encoding = "";
					}
				} else if (Prop_String
						.contains(DataSet1.get(i).Prop_Val.get(j).Property
								.replaceAll("[\\d]", ""))) {
					if (!DataSet1.get(i).Prop_Val.get(j).Value.equals("")) {
						DataSet1.get(i).Prop_Val.get(j).Encoding = soundex(DataSet1
								.get(i).Prop_Val.get(j).Value);
					} else {
						DataSet1.get(i).Prop_Val.get(j).Encoding = "";
					}
				} else {
					DataSet1.get(i).Prop_Val.get(j).Encoding = "";
				}
			}
		}

		for (int i = 0; i < DataSet2.size(); i++) {
			for (int j = 0; j < DataSet2.get(i).Prop_Val.size(); j++) {
				if (Prop_Date
						.contains(DataSet2.get(i).Prop_Val.get(j).Property)) {
					if (!DataSet2.get(i).Prop_Val.get(j).Value.equals("")) {
						DataSet2.get(i).Prop_Val.get(j).Encoding = Encodind_Date_of_birth(DataSet2
								.get(i).Prop_Val.get(j).Value);
					} else {
						DataSet2.get(i).Prop_Val.get(j).Encoding = "";
					}
				} else if (Prop_String
						.contains(DataSet2.get(i).Prop_Val.get(j).Property
								.replaceAll("[\\d]", ""))) {
					if (!DataSet2.get(i).Prop_Val.get(j).Value.equals("")) {
						DataSet2.get(i).Prop_Val.get(j).Encoding = soundex(DataSet2
								.get(i).Prop_Val.get(j).Value);
					} else {
						DataSet2.get(i).Prop_Val.get(j).Encoding = "";
					}
				} else {
					DataSet2.get(i).Prop_Val.get(j).Encoding = "";
				}
			}
		}

		// Print into a file the Encoding properties
		File DataSet1_Encoding = new File(nameData + "_Encoding.txt");
		FileWriter DS1 = new FileWriter(DataSet1_Encoding, false);
		for (int i = 0; i < DataSet1.size(); i++) {
			DS1.write((i + 1) + "-- " + DataSet1.get(i).id + "\n");
			for (int j = 0; j < DataSet1.get(i).Prop_Val.size(); j++) {
				if (DataSet1.get(i).Prop_Val.get(j).Encoding == "") {
					DS1.write("\t" + DataSet1.get(i).Prop_Val.get(j).Property
							+ "\t" + DataSet1.get(i).Prop_Val.get(j).Value
							+ "\n");
				} else {
					DS1.write("\t" + DataSet1.get(i).Prop_Val.get(j).Property
							+ "\t" + DataSet1.get(i).Prop_Val.get(j).Value
							+ " \t" + DataSet1.get(i).Prop_Val.get(j).Encoding
							+ "\n");
				}
			}
		}
		DS1.close();

		File DataSet2_Encoding = new File(nameData2 + "_Encoding.txt");
		FileWriter DS2 = new FileWriter(DataSet2_Encoding, false);
		for (int i = 0; i < DataSet2.size(); i++) {
			DS2.write((i + 1) + "-- " + DataSet2.get(i).id + "\n");
			for (int j = 0; j < DataSet2.get(i).Prop_Val.size(); j++) {
				if (DataSet2.get(i).Prop_Val.get(j).Encoding == "") {
					DS2.write("\t" + DataSet2.get(i).Prop_Val.get(j).Property
							+ "\t" + DataSet2.get(i).Prop_Val.get(j).Value
							+ "\n");
				} else {
					DS2.write("\t" + DataSet2.get(i).Prop_Val.get(j).Property
							+ "\t" + DataSet2.get(i).Prop_Val.get(j).Value
							+ " \t" + DataSet2.get(i).Prop_Val.get(j).Encoding
							+ "\n");
				}
			}
		}
		DS2.close();

		/*
		 * ** The Statistique of each property **
		 */

		ArrayList<Properties> DataSet1_Prop = Stat_Properties(DataSet1);
		ArrayList<Properties> DataSet2_Prop = Stat_Properties(DataSet2);

		// the sort the statistics
		Properties[] Sort_Prop_DataSet1 = Sort_Stat_Properties(DataSet1_Prop);
		Properties[] Sort_Prop_DataSet2 = Sort_Stat_Properties(DataSet2_Prop);

		System.out.println("SORT the properties of" + nameData);
		for (int j = 0; j < Sort_Prop_DataSet1.length; j++) {
			System.out.println(j + "\t" + Sort_Prop_DataSet1[j].name + "\t"
					+ Sort_Prop_DataSet1[j].variation.size() + "\t"
					+ Sort_Prop_DataSet1[j].id.size() + "\t"
					+ Sort_Prop_DataSet1[j].degree);
		}
		System.out.println("SORT the properties of" + nameData2);
		for (int j = 0; j < Sort_Prop_DataSet2.length; j++) {
			System.out.println(j + "\t" + Sort_Prop_DataSet2[j].name + "\t"
					+ Sort_Prop_DataSet2[j].variation.size() + "\t"
					+ Sort_Prop_DataSet2[j].id.size() + "\t"
					+ Sort_Prop_DataSet2[j].degree);
		}
		// Print into a file
		File DataSet_pro1 = new File(nameData + "_statistical_Property.txt");
		FileWriter DS_pro1 = new FileWriter(DataSet_pro1, false);
		for (int j = 0; j < Sort_Prop_DataSet1.length; j++) {
			DS_pro1.write(Sort_Prop_DataSet1[j].name + "\t \t"
					+ Sort_Prop_DataSet1[j].variation.size() + "\t \t"
					+ Sort_Prop_DataSet1[j].id.size() + "\t \t"
					+ Sort_Prop_DataSet1[j].degree + "\n");
		}
		DS_pro1.close();

		File DataSet_pro2 = new File(nameData2 + "_statistical_Property.txt");
		FileWriter DS_pro2 = new FileWriter(DataSet_pro2, false);
		for (int j = 0; j < Sort_Prop_DataSet2.length; j++) {
			DS_pro2.write(Sort_Prop_DataSet2[j].name + "\t \t"
					+ Sort_Prop_DataSet2[j].variation.size() + "\t \t"
					+ Sort_Prop_DataSet2[j].id.size() + "\t \t"
					+ Sort_Prop_DataSet2[j].degree + "\n");
		}
		DS_pro2.close();

		/*
		 * ** PMOM-based Strategy **
		 */

		/*
		 * befor we calculate the items we should eleminate the variation into
		 * the encoding string (the same variation has same encoding eliminte
		 * the second )
		 */

		for (int i = 0; i < Sort_Prop_DataSet1.length; i++) {
			for (int j = 0; j < Sort_Prop_DataSet1[i].variation.size(); j++) {
				for (int j1 = j + 1; j1 < Sort_Prop_DataSet1[i].variation
						.size(); j1++) {
					if (Prop_String.contains(Sort_Prop_DataSet1[i].name)) {
						if (soundex(Sort_Prop_DataSet1[i].variation.get(j))
								.equals(soundex(Sort_Prop_DataSet1[i].variation
										.get(j1)))) {
							Sort_Prop_DataSet1[i].variation.remove(j1);
						}
					}
				}
			}
		}

		for (int i = 0; i < Sort_Prop_DataSet2.length; i++) {
			for (int j = 0; j < Sort_Prop_DataSet2[i].variation.size(); j++) {
				for (int j1 = j + 1; j1 < Sort_Prop_DataSet2[i].variation
						.size(); j1++) {
					if (Prop_String.contains(Sort_Prop_DataSet2[i].name)) {
						if (soundex(Sort_Prop_DataSet2[i].variation.get(j))
								.equals(soundex(Sort_Prop_DataSet2[i].variation
										.get(j1)))) {
							Sort_Prop_DataSet2[i].variation.remove(j1);
						}
					}
				}
			}
		}

		best = new Best_Configuration();
		best.properties_1 = new ArrayList<String>();
		best.properties_2 = new ArrayList<String>();
		all_mappings = 0;
		found_mappings = 0;
		correct = 0;
		Precision = 0.0;
		Recall = 0.0;
		F_measure = 0.0;

		File graphe = new File("Graphes_" + threshold_sim + ".txt");

		// variation of minimum support
		for (min_support = 1; min_support <= 2; min_support++) {
			for (min_support2 = 1; min_support2 <= 2; min_support2++) {
				FileWriter Gr = new FileWriter(graphe, true);
				System.out.println("FIM " + min_support + "  " + min_support2);

				long startTime_Item = System.currentTimeMillis();

				ArrayList<Items_frequente> Items_dataSet1 = Items_Freq(
						DataSet1, Sort_Prop_DataSet1, min_support, Prop_String);
				ArrayList<Items_frequente> Items_dataSet2 = Items_Freq(
						DataSet2, Sort_Prop_DataSet2, min_support2, Prop_String);

				long EndTime_Item = System.currentTimeMillis();

				// Print the items into a file
				File Item_Data1 = new File(nameData + "_ItesmSet_"
						+ min_support + ".txt");
				FileWriter IDS11 = new FileWriter(Item_Data1, false);
				for (int i = 0; i < Items_dataSet1.size(); i++) {
					IDS11.write((i + 1) + " ");
					for (int j = 0; j < Items_dataSet1.get(i).List_item.size(); j++) {
						IDS11.write("{"
								+ Items_dataSet1.get(i).List_item.get(j).Property
								+ "\t"
								+ Items_dataSet1.get(i).List_item.get(j).Value
								+ "\t"
								+ Items_dataSet1.get(i).List_item.get(j).Encoding
								+ "\t" + "}");
					}
					IDS11.write(Items_dataSet1.get(i).Instance_item.size()
							+ "\n");

					// for (int j = 0; j <
					// Items_dataSet1.get(i).Instance_item.size();
					// j++) {
					// IDS11.write("\t"
					// + Items_dataSet1.get(i).Instance_item.get(j).id + "\n");
					// }
					// }
				}
				IDS11.close();

				File Item_Data2 = new File(nameData2 + "_ItesmSet_"
						+ min_support2 + ".txt");
				FileWriter IDS12 = new FileWriter(Item_Data2, false);
				for (int i = 0; i < Items_dataSet2.size(); i++) {
					IDS12.write((i + 1) + " ");
					for (int j = 0; j < Items_dataSet2.get(i).List_item.size(); j++) {
						IDS12.write("{"
								+ Items_dataSet2.get(i).List_item.get(j).Property
								+ "\t"
								+ Items_dataSet2.get(i).List_item.get(j).Value
								+ "\t"
								+ Items_dataSet2.get(i).List_item.get(j).Encoding
								+ "\t" + "}");
					}
					IDS12.write(Items_dataSet2.get(i).Instance_item.size()
							+ "\n");

				}
				IDS12.close();

				// Extraction of the properties that are used
				ArrayList<String> Select_s_Dataset1_FIM = new ArrayList<String>();
				ArrayList<String> Select_s_Dataset2_FIM = new ArrayList<String>();

				long startTime_Select = System.currentTimeMillis();

				for (int i = 0; i < Items_dataSet1.size(); i++) {
					if (Items_dataSet1.get(i).List_item.size() == 3) {
						for (int j = 0; j < Items_dataSet1.get(i).List_item
								.size(); j++) {
							boolean exist = false;
							for (int k = 0; k < Select_s_Dataset1_FIM.size(); k++) {
								if (Select_s_Dataset1_FIM.get(k)
										.equals(Items_dataSet1.get(i).List_item
												.get(j).Property)) {
									exist = true;
									k = Select_s_Dataset1_FIM.size() + 10;
								}
							}
							if (exist == false) {
								Select_s_Dataset1_FIM
										.add(Items_dataSet1.get(i).List_item
												.get(j).Property);
							}
						}
					}
				}

				if (Select_s_Dataset1_FIM.size() == 0) {

					for (int i = 0; i < Items_dataSet1.size(); i++) {
						if (Items_dataSet1.get(i).List_item.size() == 2) {
							for (int j = 0; j < Items_dataSet1.get(i).List_item
									.size(); j++) {
								boolean exist = false;
								for (int k = 0; k < Select_s_Dataset1_FIM
										.size(); k++) {
									if (Select_s_Dataset1_FIM.get(k).equals(
											Items_dataSet1.get(i).List_item
													.get(j).Property)) {
										exist = true;
										k = Select_s_Dataset1_FIM.size() + 10;
									}
								}
								if (exist == false) {
									Select_s_Dataset1_FIM.add(Items_dataSet1
											.get(i).List_item.get(j).Property);
								}
							}
						}
					}

				}

				for (int i = 0; i < Items_dataSet2.size(); i++) {
					if (Items_dataSet2.get(i).List_item.size() == 3) {
						for (int j = 0; j < Items_dataSet2.get(i).List_item
								.size(); j++) {
							boolean exist = false;
							for (int k = 0; k < Select_s_Dataset2_FIM.size(); k++) {
								if (Select_s_Dataset2_FIM.get(k)
										.equals(Items_dataSet2.get(i).List_item
												.get(j).Property)) {
									exist = true;
									k = Select_s_Dataset2_FIM.size() + 10;
								}
							}
							if (exist == false) {
								Select_s_Dataset2_FIM
										.add(Items_dataSet2.get(i).List_item
												.get(j).Property);
							}
						}
					}
				}

				if (Select_s_Dataset2_FIM.size() == 0) {
					for (int i = 0; i < Items_dataSet2.size(); i++) {
						if (Items_dataSet2.get(i).List_item.size() == 2) {
							for (int j = 0; j < Items_dataSet2.get(i).List_item
									.size(); j++) {
								boolean exist = false;
								for (int k = 0; k < Select_s_Dataset2_FIM
										.size(); k++) {
									if (Select_s_Dataset2_FIM.get(k).equals(
											Items_dataSet2.get(i).List_item
													.get(j).Property)) {
										exist = true;
										k = Select_s_Dataset2_FIM.size() + 10;
									}
								}
								if (exist == false) {
									Select_s_Dataset2_FIM.add(Items_dataSet2
											.get(i).List_item.get(j).Property);
								}
							}
						}
					}
				}

				long EndTime_Select = System.currentTimeMillis();

				// print into the file the selected propoerties
				DS_pro1 = new FileWriter(DataSet_pro1, true);
				DS_pro1.write("\n The selection of properties FIM based Strategy "
						+ min_support + "\n");
				for (int j = 0; j < Select_s_Dataset1_FIM.size(); j++) {
					DS_pro1.write(Select_s_Dataset1_FIM.get(j) + "\n");
				}
				DS_pro1.close();

				DS_pro2 = new FileWriter(DataSet_pro2, true);
				DS_pro2.write("\n The selection of properties FIM based Strategy "
						+ min_support2 + "\n");
				for (int j = 0; j < Select_s_Dataset2_FIM.size(); j++) {
					DS_pro2.write(Select_s_Dataset2_FIM.get(j) + "\n");
				}
				DS_pro2.close();

				/*
				 * ** Matching - FIM based methode**
				 */
				long startTime_Matching = System.currentTimeMillis();
				ArrayList<Common_instances> FIM_matching_specific = Basic_Matching_specific(
						DataSet1, Select_s_Dataset1_FIM, DataSet2,
						Select_s_Dataset2_FIM, threshold_sim);
				long EndTime_Matching = System.currentTimeMillis();

				
				
				// sort the basic matching specific based into the bigest
				// number of common properties
				Common_instances[] Sort_FIM_matching_specific = Sort_Basic_Matching(FIM_matching_specific);
				// Print into a file the alignement sorted
				File Sort_FIM_Matching_specific = new File(
						"MatchingProcess_FIM_Sorted " + min_support + "-"
								+ min_support2 + ".txt");
				FileWriter FSBMS = new FileWriter(Sort_FIM_Matching_specific,
						false);
				for (int i = 0; i < Sort_FIM_matching_specific.length; i++) {
					FSBMS.write(i
							+ "\t"
							+ Sort_FIM_matching_specific[i].Individual1.id
							+ "\t"
							+ Sort_FIM_matching_specific[i].Individual2.id
							+ "\t"
							+ Sort_FIM_matching_specific[i].common.Prop_Val
									.size()
							+ "\t"
							+ Sort_FIM_matching_specific[i].Individual1.Prop_Val
									.size()
							+ "\t"
							+ Sort_FIM_matching_specific[i].Individual2.Prop_Val
									.size() + "\t"
							+ Sort_FIM_matching_specific[i].Similarité_degree
							+ "\n");
					for (int j = 0; j < Sort_FIM_matching_specific[i].common.Prop_Val
							.size(); j++) {
						FSBMS.write("\t"
								+ Sort_FIM_matching_specific[i].common.Prop_Val
										.get(j).Property
								+ "\t"
								+ Sort_FIM_matching_specific[i].common.Prop_Val
										.get(j).Value
								+ "\t"
								+ Sort_FIM_matching_specific[i].common.Prop_Val
										.get(j).Encoding + "\n");
					}
				}
				FSBMS.close();
				// sort the basic matching based into the bigest degree
				Common_instances[] Sort_FIM_matching_specific_degree = Sort_Basic_Matching_Degree(FIM_matching_specific);
				// Print into a file the alignement
				File Sort_FIM_Matching_specific_degree = new File(
						"MatchingProcess_FIM_Sorted_degree" + min_support + "-"
								+ min_support2 + ".txt");
				FileWriter FSBMDS = new FileWriter(
						Sort_FIM_Matching_specific_degree, false);
				for (int i = 0; i < Sort_FIM_matching_specific_degree.length; i++) {
					FSBMDS.write(i
							+ "\t"
							+ Sort_FIM_matching_specific_degree[i].Individual1.id
							+ "\t"
							+ Sort_FIM_matching_specific_degree[i].Individual2.id
							+ "\t"
							+ Sort_FIM_matching_specific_degree[i].common.Prop_Val
									.size()
							+ "\t"
							+ Sort_FIM_matching_specific_degree[i].Individual1.Prop_Val
									.size()
							+ "\t"
							+ Sort_FIM_matching_specific_degree[i].Individual2.Prop_Val
									.size()
							+ "\t"
							+ Sort_FIM_matching_specific_degree[i].Similarité_degree
							+ "\n");
				}
				FSBMDS.close();

				/*
				 * ** Meausures Matching - FIM based methode**
				 */

				// research the correct alignment between all found and all
				// maping
				correct = 0;
				File CF = new File("Alignement_FIM" + min_support + "-"
						+ min_support2 + ".txt");
				FileWriter CHF = new FileWriter(CF, false);
				for (int i = 0; i < Fich_rdfalign.size(); i++) {
					for (int k = 0; k < Sort_FIM_matching_specific.length; k++) {
						if ((Fich_rdfalign.get(i).entity1.toLowerCase()
								.equals(Sort_FIM_matching_specific[k].Individual1.id
										.toLowerCase()))
								& (Fich_rdfalign.get(i).entity2.toLowerCase()
										.equals(Sort_FIM_matching_specific[k].Individual2.id
												.toLowerCase()))) {
							correct++;
							CHF.write(Fich_rdfalign.get(i).entity1 + "\t"
									+ Fich_rdfalign.get(i).entity2 + "\t" + i
									+ "\t" + k + "\n");
							k = Sort_FIM_matching_specific.length + 10;
						}
					}
				}
				CHF.close();

				// calculation of measurements
				found_mappings = Sort_FIM_matching_specific.length;
				all_mappings = Fich_rdfalign.size();
				Precision = (float) correct / found_mappings;
				Recall = (float) correct / all_mappings;
				F_measure = (2 * Precision * Recall) / (Precision + Recall);


				if (best.F_measure <= F_measure && correct != 0) {
					best.properties_1 = new ArrayList<String>();
					best.properties_2 = new ArrayList<String>();
					best.nameData = nameData;
					best.nameData2 = nameData2;
					best.min_support1 = min_support;
					best.min_support2 = min_support2;
					for (int i = 0; i < Select_s_Dataset1_FIM.size(); i++) {
						best.properties_1.add(Select_s_Dataset1_FIM.get(i));
					}

					for (int i = 0; i < Select_s_Dataset2_FIM.size(); i++) {
						best.properties_2.add(Select_s_Dataset2_FIM.get(i));
					}

					best.thresholdSim = threshold_sim;
					best.found_mappings = found_mappings;
					best.all_mappings = all_mappings;
					best.correct = correct;
					best.Precision = Precision;
					best.Recall = Recall;
					best.F_measure = F_measure;

				}

				Gr.write(min_support + "\t" + min_support2 + "\t");
				Gr.write(Select_s_Dataset1_FIM.size() + "\t"
						+ Select_s_Dataset2_FIM.size() + "\t");
				Gr.write(Items_dataSet1.size() + "\t" + Items_dataSet2.size()
						+ "\t");
				Gr.write(F_measure + "\t");
				Gr.write((((EndTime_Item - startTime_Item)
						+ (EndTime_Select - startTime_Select) + (EndTime_Matching - startTime_Matching)) / 1000)
						+ "\n");
				System.out
						.println("Time "
								+ (((EndTime_Item - startTime_Item)
										+ (EndTime_Select - startTime_Select) + (EndTime_Matching - startTime_Matching)) / 1000)
								+ " second");
				Gr.close();
			}
		}

		
		long endTime = System.currentTimeMillis();
		System.out.println("Time of execution of the program:"
				+ ((endTime - startTime) / 1000) + " second");

	}
}
