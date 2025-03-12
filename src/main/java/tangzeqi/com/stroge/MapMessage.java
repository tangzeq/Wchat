package tangzeqi.com.stroge;

import lombok.*;

import java.io.Serializable;
import java.util.Map;

/**
 * 功能描述：文本信息
 * 作者：唐泽齐
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapMessage extends BaseUser implements Serializable {
    /**
     * 入库信息
     */
    private Map<String, String> message;
}
