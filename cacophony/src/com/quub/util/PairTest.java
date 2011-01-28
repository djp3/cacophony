package com.quub.util;

import static org.junit.Assert.*;

import org.junit.Test;


public class PairTest {

	@Test
	public void testPair() {
		Pair<Integer,Integer> p1 = new Pair<Integer,Integer>(1,2);
		assertTrue(p1 != null);
		Pair<Double,Double> p2 = new Pair<Double,Double>();
		assertTrue(p2 != null);
		Pair<Double,String> p3 = new Pair<Double,String>(1.0,"Hello World");
		assertTrue(p3 != null);
	}

	@Test
	public void testGetFirst() {
		Pair<Double,String> p3 = new Pair<Double,String>(1.0,"Hello World");
		assertTrue(p3 != null);
		assertEquals(new Double(1.0),p3.getFirst());
	}

	@Test
	public void testGetSecond() {
		Pair<Double,String> p3 = new Pair<Double,String>(1.0,"Hello World");
		assertTrue(p3 != null);
		assertEquals("Hello World",p3.getSecond());
	}

	@Test
	public void testSetFirst() {
		Pair<Double,String> p3 = new Pair<Double,String>(1.0,"Hello World");
		assertTrue(p3 != null);
		assertEquals(new Double(1.0),p3.getFirst());
		p3.setFirst(new Double(2.2));
		assertEquals(new Double(2.2),p3.getFirst());
	}

	@Test
	public void testSetSecond() {
		Pair<Double,String> p3 = new Pair<Double,String>(1.0,"Hello World");
		assertTrue(p3 != null);
		assertEquals("Hello World",p3.getSecond());
		p3.setSecond("Foo");
		assertEquals("Foo",p3.getSecond());
	}

	@Test
	public void testCompareTo() {
		Pair<Double,String> p1 = new Pair<Double,String>(1.0,"Hello World");
		
		Pair<Double,String> p2 = new Pair<Double,String>(1.0,"Hello World");
		assertEquals(0,p1.compareTo(p2));
		assertEquals(p1,p2);
		assertEquals(p1.hashCode(),p2.hashCode());
		
		Pair<Double,String> p3 = new Pair<Double,String>(2.0,"Hello World");
		assertTrue(p1.compareTo(p3) < 0);
		assertTrue(p1 != p3);
		assertTrue(p1.hashCode() != p3.hashCode());
		
		Pair<Double,String> p4 = new Pair<Double,String>(0.0,"Hello World");
		assertTrue(p1.compareTo(p4) > 0);
		assertTrue(p1 != p4);
		assertTrue(p1.hashCode() != p4.hashCode());
		
		Pair<Double,String> p5 = new Pair<Double,String>(1.0,"Z");
		assertTrue(p1.compareTo(p5) < 0);
		assertTrue(p1 != p5);
		assertTrue(p1.hashCode() != p5.hashCode());
		
		Pair<Double,String> p6 = new Pair<Double,String>(1.0,"A");
		assertTrue(p1.compareTo(p6) > 0);
		assertTrue(p1 != p6);
		assertTrue(p1.hashCode() != p6.hashCode());
		
		assertEquals(p1,p1);
		assertEquals(p1.hashCode(),p1.hashCode());
		assertEquals(p2,p2);
		assertEquals(p2.hashCode(),p2.hashCode());
		assertEquals(p3,p3);
		assertEquals(p3.hashCode(),p3.hashCode());
		assertEquals(p4,p4);
		assertEquals(p4.hashCode(),p4.hashCode());
		assertEquals(p5,p5);
		assertEquals(p5.hashCode(),p5.hashCode());
		assertEquals(p6,p6);
		assertEquals(p6.hashCode(),p6.hashCode());
		
		assertTrue(!p1.equals(null));
		assertTrue(!p2.equals(null));
		assertTrue(!p3.equals(null));
		assertTrue(!p4.equals(null));
		assertTrue(!p5.equals(null));
		assertTrue(!p6.equals(null));
		
		assertTrue(!p1.equals("a"));
		assertTrue(!p2.equals(0L));
		assertTrue(!p3.equals(0.0d));
		assertTrue(!p4.equals(0));
		assertTrue(!p5.equals(new Object()));
		assertTrue(!p6.equals(true));
		
		Pair<Integer, String> p7 = new Pair<Integer,String>(0,p1.getSecond());
		assertTrue(!p7.equals(p1));
		assertTrue(!p1.equals(p7));
		
		Pair<Double, Integer> p8 = new Pair<Double,Integer>(new Double(p1.getFirst()),0);
		assertTrue(!p8.equals(p1));
		assertTrue(!p1.equals(p8));
		
		Pair<Double, String> p9 = new Pair<Double,String>(p1.getFirst()+1.0d,p1.getSecond());
		assertTrue(!p9.equals(p1));
		assertTrue(!p1.equals(p9));
		
		Pair<Double, String> p10 = new Pair<Double,String>(new Double(p1.getFirst()),"world");
		assertTrue(!p10.equals(p1));
		assertTrue(!p1.equals(p10));
	}
	

}
