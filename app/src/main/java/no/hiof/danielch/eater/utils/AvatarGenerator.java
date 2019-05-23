package no.hiof.danielch.eater.utils;

import java.util.Random;

//Generates an URL based on user's display name, and a random color.
public class AvatarGenerator {

    String[] colors = new String[]{"f44336", "e91e63", "9c27b0", "3f51b5", "03a9f4", "4caf50", "ffeb3b", "ffc107", "ff5722"};

    public String generateAvatar(String name){
        Random random = new Random();

        name.replace(' ', '+');
        String URL = "https://ui-avatars.com/api/?name=" + name
                + "&background=" + getRandomString(colors, random)
                + "&size=256";

        return URL;
    };

    private static String getRandomString(String[] array, Random random) {
        int ind = random.nextInt(array.length);
        return array[ind];
    }

}

