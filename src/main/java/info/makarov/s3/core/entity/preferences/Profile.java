package info.makarov.s3.core.entity.preferences;

import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    private String name;

    private Credentials credentials;

    private String endpoint;

    private boolean useHttps;

    private String region;

    private String delimiter;

    @Override
    public String toString() {
        return name;
    }
}
