import choral.channels.SymChannel;
import java.util.function.Function;

public class RemoteFunction@( Client, Server )< T@X, R@Y > {
	private SymChannel@( Client, Server )< Object > ch_Client_Server;

	public RemoteFunction( SymChannel@( Client, Server )< Object > ch_Client_Server ){
	    this.ch_Client_Server = ch_Client_Server;
	}

	public R@Client call( T@Client t, Function@Server< T, R > f ) {
	    return f.apply( t );
	}
}
