package io.ttyys.algo.processor.stub;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import io.ttyys.algo.AlgorithmType;
import io.ttyys.algo.text.annotation.Algorithm;
import org.apache.avro.Protocol;
import org.apache.avro.Schema;
import org.apache.avro.compiler.specific.SpecificCompiler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// todo complete it! fock lombok repository then extend the processor and corresponding plugin of intellij idea
//  (just forget eclipse)
@SupportedAnnotationTypes("io.ttyys.algo.text.annotation.Algorithm")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AlgorithmAnnotationProcessor extends AbstractProcessor {
    private Name.Table nameTable;
    private TreeMaker maker;
    private JavacTrees trees;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.nameTable = Names.instance(context).table;
        this.maker = TreeMaker.instance(context);
        this.trees = JavacTrees.instance(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        try {
            for (Element element: env.getElementsAnnotatedWith(Algorithm.class)) {
                if (element.getKind() != ElementKind.INTERFACE) {
                    this.error(element, Algorithm.class.getSimpleName());
                    return true;
                }
                this.generateStub(element);
            }
            return false;
        } catch (IOException e) {
            throw new IllegalStateException("could not process algorithm sub. ", e);
        }
    }

    protected void doProcess(Element element) throws IOException {
        this.generateStub(element);
        TreePath treePath = this.trees.getPath(element);
        Tree tree = treePath.getLeaf();
        JCTree.JCIdent packages = maker.Ident(nameTable.fromString("io.ttyys.ipc"));
        JCTree.JCImport jcImport = maker.Import(maker.Select(packages, nameTable.fromString("PingPong")), false);
        JCTree.JCCompilationUnit jcCompilationUnit = (JCTree.JCCompilationUnit) treePath.getCompilationUnit();
        List<JCTree> trs = new ArrayList<>();
        trs.addAll(jcCompilationUnit.defs);
        trs.add(jcImport);
        jcCompilationUnit.defs = com.sun.tools.javac.util.List.from(trs);
        this.trees.getTree(element).accept(new TreeTranslator() {
            @Override
            public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                super.visitClassDef(jcClassDecl);
//                        jcClassDecl.mods.annotations = com.sun.tools.javac.util.List.nil();
//                        jcClassDecl.implementing = jcClassDecl.getImplementsClause().append(maker.Ident(nameTable.fromString("Mail")));
                ListBuffer<JCTree> te = new ListBuffer<>();
                te.append(maker.MethodDef(
                        maker.Modifiers(Flags.PUBLIC),
                        nameTable.fromString("test"),
                        maker.TypeIdent(TypeTag.VOID),
                        com.sun.tools.javac.util.List.nil(),
                        com.sun.tools.javac.util.List.nil(),
                        com.sun.tools.javac.util.List.nil(),
                        maker.Block(0, com.sun.tools.javac.util.List.nil()),
                        null));
                jcClassDecl.defs = te.toList();
                this.result = jcClassDecl;
            }
        });
//        compiler.get
//        JavacTrees.instance(processingEnv).getTree(element).accept(new TreeTranslator() {
//            @Override
//            public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
//                super.visitClassDef(jcClassDecl);
//                Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
//                TreeMaker maker = TreeMaker.instance(context);
////                jcClassDecl.extending = maker.ClassLiteral()
//            }
//        });
    }

    private void extendStub() {
    }

    private String generateStub(Element element) throws IOException {
        Algorithm algorithm = element.getAnnotation(Algorithm.class);
        AlgorithmType type = algorithm.value();
        Path tmpDir = Files.createTempDirectory("avro-tmp");
        Path tmpAvpr = Files.createTempFile(tmpDir, "avpr", ".avpr.tmp");
        InputStream is = AlgorithmAnnotationProcessor.class.getClassLoader().getResourceAsStream(type.avpr());
        FileUtils.copyInputStreamToFile(is, tmpAvpr.toFile());
        Protocol protocol = Protocol.parse(tmpAvpr.toFile());
        SpecificCompiler.compileProtocol(tmpAvpr.toFile(), tmpDir.toFile());

        String protocolClassName = protocol.getNamespace() + "." + protocol.getName();
        List<String> typeClassNames = protocol.getTypes().stream().map(Schema::getFullName).collect(Collectors.toList());

        for (String typeClassName: typeClassNames) {
            try (Reader reader = Files.newBufferedReader(this.className2Path(tmpDir, typeClassName),
                    StandardCharsets.UTF_8);
                 Writer writer = processingEnv.getFiler().createSourceFile(typeClassName).openWriter()) {
                IOUtils.copy(reader, writer);
            }
        }

        try (Reader reader = Files.newBufferedReader(this.className2Path(tmpDir, protocolClassName),
                StandardCharsets.UTF_8);
             Writer writer = processingEnv.getFiler().createSourceFile(protocolClassName).openWriter()) {
            IOUtils.copy(reader, writer);
        }

        tmpAvpr.toFile().deleteOnExit();
        tmpDir.toFile().deleteOnExit();
        return protocolClassName;
    }

    private Path className2Path(Path baseDir, String className) {
        Path path = Paths.get(baseDir.toString(), className.replace('.', File.separatorChar));
        path = Paths.get(path.toString() + ".java");
        assert Files.exists(path);
        return path;
    }

    private void error(Element e, Object... args) {
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                String.format("only support interface with annotation @%S", args), e);
    }
}
