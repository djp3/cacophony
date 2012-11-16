package edu.uci.ics.luci.cacophony.model;

import java.io.Serializable;

import kyotocabinet.Visitor;
import edu.uci.ics.luci.utility.datastructure.Pair;

public abstract class KyotoCabinetVisitor<K extends Serializable, V extends Serializable> extends ModelStorageVisitor<K,V> implements Visitor{
	public static enum Response{
		NOP,REMOVE,REPLACE;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public byte[] visit_full(byte[] key, byte[] value) {
		Serializable sKey = KyotoCabinet.bytesToSerializable(key);
		Serializable sValue = KyotoCabinet.bytesToSerializable(value);
		K oKey = null;
		V oValue = null;
		if(sKey != null){
			oKey = (K) sKey;
		}
		
		if(sValue != null){
			oValue = (V) sValue;
		}
			
		Pair<Response,V> response = visit_full(oKey,oValue);
		
		if(response.getFirst().equals(Response.NOP)){
			return kyotocabinet.Visitor.NOP;
		}
		else if(response.getFirst().equals(Response.REMOVE)){
			return kyotocabinet.Visitor.REMOVE;
		}
		else{
			return KyotoCabinet.serializableToBytes(response.getSecond());
		}
	}
		 

	@Override
	public byte[] visit_empty(byte[] key) {
		throw new RuntimeException("I don't know when you ever get here:"+(String)KyotoCabinet.bytesToSerializable(key));
	}
	
	public abstract Pair<Response, V> visit_full(K key, V value);
}
