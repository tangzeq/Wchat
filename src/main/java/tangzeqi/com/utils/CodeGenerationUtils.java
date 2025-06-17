package tangzeqi.com.utils;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.sun.codemodel.CodeWriter;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JPackage;
import org.jsonschema2pojo.*;
import org.jsonschema2pojo.rules.RuleFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;

public class CodeGenerationUtils {

    public static String jsonFormat(String text) {
        try {
            // 解析JSON
            JsonObject jsonObject = new GsonBuilder().create().fromJson(text, JsonObject.class);
            // 格式化输出JSON
            String formattedJson = new GsonBuilder().setPrettyPrinting().create().toJson(jsonObject);
            return formattedJson;
        } catch (Exception ee) {
            ee.printStackTrace();
            return "Error occurred: " + ee.getMessage();
        }
    }
    public static String jsonToCode(GenerationConfig config,String text) {
        if(config == null) config = defaultConfig();
        StringWriter stringWriter = new StringWriter();
        try {
            // 创建一个JCodeModel实例
            JCodeModel codeModel = new JCodeModel();
            // 创建一个GenerationConfig实例
            // 创建一个SchemaMapper实例
            SchemaMapper mapper = new SchemaMapper(new RuleFactory(config, new NoopAnnotator() {
                @Override
                public boolean isAdditionalPropertiesSupported() {
                    return false;
                }}, new SchemaStore()), new SchemaGenerator());
            // 使用SchemaMapper将JSON字符串转换为Java类
            mapper.generate(codeModel, "Demo", "com.demo", text);
            // 生成Java代码并写入到StringWriter中
            codeModel.build(new CodeWriter() {
                @Override
                public OutputStream openBinary(JPackage jPackage, String s) throws IOException {
                    return null;
                }
                @Override
                public Writer openSource(JPackage pkg, String fileName) throws IOException {
                    return stringWriter;
                }
                @Override
                public void close() throws IOException {
                    stringWriter.close();
                }
            });
            return stringWriter.toString();
        } catch (Exception ee) {
            ee.printStackTrace();
            return "Error occurred: " + ee.getMessage();
        }
    }

    private static GenerationConfig defaultConfig() {
        return new DefaultGenerationConfig() {
            @Override
            public boolean isGenerateBuilders() { // 配置是否生成builder类
                return true;
            }

            @Override
            public boolean isIncludeHashcodeAndEquals() {
                return false;
            }

            @Override
            public boolean isIncludeToString() {
                return false;
            }

            @Override
            public boolean isInitializeCollections() {
                return false;
            }

            @Override
            public boolean isIncludeAllPropertiesConstructor() {
                return false;
            }

            @Override
            public boolean isIncludeCopyConstructor() {
                return false;
            }

            @Override
            public boolean isIncludeAdditionalProperties() {
                return false;
            }

            @Override
            public boolean isIncludeGetters() {
                return false;
            }

            @Override
            public boolean isIncludeSetters() {
                return false;
            }

            @Override
            public boolean isIncludeGeneratedAnnotation() {
                return false;
            }

            @Override
            public boolean isUseOptionalForGetters() {
                return false;
            }

            @Override
            public SourceType getSourceType() { // 配置源类型
                return SourceType.JSON;
            }
        };
    }

}
