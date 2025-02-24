package tangzeqi.com.stroge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 功能描述：节点信息
 * 作者：唐泽齐
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeNet {
    private String type;
    private String host;
    private Integer port;
}
