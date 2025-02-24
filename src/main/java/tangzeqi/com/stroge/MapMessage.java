package tangzeqi.com.stroge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 功能描述：文本信息
 * 作者：唐泽齐
 */
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
