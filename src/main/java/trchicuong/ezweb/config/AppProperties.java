package trchicuong.ezweb.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    private String name;
    private String baseUrl;

    private Bank bank = new Bank();
    private Topup topup = new Topup();
    private Gemini gemini = new Gemini();
    private Download download = new Download();
    private Storage storage = new Storage();

    @Getter @Setter
    public static class Bank {
        private String id;
        private String name;
        private String accountNumber;
        private String accountName;
    }

    @Getter @Setter
    public static class Topup {
        private String provider;
        private String secret;
        private String prefix;
    }

    @Getter @Setter
    public static class Gemini {
        private String apiKey;
        private String model;
    }

    @Getter @Setter
    public static class Download {
        private int tokenTtlHours;
    }

    @Getter @Setter
    public static class Storage {
        private String path;
    }

    public String getTransferCode(Long userId) {
        return topup.getPrefix() + userId;
    }
}
