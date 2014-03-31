package ca.unbc.cpsc472.mynextphone.models;

import java.io.Serializable;

public class Tuple implements Serializable {
	public final static long serialVersionUID = 0;
	
	private Object[] objects;
	
	public Tuple(Object[] objects) {
		this.objects = objects;
	}
	
	public Object[] getObjects() {
		return objects;
	}
	
	public Object getObject(int index) {
		return objects[index];
	}
	
	@Override
	public String toString() {
		String ret = "Tuple [objects={";
		for(Object o : objects) {
			ret += o.toString();
			if(o != objects[objects.length - 1]) {
				ret += ", ";
			}
		}
		ret += "}]";
		return ret;
	}
}
