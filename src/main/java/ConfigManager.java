package main.java;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置管理类
 * 负责水印模板的保存和加载
 */
public class ConfigManager {
    private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + ".photowatermark";
    private static final String TEMPLATES_FILE = CONFIG_DIR + File.separator + "templates.dat";
    private static final String LAST_SETTINGS_FILE = CONFIG_DIR + File.separator + "last_settings.dat";
    
    private Map<String, WatermarkSettings> templates = new HashMap<>();
    
    public ConfigManager() {
        // 确保配置目录存在
        File configDir = new File(CONFIG_DIR);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        // 加载模板
        loadTemplates();
    }
    
    /**
     * 保存水印模板
     * @param name 模板名称
     * @param settings 水印设置
     */
    public void saveTemplate(String name, WatermarkSettings settings) {
        templates.put(name, settings);
        saveTemplates();
    }
    
    /**
     * 加载水印模板
     * @param name 模板名称
     * @return 水印设置
     */
    public WatermarkSettings loadTemplate(String name) {
        return templates.get(name);
    }
    
    /**
     * 删除水印模板
     * @param name 模板名称
     */
    public void deleteTemplate(String name) {
        templates.remove(name);
        saveTemplates();
    }
    
    /**
     * 获取所有模板名称
     * @return 模板名称列表
     */
    public List<String> getTemplateNames() {
        return new ArrayList<>(templates.keySet());
    }
    
    /**
     * 保存最后使用的设置
     * @param settings 水印设置
     */
    public void saveLastSettings(WatermarkSettings settings) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(LAST_SETTINGS_FILE))) {
            oos.writeObject(settings);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 加载最后使用的设置
     * @return 水印设置，如果没有则返回null
     */
    public WatermarkSettings loadLastSettings() {
        File file = new File(LAST_SETTINGS_FILE);
        if (!file.exists()) {
            return null;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(LAST_SETTINGS_FILE))) {
            return (WatermarkSettings) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 加载所有模板
     */
    private void loadTemplates() {
        File file = new File(TEMPLATES_FILE);
        if (!file.exists()) {
            return;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(TEMPLATES_FILE))) {
            templates = (Map<String, WatermarkSettings>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            templates = new HashMap<>();
        }
    }
    
    /**
     * 保存所有模板
     */
    private void saveTemplates() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(TEMPLATES_FILE))) {
            oos.writeObject(templates);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}