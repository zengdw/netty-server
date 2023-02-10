package com.jinxin.platform.utils;

import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * yaml配置文件读取
 *
 * @author zengd
 * @version 1.0
 * @date 2022/9/19 11:01
 */
@Slf4j
public class YamlConfig {
    private static Map<String, Object> ymlMap = new HashMap<>();

    static {
        Yaml yaml = new Yaml();
        try (InputStream in = new FileInputStream(ProjectConfig.getProjectHome() + File.separator + "config.yml")) {
            ymlMap = yaml.loadAs(in, HashMap.class);
        } catch (FileNotFoundException e) {
            try (InputStream in = YamlConfig.class.getClassLoader().getResourceAsStream("config.yml")) {
                ymlMap = yaml.loadAs(in, HashMap.class);
            } catch (IOException ex) {
                log.error("配置文件[{}]读取异常", "config.yml", e);
            }
        } catch (IOException e) {
            log.error("配置文件[{}]读取异常", "config.yml", e);
        }
    }

    public static String getValue(String key) {
        String separator = ".";
        String[] separatorKeys;
        if (key.contains(separator)) {
            separatorKeys = key.split("\\.");
        } else {
            return ymlMap.get(key).toString();
        }
        Map<String, Object> finalValue = new HashMap<>();
        for (int i = 0; i < separatorKeys.length - 1; i++) {
            if (i == 0) {
                finalValue = (Map<String, Object>) ymlMap.get(separatorKeys[i]);
                continue;
            }
            if (finalValue == null) {
                break;
            }
            finalValue = (Map<String, Object>) finalValue.get(separatorKeys[i]);
        }
        return finalValue == null ? "" : finalValue.get(separatorKeys[separatorKeys.length - 1]).toString();
    }

}
