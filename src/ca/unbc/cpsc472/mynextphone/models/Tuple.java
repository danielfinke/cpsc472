package ca.unbc.cpsc472.mynextphone.models;

import java.io.Serializable;

public class Tuple implements Serializable {
	public final static long serialVersionUID = 0;
	
	private Object[] objects;
	
	/*
	 * Create a new Tuple from some items
	 * 
	 * @param objects		the items to include in the Tuple
	 * @return				the new Tuple
	 */
	public Tuple(Object[] objects) {
		this.objects = objects;
	}
	
	/*
	 * Get the set of objects in the tuple
	 * 
	 * @return		array of the objects in the tuple
	 */
	public Object[] getObjects() {
		return objects;
	}
	
	/*
	 * Fetch an object based on its index in the tuple
	 * 
	 * @param index		index of desired object
	 * @return			object at the specified index
	 */
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
