/**
 * Created by Yoann Diquélou on 27/05/15.
 */
if (navigator.geolocation)
    navigator.geolocation.getCurrentPosition(successCallback, errorCallback);
else
    alert("Dommage... Votre navigateur ne prend pas en compte la géolocalisation HTML5");

function successCallback(position){
    alert("Latitude : " + position.coords.latitude + ", longitude : " + position.coords.longitude);
    map.panTo(new google.maps.LatLng(position.coords.latitude, position.coords.longitude));
    var marker = new google.maps.Marker({
        position: new google.maps.LatLng(position.coords.latitude, position.coords.longitude),
        map: map
    });
};

function errorCallback(error){
    switch(error.code){
        case error.PERMISSION_DENIED:
            alert("L'utilisateur n'a pas autorisé l'accès à sa position");
            break;
        case error.POSITION_UNAVAILABLE:
            alert("L'emplacement de l'utilisateur n'a pas pu être déterminé");
            break;
        case error.TIMEOUT:
            alert("Le service n'a pas répondu à temps");
            break;
    }
};