package tangzeqi.com.ui.folder;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import tangzeqi.com.ui.MyPanel;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Vector;
import javax.swing.SwingWorker;

public class FolderPanel extends JPanel implements MyPanel {
    private final String project;

    private JPanel folderPanel;
    private JPanel folderPathPanel;
    private JTextField folderPathField;
    private JButton browseButton;
    private JScrollPane fileListScroll;
    private JList<String> fileList;
    private JPanel fileInfoPanel;
    private JScrollPane fileContentScroll;
    private JTextArea fileContentArea;

    public FolderPanel(String project) {
        this.project = project;
        initialize();
        setupEventHandlers();
    }

    public FolderPanel() {
        this.project = null;
    }

    private void initialize() {
        SwingUtilities.invokeLater(() -> {
            setupScrollPanes();
        });
    }

    private void setupScrollPanes() {
        // 设置文件列表滚动面板
        fileListScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        fileListScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        fileListScroll.setPreferredSize(null); // 移除首选大小，让其自动适应父容器

        // 设置文件内容滚动面板
        fileContentScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        fileContentScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        fileContentScroll.setPreferredSize(null); // 移除首选大小，让其自动适应父容器

        // 确保组件能够随滚动面板一起调整大小
        setComponentSizePolicy(fileList);
        setComponentSizePolicy(fileContentArea);
    }

    /**
     * 设置组件的大小策略，使其能够随父容器一起调整大小
     */
    private void setComponentSizePolicy(JComponent component) {
        component.setPreferredSize(null); // 移除首选大小
        component.setMaximumSize(null); // 移除最大大小
        component.setMinimumSize(null); // 移除最小大小
        // 对于文本区域，确保其能够自动换行
        if (component instanceof JTextArea) {
            JTextArea textArea = (JTextArea) component;
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
        }
        // 对于列表，确保其能够显示多行
        if (component instanceof JList) {
            JList<?> list = (JList<?>) component;
            list.setVisibleRowCount(-1); // 允许显示任意行数
        }
    }

    private void setupEventHandlers() {
        // 文件夹事件处理
        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(folderPanel) == JFileChooser.APPROVE_OPTION) {
                File selected = chooser.getSelectedFile();
                folderPathField.setText(selected.getAbsolutePath());
                updateFileList(selected.getAbsolutePath());
            }
        });

        fileList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = fileList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    String selected = fileList.getModel().getElementAt(index);
                    String fileName = selected.substring(selected.indexOf(' ') + 1); // 去掉图标

                    // 处理返回上级目录
                    if (selected.startsWith("🔙")) {
                        File currentDir = new File(folderPathField.getText());
                        File parent = currentDir.getParentFile();
                        if (parent != null) {
                            folderPathField.setText(parent.getAbsolutePath());
                            updateFileList(parent.getAbsolutePath());
                        }
                        return;
                    }

                    // 处理普通文件/文件夹
                    File file = new File(folderPathField.getText(), fileName);

                    if (file.isDirectory()) {
                        // 单击时显示文件夹内容
                        displayFolderContent(file);

                        // 双击时进入文件夹
                        if (e.getClickCount() == 2) {
                            folderPathField.setText(file.getAbsolutePath());
                            updateFileList(file.getAbsolutePath());
                        }
                    } else {
                        if (e.getClickCount() == 2) {
                            // 双击文件时打开文件
                            try {
                                Desktop.getDesktop().open(file);
                            } catch (IOException ex) {
                                fileContentArea.setText("无法打开文件：" + ex.getMessage());
                            }
                        } else {
                            // 检查文件类型
                            String fileExtension = fileName.toLowerCase();
                            boolean isTextFile = fileExtension.endsWith(".txt") ||
                                    fileExtension.endsWith(".log") ||
                                    fileExtension.endsWith(".md") ||
                                    fileExtension.endsWith(".java") ||
                                    fileExtension.endsWith(".xml") ||
                                    fileExtension.endsWith(".json") ||
                                    fileExtension.endsWith(".properties") ||
                                    fileExtension.endsWith(".yml") ||
                                    fileExtension.endsWith(".yaml") ||
                                    fileExtension.endsWith(".html") ||
                                    fileExtension.endsWith(".css") ||
                                    fileExtension.endsWith(".js") ||
                                    fileExtension.endsWith(".py") ||
                                    fileExtension.endsWith(".sql");

                            if (!isTextFile) {
                                // 显示二进制文件信息
                                String fileInfo = String.format(
                                        "文件类型：二进制文件\n" +
                                                "文件大小：%,d 字节\n" +
                                                "最后修改：%s\n" +
                                                "文件路径：%s\n" +
                                                "\n这是一个非文本文件，无法直接显示内容。\n" +
                                                "双击可以使用系统默认程序打开该文件。",
                                        file.length(),
                                        new Date(file.lastModified()).toString(),
                                        file.getAbsolutePath()
                                );
                                fileContentArea.setText(fileInfo);
                                return;
                            }

                            // 读取文本文件内容
                            new SwingWorker<String, Void>() {
                                @Override
                                protected String doInBackground() throws Exception {
                                    StringBuilder content = new StringBuilder();
                                    try {
                                        // 检测文件编码
                                        String encoding = FileEncodingDetector.detectEncoding(file);

                                        // 使用检测到的编码读取文件
                                        try (BufferedReader reader = new BufferedReader(
                                                new InputStreamReader(new FileInputStream(file), encoding))) {
                                            char[] buffer = new char[8192];
                                            int charsRead;
                                            while ((charsRead = reader.read(buffer)) != -1) {
                                                content.append(buffer, 0, charsRead);
                                                if (content.length() > 1024 * 1024) {
                                                    return "文件内容过大，无法完整显示";
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        return "读取文件时出错：" + e.getMessage();
                                    }
                                    return content.toString();
                                }

                                @Override
                                protected void done() {
                                    try {
                                        String content = get();
                                        fileContentArea.setText(content);
                                        // 滚动到顶部
                                        fileContentArea.setCaretPosition(0);
                                    } catch (Exception ex) {
                                        fileContentArea.setText("读取文件时出错：" + ex.getMessage());
                                    }
                                }
                            }.execute();
                        }
                    }
                }
            }
        });
    }

    // 文件夹相关方法
    private void updateFileList(String path) {
        File dir = new File(path);
        File[] files = dir.listFiles();
        Vector<String> fileListVector = new Vector<>();

        // 如果不是根目录，添加返回上级目录选项
        File parent = dir.getParentFile();
        if (parent != null) {
            fileListVector.add("🔙 .. (返回上级目录)");
        }

        if (files != null) {
            // 先添加目录
            Arrays.stream(files)
                    .filter(File::isDirectory)
                    .sorted(Comparator.comparing(File::getName))
                    .forEach(file -> fileListVector.add("📁 " + file.getName()));

            // 再添加文件
            Arrays.stream(files)
                    .filter(File::isFile)
                    .sorted(Comparator.comparing(File::getName))
                    .forEach(file -> {
                        String icon = getFileIcon(file);
                        fileListVector.add(icon + " " + file.getName());
                    });
        }

        fileList.setListData(fileListVector);
    }

    // 根据文件类型返回对应的图标
    private String getFileIcon(File file) {
        String name = file.getName().toLowerCase();

        // 系统和可执行文件
        if (name.endsWith(".exe") || name.endsWith(".msi") || name.endsWith(".deb") ||
                name.endsWith(".rpm") || name.endsWith(".dmg") || name.endsWith(".pkg") ||
                name.endsWith(".app") || name.endsWith(".run") || name.endsWith(".bin") ||
                name.endsWith(".command") || name.endsWith(".bat") || name.endsWith(".cmd") ||
                name.endsWith(".com") || name.endsWith(".scr") || name.endsWith(".msc")) return "💻";

        // 文档类
        if (name.endsWith(".txt") || name.endsWith(".log") || name.endsWith(".md") ||
                name.endsWith(".readme") || name.endsWith(".rtf")) return "📄";
        if (name.endsWith(".pdf")) return "📕";
        if (name.endsWith(".doc") || name.endsWith(".docx") || name.endsWith(".dot") ||
                name.endsWith(".dotx")) return "📘";
        if (name.endsWith(".xls") || name.endsWith(".xlsx") || name.endsWith(".csv") ||
                name.endsWith(".xlsb") || name.endsWith(".xlsm")) return "📗";
        if (name.endsWith(".ppt") || name.endsWith(".pptx") || name.endsWith(".pps") ||
                name.endsWith(".ppsx")) return "📙";
        if (name.endsWith(".odt") || name.endsWith(".ods") || name.endsWith(".odp") ||
                name.endsWith(".odg") || name.endsWith(".odf")) return "📝";

        // 代码类
        if (name.endsWith(".java") || name.endsWith(".class") || name.endsWith(".jar") ||
                name.endsWith(".war") || name.endsWith(".ear") || name.endsWith(".jsp")) return "☕";
        if (name.endsWith(".py") || name.endsWith(".pyc") || name.endsWith(".pyd") ||
                name.endsWith(".pyw") || name.endsWith(".pyz")) return "🐍";
        if (name.endsWith(".js") || name.endsWith(".jsx") || name.endsWith(".ts") ||
                name.endsWith(".tsx") || name.endsWith(".json") || name.endsWith(".json5")) return "🌐";
        if (name.endsWith(".c") || name.endsWith(".cpp") || name.endsWith(".h") ||
                name.endsWith(".hpp") || name.endsWith(".cc") || name.endsWith(".cxx")) return "⚙️";
        if (name.endsWith(".cs") || name.endsWith(".vb") || name.endsWith(".fs") ||
                name.endsWith(".fsx")) return "🔷";
        if (name.endsWith(".php") || name.endsWith(".phtml") || name.endsWith(".php3") ||
                name.endsWith(".php4") || name.endsWith(".php5") || name.endsWith(".php7")) return "🐘";
        if (name.endsWith(".rb") || name.endsWith(".rbw") || name.endsWith(".rake")) return "💎";
        if (name.endsWith(".go")) return "🐹";
        if (name.endsWith(".rs")) return "🦀";
        if (name.endsWith(".swift")) return "🦉";
        if (name.endsWith(".kt") || name.endsWith(".kts")) return "🎯";
        if (name.endsWith(".scala") || name.endsWith(".sc")) return "🔮";
        if (name.endsWith(".pl") || name.endsWith(".pm") || name.endsWith(".t") ||
                name.endsWith(".pod")) return "🐪";
        if (name.endsWith(".sh") || name.endsWith(".bash") || name.endsWith(".zsh") ||
                name.endsWith(".fish") || name.endsWith(".csh")) return "🐚";
        if (name.endsWith(".r") || name.endsWith(".R")) return "📊";
        if (name.endsWith(".m") || name.endsWith(".matlab")) return "📐";
        if (name.endsWith(".lua")) return "🌙";
        if (name.endsWith(".dart")) return "🎯";
        if (name.endsWith(".elm")) return "🌳";
        if (name.endsWith(".hs") || name.endsWith(".lhs")) return "λ";

        // 网页和标记语言
        if (name.endsWith(".html") || name.endsWith(".htm") || name.endsWith(".xhtml") ||
                name.endsWith(".shtml") || name.endsWith(".dhtml")) return "🌐";
        if (name.endsWith(".css") || name.endsWith(".scss") || name.endsWith(".sass") ||
                name.endsWith(".less") || name.endsWith(".styl")) return "🎨";
        if (name.endsWith(".xml") || name.endsWith(".xsl") || name.endsWith(".xslt") ||
                name.endsWith(".xsd") || name.endsWith(".svg")) return "📜";
        if (name.endsWith(".vue") || name.endsWith(".svelte")) return "🖼️";

        // 配置文件
        if (name.endsWith(".ini") || name.endsWith(".conf") || name.endsWith(".config") ||
                name.endsWith(".cfg") || name.endsWith(".toml")) return "⚙️";
        if (name.endsWith(".yml") || name.endsWith(".yaml")) return "📋";
        if (name.endsWith(".env") || name.endsWith(".dotenv")) return "🌍";
        if (name.endsWith(".properties") || name.endsWith(".props")) return "📝";

        // 压缩文件
        if (name.endsWith(".zip") || name.endsWith(".zipx") || name.endsWith(".rar") ||
                name.endsWith(".7z") || name.endsWith(".ace") || name.endsWith(".arj") ||
                name.endsWith(".bz2") || name.endsWith(".cab") || name.endsWith(".gz") ||
                name.endsWith(".gzip") || name.endsWith(".lha") || name.endsWith(".lzh") ||
                name.endsWith(".lzma") || name.endsWith(".pak") || name.endsWith(".sit") ||
                name.endsWith(".sitx") || name.endsWith(".tar") || name.endsWith(".tgz") ||
                name.endsWith(".xz") || name.endsWith(".z") || name.endsWith(".zoo")) return "📦";

        // 图片类
        if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") ||
                name.endsWith(".gif") || name.endsWith(".bmp") || name.endsWith(".webp") ||
                name.endsWith(".ico") || name.endsWith(".tiff") || name.endsWith(".tif") ||
                name.endsWith(".psd") || name.endsWith(".ai") || name.endsWith(".eps") ||
                name.endsWith(".svg") || name.endsWith(".raw") || name.endsWith(".cr2") ||
                name.endsWith(".nef") || name.endsWith(".orf") || name.endsWith(".sr2")) return "🖼️";

        // 音频类
        if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".flac") ||
                name.endsWith(".aac") || name.endsWith(".ogg") || name.endsWith(".wma") ||
                name.endsWith(".m4a") || name.endsWith(".m4p") || name.endsWith(".m4b") ||
                name.endsWith(".m4r") || name.endsWith(".opus") || name.endsWith(".aiff") ||
                name.endsWith(".au") || name.endsWith(".ra") || name.endsWith(".3gp") ||
                name.endsWith(".amr") || name.endsWith(".ac3") || name.endsWith(".dts")) return "🎵";

        // 视频类
        if (name.endsWith(".mp4") || name.endsWith(".avi") || name.endsWith(".mkv") ||
                name.endsWith(".mov") || name.endsWith(".wmv") || name.endsWith(".flv") ||
                name.endsWith(".webm") || name.endsWith(".m4v") || name.endsWith(".3gp") ||
                name.endsWith(".3g2") || name.endsWith(".asf") || name.endsWith(".rm") ||
                name.endsWith(".rmvb") || name.endsWith(".vob") || name.endsWith(".ts") ||
                name.endsWith(".mts") || name.endsWith(".m2ts") || name.endsWith(".divx") ||
                name.endsWith(".xvid") || name.endsWith(".f4v") || name.endsWith(".f4p") ||
                name.endsWith(".f4a") || name.endsWith(".f4b")) return "🎬";

        // 数据库
        if (name.endsWith(".sql") || name.endsWith(".db") || name.endsWith(".sqlite") ||
                name.endsWith(".sqlite3") || name.endsWith(".db3") || name.endsWith(".mdb") ||
                name.endsWith(".accdb") || name.endsWith(".dbf") || name.endsWith(".odb") ||
                name.endsWith(".frm") || name.endsWith(".myd") || name.endsWith(".myi")) return "🗄️";

        // 字体文件
        if (name.endsWith(".ttf") || name.endsWith(".otf") || name.endsWith(".woff") ||
                name.endsWith(".woff2") || name.endsWith(".eot") || name.endsWith(".fon") ||
                name.endsWith(".pfb") || name.endsWith(".pfm")) return "🔤";

        // 移动应用
        if (name.endsWith(".apk") || name.endsWith(".aab")) return "📱";
        if (name.endsWith(".ipa") || name.endsWith(".pxl")) return "🍎";
        if (name.endsWith(".xap") || name.endsWith(".appx")) return "🪟";
        if (name.endsWith(".bar")) return "📱";

        // 电子书
        if (name.endsWith(".epub") || name.endsWith(".mobi") || name.endsWith(".azw") ||
                name.endsWith(".azw3") || name.endsWith(".fb2") || name.endsWith(".lit")) return "📚";

        // 字处理
        if (name.endsWith(".pages")) return "📝";
        if (name.endsWith(".numbers")) return "📊";
        if (name.endsWith(".key") || name.endsWith(".keynote")) return "🎭";

        // 3D模型
        if (name.endsWith(".obj") || name.endsWith(".fbx") || name.endsWith(".dae") ||
                name.endsWith(".3ds") || name.endsWith(".blend") || name.endsWith(".max") ||
                name.endsWith(".ma") || name.endsWith(".mb")) return "🎮";

        // 虚拟化
        if (name.endsWith(".vmdk") || name.endsWith(".vdi") || name.endsWith(".vhd") ||
                name.endsWith(".hdd") || name.endsWith(".qcow2") || name.endsWith(".ova") ||
                name.endsWith(".ovf")) return "💾";

        // 其他特殊类型
        if (name.endsWith(".torrent")) return "🔗";
        if (name.endsWith(".key") || name.endsWith(".pem") || name.endsWith(".crt") ||
                name.endsWith(".cer") || name.endsWith(".p12") || name.endsWith(".pfx")) return "🔐";
        if (name.endsWith(".iso") || name.endsWith(".img") || name.endsWith(".dmg") ||
                name.endsWith(".toast") || name.endsWith(".vcd")) return "💿";
        if (name.endsWith(".dll") || name.endsWith(".so") || name.endsWith(".dylib")) return "🔧";
        if (name.endsWith(".sys") || name.endsWith(".drv")) return "⚙️";

        // 隐藏文件
        if (name.startsWith(".")) return "🔒";

        return "📎"; // 默认文件图标
    }

    private void displayFolderContent(File folder) {
        File[] files = folder.listFiles();
        if (files == null) {
            fileContentArea.setText("无法访问此文件夹");
            return;
        }

        StringBuilder content = new StringBuilder();
        content.append("文件夹内容：\n\n");

        // 添加文件夹
        Arrays.stream(files)
                .filter(File::isDirectory)
                .sorted(Comparator.comparing(File::getName))
                .forEach(file -> {
                    content.append("📁 " + file.getName())
                            .append(" (").append(file.length()).append(" 字节)\n");
                });

        content.append("\n");

        // 添加文件
        Arrays.stream(files)
                .filter(File::isFile)
                .sorted(Comparator.comparing(File::getName))
                .forEach(file -> {
                    content.append(getFileIcon(file) + " " + file.getName())
                            .append(" (").append(file.length()).append(" 字节)\n");
                });

        fileContentArea.setText(content.toString());
    }


    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    @Override
    public JComponent getComponent(String project) {
        return new FolderPanel(project).folderPanel;
    }

    // FileEncodingDetector 类
    private static class FileEncodingDetector {
        public static String detectEncoding(File file) {
            // 简单的编码检测，实际项目中可能需要更复杂的实现
            return "UTF-8";
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("FolderPanel");
            frame.setContentPane(new FolderPanel().folderPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        });
    }

}
