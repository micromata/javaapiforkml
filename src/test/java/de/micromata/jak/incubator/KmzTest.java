// ///////////////////////////////////////////////////////////////////////////
//
// $RCSfile: $
//
// Project JavaAPIforKML
//
// Author Flori (f.bachmann@micromata.de)
// Created Aug 15, 2009
// Copyright Micromata Aug 15, 2009
//
// $Id: $
// $Revision: $
// $Date: $
//
// ///////////////////////////////////////////////////////////////////////////
package de.micromata.jak.incubator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.theories.suppliers.TestedOn;

import com.sun.istack.NotNull;

import de.micromata.jak.Utils;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.Placemark;

class KmzHelper {
	private static int missingNameCounter = 1;

	public static boolean createKmz(@NotNull String name, @NotNull Kml kmzFile, Kml... additionalFiles) throws IOException {
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(name));
		out.setComment("KMZ-file created with Java API for KML");
		addKmzFile(kmzFile, out, true);

		for (Kml kml : additionalFiles) {
			addKmzFile(kml, out, false);
		}

		out.close();
		missingNameCounter = 1;
		return false;
	}

	private static void addKmzFile(Kml kmzFile, ZipOutputStream out, boolean mainfile) throws IOException, FileNotFoundException {
		String fileName = null;
		if (kmzFile.getFeature() == null || kmzFile.getFeature().getName() == null || kmzFile.getFeature().getName().length() == 0) {
			fileName = "noFeatureNameSet" + (missingNameCounter++) + ".kml";
		} else {
			fileName = kmzFile.getFeature().getName();
			if (!fileName.endsWith(".kml")) {
				fileName += ".kml";
			}
		}
		if (mainfile) {
			fileName = "doc.kml";
		}
		out.putNextEntry(new ZipEntry(URLEncoder.encode(fileName, "UTF-8")));
		kmzFile.marshal(out);

		out.closeEntry();
	}
	
	public static Kml[] unmarshalKMZ(File file) throws ZipException, IOException {
		final Kml[] EMPTY_KML_ARRAY = new Kml[0];

		ZipFile zip = new ZipFile(file);
		Enumeration< ? extends ZipEntry> entries = zip.entries();
		if (!file.exists()) {
			return EMPTY_KML_ARRAY;
		}
		ArrayList<Kml> kmlfiles = new ArrayList<Kml>();
		while (entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) entries.nextElement();

			// is directory
			if (entry.getName().contains("__MACOSX") || entry.getName().contains(".DS_STORE")) {
				continue;
			}

			String entryName = URLDecoder.decode(entry.getName(), "UTF-8");

			System.out.println("element: " + entryName);
			if (!entry.getName().endsWith(".kml")) {
				continue;
			}
			InputStream in = zip.getInputStream(entry);
			Kml unmarshal = Kml.unmarshal(in);
			kmlfiles.add(unmarshal);
		}
		zip.close();

		return kmlfiles.toArray(EMPTY_KML_ARRAY);
	}
}

public class KmzTest {
	@Test
	@Ignore
	public void testKml12() throws IOException {
		final Kml kmlmain = createKmlStructure("KmlFileMain.kml", "PlacemarkMain");

		Kml kml1 = createKmlStructure("KmlFile1.kml", "Placemark1");
		Kml kml2 = createKmlStructure(null, "Placemark2");
		Kml kml3 = createKmlStructure("KmlFile3.kml", "Placemark3");

		kmlmain.marshalAsKmz("kmzFileMain.kmz");

		kmlmain.marshalAsKmz("kmzFileMain2.kmz", kml1, kml2, kml3);

		Kml kml4 = createKmlStructure("KmlFile4", "Placemark3");
		Kml kml5 = createKmlStructure("KmlFile5", "Placemark3");
		Kml kml6 = createKmlStructure(null, "Placemark3");
		Kml kml7 = createKmlStructure("KmlFile7.kml", "Placemark3");

		
		kmlmain.marshalAsKmz("kmzFileMain3.kmz", kml1, kml2, kml3, kml4, kml5, kml6, kml7);
		

		Kml[] unmarshalKMZ = Kml.unmarshalFromKMZ(new File("kmzFileMain3.kmz"));

		for (Kml kml : unmarshalKMZ) {
			String name = null;
			if (kml.getFeature() == null || kml.getFeature().getName() == null || kml.getFeature().getName().length() == 0) {
				name = "noName";
			} else {
				name = kml.getFeature().getName();
			}

			System.out.println(name);
		}
	}

	private Kml createKmlStructure(String documentName, String placemarkName) {
		final Kml kml = new Kml();
		kml.createAndSetDocument().withName(documentName).createAndAddPlacemark().withName(placemarkName).createAndSetLineString()
		    .addToCoordinates(3528968.79007832, 5805512.1556938, 662.7).addToCoordinates(3528973.946, 5805504.127, 662.7).addToCoordinates(
		        3528973.946, 5805504.127, 662.7).addToCoordinates(3528975.646, 5805505.047, 662.7).addToCoordinates(3528975.646, 5805505.047,
		        662.7).addToCoordinates(3528977.563, 5805502.246, 662.7).addToCoordinates(3528977.563, 5805502.246, 662.7).addToCoordinates(
		        3528975.869, 5805501.4, 662.7);
		return kml;
	}

	// @Test
	public void tetetetetetet() throws FileNotFoundException {
		Kml unmarshal = Kml
		    .unmarshal("<?xml version=\"1.0\" encoding=\"UTF-8\"?><kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\"><Placemark>	<name>London, UK</name>	<open>1</open>	<Point>		<coordinates>-0.126236,51.500152,0</coordinates>	</Point></Placemark></kml>");
		unmarshal.marshal(System.out);

	}

	@Test
	public void HelloKML() {

		final Kml kml = new Kml();
		kml.createAndSetPlacemark().withName("London, UK").withOpen(true).createAndSetPoint().addToCoordinates(-0.126236, 51.500152);
		kml.marshal();
	}
}