package me.msmaciek.redefinedglowingentities.structs;

import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@ToString
public class QReversibleHashMap<K, V> {
	public final HashMap<K, ArrayList<V>> hashMap = new HashMap<>();
	@Getter private final HashMap<V, ArrayList<K>> reversedHashMap = new HashMap<>();

	public synchronized void put(K key, ArrayList<V> values) {
		if (values == null || values.isEmpty()) return;
		hashMap.computeIfAbsent(key, k -> new ArrayList<>());
		for (V value : values) {
			if (value == null) continue;
			if (!hashMap.get(key).contains(value)) {
				hashMap.get(key).add(value);
				reversedHashMap.computeIfAbsent(value, v -> new ArrayList<>());
				if (!reversedHashMap.get(value).contains(key)) {
					reversedHashMap.get(value).add(key);
				}
			}
		}
	}

	public synchronized boolean containsKey(K key) {
		return hashMap.containsKey(key);
	}

	public synchronized void putIfAbsent(K key, ArrayList<V> value) {
		if (hashMap.containsKey(key)) return;
		put(key, value);
	}

	public synchronized void remove(K key) {
		ArrayList<V> list = hashMap.remove(key);
		if (list == null) return;
		for (V value : list) {
			ArrayList<K> keys = reversedHashMap.get(value);
			if (keys != null) {
				keys.remove(key);
				if (keys.isEmpty()) reversedHashMap.remove(value);
			}
		}
	}

	public synchronized List<V> getReadOnly(K key) {
		ArrayList<V> list = hashMap.get(key);
		if (list == null) return null;
		return List.copyOf(list);
	}

	public synchronized void getAndAdd(K key, V value) {
		if (value == null) return;
		hashMap.computeIfAbsent(key, k -> new ArrayList<>());
		reversedHashMap.computeIfAbsent(value, v -> new ArrayList<>());
		ArrayList<V> values = hashMap.get(key);
		if (!values.contains(value)) values.add(value);
		ArrayList<K> keys = reversedHashMap.get(value);
		if (!keys.contains(key)) keys.add(key);
		if (values.isEmpty()) hashMap.remove(key);
		if (keys.isEmpty()) reversedHashMap.remove(value);
	}

	public synchronized void getAndRemove(K key, V value) {
		ArrayList<V> values = hashMap.get(key);
		if (values != null) {
			values.remove(value);
			if (values.isEmpty()) hashMap.remove(key);
		}
		ArrayList<K> keys = reversedHashMap.get(value);
		if (keys != null) {
			keys.remove(key);
			if (keys.isEmpty()) reversedHashMap.remove(value);
		}
	}
}
