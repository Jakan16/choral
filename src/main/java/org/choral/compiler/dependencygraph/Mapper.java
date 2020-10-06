package org.choral.compiler.dependencygraph;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Mapper {

	private static final Function< ?, ? > identityFunction = v -> v;

	public static < K, V > Map< K, V > mapping( Iterable< K > from, Iterable< V > to){
		Map< K, V > map = new HashMap<>();
		Iterator<V> vIterator = to.iterator();
		from.forEach( f -> {
			assert vIterator.hasNext();
			map.put( f, vIterator.next() );
		} );

		return map;
	}

	public static < K, V, K2, V2 > Map< K, V > mapping( Iterable< K2 > from, Iterable< V2 > to, Function< K2, K > kMap, Function<V2, V> vMap ){
		Map< K, V > map = new HashMap<>();
		Iterator<V2> vIterator = to.iterator();
		from.forEach( k -> {
			assert vIterator.hasNext();
			map.put( kMap.apply( k ), vMap.apply( vIterator.next() ) );
		} );

		return map;
	}

	public static < K, V, K2 > Map< K, V > mapping( Iterable< K2 > fromTo, Function< K2, K > kMap, Function<K2, V> vMap ){
		Map< K, V > map = new HashMap<>();
		fromTo.forEach( k -> map.put( kMap.apply( k ), vMap.apply( k ) ) );

		return map;
	}

	public static < K, K2 > Map< K, K > idMap( Iterable< K2 > from, Function< K2, K > kMap ){
		Map< K, K > map = new HashMap<>();
		from.forEach( k -> {
			K key = kMap.apply( k );
			map.put( key, key );
		} );

		return map;
	}

	public static < T, R > List< R > map( List< T > list, Function< T, R > mapFunction ){
		return list.stream().map( mapFunction ).collect( Collectors.toList());
	}

	@SuppressWarnings( "unchecked" )
	public static < T extends R, R > Function< T, R > id(){
		return (Function< T, R >) identityFunction;
	}
}
