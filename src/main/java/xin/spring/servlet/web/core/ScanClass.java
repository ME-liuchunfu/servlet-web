package xin.spring.servlet.web.core;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ScanClass {

    public static ScanClass defaultScanClass;

    public synchronized static ScanClass getScanClass(ClassLoader classLoader, boolean clear) {
        if (Objects.isNull(defaultScanClass)) {
            defaultScanClass = new ScanClass(classLoader);
        } else {
            if (clear) {
                defaultScanClass.clear();
            }
        }
        return defaultScanClass;
    }

    private List<Class<?>> classList = new ArrayList<Class<?>>();
    private List<String> fileList = new ArrayList<String>();

    public ScanClass(ClassLoader classLoader) {
        String path = classLoader.getResource(".").getPath();
        String root = classLoader.getResource("/").getPath();
        scan(fileList, new File(path));
        fileList.stream().filter(v->v.endsWith(".class"))
                .map(v -> v.replace(root, "")
                        .replaceAll("\\\\", "/")
                        .replaceAll("/", ".")
                        .replaceAll("\\.class", ""))
                .map(v -> {
                    try {
                        Class<?> clazz = Class.forName(v);
                        return clazz;
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    return null;
                }).filter(Objects::nonNull)
                .filter(Objects::nonNull)
                .forEach(v->{
                    classList.add(v);
                });
    }

    private void scan(List<String> list, File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                scan(list, files[i]);
            }
        }
        else {
            list.add(file.toURI().getPath());
        }
    }

    private void clear() {
        this.getClassList().clear();
        this.getFileList().clear();
    }

    public Object getObjInstance(Class<?> v) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Object instant = v.getConstructor().newInstance();
        return instant;
    }

    public List<Class<?>> getClassList() {
        return classList;
    }

    public void setClassList(List<Class<?>> classList) {
        this.classList = classList;
    }

    public List<String> getFileList() {
        return fileList;
    }

    public void setFileList(List<String> fileList) {
        this.fileList = fileList;
    }
}
