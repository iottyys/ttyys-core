package io.ttyys.core.compiler;

import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;

public class PackageInternalsFinder {
    private static final String PKG_SEPARATOR = ".";
    private static final String DIR_SEPARATOR = "/";
    private static final String CLASS_FILE_EXTENSION = ".class";

    private final ClassLoader classLoader;

    public PackageInternalsFinder(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public List<JavaFileObject> find(String packageName) throws IOException {
        String javaPackageName = packageName.replaceAll("\\.", DIR_SEPARATOR);
        List<JavaFileObject> result = new ArrayList<>();
        Enumeration<URL> urls = this.classLoader.getResources(javaPackageName);
        while (urls.hasMoreElements()) {
            URL packageFolderURL = urls.nextElement();
            result.addAll(listUnder(packageName, packageFolderURL));
        }
        return result;
    }

    private Collection<JavaFileObject> listUnder(String packageName, URL packageFolderURL) {
        File dir = new File(packageFolderURL.getFile());
        if (dir.isDirectory()) {
            return this.processDir(packageName, dir);
        }
        return this.processJar(packageFolderURL);
    }

    private List<JavaFileObject> processJar(URL packageFolderURL) {
        List<JavaFileObject> result = new ArrayList<>();
        try {
            String jarUri = packageFolderURL.toExternalForm().substring(0,
                    packageFolderURL.toExternalForm().lastIndexOf("!/"));
            JarURLConnection jar = (JarURLConnection) packageFolderURL.openConnection();
            String rootEntryName = jar.getEntryName();
            int rootEnd = rootEntryName.length() + 1;
            Enumeration<JarEntry> entries = jar.getJarFile().entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith(rootEntryName)
                        && name.indexOf("/", rootEnd) == -1
                        && name.endsWith(CLASS_FILE_EXTENSION)) {
                    URI uri = URI.create(jarUri + "!/" + name);
                    String binaryName = name.replaceAll(DIR_SEPARATOR, PKG_SEPARATOR);
                    binaryName = binaryName.replaceAll(CLASS_FILE_EXTENSION + "$", "");
                    result.add(new CustomJavaFileObject(binaryName, uri));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("could not open " + packageFolderURL + " as jar file. ", e);
        }
        return result;
    }

    private List<JavaFileObject> processDir(String packageName, File dir) {
        List<JavaFileObject> result = new ArrayList<>();
        File[] childFiles = dir.listFiles();
        if (childFiles == null) {
            return result;
        }
        for (File file: childFiles) {
            if (file.isFile()) {
                if (file.getName().endsWith(CLASS_FILE_EXTENSION)) {
                    String binaryName = packageName + "." + file.getName();
                    binaryName = binaryName.replaceAll(CLASS_FILE_EXTENSION + "$", "");
                    result.add(new CustomJavaFileObject(binaryName, file.toURI()));
                }
            }
        }
        return result;
    }
}
