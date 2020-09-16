package marquez.spark.agent.transformers;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.internal.SessionState;

import javassist.ByteArrayClassPath;
import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import marquez.spark.agent.SparkListener;

public class DataFrameWriterTransformer implements ClassFileTransformer {
  private static final String TBI = "org.apache.spark.sql.DataFrameWriter";
  protected final ClassPool pool = new ClassPool(true);//ClassPool.getDefault();

  public static final String CODE = String.format("{ %s.registerOutput(session, name, command); }", SparkListener.class.getName());
  public static final String CODE2 = String.format("{ %s.registerOutput(qe); }", SparkListener.class.getName());

  @Override
  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
    if (!className.contains("DataFrameWriter")) {
//    if (!className.replace('/', '.').equals(TBI)) {
      return classfileBuffer;
    }
    System.out.println("DataFrameWriterTransformer.transform(" +className+")");
    try {
      this.pool.insertClassPath(new ByteArrayClassPath(TBI, classfileBuffer));
      CtClass ctClass = this.pool.get(TBI);
      if (ctClass.isFrozen()) {
        System.err.println("  FROZEN!!!");
        return classfileBuffer;
      }
      // TODO: figure out why this is needed
      this.pool.insertClassPath(new ClassClassPath(SparkSession.class));
      System.out.println("Code beggining: " + CODE );
      ctClass.getDeclaredMethod("runCommand").insertBefore(CODE);
      System.out.println("Code end: " + CODE2 );
      ctClass.getDeclaredMethod("runCommand").instrument(new ExprEditor() {
        @Override
        public void edit(MethodCall m) throws CannotCompileException {
          if (m.getClassName().equals(SessionState.class.getName()) && m.getMethodName().equals("executePlan")) {
            m.replace("{ $_ = $proceed($$); marquez.spark.agent.SparkListener.registerOutput($_); }");
          }
          super.edit(m);
        }
      });
      return ctClass.toBytecode();
    } catch (Throwable throwable) {
      System.err.println("BLETCH!!!");
      throwable.printStackTrace();
      return classfileBuffer;
    }
  }
}
