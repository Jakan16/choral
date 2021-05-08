import choral.channels.SymChannel;
import java.util.ArrayList;
import java.util.List;

public class Mergesort@( A, B, C ){

	SymChannel@( A, B )< Object > ch_A_B;
	SymChannel@( B, C )< Object > ch_B_C;
	SymChannel@( C, A )< Object > ch_A_C;

	public Mergesort(
		SymChannel@( A, B )< Object > ch_A_B,
		SymChannel@( B, C )< Object > ch_B_C,
		SymChannel@( C, A )< Object > ch_A_C
	){
		this.ch_A_B = ch_A_B; this.ch_B_C = ch_B_C;	this.ch_A_C = ch_A_C;
	}

	public List@A< Integer > sort ( List@A< Integer > a ){
		if( a.size() > 1 ){
			Mergesort@( B, C, A ) mb = new Mergesort@( B, C, A )( ch_B_C, ch_A_C, ch_A_B );
			Mergesort@( C, A, B ) mc = new Mergesort@( C, A, B )( ch_A_C, ch_A_B, ch_B_C );
			Double pivot = a.size() / 2
				>> Math::floor
				>> Double::valueOf;
			List< Integer > lhs = a.subList( 0, pivot.intValue() ) >> mb::sort;
			List< Integer > rhs = a.subList( pivot.intValue(), a.size() ) >> mc::sort;
			return merge( lhs, rhs );
		} else {
			return a;
		}
	}

	private List@A< Integer > merge ( List@B< Integer> lhs, List@C< Integer> rhs ) {
		if( lhs.size() > 0 ) {
			if( rhs.size() > 0 ){
				ArrayList< Integer > result = new ArrayList< Integer >();
				if( lhs.get( 0 ) <= rhs.get( 0 ) ){
					lhs.get( 0 ) >> result::add;
					merge( lhs.subList( 1, lhs.size() ), rhs ) >> result::addAll;
					return result;
				} else {
					rhs.get( 0 ) >> result::add;
					merge( lhs, rhs.subList( 1, rhs.size() ) ) >> result::addAll;
					return result;
				}
			} else {
				return lhs;
			}
		} else {
			return rhs;
		}
	}

}
