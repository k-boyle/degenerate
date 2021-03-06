package kboyle.degenerate;


public enum Utils {
    ;

    public static boolean insensitiveContains(String a, String b) {
        for (var i = a.length() - b.length(); i >= 0; i--)
            if (a.regionMatches(true, i, b, 0, b.length()))
                return true;
        return false;
    }

    public static String getTweetUrl(String userId, String tweetId) {
        return String.format("https://twitter.com/%s/status/%s", userId, tweetId);
    }
}
