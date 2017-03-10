package com.nerdyneutrino.modron_monodrone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Patrick on 3/10/2017.
 */
public class MyObjectTest {
	MyObject obj100x100;
	MyObject obj123x456;

	@Before
	public void setUp() throws Exception {
		obj100x100 = new MyObject.Builder(100, 100).build();
		obj123x456 = new MyObject.Builder(123, 456).build();
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void getWidth() throws Exception {
		assertEquals(100, obj100x100.getWidth());
		assertEquals(123, obj123x456.getWidth());
	}

	@Test
	public void getHeight() throws Exception {
		assertEquals(100, obj100x100.getHeight());
		assertEquals(456, obj123x456.getHeight());
	}

	@Test
	public void setWidth() throws Exception {
		obj100x100.setWidth(200, true);

		assertEquals(200, obj100x100.getHeight());
		assertEquals(200, obj100x100.getWidth());

		obj100x100.setWidth(50, false);

		assertEquals(200, obj100x100.getHeight());
		assertEquals(50, obj100x100.getWidth());
	}

	@Test
	public void setHeight() throws Exception {
		obj100x100.setHeight(200, true);

		assertEquals(200, obj100x100.getHeight());
		assertEquals(200, obj100x100.getWidth());

		obj100x100.setHeight(500, false);

		assertEquals(500, obj100x100.getHeight());
		assertEquals(200, obj100x100.getWidth());
	}

}