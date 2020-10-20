package org.choral.compiler.dependencygraph;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Provides utilities for mapping from one type to another,
 * or creating maps to use as look-up tables.
 */
public class Mapper {

	private static final Function< ?, ? > identityFunction = v -> v;

	/**
	 * Creates a 1-1 mapping from {@code from iterable} to {@code to iterable}.
	 * The two iterables must be equal in length.
	 *
	 * @param from The elements to use as keys
	 * @param to The elements to use as values
	 * @param <K> The type of the keys
	 * @param <V> The type of the values
	 * @return A map where the i'th key maps to the i'th value
	 */
	public static < K, V > Map< K, V > mapping( Iterable< K > from, Iterable< V > to){
		Map< K, V > map = new HashMap<>();
		Iterator<V> vIterator = to.iterator();
		from.forEach( f -> {
			assert vIterator.hasNext();
			map.put( f, vIterator.next() );
		} );

		return map;
	}

	/**
	 * Creates a 1-1 mapping from {@code from iterable} to {@code to iterable},
	 * and applies the supplied functions to the input.
	 * The two iterables must be equal in length.
	 *
	 * @param from The source elements to use as keys
	 * @param to The source elements to use as values
	 * @param kMap function the maps a single key from {@link K2} to {@link K}
	 * @param vMap function the maps a single value from {@link V2} to {@link V}
	 * @param <K> The type of keys
	 * @param <V> The type of values
	 * @param <K2> The type of the source keys
	 * @param <V2> The type of the source values
	 * @return A map where the transformed i'th key maps to the transformed i'th value
	 */
	public static < K, V, K2, V2 > Map< K, V > mapping(
			Iterable< K2 > from,
			Iterable< V2 > to,
			Function< K2, K > kMap,
			Function<V2, V> vMap
	){
		Map< K, V > map = new HashMap<>();
		Iterator<V2> vIterator = to.iterator();
		from.forEach( k -> {
			assert vIterator.hasNext();
			map.put( kMap.apply( k ), vMap.apply( vIterator.next() ) );
		} );

		return map;
	}

	/**
	 * Creates a 1-1 mapping from {@code fromTo iterable} to itself,
	 * and applies the supplied functions to create separate key values pairs.
	 *
	 * @param fromTo The source elements to use for both keys and values
	 * @param kMap function the maps a single key from {@link T} to {@link K}
	 * @param vMap function the maps a single value from {@link T} to {@link V}
	 * @param <K> The type of keys
	 * @param <V> The type of values
	 * @param <T> The type of the source
	 * @return A map where the transformed i'th key maps to the transformed i'th value
	 */
	public static < K, V, T > Map< K, V > mapping( Iterable< T > fromTo, Function< T, K > kMap, Function< T, V> vMap ){
		Map< K, V > map = new HashMap<>();
		fromTo.forEach( k -> map.put( kMap.apply( k ), vMap.apply( k ) ) );

		return map;
	}

	/**
	 * Creates a mapping for each element to itself.
	 *
	 * @param fromTo The elements to use for both keys and values
	 * @param <T> The type of the elements
	 * @return A map where each key maps to itself
	 */
	public static < T > Map< T, T > idMap( Iterable< T > fromTo ){
		Map< T, T > map = new HashMap<>();
		fromTo.forEach( k -> map.put( k, k ) );
		return map;
	}

	/**
	 * Applies {@code kMap} to each element, and creates a mapping from the element to itself.
	 *
	 * @param fromTo The source elements to use for both keys and values
	 * @param kMap function the maps a single value from {@link T} to {@link K}
	 * @param <K> The type of the transformed elements
	 * @param <T> the type of source elements;
	 * @return A map where each key maps to itself
	 */
	public static < K, T > Map< K, K > idMap( Iterable< T > fromTo, Function< T, K > kMap ){
		Map< K, K > map = new HashMap<>();
		fromTo.forEach( k -> {
			K key = kMap.apply( k );
			map.put( key, key );
		} );

		return map;
	}

	/**
	 * Maps a {@link List} of {@link T} to {@link List} of {@link R}
	 * using the given element wise mapping function.
	 *
	 * @param list The original list
	 * @param mapFunction The per element mapping function
	 * @param <T> The type of the original {@link List}
	 * @param <R> The type of the new {@link List}
	 * @return A new {@link List} with all elements mapped
	 */
	public static < T, R > List< R > map( List< T > list, Function< T, R > mapFunction ){
		return list.stream().map( mapFunction ).collect( Collectors.toList());
	}

	/**
	 * Creates a list from two {@link Iterable} inputs by repeatedly applying mapFunc on each index on the iterables.
	 * @param from1 The first source
	 * @param from2 The second source
	 * @param mapFunc The mapping function to transform a pair of elements from each source to a single result
	 * @param <T1> The type of the first source
	 * @param <T2> The type of the second source
	 * @param <R> The type of the resulting list
	 * @return A new list generated from the two sources
	 */
	public static < T1, T2, R > List< R > map(
			Iterable< T1 > from1,
			Iterable< T2 > from2,
			BiFunction< T1, T2, R > mapFunc
	){
		Iterator< T1 > it = from1.iterator();
		List< R > result = new ArrayList<>();
		from2.forEach( t2 -> {
			assert it.hasNext();
			result.add( mapFunc.apply( it.next(), t2 ) );
		} );

		return result;
	}

	/**
	 * The identity function
	 * @param <T> The type of the output, which is a subtype of {@link R}
	 * @param <R> The type of the input
	 * @return The input
	 */
	@SuppressWarnings( "unchecked" )
	public static < T extends R, R > Function< T, R > id(){
		return (Function< T, R >) identityFunction;
	}
}
