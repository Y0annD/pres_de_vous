function initialize() {
    map = new google.maps.Map(document.getElementById('map-canvas'), {
        center: new google.maps.LatLng(48.3612161, -4.5661822),
        zoom: 15,
        mapTypeId: google.maps.MapTypeId.ROADMAP,
        // On vire ce qui sert à rien
        mapTypeControl: false,
        streetViewControl: false
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
        center = new google.maps.LatLng(place.geometry.location.lat(), place.geometry.location.lng());
        map.setCenter(center);
    });

    //Listener : clic champ
    input.addEventListener('click', function() {
        input.value = '';
    });
}