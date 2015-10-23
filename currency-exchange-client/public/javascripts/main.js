(function(){

    var model;
    var params = '';
    var searchModel;
    var autocompleteList = [];
    var currencyCodes = [];
    var listRoute = '#list';
    var exchangeRoute = '#exchange';

	function populateModel(params, callback){
        showLoading();

	    $.getJSON('/currencies' + params, function(data){
            showOk();

	        model = data;

	        if(!searchModel) {
	           searchModel = data;
               updateDatalist();
           }

	        if(callback)
	            callback();
	    });
	}

	function updateDatalist(){
        if(searchModel){
            var currencies = model.currencies;

            for(var i in currencies){
                autocompleteList[i] = {
                    value: currencies[i].code + ' - ' + currencies[i].name,
                    data: currencies[i].code
                };

                currencyCodes[i] = currencies[i].code;
            }

            $('#search').autocomplete({
                lookup: autocompleteList,
                onSelect: function (suggestion) {
                    location.hash = listRoute + '/' + suggestion.data;
                }
            });

            $('#from').autocomplete({
                lookup: autocompleteList,
                onSelect: function (suggestion) {
                    clearResult();
                    $('#from').attr('data-value', suggestion.data);
                    $('#to').focus();
                }
            });

            $('#to').autocomplete({
                lookup: autocompleteList,
                onSelect: function (suggestion) {
                    $('#to').attr('data-value', suggestion.data);

                    clearResult();
                    doExchange();
                }
            });
        } else {
            populateModel('', updateDatalist);
        }
	}

	function listCurrencies(){
	    if(model){
	        var currencies = model.currencies;

	        var tbody = document.getElementById('currenciesBody');
	        tbody.innerHTML = '';

	        for(var i in currencies){
	            var tr = document.createElement('tr');

	            var tdCode = document.createElement('td');
	            var txCode = document.createTextNode(currencies[i].code);
	            tdCode.appendChild(txCode);

	            var tdName = document.createElement('td');
                var txName = document.createTextNode(currencies[i].name);
                tdName.appendChild(txName);

	            var tdRate = document.createElement('td');
                var txRate = document.createTextNode(currencies[i].rate);
                tdRate.appendChild(txRate);

                tr.appendChild(tdCode);
                tr.appendChild(tdName);
                tr.appendChild(tdRate);

                tbody.appendChild(tr);
	        }

            $('div#currencyExchange').hide();
            $('div#listCurrencies').show();
	    }
	}

    function doExchange(){
        var amount = $('#amount').val();
        var fromCurrency = $('#from').attr('data-value');
        var toCurrency = $('#to').attr('data-value');

        if(amount === ''){
            $('#amount').addClass('error');
        } else if(fromCurrency === '' || currencyCodes.indexOf(fromCurrency) === -1){
            $('#from').addClass('error');
        } else if(toCurrency === '' || currencyCodes.indexOf(toCurrency) === -1){
            $('#to').addClass('error');
        } else {
    	    showLoading();

    	    $.getJSON('/exchange/' + parseFloat(amount) + '/' + fromCurrency + '/' + toCurrency, function(data){
    	        showOk();

                $('#result').html(data.result + ' ' + toCurrency);
                $('#exchangeResult').show();
            });
        }
    }

    function clearResult(){
        $('#result').html('');
        $('#exchangeResult').hide();
    }

	function exchangeCurrencies(){
        populateModel('', function(){
            $('div#listCurrencies').hide();
            $('div#currencyExchange').show();
        });
    }

	function hideMessages(){
	    $('.messages').hide();
	}

    function showLoading(){
	    $('#loading').show();
    }

    function showOk(){
        $('#loading').hide();
        $('#load-ok').show();

        setTimeout(hideMessages, 2000);
    }

    function route(hash) {
        var query = hash.split('/');

        if (query.length > 0) {
            if (query[0] === listRoute) {
                $('li#btnExchangeCurrencies').removeClass('active');
                $('li#btnListCurrencies').addClass('active');
                clearResult();
                $('#amount').val('');
                $('#from').attr('data-value','');
                $('#from').val('');
                $('#to').attr('data-value','');
                $('#to').val('');

                params = '';

                if(query[1]){
                    params = '/' + query[1];
                }

                populateModel(params, listCurrencies);
            } else if (query[0] === exchangeRoute) {
                $('li#btnListCurrencies').removeClass('active');
                $('li#btnExchangeCurrencies').addClass('active');

                $('#search').val('');

                exchangeCurrencies();
            }
        }
    }

	window.onload = function(){
        $('li#btnListCurrencies').click(function(){
            location.hash = listRoute;
        });

        $('li#btnExchangeCurrencies').click(function(){
            location.hash = exchangeRoute;

            $('li').removeClass('active');
            $(this).addClass('active');
        });

        $('li#btnRefresh').click(function(){
            populateModel(params, listCurrencies);
        });

        $('div#fields input').keyup(function(e){
            if($(this).val() !== ''){
                $(this).removeClass('error');
            }
        });

        $('button#calculate').click(function(){
            doExchange();
        });

        route(location.hash);
	};

    window.onhashchange = function() {
        route(location.hash);
    };
})();
