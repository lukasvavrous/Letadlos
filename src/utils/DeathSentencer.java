package utils;

public class DeathSentencer {
    public static String getTextForCollisionType(ColliderType type){
        String sentence;

        switch (type){
            case OTHER -> sentence = "Zabil tě neznámý objekt";
            case BUILDING -> sentence = "Narazil jsi do stavby";
            case TERRAIN -> sentence = "Spadl si na zem";
            case RUNWAY -> sentence = "Vzletová plocha byla tvou zkázou";
            default -> sentence = "Neznámý důvod smrti";
        }

        return sentence;
    }
}
