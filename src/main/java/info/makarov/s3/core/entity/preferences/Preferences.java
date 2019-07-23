package info.makarov.s3.core.entity.preferences;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class Preferences {

    private List<Profile> profiles;

    private Profile defaultProfile = new Profile()
            .setName("default")
            .setRegion("us-east-1")
            .setCredentials(new Credentials("", ""))
            .setEndpoint("https://s3.example.com")
            .setUseHttps(true)
            .setDelimiter("-");

    private String version = "1.0";
}
