package tangzeqi.com.stroge;

import lombok.*;

import java.io.Serializable;

/**
 * 功能描述：UPD信息
 * 作者：唐泽齐
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UPDMessage extends BaseUser implements Serializable {
    /**
     * 验证
     */
    private String token;
    private String mac;
}
