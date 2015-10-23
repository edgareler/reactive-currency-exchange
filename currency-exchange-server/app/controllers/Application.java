package controllers;

import java.util.Iterator;

import javax.inject.Inject;

import org.jongo.MongoCursor;
import org.json.JSONObject;

import models.Currency;
import play.libs.F.Promise;
import play.libs.ws.WSClient;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {

	private final String base_url = "https://openexchangerates.org/api/";
	private final String app_id = "049968d0de5a4d7bb85ad2169a54d406";
	private final String latest_url = base_url + "latest.json?app_id=" + app_id;
	private final String currencies_url = base_url + "currencies.json?app_id=" + app_id;

	@Inject WSClient ws;
	
	public Result index() {
		return status(403, "Forbidden");
	}

	public Promise<Result> listCurrencies(String term) {
		Promise<Result> promiseResult;
		
		MongoCursor<Currency> currenciesFound = Currency.find(term);
		
		if(currenciesFound.count() > 0){
			Promise<Result> promiseCurrencies = Promise.promise(() -> {
				return ok(getJsonDB(currenciesFound).toString());
			});
			
			promiseResult = syncRates().flatMap(respMap ->
				promiseCurrencies
			);
		} else {
			promiseResult = syncCurrencies().flatMap(resp ->
				syncRates().map(respMap -> {
					MongoCursor<Currency> currenciesFound_2 = Currency.find(term);
					
					return ok(getJsonDB(currenciesFound_2).toString());
				})
			);			
		}
		
		return promiseResult;
	}
	
	public JSONObject getJsonDB(MongoCursor<Currency> currencies){
		JSONObject resultJson = new JSONObject();
		
		while(currencies.hasNext()){
			Currency currency = currencies.next();
			
			JSONObject jso = new JSONObject();					
			
			jso.put("code", currency.code);
			jso.put("name", currency.name);
			jso.put("rate", currency.rate);
			
			resultJson.append("currencies", jso);
		}

		return resultJson;
	}
	
	public Float calculateExchange(Float amount, String from, String to){
		Currency fromCurrency = Currency.findByCode(from);
		Currency toCurrency = Currency.findByCode(to);
		
		if(fromCurrency != null && toCurrency != null && fromCurrency.rate != null && toCurrency.rate != null){
			Float result = (amount * toCurrency.rate) / fromCurrency.rate;
			
			return result;
		}
		
		return null;
	}
	
	public Promise<Result> exchange(Float amount, String from, String to){
		Float result = calculateExchange(amount, from, to);
		
		Promise<Result> promiseResult;
		
		if(result != null) {
			Promise<Result> promiseExchange = Promise.promise(() -> {
				JSONObject resultJson = new JSONObject();
				resultJson.put("result", result);
				
				return ok(resultJson.toString());
			});
			
			promiseResult = syncRates().flatMap((resp) ->
				promiseExchange
			);
		} else {
			promiseResult = syncCurrencies().flatMap((resp) ->
				syncRates().map(respMap -> {
					Float result2 = calculateExchange(amount, from, to);
					
					JSONObject resultJson = new JSONObject();
					resultJson.put("result", result2);
					
					return ok(resultJson.toString());
				})
			);
		}
		
		return promiseResult;
	}
	
	public void storeCurrencies(JSONObject currencies){
		Iterator<?> keys = currencies.keys();
		
		while(keys.hasNext()){
			String key = (String) keys.next();
			if(currencies.get(key) instanceof String){
				Currency currency = Currency.findByCode(key);
				
				if(currency == null){
					Currency newCurrency = new Currency();
					newCurrency.code = key;
					newCurrency.name = currencies.getString(key);
					newCurrency.insert();
				}
			}
		}
	}
	
	public Promise<Result> syncCurrencies(){
		Promise<Result> promiseCurrencies = ws.url(currencies_url).get().map(resp -> {
			JSONObject currencies = new JSONObject(resp.asJson().toString());

			storeCurrencies(currencies);
			
			return ok();
		});
		
		return promiseCurrencies;
	}
	
	public void updateRates(JSONObject rates){
		Iterator<?> keys = rates.keys();
		
		while(keys.hasNext()){
			String key = (String) keys.next();
			if(rates.get(key) != null){
				Currency.currencies().update("{code: #}", key)
						.with("{$set: {rate: #}}", rates.get(key));
			}
		}
	}
	
	public Promise<Result> syncRates(){
		Promise<Result> promiseRates = ws.url(latest_url).get().map(resp -> {
			JSONObject jsonObject = new JSONObject(resp.asJson().toString());

			Boolean doUpdate = false;
			
			Long timestampRequest = jsonObject.getLong("timestamp");
			
			Currency usd = Currency.findByCode("USD");

			Long timestampDB = 0L;
			
			if(usd == null || usd.rate == null) {
				doUpdate = true;
			} else {
				timestampDB = usd._id.getTimestamp() * 1L;
				
				if(timestampRequest > timestampDB){
					doUpdate = true;
				}
			}
			
			if(doUpdate == true && jsonObject.get("rates") instanceof JSONObject){
				JSONObject rates = jsonObject.getJSONObject("rates");
				
				updateRates(rates);
			}
			
			return ok();
		});
		
		return promiseRates;
	}
	
}
