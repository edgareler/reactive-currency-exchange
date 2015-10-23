package controllers;

import javax.inject.Inject;

import play.*;
import play.libs.F.Promise;
import play.libs.ws.WS;
import play.libs.ws.WSClient;
import play.mvc.*;

import views.html.*;

public class Application extends Controller {

	private final String base_url = "http://localhost:9000/";

	@Inject WSClient ws;
	
    public Result index() {
        return ok(index.render());
    }

    public Promise<Result> listCurrencies(String term){
    	String url = base_url + "currencies/" + term;
    	
		Promise<Result> jsonPromise = ws.url(url).get().map(
				response -> (Result) ok(response.asJson())
		);
		
		return jsonPromise;
    }

    public Promise<Result> exchange(Float amount, String from, String to){
    	String url = base_url + "exchange/" + amount + "/" + from + "/" + to;
    	
		Promise<Result> jsonPromise = ws.url(url).get().map(
				response -> (Result) ok(response.asJson())
		);
		
		return jsonPromise;
    }
    
}
