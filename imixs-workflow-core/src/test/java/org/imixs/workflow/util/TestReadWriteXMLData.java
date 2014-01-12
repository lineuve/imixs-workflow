package org.imixs.workflow.util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import junit.framework.Assert;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.xml.EntityCollection;
import org.imixs.workflow.xml.XMLItemCollectionAdapter;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test class reads and writes a model from the file system
 * 
 * 
 * @author rsoika
 */
public class TestReadWriteXMLData {
	private final static Logger logger = Logger.getLogger(TestReadWriteXMLData.class
			.getName());

	@Before
	public void setup() {

	}

	@After
	public void teardown() {

	}

	@Test
	public void testRead() {
		List<ItemCollection> col = null;

		try {
			col = XMLItemCollectionAdapter
					.readCollectionFromInputStream(getClass()
							.getResourceAsStream("/model.ixm"));
		} catch (JAXBException e) {
			Assert.fail();
		} catch (IOException e) {
			Assert.fail();
		}

		Assert.assertEquals(14, col.size());
	}

	/**
	 * Writes a new ixm file
	 */
	@Test
	@Ignore
	public void testWrite() {
		List<ItemCollection> col = null;
		// read default content
		try {
			col = XMLItemCollectionAdapter
					.readCollectionFromInputStream(getClass()
							.getResourceAsStream("/model.ixm"));
		} catch (JAXBException e) {
			Assert.fail();
		} catch (IOException e) {
			Assert.fail();
		}

		// create JAXB object
		EntityCollection xmlCol = null;
		try {
			xmlCol = XMLItemCollectionAdapter.putCollection(col);
		} catch (Exception e1) {

			e1.printStackTrace();
			Assert.fail();
		}

		// now write back to file
		File file = null;
		try {

			file = new File("src/test/resources/export_test.xml");
			JAXBContext jaxbContext = JAXBContext
					.newInstance(EntityCollection.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			jaxbMarshaller.marshal(xmlCol, file);
			jaxbMarshaller.marshal(xmlCol, System.out);

		} catch (JAXBException e) {
			e.printStackTrace();
			Assert.fail();
		}
		Assert.assertNotNull(file);
	}

}
