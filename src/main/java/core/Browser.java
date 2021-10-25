package core;

public enum Browser {
    CHROME("chrome"),
    FIREFOX("firefox"),
    INTERNET_EXPLORER("ie"),
    CHROME_HEADLESS("headless"),
    CHROME_INCOGNITO("incognito");

    private final String text;

    Browser(String text) {
        this.text = text;
    }

    public String get() {
        return this.text;
    }

    @Override
    public String toString() {
        return this.text;
    }

    public static Browser findByString(String str) {
        for (Browser v : values()) {
            if (v.get().equals(str)) {
                return v;
            }
        }
        throw new IllegalArgumentException();
    }
}
