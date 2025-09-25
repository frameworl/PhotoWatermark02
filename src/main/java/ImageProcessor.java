package main.java;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.ImageIcon;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * 图片处理类
 * 负责图片的导入、导出和格式支持
 */
public class ImageProcessor {
    private String outputFormat = "jpeg"; // 默认输出格式
    private float jpegQuality = 0.9f;     // 默认JPEG质量
    private String namingRule = "original"; // 命名规则：original, prefix, suffix
    private String customText = "watermarked"; // 自定义前缀或后缀
    
    /**
     * 加载图片文件
     * @param file 图片文件
     * @return 加载的图片
     */
    public BufferedImage loadImage(File file) {
        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 创建图片缩略图
     * @param file 图片文件
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @return 缩略图
     */
    public ImageIcon createThumbnail(File file, int maxWidth, int maxHeight) {
        try {
            BufferedImage originalImage = ImageIO.read(file);
            if (originalImage == null) {
                return null;
            }
            
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            
            // 计算缩放比例
            double scale = Math.min(
                    (double) maxWidth / originalWidth,
                    (double) maxHeight / originalHeight
            );
            
            // 计算缩略图尺寸
            int thumbWidth = (int) (originalWidth * scale);
            int thumbHeight = (int) (originalHeight * scale);
            
            // 创建缩略图
            BufferedImage thumbnail = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = thumbnail.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImage, 0, 0, thumbWidth, thumbHeight, null);
            g2d.dispose();
            
            return new ImageIcon(thumbnail);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 保存图片到文件
     * @param image 图片
     * @param outputFile 输出文件
     */
    public void saveImage(Image image, File outputFile) {
        try {
            // 转换为BufferedImage
            BufferedImage bufferedImage;
            if (image instanceof BufferedImage) {
                bufferedImage = (BufferedImage) image;
            } else {
                bufferedImage = new BufferedImage(
                        image.getWidth(null),
                        image.getHeight(null),
                        outputFormat.equalsIgnoreCase("png") ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB
                );
                Graphics2D g2d = bufferedImage.createGraphics();
                g2d.drawImage(image, 0, 0, null);
                g2d.dispose();
            }
            
            // 根据输出格式保存
            if (outputFormat.equalsIgnoreCase("jpeg") || outputFormat.equalsIgnoreCase("jpg")) {
                saveJPEG(bufferedImage, outputFile, jpegQuality);
            } else if (outputFormat.equalsIgnoreCase("png")) {
                ImageIO.write(bufferedImage, "png", outputFile);
            } else {
                // 默认使用JPEG
                saveJPEG(bufferedImage, outputFile, jpegQuality);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 保存JPEG图片，可控制质量
     * @param image 图片
     * @param outputFile 输出文件
     * @param quality 质量 (0.0-1.0)
     */
    private void saveJPEG(BufferedImage image, File outputFile, float quality) throws IOException {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IOException("No JPEG writer found");
        }
        
        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);
        
        try (FileImageOutputStream output = new FileImageOutputStream(outputFile)) {
            writer.setOutput(output);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
    }
    
    /**
     * 根据命名规则生成输出文件名
     * @param originalName 原文件名
     * @return 输出文件名
     */
    public String getOutputFileName(String originalName) {
        String baseName = originalName;
        String extension = "";
        
        // 分离文件名和扩展名
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = originalName.substring(0, dotIndex);
            extension = originalName.substring(dotIndex);
        }
        
        // 根据输出格式修改扩展名
        if (outputFormat.equalsIgnoreCase("jpeg")) {
            extension = ".jpg";
        } else if (outputFormat.equalsIgnoreCase("png")) {
            extension = ".png";
        }
        
        // 根据命名规则生成文件名
        if (namingRule.equals("original")) {
            return baseName + extension;
        } else if (namingRule.equals("prefix")) {
            return customText + baseName + extension;
        } else if (namingRule.equals("suffix")) {
            return baseName + customText + extension;
        } else {
            return baseName + "_" + customText + extension;
        }
    }
    
    // Getters and Setters
    
    public String getOutputFormat() {
        return outputFormat;
    }
    
    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }
    
    public float getJpegQuality() {
        return jpegQuality;
    }
    
    public void setJpegQuality(float jpegQuality) {
        this.jpegQuality = jpegQuality;
    }
    
    public String getNamingRule() {
        return namingRule;
    }
    
    public void setNamingRule(String namingRule) {
        this.namingRule = namingRule;
    }
    
    public String getCustomText() {
        return customText;
    }
    
    public void setCustomText(String customText) {
        this.customText = customText;
    }
    
    /**
     * 调整图片大小
     * @param image 原图片
     * @param width 新宽度
     * @param height 新高度
     * @return 调整大小后的图片
     */
    public BufferedImage resizeImage(BufferedImage image, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, 
                image.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : image.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        return resizedImage;
    }
}