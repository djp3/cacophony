package edu.uci.ics.luci.cacophony.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import kyotocabinet.DB;
import kyotocabinet.Visitor;

import org.apache.log4j.Logger;

import edu.uci.ics.luci.cacophony.node.CNodePool;

public class KyotoCabinet<K extends Serializable, V extends Serializable> extends ModelStorage<K,V>{
	
	private static transient volatile Logger log = null;
	public static Logger getLog(){
		if(log == null){
			log = Logger.getLogger(CNodePool.class);
		}
		return log;
	}
	
	
	static public byte[] serializableToBytes(Serializable x) {
		if(x == null){
			return(new byte[0]);
		}
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream out = null;
		
		try {
			out = new ObjectOutputStream(bos);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}   
		
		byte[] ret=null;
		try {
			out.writeObject(x);
			ret = bos.toByteArray();
		} catch (IOException e) {
			getLog().error("Unable to serialize something:"+x.getClass().getCanonicalName()+"\n"+e);
		} finally {
			try {
				out.close();
			} catch (IOException e) {
			}
			try {
				bos.close();
			} catch (IOException e) {
			}
		}
		return ret;
	}
	

	static public Serializable bytesToSerializable(byte[] bs) {
		if((bs == null)||(bs.length == 0)){
			return null;
		}
		
		ByteArrayInputStream bis = new ByteArrayInputStream(bs);
		ObjectInput in = null;
		try {
			in = new ObjectInputStream(bis);
			return (Serializable) (in.readObject()); 
		} catch (IOException e) {
			getLog().error("Deserialization failed:"+e);
			return(null);
		} catch (ClassNotFoundException e) {
			getLog().error("Deserialization failed:"+e);
			return(null);
		} finally {
			try {
				bis.close();
			} catch (IOException e) {
			}
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	private DB db = null;
	private boolean shuttingDown = false;
	private String fileNameBase = null;
	
	public KyotoCabinet(){
		this(null);
	}
	
	public KyotoCabinet(String fileNameBase){
		super();
		this.db = new DB();
		this.fileNameBase = fileNameBase;
	}
	
	@Override
	public synchronized void setQuitting(boolean quitting) {
		if(shuttingDown == false){
			if(quitting == true){
				shuttingDown = true;
				if(db != null){
					if(!db.close()){
						getLog().error("Unable to shutdown Kyoto Cabinet:"+db.error());
					}
					db = null;
				}
			}
		}
		else{
			if(quitting == false){
				getLog().fatal("Trying to undo a shutdown! Can't do that");
			}
			else{
				getLog().fatal("Trying to shutdown twice! Don't do that");
			}
		}
		
	}

	@Override
	public synchronized boolean open(boolean deleteExisting,boolean testing){
		String fileName = null;
		boolean ret = false;
		int flags;
		if( deleteExisting){
			flags = (DB.OWRITER | DB.OTRUNCATE | DB.OCREATE);
		}
		else{
			flags = (DB.OWRITER | DB.OCREATE);
		}
		
		if(fileNameBase==null){
			if(testing){
				fileName = "test.kct";
			} else{
				fileName = "models.kct";
			}
		}
		else{
			fileName = fileNameBase+".kct";
		}
		
		if (!(ret = db.open(fileName, flags))){
			throw new RuntimeException("open error: " + db.error());
		}
		
		return ret; 
	}
	
	@Override
	public synchronized Error error() {
		return(new Error(db.error()));
	}
	
	
	
	@Override
	public synchronized boolean set(K key, V value) {
		byte[] keyBytes = serializableToBytes(key);
		if(keyBytes == null){
			return false;
		}
		
		byte[] valueBytes = serializableToBytes(value);
		if(valueBytes == null){
			return false;
		}
		
		return(db.set(keyBytes, valueBytes));
	}
	
	
	@Override
	public synchronized boolean contains(Serializable key) {
		byte[] keyBytes = serializableToBytes(key);
		if(keyBytes == null){
			return false;
		}
		byte[] x = db.get(keyBytes);
		if(x == null){
			return false;
		}
		else{
			return true;
		}
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public synchronized V get(K key) {
		byte[] keyBytes = serializableToBytes(key);
		if(keyBytes == null){
			return null;
		}
		
		return (V) (bytesToSerializable(db.get(keyBytes)));
	}
	
	@Override
	public synchronized boolean remove(K key) {
		byte[] keyBytes = serializableToBytes(key);
		if(keyBytes == null){
			return false;
		}
		
		return(db.remove(keyBytes));
	}

	public synchronized boolean iterate(KyotoCabinetVisitor<K,V> visitor, boolean writable) {
		return(iterate((ModelStorageVisitor<K,V>)visitor,writable));
	}
	
	@Override
	public synchronized boolean iterate(ModelStorageVisitor<K,V> visitor, boolean writable) {
		if(visitor instanceof KyotoCabinetVisitor){
			Visitor v = (Visitor) visitor;
			return(db.iterate(v, writable));
		}
		else{
			throw new RuntimeException("Wrong visitor class passed to iterate:"+visitor.getClass().getCanonicalName());
		}
	}
	
}
