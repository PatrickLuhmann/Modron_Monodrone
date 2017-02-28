package com.nerdyneutrino.modron_monodrone;

import android.graphics.Rect;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
	@Test
	public void addition_isCorrect() throws Exception {
		assertEquals(4, 2 + 2);
	}

	@Test
	public void testReturnOne() throws Exception {
		assertEquals(1, Rectangles.returnOne());
		assertNotEquals(0, Rectangles.returnOne());
		assertNotEquals(2, Rectangles.returnOne());
	}

	@Test
	public void testReturnTwo() throws Exception {
		Rectangles r = new Rectangles();
		assertEquals(2, r.returnTwo());
		assertNotEquals(0, r.returnTwo());
		assertNotEquals(1, r.returnTwo());
	}

	@Test
	public void compute_0_0() throws Exception {
		Rectangles r = new Rectangles();
		assertEquals(-1, r.compute(0, 0));
	}
}