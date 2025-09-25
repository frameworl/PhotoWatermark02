package main.java;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 水印管理类
 * 负责水印的应用和管理
 */
public class WatermarkManager {
    private WatermarkSettings settings = new WatermarkSettings();
    
    /**
     * 获取水印设置
     * @return 水印设置对象
     */
    public WatermarkSettings getSettings() {
        return settings;
    }
    
    /**
     * 应用水印到图片
     * @param image 原图片
     * @return 添加水印后的图片
     */
    public BufferedImage applyWatermark(BufferedImage image) {
        if (image == null) {
            return null;
        }
        
        // 创建一个新的图像，保留原图像的类型，确保支持透明度
        BufferedImage result = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        
        // 绘制原图
        Graphics2D g2d = result.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        
        // 设置渲染提示
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // 根据水印类型应用水印
        if (settings.getType() == WatermarkType.TEXT) {
            applyTextWatermark(g2d, image.getWidth(), image.getHeight());
        } else {
            applyImageWatermark(g2d, image.getWidth(), image.getHeight());
        }
        
        g2d.dispose();
        return result;
    }
    
    /**
     * 应用文本水印
     * @param g2d Graphics2D对象
     * @param width 图片宽度
     * @param height 图片高度
     */
    private void applyTextWatermark(Graphics2D g2d, int width, int height) {
        // 创建字体
        Font font = new Font(settings.getFontName(), settings.getFontStyle(), settings.getFontSize());
        g2d.setFont(font);
        
        // 获取文本尺寸
        FontMetrics metrics = g2d.getFontMetrics(font);
        int textWidth = metrics.stringWidth(settings.getText());
        int textHeight = metrics.getHeight();
        
        // 计算位置
        int x = (width - textWidth) * settings.getPositionX() / 100;
        int y = (height - textHeight) * settings.getPositionY() / 100 + metrics.getAscent();
        
        // 保存当前变换
        AffineTransform originalTransform = g2d.getTransform();
        
        // 应用旋转
        if (settings.getRotation() != 0) {
            g2d.rotate(Math.toRadians(settings.getRotation()), x + textWidth / 2, y - textHeight / 2);
        }
        
        // 设置透明度
        AlphaComposite alphaComposite = AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, settings.getTextOpacity());
        g2d.setComposite(alphaComposite);
        
        // 如果启用阴影
        if (settings.isEnableShadow()) {
            g2d.setColor(settings.getShadowColor());
            g2d.drawString(settings.getText(), x + 2, y + 2);
        }
        
        // 绘制文本
        g2d.setColor(settings.getTextColor());
        g2d.drawString(settings.getText(), x, y);
        
        // 恢复原始变换
        g2d.setTransform(originalTransform);
    }
    
    /**
     * 应用图片水印
     * @param g2d Graphics2D对象
     * @param width 图片宽度
     * @param height 图片高度
     */
    private void applyImageWatermark(Graphics2D g2d, int width, int height) {
        if (settings.getWatermarkImage() == null || !settings.getWatermarkImage().exists()) {
            return;
        }
        
        try {
            // 加载水印图片
            BufferedImage watermarkImg = ImageIO.read(settings.getWatermarkImage());
            if (watermarkImg == null) {
                return;
            }
            
            // 计算缩放后的尺寸
            int wmWidth = (int) (watermarkImg.getWidth() * settings.getImageScale());
            int wmHeight = (int) (watermarkImg.getHeight() * settings.getImageScale());
            
            // 计算位置
            int x = (width - wmWidth) * settings.getPositionX() / 100;
            int y = (height - wmHeight) * settings.getPositionY() / 100;
            
            // 保存当前变换
            AffineTransform originalTransform = g2d.getTransform();
            
            // 应用旋转
            if (settings.getRotation() != 0) {
                g2d.rotate(Math.toRadians(settings.getRotation()), x + wmWidth / 2, y + wmHeight / 2);
            }
            
            // 设置透明度
            AlphaComposite alphaComposite = AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, settings.getImageOpacity());
            g2d.setComposite(alphaComposite);
            
            // 绘制水印图片
            g2d.drawImage(watermarkImg, x, y, wmWidth, wmHeight, null);
            
            // 恢复原始变换
            g2d.setTransform(originalTransform);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 设置水印类型
     * @param type 水印类型
     */
    public void setWatermarkType(WatermarkType type) {
        settings.setType(type);
    }
    
    /**
     * 获取当前水印设置
     * @return 水印设置
     */
    public WatermarkSettings getCurrentSettings() {
        return settings;
    }
    
    /**
     * 应用水印设置
     * @param settings 水印设置
     */
    public void applySettings(WatermarkSettings settings) {
        this.settings = settings;
    }
}