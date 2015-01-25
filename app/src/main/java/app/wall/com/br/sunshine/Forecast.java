package app.wall.com.br.sunshine;

/**
 * Created by Manuele on 20/01/2015.
 */
public enum Forecast {
    SUNNY ( "Today - Sunny" ),
    FOGGY ( "Tomorrow - Foggy" ),
    CLOUDY ( "Mon - Cloudy" ),
    SLEETING ( "Tue - Sleeting" ),
    PARTLY_CLOUDY ( "Wed - Partly Cloudy" ),
    RAINY ( "Thu - Rainy" ),
    SNOWING ( "Fri - Snowing" ),
    WINDY ( "Sat - Windy" ),
    SNOWY ( "Sun - Snowy" );

    private String name;

    Forecast( final String name )
    {
        setName( name );
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
