package election;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import nation.Party;
import nation.Province;
import nation.Region;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ElectionData {
	private int seats;
	private long population;
	private List<Province> provinces;
	private List<Region> regions;
	private List<Party> parties;

	//create the input form using either the default or a given file
	public ElectionData() {
		seats = 0;
		population = 0;
		provinces = new ArrayList<>();
		regions = new ArrayList<>();
		parties = new ArrayList<>();
	}

	public int getSeats() {
		return seats;
	}

	public long getPopulation() {
		return population;
	}

	public List<Party> getParties() {
		return parties;
	}

	public Party getParty(int index) {
		return parties.get(index);
	}

	public void setParties(List<Party> newParties) {
		parties = newParties;
	}

	public void addParty(Party newParty) {
		parties.add(newParty);
	}

	public List<Province> getProvinces() {
		return provinces;
	}

	public void setProvinces(List<Province> newProvinces) {
		provinces = newProvinces;
		regions.clear();
	}

	public void addProvince(Province newProvince) {
		provinces.add(newProvince);
	}

	public List<Region> getRegions() {
		return regions;
	}

	public Region getRegion(int index) {
		return regions.get(index);
	}

	public void setRegions(List<Region> newRegions) {
		regions = newRegions;
		population = 0;
		seats = 0;
		for (Region r : newRegions) {
			population += r.getPopulation();
			seats += r.getSeats();
		}
	}

	public void addRegion(Region newRegion) {
		regions.add(newRegion);
		population += newRegion.getPopulation();
		seats += newRegion.getSeats();
	}

	public static ElectionData importFromTextFile(File f) throws Exception {
		ElectionData election = new ElectionData();
		Scanner sc = new Scanner(f);

		//skip intro lines
		sc.nextLine();
		sc.nextLine();
		sc.nextLine();
		sc.nextLine();
		String line;
		do {
			line = sc.nextLine();
		} while (line.equals(""));

		//read list of parties
		while (!line.equals("Province:")) {
			String name = line;
			line = sc.nextLine();
			Double approval = Double.parseDouble(line.substring(line.indexOf("Approval:") + 9, line.indexOf('%')).trim()) / 100.0;
			int r = sc.nextInt();
			int g = sc.nextInt();
			int b = sc.nextInt();
			sc.nextLine();
			Party newParty = new Party(name, new Color(r, g, b), approval);
			election.addParty(newParty);
			line = sc.nextLine();
			while (line.equals("")) {
				line = sc.nextLine();
			}
		}
		//skip to first province entry
		do {
			line = sc.nextLine();
		} while (line.length() == 0 || line.charAt(line.length() - 1) != ':');

		//read all Provinces and regions
		//reads until end of file, or reach delimiter string of "---#"
		while (sc.hasNextLine() && !line.contains("---#")) {
			//initialize name and regions list
			String pName = line.substring(0, line.length() - 1);
			Province newProvince = new Province(pName);
			election.addProvince(newProvince);

			//read all regions in province
			//scan all lines until next province 
			do {
				line = sc.nextLine();
				//read line if not blank
				if (!line.equals("") && !line.contains("---#") && line.charAt(line.length() - 1) != ':') {
					Scanner reg = new Scanner(line);
					String rName = reg.next();
					//check for additional words in name
					while (!reg.hasNextLong()) {
						rName = rName.concat(" ").concat(reg.next());
					}
					long rPop = reg.nextLong();
					int rSeat = reg.nextInt();
					Map<String, Double> rSupport = new HashMap<>();
					int count = 0;
					//read regional party support, entries cannot exceed party count
					while (reg.hasNextDouble() && count < election.getParties().size()) {
						rSupport.put(election.getParty(count).getName(), reg.nextDouble());
						count++;
					}
					//create region and add to division lists
					Region newRegion = new Region(rName, rPop, rSeat, newProvince, rSupport);
					election.addRegion(newRegion);
					reg.close();
				}
			}
			while ((line.length() == 0 || line.charAt(line.length() - 1) != ':') && sc.hasNextLine() && !line.contains("---#"));
		}
		sc.close();
		return election;
	}

	public static ElectionData importFromXMLFile(File file) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = factory.newDocumentBuilder();
		Document doc = dBuilder.parse(file);
		ElectionData election = null;

		if (doc.getDocumentElement().getNodeName().equals("ElectionData")) {
			election = new ElectionData();
			NodeList lst = doc.getDocumentElement().getChildNodes();
			for (int a = 0; a < lst.getLength(); a++) {
				Node n = lst.item(a);
				if (n.getNodeName().equals("parties")) {
					NodeList parties = n.getChildNodes();
					for (int b = 0; b < parties.getLength(); b++) {
						Node p = parties.item(b);
						if (p.getNodeName().equals("Party")) {
							Party party = new Party();
							NamedNodeMap pAttributes = p.getAttributes();
							if (pAttributes != null) {
								party.setName(pAttributes.getNamedItem("name").getTextContent());
							}
							NodeList pElements = p.getChildNodes();
							for (int c = 0; c < pElements.getLength(); c++) {
								Node pInfo = pElements.item(c);
								if (pInfo.getNodeName().equals("approval")) {
									String approval = pInfo.getTextContent();
									try {
										party.setApproval(Math.max(0.0, Math.min(1.0, Double.parseDouble(approval) / 100.0)));
									} catch (Exception ex) {
										party.setApproval(0.0);
									}
								} else if (pInfo.getNodeName().equals("Color")) {
									int red = 128, green = 128, blue = 128;
									NodeList pColors = pInfo.getChildNodes();
									for (int d = 0; d < pColors.getLength(); d++) {
										Node rgb = pColors.item(d);
										if (rgb.getNodeName().equals("Red")) {
											String s = rgb.getTextContent();
											try {
												red = Math.max(0, Math.min(255, Integer.parseInt(s)));
											} catch (Exception ex) {
												red = 128;
											}
										} else if (rgb.getNodeName().equals("Green")) {
											String s = rgb.getTextContent();
											try {
												green = Math.max(0, Math.min(255, Integer.parseInt(s)));
											} catch (Exception ex) {
												green = 128;
											}
										} else if (rgb.getNodeName().equals("Blue")) {
											String s = rgb.getTextContent();
											try {
												blue = Math.max(0, Math.min(255, Integer.parseInt(s)));
											} catch (Exception ex) {
												blue = 128;
											}
										} else {
											invalidXML(rgb.getNodeName());
										}
									}
									party.setColor(new Color(red, green, blue));
								} else {
									invalidXML(pInfo.getNodeName());
								}
							}
							election.addParty(party);
						} else {
							invalidXML(p.getNodeName());
						}
					}
				} else if (n.getNodeName().equals("provinces")) {
					NodeList provinces = n.getChildNodes();
					for (int b = 0; b < provinces.getLength(); b++) {
						Node p = provinces.item(b);
						if (p.getNodeName().equals("Province")) {
							String pName = "";
							NamedNodeMap pAttributes = p.getAttributes();
							if (pAttributes != null) {
								pName = pAttributes.getNamedItem("name").getTextContent();
							}
							Province province = new Province(pName);
							NodeList rLst = p.getChildNodes();
							for (int c = 0; c < rLst.getLength(); c++) {
								Node nr = rLst.item(c);
								if (nr.getNodeName().equals("regions")) {
									NodeList regions = nr.getChildNodes();
									for (int d = 0; d < regions.getLength(); d++) {
										Node r = regions.item(d);
										if (r.getNodeName().equals("Region")) {
											String rName = "";
											long rPopulation = 0;
											int rSeats = 0;
											HashMap<String, Double> rParties = new HashMap<>();
											NamedNodeMap rAttributes = r.getAttributes();
											if (rAttributes != null) {
												rName = rAttributes.getNamedItem("name").getTextContent();
											}
											NodeList rElements = r.getChildNodes();
											for (int e = 0; e < rElements.getLength(); e++) {
												Node rInfo = rElements.item(e);
												if (rInfo.getNodeName().equals("population")) {
													String pop = rInfo.getTextContent();
													try {
														rPopulation = Math.max(0L, Long.parseLong(pop));
													} catch (Exception ex) {
														rPopulation = 0L;
													}
												} else if (rInfo.getNodeName().equals("seats")) {
													String seat = rInfo.getTextContent();
													try {
														rSeats = Math.max(0, Integer.parseInt(seat));
													} catch (Exception ex) {
														rSeats = 0;
													}
												} else if (rInfo.getNodeName().equals("support")) {
													NodeList rSupport = rInfo.getChildNodes();
													for (int f = 0; f < rSupport.getLength(); f++) {
														Node support = rSupport.item(f);
														if (support.getNodeName().equals("Support")) {
															String party = "";
															double value;
															NamedNodeMap supportAttributes = support.getAttributes();
															if (rAttributes != null) {
																party = supportAttributes.getNamedItem("name").getTextContent();
															}
															String valueText = support.getTextContent();
															try {
																value = Math.max(0.0, Math.min(1.0, Double.parseDouble(valueText) / 100.0));
															} catch (Exception ex) {
																value = 0.0;
															}
															rParties.put(party, value);
														} else {
															invalidXML(support.getNodeName());
														}
													}
												} else {
													invalidXML(rInfo.getNodeName());
												}
											}
											Region region = new Region(rName, rPopulation, rSeats, province, rParties);
											election.addRegion(region);
										} else {
											invalidXML(r.getNodeName());
										}
									}
								} else {
									invalidXML(nr.getNodeName());
								}
							}
							election.addProvince(province);
						} else {
							invalidXML(p.getNodeName());
						}
					}
				} else {
					invalidXML(n.getNodeName());
				}
			}
		} else {
			invalidXML(doc.getDocumentElement().getNodeName());
		}

		return election;
	}

	private static void invalidXML(String tag) {
		if (!tag.equals("#text")) { //ignore "#text" without raising errors
			System.err.println("Invalid XML tag on ElectionData: " + tag);
		}
	}

	//writes the updated data to the given output file
	//uses same format as input so data can be reused as input
	public void writeToTextFile(File f) {
		try {
			PrintWriter writer = new PrintWriter(f, StandardCharsets.UTF_8);
			writer.println("Parties:");
			writer.println("Party name");
			writer.println("Approval: %");
			writer.println("RGB party colour");
			writer.println("");
			for (Party p : parties) {
				writer.println(p.getName());
				writer.println("Approval: " + p.getApproval() * 100 + "%");
				Color c = p.getColor();
				writer.println(c.getRed() + " " + c.getGreen() + " " + c.getBlue());
				writer.println("");
			}
			writer.println("Province:");
			writer.println("Region\t\tPopulation\tSeats\tParty Support (Party 1, Party 2, Party 3...)");
			writer.println("");
			for (Province pr : provinces) {
				writer.println(pr.getName() + ":");
				for (Region r : pr.getRegions()) {
					writer.print(r.getName() + "\t" + r.getPopulation() + "\t\t" + r.getSeats() + "\t");
					for (Party p : parties) {
						writer.print(r.getSupport(p.getName()) * 100 + "\t");
					}
					writer.println();
				}
			}
			writer.close();
		} catch (IOException e) {
			System.err.println("IO Error, export failed");
		}
	}

	public void writeToXMLFile(File f) {
		try {
			PrintWriter writer = new PrintWriter(f, StandardCharsets.UTF_8);
			String s = toXML();
			writer.write(s);
			writer.close();
		} catch (IOException e) {
			System.err.println("IO Error, export failed");
		}
	}

	public String toXML() {
		String s = "<ElectionData>\n";
		//List of nodes
		s = s.concat("\t<parties>\n");
		for (Party p : parties) {
			s = s.concat("\t\t<Party name=\"" + p.getName() + "\">\n");
			s = s.concat("\t\t\t<approval>" + (p.getApproval() * 100) + "</approval>\n");
			s = s.concat("\t\t\t<Color>\n");
			s = s.concat("\t\t\t\t<Red>" + p.getColor().getRed() + "</Red>\n");
			s = s.concat("\t\t\t\t<Green>" + p.getColor().getGreen() + "</Green>\n");
			s = s.concat("\t\t\t\t<Blue>" + p.getColor().getBlue() + "</Blue>\n");
			s = s.concat("\t\t\t</Color>\n");
			s = s.concat("\t\t</Party>\n");
		}
		s = s.concat("\t</parties>\n");
		s = s.concat("\t<provinces>\n");
		for (Province pr : provinces) {
			s = s.concat("\t\t<Province name=\"" + pr.getName() + "\">\n");
			s = s.concat("\t\t\t<regions>\n");
			for (Region r : pr.getRegions()) {
				s = s.concat("\t\t\t\t<Region name=\"" + r.getName() + "\">\n");
				s = s.concat("\t\t\t\t\t<population>" + r.getPopulation() + "</population>\n");
				s = s.concat("\t\t\t\t\t<seats>" + r.getSeats() + "</seats>\n");
				s = s.concat("\t\t\t\t\t<support>\n");
				for (String p : r.getSupport().keySet()) {
					s = s.concat("\t\t\t\t\t\t<Support name=\"" + p + "\">" + (r.getSupport(p) * 100.0) + "</Support>\n");
				}
				s = s.concat("\t\t\t\t\t</support>\n");
				s = s.concat("\t\t\t\t</Region>\n");
			}
			s = s.concat("\t\t\t</regions>\n");
			s = s.concat("\t\t</Province>\n");
		}
		s = s.concat("\t</provinces>\n");
		s = s.concat("</ElectionData>\n");
		return s;
	}

	public static ElectionData generateDefault() {
		ElectionData election = new ElectionData();

		election.addParty(new Party("Red Party", new Color(255, 0, 0)));
		election.addParty(new Party("Blue Party", new Color(0, 0, 255)));

		Province p1 = new Province("Subdivision A");
		election.addProvince(p1);
		Province p2 = new Province("Subdivision B");
		election.addProvince(p2);

		Map<String, Double> support = new HashMap<>();
		support.put(election.getParty(0).getName(), 0.6);
		support.put(election.getParty(1).getName(), 0.4);
		election.addRegion(new Region("City A", 1000, 5, p1, support));
		support = new HashMap<>();
		support.put(election.getParty(0).getName(), 0.4);
		support.put(election.getParty(1).getName(), 0.6);
		election.addRegion(new Region("City B", 600, 3, p1, support));
		support = new HashMap<>();
		support.put(election.getParty(0).getName(), 0.5);
		support.put(election.getParty(1).getName(), 0.5);
		election.addRegion(new Region("City C", 400, 2, p2, support));

		return election;
	}
}