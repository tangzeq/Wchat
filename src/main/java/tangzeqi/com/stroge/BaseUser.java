package tangzeqi.com.stroge;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 功能描述：用户信息
 * 作者：唐泽齐
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseUser implements Serializable {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 全局唯一识别号
     */
    private String unicode;

    /**
     * 用户名称
     */
    private String name;

    /**
     * 用户头像
     */
    private String picture;

    /**
     * 时间
     */
    private Long time;

    private Long index;
}
