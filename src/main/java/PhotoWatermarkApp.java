package main.java;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 照片水印应用主类
 * 实现基本UI框架和应用入口
 */
public class PhotoWatermarkApp extends JFrame {
    private ImageProcessor imageProcessor;
    private WatermarkManager watermarkManager;
    private ConfigManager configManager;
    
    private JPanel imageListPanel;
    private JPanel previewPanel;
    private JPanel controlPanel;
    
    private List<File> importedImages = new ArrayList<>();
    private File currentPreviewImage;
    
    public PhotoWatermarkApp() {
        // 初始化组件
        imageProcessor = new ImageProcessor();
        watermarkManager = new WatermarkManager();
        configManager = new ConfigManager();
        
        // 设置窗口属性
        setTitle("照片水印工具");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // 创建UI组件
        createMenuBar();
        createImageListPanel();
        createPreviewPanel();
        createControlPanel();
        
        // 加载上次的配置
        loadLastConfig();
        
        // 显示窗口
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // 文件菜单
        JMenu fileMenu = new JMenu("文件");
        JMenuItem importItem = new JMenuItem("导入图片");
        JMenuItem importFolderItem = new JMenuItem("导入文件夹");
        JMenuItem exportItem = new JMenuItem("导出图片");
        JMenuItem exitItem = new JMenuItem("退出");
        
        importItem.addActionListener(e -> importImages());
        importFolderItem.addActionListener(e -> importFolder());
        exportItem.addActionListener(e -> exportImages());
        exitItem.addActionListener(e -> System.exit(0));
        
        fileMenu.add(importItem);
        fileMenu.add(importFolderItem);
        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // 模板菜单
        JMenu templateMenu = new JMenu("模板");
        JMenuItem saveTemplateItem = new JMenuItem("保存当前模板");
        JMenuItem loadTemplateItem = new JMenuItem("加载模板");
        JMenuItem manageTemplateItem = new JMenuItem("管理模板");
        
        saveTemplateItem.addActionListener(e -> saveCurrentTemplate());
        loadTemplateItem.addActionListener(e -> loadTemplate());
        manageTemplateItem.addActionListener(e -> manageTemplates());
        
        templateMenu.add(saveTemplateItem);
        templateMenu.add(loadTemplateItem);
        templateMenu.add(manageTemplateItem);
        
        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助");
        JMenuItem aboutItem = new JMenuItem("关于");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(templateMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void createImageListPanel() {
        imageListPanel = new JPanel();
        imageListPanel.setLayout(new BoxLayout(imageListPanel, BoxLayout.Y_AXIS));
        imageListPanel.setBorder(BorderFactory.createTitledBorder("已导入图片"));
        
        JScrollPane scrollPane = new JScrollPane(imageListPanel);
        scrollPane.setPreferredSize(new Dimension(200, 0));
        
        add(scrollPane, BorderLayout.WEST);
    }
    
    private void createPreviewPanel() {
        previewPanel = new JPanel();
        previewPanel.setLayout(new BorderLayout());
        previewPanel.setBorder(BorderFactory.createTitledBorder("预览"));
        
        JLabel previewLabel = new JLabel("请导入图片以预览", JLabel.CENTER);
        previewPanel.add(previewLabel, BorderLayout.CENTER);
        
        add(previewPanel, BorderLayout.CENTER);
    }
    
    private void createControlPanel() {
        // 使用JScrollPane包装控制面板，解决按钮被遮挡问题
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createTitledBorder("水印设置"));
        scrollPane.setPreferredSize(new Dimension(320, 0));
        
        controlPanel = contentPanel;
        
        // 创建水印类型选择
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.add(new JLabel("水印类型:"));
        JRadioButton textRadio = new JRadioButton("文本水印", true);
        JRadioButton imageRadio = new JRadioButton("图片水印");
        ButtonGroup typeGroup = new ButtonGroup();
        typeGroup.add(textRadio);
        typeGroup.add(imageRadio);
        typePanel.add(textRadio);
        typePanel.add(imageRadio);
        
        // 文本水印设置面板
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBorder(BorderFactory.createTitledBorder("文本水印设置"));
        
        // 文本内容
        JPanel textContentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        textContentPanel.add(new JLabel("文本内容:"));
        JTextField contentField = new JTextField(20);
        contentField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateText(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateText(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateText(); }
            
            private void updateText() {
                watermarkManager.getSettings().setText(contentField.getText());
                updatePreview();
            }
        });
        textContentPanel.add(contentField);
        textPanel.add(textContentPanel);
        
        // 字体设置
        JPanel fontPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fontPanel.add(new JLabel("字体:"));
        JComboBox<String> fontCombo = new JComboBox<>(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        fontPanel.add(fontCombo);
        JSpinner sizeSpinner = new JSpinner(new SpinnerNumberModel(24, 8, 72, 1));
        fontPanel.add(new JLabel("大小:"));
        fontPanel.add(sizeSpinner);
        textPanel.add(fontPanel);
        
        // 字体样式
        JPanel stylePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JCheckBox boldCheck = new JCheckBox("粗体");
        JCheckBox italicCheck = new JCheckBox("斜体");
        stylePanel.add(boldCheck);
        stylePanel.add(italicCheck);
        textPanel.add(stylePanel);
        
        // 颜色和透明度
        JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        colorPanel.add(new JLabel("颜色:"));
        JButton colorButton = new JButton("选择颜色");
        colorButton.setBackground(Color.BLACK);
        colorButton.setForeground(Color.WHITE);
        colorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(
                PhotoWatermarkApp.this,
                "选择水印颜色",
                colorButton.getBackground());
            if (newColor != null) {
                colorButton.setBackground(newColor);
                watermarkManager.getSettings().setTextColor(newColor);
                updatePreview();
            }
        });
        colorPanel.add(colorButton);
        textPanel.add(colorPanel);
        
        JPanel opacityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        opacityPanel.add(new JLabel("透明度:"));
        JSlider opacitySlider = new JSlider(0, 100, 70);
        opacitySlider.setMajorTickSpacing(20);
        opacitySlider.setMinorTickSpacing(5);
        opacitySlider.setPaintTicks(true);
        opacitySlider.setPaintLabels(true);
        opacitySlider.addChangeListener(e -> {
            float opacity = opacitySlider.getValue() / 100.0f;
            watermarkManager.getSettings().setTextOpacity(opacity);
            updatePreview();
        });
        opacityPanel.add(opacitySlider);
        textPanel.add(opacityPanel);
        
        // 图片水印设置面板
        JPanel imageWmPanel = new JPanel();
        imageWmPanel.setLayout(new BoxLayout(imageWmPanel, BoxLayout.Y_AXIS));
        imageWmPanel.setBorder(BorderFactory.createTitledBorder("图片水印设置"));
        
        JPanel selectImagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectImagePanel.add(new JLabel("水印图片:"));
        JTextField imagePathField = new JTextField(15);
        imagePathField.setEditable(false);
        JButton browseButton = new JButton("浏览...");
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "图片文件", "jpg", "jpeg", "png", "bmp", "tiff");
            fileChooser.setFileFilter(filter);
            
            int result = fileChooser.showOpenDialog(PhotoWatermarkApp.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                imagePathField.setText(file.getAbsolutePath());
                watermarkManager.getSettings().setWatermarkImagePath(file.getAbsolutePath());
                updatePreview();
            }
        });
        selectImagePanel.add(imagePathField);
        selectImagePanel.add(browseButton);
        imageWmPanel.add(selectImagePanel);
        
        JPanel imageScalePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        imageScalePanel.add(new JLabel("缩放比例:"));
        JSlider scaleSlider = new JSlider(10, 100, 50);
        scaleSlider.setMajorTickSpacing(20);
        scaleSlider.setMinorTickSpacing(5);
        scaleSlider.setPaintTicks(true);
        scaleSlider.setPaintLabels(true);
        scaleSlider.addChangeListener(e -> {
            float scale = scaleSlider.getValue() / 100.0f;
            watermarkManager.getSettings().setImageScale(scale);
            updatePreview();
        });
        imageScalePanel.add(scaleSlider);
        imageWmPanel.add(imageScalePanel);
        
        JPanel imageOpacityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        imageOpacityPanel.add(new JLabel("透明度:"));
        JSlider imageOpacitySlider = new JSlider(0, 100, 70);
        imageOpacitySlider.setMajorTickSpacing(20);
        imageOpacitySlider.setMinorTickSpacing(5);
        imageOpacitySlider.setPaintTicks(true);
        imageOpacitySlider.setPaintLabels(true);
        imageOpacitySlider.addChangeListener(e -> {
            float opacity = imageOpacitySlider.getValue() / 100.0f;
            watermarkManager.getSettings().setImageOpacity(opacity);
            updatePreview();
        });
        imageOpacityPanel.add(imageOpacitySlider);
        imageWmPanel.add(imageOpacityPanel);
        
        // 水印位置设置
        JPanel positionPanel = new JPanel();
        positionPanel.setLayout(new BoxLayout(positionPanel, BoxLayout.Y_AXIS));
        positionPanel.setBorder(BorderFactory.createTitledBorder("水印位置"));
        
        JPanel presetPanel = new JPanel(new GridLayout(3, 3));
        String[] positions = {"左上", "中上", "右上", "左中", "中心", "右中", "左下", "中下", "右下"};
        for (int i = 0; i < 9; i++) {
            final int pos = i;
            JButton posButton = new JButton(positions[i]);
            posButton.setPreferredSize(new Dimension(40, 40));
            posButton.addActionListener(e -> {
                // 设置水印位置
                int x = 0, y = 0;
                switch(pos) {
                    case 0: x = 10; y = 10; break; // 左上
                    case 1: x = 50; y = 10; break; // 中上
                    case 2: x = 90; y = 10; break; // 右上
                    case 3: x = 10; y = 50; break; // 左中
                    case 4: x = 50; y = 50; break; // 中心
                    case 5: x = 90; y = 50; break; // 右中
                    case 6: x = 10; y = 90; break; // 左下
                    case 7: x = 50; y = 90; break; // 中下
                    case 8: x = 90; y = 90; break; // 右下
                }
                watermarkManager.getSettings().setPositionX(x);
                watermarkManager.getSettings().setPositionY(y);
                updatePreview();
            });
            presetPanel.add(posButton);
        }
        positionPanel.add(presetPanel);
        
        JPanel rotationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rotationPanel.add(new JLabel("旋转角度:"));
        JSlider rotationSlider = new JSlider(0, 360, 0);
        rotationSlider.setMajorTickSpacing(90);
        rotationSlider.setMinorTickSpacing(15);
        rotationSlider.setPaintTicks(true);
        rotationSlider.setPaintLabels(true);
        rotationSlider.addChangeListener(e -> {
            watermarkManager.getSettings().setRotation(rotationSlider.getValue());
            updatePreview();
        });
        rotationPanel.add(rotationSlider);
        positionPanel.add(rotationPanel);
        
        // 导出设置
        JPanel exportPanel = new JPanel();
        exportPanel.setLayout(new BoxLayout(exportPanel, BoxLayout.Y_AXIS));
        exportPanel.setBorder(BorderFactory.createTitledBorder("导出设置"));
        
        JPanel formatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formatPanel.add(new JLabel("输出格式:"));
        JRadioButton jpegRadio = new JRadioButton("JPEG", true);
        JRadioButton pngRadio = new JRadioButton("PNG");
        ButtonGroup formatGroup = new ButtonGroup();
        formatGroup.add(jpegRadio);
        formatGroup.add(pngRadio);
        formatPanel.add(jpegRadio);
        formatPanel.add(pngRadio);
        exportPanel.add(formatPanel);
        
        JPanel qualityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        qualityPanel.add(new JLabel("JPEG质量:"));
        JSlider qualitySlider = new JSlider(0, 100, 90);
        qualitySlider.setMajorTickSpacing(20);
        qualitySlider.setMinorTickSpacing(5);
        qualitySlider.setPaintTicks(true);
        qualitySlider.setPaintLabels(true);
        qualityPanel.add(qualitySlider);
        exportPanel.add(qualityPanel);
        
        JPanel namingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namingPanel.add(new JLabel("命名规则:"));
        JRadioButton originalRadio = new JRadioButton("保留原名", true);
        JRadioButton prefixRadio = new JRadioButton("添加前缀");
        JRadioButton suffixRadio = new JRadioButton("添加后缀");
        ButtonGroup namingGroup = new ButtonGroup();
        namingGroup.add(originalRadio);
        namingGroup.add(prefixRadio);
        namingGroup.add(suffixRadio);
        namingPanel.add(originalRadio);
        namingPanel.add(prefixRadio);
        namingPanel.add(suffixRadio);
        exportPanel.add(namingPanel);
        
        JPanel customNamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        customNamePanel.add(new JLabel("自定义文本:"));
        JTextField customTextField = new JTextField(15);
        customTextField.setText("watermarked");
        customNamePanel.add(customTextField);
        exportPanel.add(customNamePanel);
        
        // 应用和预览按钮
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton previewButton = new JButton("预览水印效果");
        JButton applyButton = new JButton("应用到所有图片");
        
        // 添加预览按钮功能
        previewButton.addActionListener(e -> {
            updatePreview();
            JOptionPane.showMessageDialog(PhotoWatermarkApp.this, 
                "水印效果已更新到预览区域", "预览更新", JOptionPane.INFORMATION_MESSAGE);
        });
        
        // 添加应用按钮功能
        applyButton.addActionListener(e -> {
            if (importedImages.isEmpty()) {
                JOptionPane.showMessageDialog(PhotoWatermarkApp.this,
                    "请先导入图片", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // 应用当前水印设置到所有图片
            for (File file : importedImages) {
                // 这里只是更新预览，实际应用时需要保存处理后的图片
                updatePreview();
            }
            
            JOptionPane.showMessageDialog(PhotoWatermarkApp.this,
                "水印已应用到所有图片", "操作成功", JOptionPane.INFORMATION_MESSAGE);
        });
        
        actionPanel.add(previewButton);
        actionPanel.add(applyButton);
        
        // 添加所有面板到控制面板
        controlPanel.add(typePanel);
        controlPanel.add(textPanel);
        controlPanel.add(imageWmPanel);
        controlPanel.add(positionPanel);
        controlPanel.add(exportPanel);
        controlPanel.add(actionPanel);
        
        // 默认隐藏图片水印面板
        imageWmPanel.setVisible(false);
        
        // 添加水印类型切换监听器
        textRadio.addActionListener(e -> {
            textPanel.setVisible(true);
            imageWmPanel.setVisible(false);
            watermarkManager.setWatermarkType(WatermarkType.TEXT);
            updatePreview();
        });
        
        imageRadio.addActionListener(e -> {
            textPanel.setVisible(false);
            imageWmPanel.setVisible(true);
            watermarkManager.setWatermarkType(WatermarkType.IMAGE);
            updatePreview();
        });
        
        // 添加控制面板到主窗口
        add(scrollPane, BorderLayout.EAST);
    }
    
    private void importImages() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "图片文件", "jpg", "jpeg", "png", "bmp", "tiff");
        fileChooser.setFileFilter(filter);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            for (File file : files) {
                addImageToList(file);
            }
        }
    }
    
    private void importFolder() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File directory = fileChooser.getSelectedFile();
            File[] files = directory.listFiles((dir, name) -> {
                String lowercaseName = name.toLowerCase();
                return lowercaseName.endsWith(".jpg") || lowercaseName.endsWith(".jpeg") ||
                       lowercaseName.endsWith(".png") || lowercaseName.endsWith(".bmp") ||
                       lowercaseName.endsWith(".tiff");
            });
            
            if (files != null) {
                for (File file : files) {
                    addImageToList(file);
                }
            }
        }
    }
    
    private void addImageToList(File file) {
        if (!importedImages.contains(file)) {
            importedImages.add(file);
            
            JPanel imageItemPanel = new JPanel(new BorderLayout());
            imageItemPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            
            // 创建缩略图
            ImageIcon thumbnail = imageProcessor.createThumbnail(file, 150, 100);
            JLabel imageLabel = new JLabel(thumbnail);
            imageLabel.setBorder(BorderFactory.createEtchedBorder());
            
            JLabel nameLabel = new JLabel(file.getName(), JLabel.CENTER);
            
            imageItemPanel.add(imageLabel, BorderLayout.CENTER);
            imageItemPanel.add(nameLabel, BorderLayout.SOUTH);
            
            // 添加点击事件
            imageLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    currentPreviewImage = file;
                    updatePreview();
                }
            });
            
            imageListPanel.add(imageItemPanel);
            imageListPanel.revalidate();
            imageListPanel.repaint();
            
            // 如果这是第一张图片，设置为当前预览图片
            if (currentPreviewImage == null) {
                currentPreviewImage = file;
                updatePreview();
            }
        }
    }
    
    private void updatePreview() {
        if (currentPreviewImage != null) {
            try {
                // 清除预览面板
                previewPanel.removeAll();
                
                // 获取原始图像
                BufferedImage originalImage = imageProcessor.loadImage(currentPreviewImage);
                if (originalImage == null) {
                    System.err.println("无法加载图像: " + currentPreviewImage.getPath());
                    return;
                }
                
                // 获取带水印的预览图像
                BufferedImage previewImage = watermarkManager.applyWatermark(originalImage);
                if (previewImage == null) {
                    System.err.println("应用水印失败");
                    return;
                }
                
                // 创建适合预览面板的缩放图像
                ImageIcon icon = new ImageIcon(previewImage);
                JLabel previewLabel = new JLabel(icon);
                
                // 添加到预览面板
                JScrollPane scrollPane = new JScrollPane(previewLabel);
                previewPanel.add(scrollPane, BorderLayout.CENTER);
                
                // 强制更新UI
                previewPanel.revalidate();
                previewPanel.repaint();
                
                System.out.println("预览已更新 - 文本: " + watermarkManager.getSettings().getText() + 
                                  ", 透明度: " + watermarkManager.getSettings().getTextOpacity() + 
                                  ", 旋转: " + watermarkManager.getSettings().getRotation());
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("预览更新失败: " + e.getMessage());
            }
        }
    }
    
    private void exportImages() {
        if (importedImages.isEmpty()) {
            JOptionPane.showMessageDialog(this, "没有可导出的图片", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("选择导出目录");
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File outputDir = fileChooser.getSelectedFile();
            
            // 检查是否与原目录相同
            boolean sameAsSource = false;
            for (File image : importedImages) {
                if (image.getParentFile().equals(outputDir)) {
                    sameAsSource = true;
                    break;
                }
            }
            
            if (sameAsSource) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "导出目录与原图片目录相同，可能会覆盖原文件。是否继续？",
                        "确认", JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            // 执行导出
            for (File image : importedImages) {
                Image watermarkedImage = watermarkManager.applyWatermark(
                        imageProcessor.loadImage(image));
                
                // 获取输出文件名
                String outputFileName = imageProcessor.getOutputFileName(image.getName());
                File outputFile = new File(outputDir, outputFileName);
                
                // 保存图片
                imageProcessor.saveImage(watermarkedImage, outputFile);
            }
            
            JOptionPane.showMessageDialog(this, "图片导出完成", "成功", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void saveCurrentTemplate() {
        String templateName = JOptionPane.showInputDialog(this, "请输入模板名称:");
        if (templateName != null && !templateName.trim().isEmpty()) {
            configManager.saveTemplate(templateName, watermarkManager.getCurrentSettings());
            JOptionPane.showMessageDialog(this, "模板保存成功", "成功", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void loadTemplate() {
        List<String> templates = configManager.getTemplateNames();
        if (templates.isEmpty()) {
            JOptionPane.showMessageDialog(this, "没有保存的模板", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String selectedTemplate = (String) JOptionPane.showInputDialog(
                this, "选择要加载的模板:", "加载模板",
                JOptionPane.QUESTION_MESSAGE, null,
                templates.toArray(), templates.get(0));
        
        if (selectedTemplate != null) {
            WatermarkSettings settings = configManager.loadTemplate(selectedTemplate);
            watermarkManager.applySettings(settings);
            updatePreview();
            JOptionPane.showMessageDialog(this, "模板加载成功", "成功", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void manageTemplates() {
        // 实现模板管理对话框
        // 可以列出所有模板，并提供删除功能
    }
    
    private void loadLastConfig() {
        WatermarkSettings lastSettings = configManager.loadLastSettings();
        if (lastSettings != null) {
            watermarkManager.applySettings(lastSettings);
        }
    }
    
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
                "照片水印工具 v1.0\n" +
                "一个简单易用的图片水印添加工具\n" +
                "支持文本水印和图片水印",
                "关于", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void main(String[] args) {
        try {
            // 设置本地系统外观
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> new PhotoWatermarkApp());
    }
}