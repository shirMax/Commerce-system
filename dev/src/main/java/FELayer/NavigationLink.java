package FELayer;

public class NavigationLink {
    private String name;
    private String url;

    public NavigationLink(String name, String url) {
        this.name = name;
        this.url = url;
    }

    // getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}