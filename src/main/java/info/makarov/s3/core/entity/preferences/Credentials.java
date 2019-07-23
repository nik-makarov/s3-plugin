package info.makarov.s3.core.entity.preferences;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Credentials {

    private String accessKey;

    private String secretKey;

}
