function initialize() {
    map = new google.maps.Map(document.getElementById("map_canvas"), {
        zoom: 19,
        center: new google.maps.LatLng(48.3612161, -4.5661822),
        mapTypeId: google.maps.MapTypeId.ROADMAP
    });
} 