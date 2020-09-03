package marquez.spark.agent.transformers;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import marquez.spark.agent.SparkListener;

public class PairRDDFunctionsTransformer implements ClassFileTransformer {
    private static final String TBI = "org.apache.spark.rdd.PairRDDFunctions";
    protected final ClassPool pool = ClassPool.getDefault();

  public static final String CODE = String.format("{ %s.registerOutput(this, conf); }", SparkListener.class.getName());


  @Override
  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
    if (!className.replace('/', '.').equals(TBI)) {
//      if (className.contains("PairRDDFunctions"))
//          System.out.println("  SKIP(" +className.replace('/', '.')+")");
      return classfileBuffer;
    }
    System.out.println("PairRDDFunctionsTransformer.transform(" +className+")");
    try {
      this.pool.insertClassPath(new ByteArrayClassPath(TBI, classfileBuffer));
      CtClass ctClass = this.pool.get(TBI);
      if (ctClass.isFrozen()) {
        System.err.println("  FROZEN!!!");
        return classfileBuffer;
      }
      System.out.println("Code: " + CODE );
      ctClass.getDeclaredMethod("saveAsNewAPIHadoopDataset").insertBefore(CODE);
      ctClass.getDeclaredMethod("saveAsHadoopDataset").insertBefore(CODE);
      return ctClass.toBytecode();
    } catch (Throwable throwable) {
      System.err.println("BLETCH!!!");
      throwable.printStackTrace();
      return classfileBuffer;
    }
  }
}
