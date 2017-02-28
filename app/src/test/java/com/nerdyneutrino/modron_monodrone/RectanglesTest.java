package com.nerdyneutrino.modron_monodrone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Patrick on 2/28/2017.
 */
public class RectanglesTest {
	Rectangles r;

	@Before
	public void setUp() throws Exception {
		r = new Rectangles();
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void returnOne() throws Exception {
		assertEquals(1, Rectangles.returnOne());
		assertNotEquals(0, Rectangles.returnOne());
		assertNotEquals(2, Rectangles.returnOne());
	}

	@Test
	public void returnTwo() throws Exception {
		assertEquals(2, r.returnTwo());
		assertNotEquals(0, r.returnTwo());
		assertNotEquals(1, r.returnTwo());
	}

	@Test
	public void compute2() throws Exception {
		assertEquals(100, Rectangles.compute2(0.0f, 0.0f, 100));
		assertEquals(1, Rectangles.compute2(-2.5f, 1.0f, 100));
		assertEquals(33, Rectangles.compute2(-0.75f, 0.1f, 100));
	}

	@Test
	public void compute() throws Exception {
		r.xvt = -0.75f;
		r.yvt = 0.1f;
		assertEquals(0xFF547AE0, r.compute(0,0));
	}

}