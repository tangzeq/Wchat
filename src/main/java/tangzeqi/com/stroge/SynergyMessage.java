package tangzeqi.com.stroge;

import lombok.*;

import java.io.Serializable;

/**
 * 功能描述：协同信息
 * 作者：唐泽齐
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SynergyMessage extends BaseUser implements Serializable {
    private int startOffset;
    private int endOffset;
    private int oldLength;
    private int newLength;
    private long oldTimeStamp;
    private String str;
    private String file;
    private String project;
}
