package models;

import org.bson.types.ObjectId;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;

import uk.co.panaxiom.playjongo.PlayJongo;

public class Currency {
	public static MongoCollection currencies(){
		return PlayJongo.getCollection("currency");
	}
	
	public ObjectId _id;
	
	public String code;
	
	public String name;
	
	public Float rate;

	public void insert() {
		currencies().save(this);
	}
	
	public void remove(){
		currencies().remove(this._id);
	}
	
	public static MongoCursor<Currency> find(String term){
		String search = "";
		
		if(!term.equals("all")){
			search = "{code: '" + term + "'}";
		}
		
		MongoCursor<Currency> currenciesFound =  currencies()
				.find(search)
				.sort("{code: 1}")
				.as(Currency.class);
		
		return currenciesFound;
	}

	public static Currency findByCode(String code){
		return currencies().findOne("{code: #}", code).as(Currency.class);
	}
}
