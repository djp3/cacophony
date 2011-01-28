package com.quub.util;

import java.io.Serializable;


public class Pair<T1, T2> implements Comparable<Pair<T1,T2>>,Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 736081024370816902L;
	
	T1 a = null;
	T2 b = null;
	
	public Pair() {
	}
	
	public Pair(T1 a1, T2 b1){
		setFirst(a1);
		setSecond(b1);
	}
	
	public T1 getFirst(){
		return a;
	}
	
	public T2 getSecond(){
		return b;
	}
	
	public void setFirst(T1 a1){
		this.a = a1;
	}
	
	public void setSecond(T2 b1){
		this.b = b1;
	}

	@SuppressWarnings("unchecked")
	public int compareTo(Pair<T1,T2> p) {
		int x = ((Comparable<T1>) getFirst()).compareTo(p.getFirst());
		if(x == 0){
			return ((Comparable<T2>) getSecond()).compareTo(p.getSecond());
		}
		else{
			return x;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (!getClass().equals(obj.getClass()))
			return false;
		
		@SuppressWarnings("unchecked")
		final Pair<T1,T2> other = (Pair<T1,T2>) obj;
		if(!this.getFirst().getClass().equals(other.getFirst().getClass())){
			return false;
		}
		if(!this.getSecond().getClass().equals(other.getSecond().getClass())){
			return false;
		}
		if(!this.getFirst().equals(other.getFirst())){
			return false;
		}
		if(!this.getSecond().equals(other.getSecond())){
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		if(this.getFirst() != null){
			result = prime * result + this.getFirst().hashCode();
		}
		if(this.getSecond() != null){
			result = prime * result + this.getSecond().hashCode();
		}
		return result;
	}
		
	
	public String toString(){
		return ("<"+getFirst()+":"+getSecond()+">");
	}


}
