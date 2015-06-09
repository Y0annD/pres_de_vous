function initialize() {
    var latitude = 48.3612161;
    var longitude = -4.5661822;

    map = new google.maps.Map(document.getElementById('map-canvas'), {
        center: new google.maps.LatLng(latitude, longitude),
        zoom: 15,
        mapTypeId: google.maps.MapTypeId.ROADMAP,
        // On vire ce qui sert à rien
        mapTypeControl: false,
        streetViewControl: false
    });

    var contentString = 'Blank';
    var infowindow = new google.maps.InfoWindow({
        content: contentString
    });

    // Récupération JSON
    var markers = [];

    loadJSON(latitude, longitude, function(response) {
        var json = JSON.parse(response);
        json.forEach(function(element){
            var marker = new google.maps.Marker({
                map: map,
                title: element.location.name,
                image: element.images.standard_resolution,
                position: new google.maps.LatLng(element.location.latitude, element.location.longitude)
            });

            markers.push(marker);

            google.maps.event.addListener(marker, 'click', function() {
                infowindow.setContent(marker.title+'<img alt="'+marker.title+'" src="'+marker.image.url+'"></img>');
                infowindow.open(map, marker);
            });
        })
    });


    // Création champ de recherche
    var input = document.getElementById('search');
    var autocomplete = new google.maps.places.Autocomplete(input);
    var place;
    var center;

    // Ancrage top-left
    map.controls[google.maps.ControlPosition.TOP_LEFT].push(input);

    // Listener : Enter = premier résultat
    google.maps.event.addDomListener(input, 'keydown', function(key) {
        if (key.keyCode === 13 && !key.triggered) {
            google.maps.event.trigger(this, 'keydown', {keyCode: 40});
            google.maps.event.trigger(this, 'keydown', {keyCode: 13, triggered: true});
        }
    });

    // Listener : clic sur un résultat
    google.maps.event.addListener(autocomplete, 'place_changed', function() {
        place = autocomplete.getPlace();
        latitude = place.geometry.location.lat();
        longitude = place.geometry.location.lng();

        center = new google.maps.LatLng(latitude, longitude);
        map.setCenter(center);

        markers.forEach(function(element) {
            element.setMap(null);
        });

        markers = [];

        loadJSON(latitude, longitude, function(response) {
            var json = JSON.parse(response);
            json.forEach(function(element){
                var marker = new google.maps.Marker({
                    map: map,
                    position: new google.maps.LatLng(element.location.latitude, element.location.longitude)
                });

                markers.push(marker);
            })
            console.log(markers);
        });
    });

    // Listener : clic champ
    input.addEventListener('click', function() {
        input.value = '';
    });
}

function loadJSON(lat, lng, callback) {
    var xobj = new XMLHttpRequest();
    xobj.overrideMimeType('application/json');
    xobj.open('GET', '../insta/'+lat+'/'+lng, true);
    xobj.onreadystatechange = function() {
        if (xobj.readyState == 4 && xobj.status == '200')
            callback(xobj.responseText);
    };
    xobj.send(null);
}