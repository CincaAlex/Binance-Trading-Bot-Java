package Strategy;
import API.APIManager;

public abstract class Strategy {
    protected APIManager apiManager;

    public Strategy(APIManager apiManager) {
        this.apiManager = apiManager;
    }

    public abstract String execute(String symbol);
}
