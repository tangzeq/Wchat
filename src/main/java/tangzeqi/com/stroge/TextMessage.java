package tangzeqi.com.stroge;

import lombok.*;

import java.io.Serializable;

/**
 * 功能描述：文本信息
 * 作者：唐泽齐
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextMessage extends BaseUser implements Serializable {
    /**
     * 入库信息
     */
    private String message;
}
