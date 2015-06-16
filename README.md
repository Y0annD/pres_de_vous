# Près de vous

Application web orientée mobile pour voir les photos localisées près

de soi.

#instalation

Pour utiliser près de vous, vous devez avoir une instance de *MongoDB*
en cours d'execution, et avoir configuré correctement le fichier
*conf.json* présent dans à la racine du projet.


#Classe credentials pour proxy

```java
public class Credentials {

    public static String proxyHost = "host";
    public static int proxyPort = 3128;
    public static String username = "username";
    public static String password = "password";
    public static String proxy_auth = "Basic eeeeeeeeeeeeeeeeeeeeeeeeee=";

}
```

#lancement du serveur

Il ne vous reste plus qu'à lancer le serveur avec *vertX*.

