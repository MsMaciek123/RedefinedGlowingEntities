package me.msmaciek.redefinedglowingentities.structs;

import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;

@ToString
public class QReversibleHashMap<K, V> {
	public HashMap<K, ArrayList<V>> hashMap = new HashMap<>();
	@Getter HashMap<V, ArrayList<K>> reversedHashMap = new HashMap<>();

	public void put(K key, ArrayList<V> values) {
		if(!hashMap.containsKey(key)) {
			hashMap.put(key, new ArrayList<>());
		}
		hashMap.get(key).addAll(values);
		for(V value : values) {
			if(!reversedHashMap.containsKey(value)) {
				reversedHashMap.put(value, new ArrayList<>());
			}
			reversedHashMap.get(value).add(key);
		}
	}

	public boolean containsKey(K key) {
		return hashMap.containsKey(key);
	}

	public void putIfAbsent(K key, ArrayList<V> value) {
		if(hashMap.containsKey(key))
			return;

		put(key, value);
	}

	public void remove(K key) {
		for(V value : hashMap.get(key)) {
			reversedHashMap.get(value).remove(key);
			if(reversedHashMap.get(value).isEmpty())
				reversedHashMap.remove(value);
		}
		hashMap.remove(key);
	}

	public ArrayList<V> getReadOnly(K key) {
		return hashMap.get(key);
	}

	public void getAndAdd(K key, V value) {
		if(!hashMap.containsKey(key)) {
			hashMap.put(key, new ArrayList<>());
		}
		if(!reversedHashMap.containsKey(value)) {
			reversedHashMap.put(value, new ArrayList<>());
		}
		hashMap.get(key).add(value);
		reversedHashMap.get(value).add(key);

		if(hashMap.get(key).isEmpty())
			hashMap.remove(key);
		if(reversedHashMap.get(value).isEmpty())
			reversedHashMap.remove(value);
	}

	public void getAndRemove(K key, V value) {
		hashMap.get(key).remove(value);
		reversedHashMap.get(value).remove(key);

		if(hashMap.get(key).isEmpty())
			hashMap.remove(key);
		if(reversedHashMap.get(value).isEmpty())
			reversedHashMap.remove(value);
	}
}
