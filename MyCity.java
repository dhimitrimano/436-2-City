// DHIMITRI MANO

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MyCity {
    private static String API_KEY_1 = "[not available]";
    private static String city = "";
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("ERROR: Please use a city name! Use as many spaces as needed.");
            return;
        }
        for (String arg : args) {
            city += arg + " ";
        }
        city = city.substring(0, city.length() - 1);
        URL start = new URL("http://api.openweathermap.org/geo/1.0/direct?q=" + city + "&limit=1&appid=" + API_KEY_1);
        
        HttpURLConnection connection = connectionOpen(start);

        if (connection == null) {
            System.out.println("ERROR: " + city + " is not a valid city!");
            return;
        }

        String line = (new BufferedReader(new InputStreamReader(connection.getInputStream()))).readLine();
        if (line.equals("[]")) {
            System.out.println("ERROR: " + city + " is not a valid city!");
            return;
        }
        String lat = line.substring(line.indexOf("\"lat\":") + 6, line.indexOf(",", line.indexOf("\"lat\":") + 6));
        String lon = line.substring(line.indexOf("\"lon\":") + 6, line.indexOf(",", line.indexOf("\"lon\":") + 6));
        String country = line.substring(line.indexOf("\"country\":\"") + 11, line.indexOf("\"country\":\"") + 13);

        start = new URL("http://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + API_KEY_1);

        connection = connectionOpen(start);

        if (connection == null) {
            System.out.println("ERROR: An unexpected exception occured.");
            return;
        }

        line = (new BufferedReader(new InputStreamReader(connection.getInputStream()))).readLine();

        try {
            Double mainTemp = Double.parseDouble(line.substring(line.indexOf("\"temp\":") + 7,
                                                 line.indexOf(",", line.indexOf("\"temp\":") + 7))) - 273.15;
            Double minTemp = Double.parseDouble(line.substring(line.indexOf("\"temp_min\":") + 11,
                                                line.indexOf(",", line.indexOf("\"temp_min\":") + 11))) - 273.15;
            Double maxTemp = Double.parseDouble(line.substring(line.indexOf("\"temp_max\":") + 11,
                                                line.indexOf(",", line.indexOf("\"temp_max\":") + 11))) - 273.15;
            String weather = line.substring(line.indexOf("\"description\":\"") + 15,
                                            line.indexOf("\",", line.indexOf("\"description\":\"") + 15));
            Double visibility = Double.parseDouble(line.substring(line.indexOf("\"visibility\":") + 13,
                                                   line.indexOf(",", line.indexOf("\"visibility\":") + 13))) / 1000;
            String[] directions = {"N/A","north","north northeast","northeast","east northeast","east","east southeast",
                                   "southeast","south southeast","south","south southwest","southwest","west southwest",
                                   "west","west northwest","northwest","north northwest"};
            Double wind_speed = 0.0;
            int wind_dir = 0;
            if (line.indexOf("\"wind\":") != -1) {
                wind_speed = Double.parseDouble(line.substring(line.indexOf("\"speed\":") + 8,
                                                line.indexOf(",", line.indexOf("\"speed\":") + 8)));
                wind_dir = Integer.parseInt(line.substring(line.indexOf("\"deg\":") + 6,
                                            Math.min(line.indexOf(",", line.indexOf("\"deg\":") + 6),
                                            line.indexOf("}", line.indexOf("\"deg\":") + 6))));
                wind_dir = (int)(((wind_dir + 11) % 360 / (22.5))) + 1;
            }
            int humidity = -25;
            if (line.indexOf("\"humidity\":") != -1) {
                humidity = Integer.parseInt(line.substring(line.indexOf("\"humidity\":") + 11,
                                            Math.min(line.indexOf(",", line.indexOf("\"humidity\":") + 11),
                                            line.indexOf("}", line.indexOf("\"humidity\":") + 11))));
            }
            System.out.println(city + "\nWeather currently consists of " + weather);
            System.out.println("Current temperature is " + String.format("%.2f", mainTemp) + "°C");
            System.out.println("Temperature currently rests between " + String.format("%.2f", minTemp) +
                               "°C and " + String.format("%.2f", maxTemp) + "°C");
            System.out.println("Visibility reaches " + String.format("%.3f", visibility) + " kilometers");
            System.out.println("Wind speed is at " + wind_speed + " meters per second towards the " +
                               directions[wind_dir]);
            if (humidity != -25) {
                System.out.println("Humidity is at " + humidity + " percent"); 
            }
        } catch(Exception e) {
            System.out.println("ERROR: An unexpected exception occured.");
            return;
        }
        
        start = new URL("https://restcountries.com/v3.1/alpha/" + country);
        connection = connectionOpen(start);

        if (connection == null) {
            System.out.println("ERROR: An unexpected exception occured.");
            return;
        }
        line = (new BufferedReader(new InputStreamReader(connection.getInputStream()))).readLine();

        try {
            country = line.substring(line.indexOf("\"official\":\"") + 12,
                                     line.indexOf("\",", line.indexOf("\"official\":\"") + 12));
            String name = line.substring(line.indexOf("\"common\":\"") + 10,
                                         line.indexOf("\",", line.indexOf("\"common\":\"") + 10));
            Double capLat = Double.parseDouble(line.substring(line.indexOf("\"latlng\":[") + 11,
                                               line.indexOf(",", line.indexOf("\"latlng\":[") + 11)));
            Double capLon = Double.parseDouble(line.substring(line.indexOf(",", line.indexOf("\"latlng\":[") + 12) + 1,
                                               line.indexOf("]", line.indexOf("\"latlng\":[") + 11)));
            String region = line.substring(line.indexOf("\"region\":\"") + 10,
                                           line.indexOf("\"", line.indexOf("\"region\":\"") + 10));
            String subregion = line.substring(line.indexOf("\"subregion\":\"") + 13,
                                              line.indexOf("\"", line.indexOf("\"subregion\":\"") + 13));
            Boolean lang = true;
            String languages = "";
            if (line.indexOf("\"languages\":{") != -1) {
                line = line.substring(line.indexOf("\"languages\":{") + 13);
                while (!line.substring(0,2).equals("\"}")) {
                    languages += line.substring(line.indexOf(":") + 2,
                                           line.indexOf("\"", line.indexOf(":") + 2));
                    languages += ", ";
                    line = line.substring(line.indexOf("\"", line.indexOf(":") + 2));
                }
                languages = languages.substring(0, languages.length() - 2);
            } else {
                languages = "N/A";
            }

            if (country.equals(city) || name.equals(city)) {
                System.out.println("\n" + city + " is a country");
            } else if (country.equals(name)) {
                System.out.println("\n" + city + " is considered part of " + country);
            } else {
                System.out.println("\n" + city + " is considered part of the " + country + ", commonly known as " + name);
            }
            System.out.println("It is part of the " + region + " region, in the " + subregion + " subregion");
            if (languages.indexOf(",") != -1) {
                System.out.println("This country's official languages are " + languages);
            } else {
                System.out.println("This country's official language is " + languages);
            }

        } catch(Exception e) {
            System.out.println("ERROR: An unexpected exception occured.");
            return;
        }
    }

    private static HttpURLConnection FiveHandler (URL start, int num) throws IOException {
        try {
            TimeUnit.MILLISECONDS.sleep((int)(100 * (Math.pow(2, num))));
        } catch(Exception e) {
            return null;
        }
        HttpURLConnection connection = (HttpURLConnection) start.openConnection();
        connection.setRequestMethod("GET");
        int responseCode;
        responseCode = connection.getResponseCode();
        if (responseCode / 100 != 2) {
            if (responseCode / 100 == 5 && num < 5) {
                return FiveHandler(start, num + 1);
            }
            return null;
        }
        return connection;
    }

    private static HttpURLConnection connectionOpen (URL start) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) start.openConnection();
        connection.setRequestMethod("GET");
        int responseCode;
        try {
		    responseCode = connection.getResponseCode();
        } catch(Exception e) {
            return null;
        }
        if (responseCode / 100 == 5) {
            connection = FiveHandler(start, 0);
        }
        if (connection == null || connection.getResponseCode() / 100 != 2) {
            return null;
        }
        return connection;
    }
}
