package main.java;

import java.awt.*;
import java.io.File;
import java.io.Serializable;

/**
 * 水印设置类
 * 存储水印的所有配置参数
 */
public class WatermarkSettings implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // 水印类型
    private WatermarkType type = WatermarkType.TEXT;
    
    // 文本水印设置
    private String text = "Watermark";
    private String fontName = "Arial";
    private int fontSize = 24;
    private boolean bold = false;
    private boolean italic = false;
    private Color textColor = Color.BLACK;
    private float textOpacity = 0.7f;
    private boolean enableShadow = false;
    private Color shadowColor = Color.WHITE;
    
    // 图片水印设置
    private File watermarkImage = null;
    private float imageScale = 0.5f;
    private float imageOpacity = 0.7f;
    private String watermarkImagePath = null;
    
    // 位置设置
    private int positionX = 50; // 百分比位置 (0-100)
    private int positionY = 50; // 百分比位置 (0-100)
    private int rotation = 0;   // 旋转角度 (0-360)
    
    // 构造函数
    public WatermarkSettings() {
    }
    
    // Getters and Setters
    
    public WatermarkType getType() {
        return type;
    }
    
    public void setType(WatermarkType type) {
        this.type = type;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getFontName() {
        return fontName;
    }
    
    public void setFontName(String fontName) {
        this.fontName = fontName;
    }
    
    public int getFontSize() {
        return fontSize;
    }
    
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }
    
    public boolean isBold() {
        return bold;
    }
    
    public void setBold(boolean bold) {
        this.bold = bold;
    }
    
    public boolean isItalic() {
        return italic;
    }
    
    public void setItalic(boolean italic) {
        this.italic = italic;
    }
    
    public Color getTextColor() {
        return textColor;
    }
    
    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }
    
    public float getTextOpacity() {
        return textOpacity;
    }
    
    public void setTextOpacity(float textOpacity) {
        this.textOpacity = textOpacity;
    }
    
    public boolean isEnableShadow() {
        return enableShadow;
    }
    
    public void setEnableShadow(boolean enableShadow) {
        this.enableShadow = enableShadow;
    }
    
    public Color getShadowColor() {
        return shadowColor;
    }
    
    public void setShadowColor(Color shadowColor) {
        this.shadowColor = shadowColor;
    }
    
    public File getWatermarkImage() {
        return watermarkImage;
    }
    
    public void setWatermarkImage(File watermarkImage) {
        this.watermarkImage = watermarkImage;
    }
    
    public String getWatermarkImagePath() {
        return watermarkImagePath;
    }
    
    public void setWatermarkImagePath(String path) {
        this.watermarkImagePath = path;
        if (path != null) {
            this.watermarkImage = new File(path);
        }
    }
    
    public float getImageScale() {
        return imageScale;
    }
    
    public void setImageScale(float imageScale) {
        this.imageScale = imageScale;
    }
    
    public float getImageOpacity() {
        return imageOpacity;
    }
    
    public void setImageOpacity(float imageOpacity) {
        this.imageOpacity = imageOpacity;
    }
    
    public int getPositionX() {
        return positionX;
    }
    
    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }
    
    public int getPositionY() {
        return positionY;
    }
    
    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }
    
    public int getRotation() {
        return rotation;
    }
    
    public void setRotation(int rotation) {
        this.rotation = rotation;
    }
    
    // 获取字体样式
    public int getFontStyle() {
        int style = Font.PLAIN;
        if (bold) style |= Font.BOLD;
        if (italic) style |= Font.ITALIC;
        return style;
    }
}